import api from "./client";

export function registerUser(payload) {
  return api.post("/auth/register", payload).then((response) => response.data);
}

export function loginUser(payload) {
  return api.post("/auth/login", payload).then((response) => response.data);
}

export function loginWithGoogle(idToken) {
  return api.post("/auth/google", { idToken }).then((response) => response.data);
}
