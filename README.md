# EliteResume

EliteResume is an AI-powered resume builder with role-specific content suggestions.

## Current Scaffold

```text
EliteResume/
├── backend/
│   ├── pom.xml
│   ├── .env.example
│   ├── storage/
│   │   └── resumes/
│   └── src/
│       ├── main/
│       │   ├── java/com/eliteresume/api/
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── exception/
│       │   │   ├── repository/
│       │   │   ├── security/
│       │   │   ├── service/
│       │   │   └── EliteResumeApiApplication.java
│       │   └── resources/application.yml
│       └── test/java/com/eliteresume/api/
├── frontend/
└── docs/
    └── api-endpoints.md
```

## Backend Stack

- Java 17
- Spring Boot 3
- Spring Security + JWT
- BCrypt password hashing
- Google OAuth ID token verification
- Spring Data JPA
- MySQL
- Gemini API via WebClient
- OpenPDF for server-side PDF generation

## Local Backend Setup

1. Create a MySQL database user or use an existing local MySQL user.
2. Copy `backend/.env.example` values into your environment.
3. Set a strong `JWT_SECRET` of at least 32 characters.
4. Set `GEMINI_API_KEY` from Gemini AI Studio.
5. Set `GOOGLE_CLIENT_ID` from your Google OAuth web client.
6. Start the API:

```bash
cd backend
mvn spring-boot:run
```

The backend runs on `http://localhost:8081` by default.

## Resume PDF Format

The PDF generator follows the provided reference image:

- Centered uppercase candidate name
- Compact contact line
- Short objective paragraph
- Uppercase section headers with horizontal separators
- Education, technical skills, projects, experience, certificates, and languages
- Bullet lists for projects and experience

## Next Build Step

The next implementation phase should add the React Vite frontend with Tailwind CSS, authentication pages, dashboard, dynamic resume builder form, generated preview, and PDF download integration.
