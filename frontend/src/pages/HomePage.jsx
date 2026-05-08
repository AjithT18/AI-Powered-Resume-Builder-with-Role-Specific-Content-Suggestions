import { ArrowRight, BrainCircuit, FileCheck2, Target } from "lucide-react";
import { Link } from "react-router-dom";
import AppNavbar from "../components/AppNavbar.jsx";

export default function HomePage() {
  return (
    <div className="page-shell">
      <AppNavbar />
      <main>
        <section className="container-pad grid min-h-[calc(100vh-4rem)] items-center gap-10 py-12 lg:grid-cols-[1.05fr_0.95fr]">
          <div className="max-w-2xl">
            <p className="text-sm font-black uppercase text-mint">AI-powered resume generation</p>
            <h1 className="mt-4 text-4xl font-black leading-tight text-ink sm:text-5xl lg:text-6xl">
              EliteResume
            </h1>
            <p className="mt-5 max-w-xl text-lg leading-8 text-steel">
              Build polished resumes that adapt to the role you want. Paste a job description, add your background, and get professionally optimized objectives, project bullets, and experience content.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link className="btn-primary" to="/register">
                Get Started
                <ArrowRight className="h-4 w-4" />
              </Link>
              <Link className="btn-secondary" to="/login">
                Login
              </Link>
            </div>
          </div>

          <div className="panel overflow-hidden">
            <div className="grid gap-0">
              {[
                { icon: BrainCircuit, title: "AI content refinement", text: "Gemini enhances your wording while keeping your original bullet count intact." },
                { icon: Target, title: "Role-specific optimization", text: "Suggestions are shaped by the pasted job description and the skills in your profile." },
                { icon: FileCheck2, title: "Stored PDF output", text: "Generate a clean, professional PDF and download it from your dashboard." },
              ].map((item) => {
                const Icon = item.icon;
                return (
                  <div key={item.title} className="grid grid-cols-[44px_1fr] gap-4 border-b border-slate-200 p-5 last:border-b-0">
                    <span className="flex h-11 w-11 items-center justify-center rounded-md bg-emerald-50 text-mint">
                      <Icon className="h-5 w-5" />
                    </span>
                    <div>
                      <h2 className="text-base font-black text-ink">{item.title}</h2>
                      <p className="mt-1 text-sm leading-6 text-steel">{item.text}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
