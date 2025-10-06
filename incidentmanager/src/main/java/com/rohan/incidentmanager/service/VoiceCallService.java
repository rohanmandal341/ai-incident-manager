package com.rohan.incidentmanager.service;

import com.rohan.incidentmanager.entity.Incident;
import com.rohan.incidentmanager.entity.Project;
import com.rohan.incidentmanager.entity.User;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class VoiceCallService {

    @Value("${twilio.accountSid:}")
    private String accountSid;

    @Value("${twilio.authToken:}")
    private String authToken;

    @Value("${twilio.fromNumber:}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
        }
    }

    public boolean isConfigured() {
        return accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && fromNumber != null && !fromNumber.isBlank();
    }

    // Normalize to E.164-ish: remove spaces/dashes/parentheses; ensure it starts with +
    private String normalize(String num) {
        if (num == null) return null;
        String n = num.replaceAll("[\\s\\-()]", "");
        if (!n.startsWith("+")) return n; // assume already stored in + format; if not, Twilio will reject and we log it
        return n;
    }

    // Utility: call a single E.164 number (+1..., +91..., etc.)
    public void callNumber(String toNumber, String message) {
        if (!isConfigured() || toNumber == null || toNumber.isBlank()) return;
        try {
            String to = normalize(toNumber);
            if (to == null || to.isBlank()) {
                System.err.println("Voice call skipped: empty/invalid toNumber");
                return;
            }
            // keep under ~30s (roughly 80–100 words)
            String trimmed = message == null ? "" : message.trim();
            if (trimmed.length() > 700) trimmed = trimmed.substring(0, 700) + " ...";
            String safe = trimmed.replace("&", "and").replace("<", "").replace(">", "");
            String twimlUrl = "https://twimlets.com/message?Message%5B0%5D=" + urlEncode(safe);

            System.out.println("Twilio call -> to=" + to + " from=" + fromNumber + " msgChars=" + safe.length());
            Call.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), URI.create(twimlUrl)).create();
        } catch (Exception e) {
            System.err.println("Voice call failed to " + toNumber + " err=" + e.getMessage());
        }
    }

    // Convenience call helpers by role list
    public void callDevelopers(Project p, String message) {
        List<String> nums = p.getDevelopers() == null ? List.of() :
                p.getDevelopers().stream()
                        .map(User::getPhone)
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
        nums.forEach(n -> callNumber(n, message));
    }

    public void callLead(Project p, String message) {
        String phone = p.getTeamLead() != null ? p.getTeamLead().getPhone() : null;
        if (phone != null && !phone.isBlank()) callNumber(phone, message);
        else System.err.println("Lead call skipped: no phone configured");
    }

    public void callCTO(Project p, String message) {
        String phone = p.getHead() != null ? p.getHead().getPhone() : null;
        if (phone != null && !phone.isBlank()) callNumber(phone, message);
        else System.err.println("CTO call skipped: no phone configured");
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    // Message builders kept for fallback/template use
    public String msgNewDown(Project p, Incident inc) {
        return "Alert. " + p.getProjectName() + " is down. Health check failed. Incident " + inc.getId() + ". Please investigate.";
    }

    public String msgEscLead(Project p, Incident inc) {
        return "Escalation. " + p.getProjectName() + " still down. Lead attention required. Incident " + inc.getId() + ".";
    }

    public String msgEscCTO(Project p, Incident inc) {
        return "Critical escalation. " + p.getProjectName() + " still down. CTO attention required. Incident " + inc.getId() + ".";
    }

    public String msgResolved(Project p, Incident inc) {
        return "Resolved. " + p.getProjectName() + " is back up. Incident " + inc.getId() + " closed.";
    }
}
