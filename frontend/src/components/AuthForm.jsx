import { GoogleLogin } from "@react-oauth/google";
import { Loader2 } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function AuthForm({ mode }) {
  const isRegister = mode === "register";
  const navigate = useNavigate();
  const { login, register, googleLogin } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      if (isRegister) {
        await register(form);
      } else {
        await login(form);
      }
      navigate("/dashboard");
    } catch (exception) {
      setError(exception.response?.data?.message || "Authentication failed. Check your details and try again.");
    } finally {
      setLoading(false);
    }
  }

  async function handleGoogleSuccess(response) {
    setLoading(true);
    setError("");
    try {
      await googleLogin(response.credential);
      navigate("/dashboard");
    } catch (exception) {
      setError(exception.response?.data?.message || "Google sign-in failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="panel mx-auto w-full max-w-md p-6">
      <div className="mb-6">
        <p className="text-sm font-semibold text-mint">EliteResume</p>
        <h1 className="mt-2 text-2xl font-black text-ink">{isRegister ? "Create your account" : "Welcome back"}</h1>
      </div>

      <form className="grid gap-4" onSubmit={submit}>
        <label className="grid gap-2">
          <span className="field-label">Email</span>
          <input
            className="field-input"
            type="email"
            value={form.email}
            onChange={(event) => setForm((value) => ({ ...value, email: event.target.value }))}
            placeholder="you@example.com"
            required
          />
        </label>
        <label className="grid gap-2">
          <span className="field-label">Password</span>
          <input
            className="field-input"
            type="password"
            value={form.password}
            onChange={(event) => setForm((value) => ({ ...value, password: event.target.value }))}
            placeholder="Minimum 8 characters"
            required
          />
        </label>

        {error && <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

        <button className="btn-primary w-full" type="submit" disabled={loading}>
          {loading && <Loader2 className="h-4 w-4 animate-spin" />}
          {isRegister ? "Register" : "Login"}
        </button>
      </form>

      <div className="my-5 flex items-center gap-3 text-xs font-semibold uppercase text-slate-400">
        <span className="h-px flex-1 bg-slate-200" />
        Or
        <span className="h-px flex-1 bg-slate-200" />
      </div>

      <div className="flex justify-center">
        <GoogleLogin onSuccess={handleGoogleSuccess} onError={() => setError("Google sign-in was cancelled or failed.")} />
      </div>

      <p className="mt-6 text-center text-sm text-steel">
        {isRegister ? "Already have an account?" : "New to EliteResume?"}{" "}
        <Link className="font-bold text-mint" to={isRegister ? "/login" : "/register"}>
          {isRegister ? "Login" : "Register"}
        </Link>
      </p>
    </div>
  );
}
