# EliteResume

EliteResume is an AI-powered resume builder with role-specific content suggestions.

## Current Scaffold

```text
EliteResume/
├── README.md
├── backend/
│   ├── pom.xml
│   ├── .env
│   ├── .env.example
│   ├── backend-run.log
│   ├── backend-run-8081.log
│   ├── storage/
│   │   └── resumes/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/eliteresume/api/
│   │       │   ├── config/
│   │       │   │   ├── AppProperties.java
│   │       │   │   ├── BeanConfig.java
│   │       │   │   ├── SchemaMaintenanceConfig.java
│   │       │   │   └── SecurityConfig.java
│   │       │   ├── controller/
│   │       │   │   ├── AuthController.java
│   │       │   │   └── ResumeController.java
│   │       │   ├── dto/
│   │       │   │   ├── AuthDtos.java
│   │       │   │   ├── GeminiDtos.java
│   │       │   │   └── ResumeDtos.java
│   │       │   ├── entity/
│   │       │   │   ├── AuthProvider.java
│   │       │   │   ├── Certificate.java
│   │       │   │   ├── Education.java
│   │       │   │   ├── Experience.java
│   │       │   │   ├── Language.java
│   │       │   │   ├── Project.java
│   │       │   │   ├── Resume.java
│   │       │   │   ├── ScoreType.java
│   │       │   │   ├── Skill.java
│   │       │   │   └── User.java
│   │       │   ├── exception/
│   │       │   │   ├── ApiException.java
│   │       │   │   └── GlobalExceptionHandler.java
│   │       │   ├── repository/
│   │       │   │   ├── CertificateRepository.java
│   │       │   │   ├── EducationRepository.java
│   │       │   │   ├── ExperienceRepository.java
│   │       │   │   ├── LanguageRepository.java
│   │       │   │   ├── ProjectRepository.java
│   │       │   │   ├── ResumeRepository.java
│   │       │   │   ├── SkillRepository.java
│   │       │   │   └── UserRepository.java
│   │       │   ├── security/
│   │       │   │   ├── JwtAuthenticationFilter.java
│   │       │   │   ├── JwtService.java
│   │       │   │   └── UserPrincipalService.java
│   │       │   ├── service/
│   │       │   │   ├── AuthService.java
│   │       │   │   ├── CurrentUserService.java
│   │       │   │   ├── GeminiService.java
│   │       │   │   ├── GoogleOAuthService.java
│   │       │   │   ├── ResumeMapper.java
│   │       │   │   ├── ResumePdfService.java
│   │       │   │   └── ResumeService.java
│   │       │   └── EliteResumeApiApplication.java
│   │       └── resources/
│   │           └── application.yml
│   └── target/
├── frontend/
│   ├── package.json
│   ├── package-lock.json
│   ├── .env.example
│   ├── index.html
│   ├── vite.config.js
│   ├── postcss.config.js
│   ├── tailwind.config.js
│   ├── src/
│   │   ├── main.jsx
│   │   ├── App.jsx
│   │   ├── styles.css
│   │   ├── api/
│   │   │   ├── authApi.js
│   │   │   ├── client.js
│   │   │   └── resumeApi.js
│   │   ├── components/
│   │   ├── context/
│   │   ├── data/
│   │   └── pages/
│   ├── dist/
│   └── node_modules/
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
