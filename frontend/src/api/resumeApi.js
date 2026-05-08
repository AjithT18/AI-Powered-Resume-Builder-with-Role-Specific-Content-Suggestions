import api from "./client";

export function listResumes() {
  return api.get("/resumes").then((response) => response.data);
}

export function saveResume(payload) {
  return api.post("/resumes", payload).then((response) => response.data);
}

export function generateResume(payload) {
  return api.post("/resumes/generate", payload).then((response) => response.data);
}

export async function downloadResume(resumeId) {
  const response = await api.get(`/resumes/${resumeId}/download`, {
    responseType: "blob",
  });
  const blobUrl = URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));
  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = `eliteresume-${resumeId}.pdf`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(blobUrl);
}
