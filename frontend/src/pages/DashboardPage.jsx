import { ArrowRight, Download, FileText, Sparkles } from "lucide-react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { downloadResume, listResumes } from "../api/resumeApi.js";
import DashboardLayout from "../components/DashboardLayout.jsx";
import { useAuth } from "../context/AuthContext.jsx";

export default function DashboardPage() {
  const { user } = useAuth();
  const [resumes, setResumes] = useState([]);

  useEffect(() => {
    listResumes().then(setResumes).catch(() => setResumes([]));
  }, []);

  return (
    <DashboardLayout>
      <div className="grid gap-6">
        <section className="section-band rounded-lg px-6 py-8">
          <p className="text-sm font-black uppercase text-mint">Dashboard</p>
          <h1 className="mt-3 text-3xl font-black text-ink">Welcome, {user?.email}</h1>
          <p className="mt-3 max-w-3xl text-base leading-7 text-steel">
            Create role-ready resumes, refine your content with AI, preview the result, and download a clean PDF matching the professional format you provided.
          </p>
          <Link className="btn-primary mt-6" to="/builder">
            Create Resume
            <ArrowRight className="h-4 w-4" />
          </Link>
        </section>

        <section className="grid gap-4 md:grid-cols-3">
          <div className="panel p-5">
            <Sparkles className="h-5 w-5 text-mint" />
            <h2 className="mt-3 text-lg font-black text-ink">AI Suggestions</h2>
            <p className="mt-2 text-sm leading-6 text-steel">Career objective, projects, and experience bullets tailored to target roles.</p>
          </div>
          <div className="panel p-5">
            <FileText className="h-5 w-5 text-ember" />
            <h2 className="mt-3 text-lg font-black text-ink">{resumes.length} Resume{resumes.length === 1 ? "" : "s"}</h2>
            <p className="mt-2 text-sm leading-6 text-steel">Your saved resume drafts and generated PDFs appear here.</p>
          </div>
          <div className="panel p-5" id="contact">
            <Download className="h-5 w-5 text-slate-700" />
            <h2 className="mt-3 text-lg font-black text-ink">PDF Storage</h2>
            <p className="mt-2 text-sm leading-6 text-steel">Generated files are stored by the backend and remain available for download.</p>
          </div>
        </section>

        <section className="panel overflow-hidden" id="about">
          <div className="border-b border-slate-200 px-5 py-4">
            <h2 className="text-lg font-black text-ink">Recent Resumes</h2>
          </div>
          <div className="divide-y divide-slate-200">
            {resumes.length === 0 ? (
              <p className="px-5 py-8 text-sm text-steel">No resumes yet. Start with Create Resume.</p>
            ) : (
              resumes.map((resume) => (
                <div key={resume.id} className="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <h3 className="font-bold text-ink">{resume.fullName || "Untitled Resume"}</h3>
                    <p className="text-sm text-steel">{resume.designation || "No designation added"}</p>
                  </div>
                  <button className="btn-secondary" type="button" disabled={!resume.pdfPath} onClick={() => downloadResume(resume.id)}>
                    <Download className="h-4 w-4" />
                    Download
                  </button>
                </div>
              ))
            )}
          </div>
        </section>
      </div>
    </DashboardLayout>
  );
}
