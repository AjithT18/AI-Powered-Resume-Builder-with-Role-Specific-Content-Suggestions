import { createContext, useContext, useMemo, useState } from "react";
import { loginUser, loginWithGoogle, registerUser } from "../api/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("eliteresume_token"));
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("eliteresume_user");
    return raw ? JSON.parse(raw) : null;
  });

  function storeSession(session) {
    localStorage.setItem("eliteresume_token", session.token);
    localStorage.setItem("eliteresume_user", JSON.stringify({ userId: session.userId, email: session.email }));
    setToken(session.token);
    setUser({ userId: session.userId, email: session.email });
  }

  async function login(payload) {
    const session = await loginUser(payload);
    storeSession(session);
    return session;
  }

  async function register(payload) {
    const session = await registerUser(payload);
    storeSession(session);
    return session;
  }

  async function googleLogin(credential) {
    const session = await loginWithGoogle(credential);
    storeSession(session);
    return session;
  }

  function logout() {
    localStorage.removeItem("eliteresume_token");
    localStorage.removeItem("eliteresume_user");
    setToken(null);
    setUser(null);
  }

  const value = useMemo(
    () => ({ token, user, login, register, googleLogin, logout }),
    [token, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return value;
}
