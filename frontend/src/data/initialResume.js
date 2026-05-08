export const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

export const emptyResume = {
  id: null,
  firstName: "",
  middleName: "",
  lastName: "",
  profileImagePath: "",
  designation: "",
  email: "",
  phone: "",
  linkedIn: "",
  github: "",
  portfolio: "",
  jobDescription: "",
  careerObjective: "",
  education: [
    {
      school: "",
      degree: "",
      startYear: "",
      endYear: "",
      scoreType: "CGPA",
      score: "",
    },
  ],
  skills: [{ name: "" }],
  projects: [
    {
      title: "",
      startMonth: "Jan",
      startYear: "",
      endMonth: "Dec",
      endYear: "",
      present: false,
      bullets: [""],
    },
  ],
  experiences: [
    {
      role: "",
      organization: "",
      startMonth: "Jan",
      startYear: "",
      endMonth: "Dec",
      endYear: "",
      present: true,
      bullets: [""],
    },
  ],
  certificates: [{ name: "", organization: "" }],
  languages: [{ name: "" }],
};

export function normalizeResumePayload(resume) {
  return {
    ...resume,
    education: resume.education.map((item) => ({
      ...item,
      startYear: numberOrNull(item.startYear),
      endYear: numberOrNull(item.endYear),
    })),
    projects: resume.projects.map((item) => ({
      ...item,
      startYear: numberOrNull(item.startYear),
      endYear: item.present ? null : numberOrNull(item.endYear),
      endMonth: item.present ? null : item.endMonth,
      bullets: item.bullets.filter(Boolean),
    })),
    experiences: resume.experiences.map((item) => ({
      ...item,
      startYear: numberOrNull(item.startYear),
      endYear: item.present ? null : numberOrNull(item.endYear),
      endMonth: item.present ? null : item.endMonth,
      bullets: item.bullets.filter(Boolean),
    })),
    skills: resume.skills.filter((item) => item.name),
    certificates: resume.certificates.filter((item) => item.name || item.organization),
    languages: resume.languages.filter((item) => item.name),
  };
}

function numberOrNull(value) {
  if (value === "" || value === null || value === undefined) return null;
  return Number(value);
}
