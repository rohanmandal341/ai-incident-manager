package com.rohan.incidentmanager.service;

import com.rohan.incidentmanager.ai.GroqChatClient;
import com.rohan.incidentmanager.entity.Incident;
import com.rohan.incidentmanager.entity.Project;
import com.rohan.incidentmanager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private VoiceCallService voiceCallService;
    @Autowired private GroqChatClient groq;

    private void sendMail(List<String> to, List<String> cc, String subject, String body) {
        if (to == null || to.isEmpty()) return;
        SimpleMailMessage m = new SimpleMailMessage();
        m.setTo(to.toArray(new String[0]));
        if (cc != null && !cc.isEmpty()) {
            m.setCc(cc.toArray(new String[0]));
        }
        m.setSubject(subject);
        m.setText(body);
        mailSender.send(m);
    }

    private List<String> devEmails(Project p) {
        List<String> list = new ArrayList<>();
        if (p.getDevelopers() != null) {
            for (User u : p.getDevelopers()) {
                if (u != null && u.getEmail() != null) list.add(u.getEmail());
            }
        }
        return list;
    }

    private String leadEmail(Project p) {
        return p.getTeamLead() != null ? p.getTeamLead().getEmail() : null;
    }

    private String ctoEmail(Project p) {
        return p.getHead() != null ? p.getHead().getEmail() : null;
    }

    // -------- AI helpers --------

    private String aiEmailPara(Project p, Incident inc, String audience) {
        long minutes = Duration.between(inc.getCreatedAt(), LocalDateTime.now()).toMinutes();
        String system = "You are an on-call SRE assistant. Write one short actionable paragraph (3-5 sentences, <= 600 chars) for the specified audience. Avoid stack traces or secrets.";
        String user = """
Audience: %s
Project: %s
Health URL: %s
Current status: %s
Minutes since incident: %d
Reason: %s

Write next steps and likely causes, concise and pragmatic.
""".formatted(audience, p.getProjectName(), safe(p.getHealthUrl()), p.getLastStatus(), minutes, safe(inc.getReason()));
        String out = groq.chat(system, user);
        if (out == null || out.isBlank() || "No answer.".equalsIgnoreCase(out)) return "";
        return out.length() > 600 ? out.substring(0, 600) + " ..." : out;
    }

    private String aiVoiceScript(Project p, Incident inc, String audience, String tone) {
        long minutes = Duration.between(inc.getCreatedAt(), LocalDateTime.now()).toMinutes();
        String system = "You are a voice alert generator. Produce a clear spoken message under 30 seconds (~60-80 words). No URLs, no secrets. End with one concrete action.";
        String user = """
Audience: %s
Tone: %s
Project: %s
Status: %s
Elapsed minutes: %d
Reason: %s

Return only the speech text.
""".formatted(audience, tone, p.getProjectName(), p.getLastStatus(), minutes, safe(inc.getReason()));
        String out = groq.chat(system, user);
        if (out == null || out.isBlank() || "No answer.".equalsIgnoreCase(out)) {
            // fallback to previous static voice lines
            return "Alert. " + p.getProjectName() + " status " + p.getLastStatus() + ". Please investigate the health checks and recent changes.";
        }
        // Soft cap in case the model overruns
        if (out.length() > 650) out = out.substring(0, 650) + " ...";
        return out.replaceAll("[\\r\\n]+", " ");
    }

    private String safe(String s) {
        if (s == null) return "N/A";
        // strip potential secrets-like tokens
        return s.replaceAll("(?i)(token|secret|password)=[^\\s]+", "$1=[redacted]");
    }

    // -------- Notifications --------

    @Transactional(readOnly = true)
    public void notifyNewIncident(Project p, Incident inc) {
        // Voice: Developer first, AI 25-30s script
        if (voiceCallService.isConfigured()) {
            String speech = aiVoiceScript(p, inc, "Developer", "calm and urgent");
            voiceCallService.callDevelopers(p, speech);
        }
        // Email: Devs TO, Lead & CTO CC + AI paragraph
        List<String> to = devEmails(p);
        List<String> cc = new ArrayList<>();
        String lead = leadEmail(p);
        String cto = ctoEmail(p);
        if (lead != null) cc.add(lead);
        if (cto != null) cc.add(cto);

        String aiPara = aiEmailPara(p, inc, "Developer");
        String body = EmailTemplates.bodyNewDown(p, inc)
                + (aiPara.isBlank() ? "" : ("\nAI summary:\n" + aiPara + "\n"));
        sendMail(to, cc, EmailTemplates.subjectNewDown(p), body);
    }

    @Transactional(readOnly = true)
    public void notifyEscalationLead(Project p, Incident inc) {
        if (voiceCallService.isConfigured()) {
            String speech = aiVoiceScript(p, inc, "Team Lead", "decisive and brief");
            voiceCallService.callLead(p, speech);
        }
        List<String> to = new ArrayList<>();
        String lead = leadEmail(p);
        if (lead != null) to.add(lead);
        List<String> cc = new ArrayList<>(devEmails(p));
        String cto = ctoEmail(p);
        if (cto != null) cc.add(cto);

        String aiPara = aiEmailPara(p, inc, "Team Lead");
        String body = EmailTemplates.bodyEscLead(p, inc)
                + (aiPara.isBlank() ? "" : ("\nAI summary:\n" + aiPara + "\n"));
        sendMail(to, cc, EmailTemplates.subjectEscLead(p), body);
    }

    @Transactional(readOnly = true)
    public void notifyEscalationCTO(Project p, Incident inc) {
        if (voiceCallService.isConfigured()) {
            String speech = aiVoiceScript(p, inc, "CTO", "executive concise");
            voiceCallService.callCTO(p, speech);
        }
        List<String> to = new ArrayList<>();
        String cto = ctoEmail(p);
        if (cto != null) to.add(cto);
        List<String> cc = new ArrayList<>();
        String lead = leadEmail(p);
        if (lead != null) cc.add(lead);
        cc.addAll(devEmails(p));

        String aiPara = aiEmailPara(p, inc, "CTO");
        String body = EmailTemplates.bodyEscCTO(p, inc)
                + (aiPara.isBlank() ? "" : ("\nAI summary:\n" + aiPara + "\n"));
        sendMail(to, cc, EmailTemplates.subjectEscCTO(p), body);
    }

    @Transactional(readOnly = true)
    public void notifyResolved(Project p, Incident inc) {
        if (voiceCallService.isConfigured()) {
            String speech = aiVoiceScript(p, inc, "All", "positive and succinct");
            voiceCallService.callDevelopers(p, speech);
            voiceCallService.callLead(p, speech);
        }
        List<String> to = new ArrayList<>(devEmails(p));
        String lead = leadEmail(p);
        if (lead != null) to.add(lead);
        List<String> cc = new ArrayList<>();
        String cto = ctoEmail(p);
        if (cto != null) cc.add(cto);

        String aiPara = aiEmailPara(p, inc, "All");
        String body = EmailTemplates.bodyResolved(p, inc)
                + (aiPara.isBlank() ? "" : ("\nAI summary:\n" + aiPara + "\n"));
        sendMail(to, cc, EmailTemplates.subjectResolved(p), body);
    }
}
