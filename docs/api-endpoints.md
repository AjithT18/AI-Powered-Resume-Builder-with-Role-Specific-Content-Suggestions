# EliteResume API Endpoints

Base URL: `http://localhost:8081`

## Authentication

### Register

`POST /api/auth/register`

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

Returns:

```json
{
  "token": "jwt-token",
  "userId": 1,
  "email": "user@example.com"
}
```

### Login

`POST /api/auth/login`

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

### Google Login

`POST /api/auth/google`

```json
{
  "idToken": "google-id-token-from-frontend"
}
```

## Resumes

All resume endpoints require:

`Authorization: Bearer <jwt-token>`

### List Resumes

`GET /api/resumes`

### Get Resume

`GET /api/resumes/{id}`

### Save Draft

`POST /api/resumes`

Saves all user-entered resume form data without calling Gemini or generating a PDF.

### Generate Resume

`POST /api/resumes/generate`

Saves all form data, calls Gemini, preserves the same number of project and experience bullet points, stores the generated PDF, and returns the enhanced resume.

```json
{
  "id": null,
  "firstName": "Ajith",
  "middleName": "",
  "lastName": "T",
  "designation": "Software Developer",
  "email": "ajith@example.com",
  "phone": "+91 7483424493",
  "linkedIn": "https://linkedin.com/in/example",
  "github": "https://github.com/example",
  "portfolio": "https://example.dev",
  "jobDescription": "Paste target job description here",
  "education": [
    {
      "school": "The Oxford College of Engineering",
      "degree": "BE, Computer Science and Engineering",
      "startYear": 2022,
      "endYear": 2026,
      "scoreType": "CGPA",
      "score": "8.61"
    }
  ],
  "skills": [
    { "name": "Java" },
    { "name": "Spring Boot" },
    { "name": "React" }
  ],
  "projects": [
    {
      "title": "Smart Infrastructure for In-motion EV Charging",
      "startMonth": "Jan",
      "startYear": 2025,
      "endMonth": "Apr",
      "endYear": 2025,
      "present": false,
      "bullets": [
        "Built wireless EV charging prototype",
        "Tracked voltage and temperature"
      ]
    }
  ],
  "experiences": [
    {
      "role": "Web Development with GenAI Intern",
      "organization": "Rooman Technologies",
      "startMonth": "Feb",
      "startYear": 2026,
      "endMonth": null,
      "endYear": null,
      "present": true,
      "bullets": [
        "Worked on web applications with AI features",
        "Built responsive interfaces"
      ]
    }
  ],
  "certificates": [
    { "name": "Java Programming", "organization": "Udemy" }
  ],
  "languages": [
    { "name": "English" },
    { "name": "Kannada" }
  ]
}
```

### Download PDF

`GET /api/resumes/{id}/download`

Returns the generated PDF file as an attachment.
