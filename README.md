


# 🚀 AI-Powered Cloud-Native Incident Management System

### 🔥 Think PagerDuty + Datadog + ChatGPT Ops Assistant

An **AI-driven Incident Management System** that monitors services, detects outages in real-time, alerts engineers via **Twilio calls**, sends **escalation emails**, and integrates an **AI chatbot** for troubleshooting.  

Built with **Spring Boot (Backend)**, **React (Frontend)**, **MySQL**, **Twilio**, and **Groq LLM** — this project demonstrates **end-to-end DevOps automation and AI integration**.

---

## 🧠 Why This Project?

Every enterprise faces challenges with **outages**, **alerts**, **incident tracking**, and **escalation management**.

This system mimics how **Datadog**, **PagerDuty**, and **NewRelic** handle incident response — but enhanced with **AI insights** and **automated workflows**.

**Perfect resume booster 🚀** — shows skills in:
- Cloud-native architecture
- DevOps automation
- AI integration (Groq API)
- Microservices
- Alerting & monitoring

---

## 🧩 Key Features

| Feature | Description |
|----------|-------------|
| **24/7 Monitoring** | Pings microservices like `/actuator/health` every 30 seconds. |
| **Real-Time Alerts** | Sends **emails** + **Twilio voice calls** instantly when service is down. |
| **AI Incident Assistant** | Chatbot helps diagnose the issue with LLM-powered suggestions. |
| **Escalation Workflow** | Auto-escalates from Developer → Lead → CTO if not resolved in time. |
| **Daily Reports** | Automated email summaries with uptime %, MTTR, and incident trends. |
| **Multi-Role Dashboards** | Separate dashboards for Dev, Lead, CTO for better visibility. |
| **Dockerized Deployment** | Fully containerized for easy cloud or local deployment. |

---

## 🏗️ System Architecture

```

User → Frontend (React)
↓
Backend (Spring Boot)
↓
MySQL Database
↓
AI Layer (Groq API)
↓
Notification Layer (Twilio, SMTP)

````

---

## ⚙️ Tech Stack

**Frontend:** React, TailwindCSS  
**Backend:** Spring Boot, REST APIs  
**Database:** MySQL  
**AI:** Groq Llama 3.3-70B  
**Alerts:** Twilio (Voice + SMS), Gmail SMTP  
**Containerization:** Docker & Docker Compose  

---

## 📊 Dashboards & Screenshots

### 🧑‍💻 Developer & AI Chatbot Interface
| Chatbot | Developer Dashboard | Add New Service |
|----------|--------------------|----------------|
| ![ChatBot](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/ChatBot.png) | ![Dev Dashboard](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/devloper_Dashboard.png) | ![Add New Service](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/add_new_service.png) |

---

### 👨‍💼 Lead Dashboard & Analytics
| Lead Dashboard | Analytics Chart |
|----------------|----------------|
| ![Lead Dashboard](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/Lead_DashBoard.png) | ![Chart](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/LeadDashboard_Chart.png) |

---

### 🧑‍💼 CTO Dashboard & Escalation Emails
| CTO Dashboard | CTO Email | Developer Escalation |
|----------------|-----------|----------------------|
| ![CTO Dashboard](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/CTO_Dashboard.png) | ![CTO Mail](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/CTO_Email_esaclation3.jpg) | ![Dev Mail](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/devloper_mail_escalation1.jpg) |

---

### ✉️ Escalation Alerts (AI + Twilio Integrated)
| Escalation 2 Lead | Generic Chatbot |
|------------------|----------------|
| ![Escalation 2 Lead](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/esaclation2_lead_mail.jpg) | ![Chatbot for Users](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/generic_chatbot_for_users.png) |

---

### 🔐 Authentication & Registration
| Login | Register |
|--------|-----------|
| ![Login Page](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/LoginPage.png) | ![Register Page](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/Register_newUser.png) |

---

## 🎥 Demo Videos

| Raised Call Alert | Resolved Call |
|------------------|----------------|
| 🎬 [Watch Incident Raised Call](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/issue_raised_call.mp4) | 🎬 [Watch Incident Resolved Call](https://github.com/rohanmandal341/ai-incident-manager/blob/main/incidentmanagemeent_Images/issue_resolved_call.mp4) |

---

## 🧾 Example `.env` Template

```env
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/incidentmanager
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=<your_mysql_password>

JWT_SECRET=<your_jwt_secret>
JWT_EXPIRATION_MS=259200000

TWILIO_ACCOUNT_SID=<your_twilio_sid>
TWILIO_AUTH_TOKEN=<your_twilio_auth_token>
TWILIO_FROM_NUMBER=<your_twilio_phone_number>

AI_BACKEND=groq
GROQ_API_KEY=<your_groq_api_key>
GROQ_BASE_URL=https://api.groq.com/openai/v1
AI_MODEL=llama-3.3-70b-versatile

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<your_email>
MAIL_PASSWORD=<your_app_password>
MAIL_SMTP_AUTH=true
MAIL_STARTTLS_ENABLE=true

SERVER_PORT=8082
````

> ⚠️ **Important:** Never commit your `.env` file.
> Add this line to your `.gitignore`:
>
> ```
> .env
> ```

---

## 🐳 Docker Deployment

```yaml
version: '3.8'
services:
  mysql-db:
    image: mysql:8
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    ports:
      - "3307:3306"
    volumes:
      - db_data:/var/lib/mysql

  backend:
    build: ./backend
    restart: always
    depends_on:
      - mysql-db
    ports:
      - "8082:8082"
    env_file:
      - ./backend/.env

  frontend:
    build: ./incident-frontend
    restart: always
    depends_on:
      - backend
    ports:
      - "3000:3000"

volumes:
  db_data:
```

---

## 🚀 Run Locally

```bash
# Clone repository
git clone https://github.com/rohanmandal341/ai-incident-manager.git
cd ai-incident-manager

# Start containers
docker-compose up --build
```

Then visit:

* **Frontend:** [http://localhost:3000](http://localhost:3000)
* **Backend:** [http://localhost:8082](http://localhost:8082)

---

## 🧠 AI Escalation Flow

```
[Service Down] →
  Twilio Calls Dev →
  Email Alert to Dev, Lead, CTO →
  Wait (10 mins) →
  If Unresolved → Call Lead →
  Wait (30 mins) →
  If Still Down → Call CTO → All Notified
```

---

## 🏆 Outcome

✅ Real-world enterprise-grade **Incident Response System**
✅ Integrated **AI + DevOps + Monitoring**
✅ Full-stack + cloud-native + automation project
✅ Excellent portfolio project for **FAANG / top startup interviews**

---

## 👨‍💻 Author

**Rohan Mandal**
📧 [rohanmandal7789999@gmail.com](mailto:rohanmandal7789999@gmail.com)
🌐 [GitHub Repository](https://github.com/rohanmandal341/ai-incident-manager)

---

⭐ *If you liked this project, give it a star on GitHub!* ⭐

```

---

✅ This version:
- Keeps everything beautiful & complete  
- **Removes all real secrets**
- Keeps it 100% **safe and professional**  
- Ready for public GitHub profile  

Would you like me to add a **“Live Demo (Deployed Link)”** section placeholder too (so you can add Render / Railway URL later)?
```
