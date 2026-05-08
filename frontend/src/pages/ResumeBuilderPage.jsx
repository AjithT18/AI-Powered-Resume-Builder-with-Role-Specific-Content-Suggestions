import { Download, Loader2, Plus, Save, Sparkles, Trash2, Upload } from "lucide-react";
import { useMemo, useState } from "react";
import { downloadResume, generateResume, saveResume } from "../api/resumeApi.js";
import DashboardLayout from "../components/DashboardLayout.jsx";
import { SelectField, TextArea, TextField } from "../components/FormControls.jsx";
import ResumePreview from "../components/ResumePreview.jsx";
import { emptyResume, months, normalizeResumePayload } from "../data/initialResume.js";

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

export default function ResumeBuilderPage() {
  const [resume, setResume] = useState(() => clone(emptyResume));
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);

  const previewResume = useMemo(() => resume, [resume]);

  function patch(path, value) {
    setResume((current) => {
      const next = clone(current);
      let cursor = next;
      for (let i = 0; i < path.length - 1; i++) cursor = cursor[path[i]];
      cursor[path[path.length - 1]] = value;
      return next;
    });
  }

  function addItem(key, item) {
    setResume((current) => ({ ...current, [key]: [...current[key], clone(item)] }));
  }

  function removeItem(key, index) {
    setResume((current) => ({ ...current, [key]: current[key].filter((_, itemIndex) => itemIndex !== index) }));
  }

  async function handleProfileImage(file) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => patch(["profileImagePath"], reader.result);
    reader.readAsDataURL(file);
  }

  async function handleSave() {
    setLoading(true);
    setStatus("");
    try {
      const saved = await saveResume(normalizeResumePayload(resume));
      setResume((current) => ({ ...current, id: saved.id }));
      setStatus("Draft saved.");
    } catch (error) {
      setStatus(error.response?.data?.message || "Could not save draft.");
    } finally {
      setLoading(false);
    }
  }

  async function handleGenerate() {
    setLoading(true);
    setStatus("");
    try {
      const generated = await generateResume(normalizeResumePayload(resume));
      setResume((current) => ({ ...current, ...generated }));
      setStatus("Resume generated with AI-enhanced content.");
    } catch (error) {
      const detail =
        error.response?.data?.message ||
        error.response?.data?.error ||
        (typeof error.response?.data === "string" ? error.response.data : "") ||
        error.message;
      setStatus(detail ? `Could not generate resume: ${detail}` : "Could not generate resume.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <DashboardLayout>
      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_560px]">
        <div className="grid gap-6">
          <section className="section-band rounded-lg px-5 py-5">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="text-sm font-black uppercase text-mint">Resume Builder</p>
                <h1 className="mt-2 text-3xl font-black text-ink">Create a role-ready resume</h1>
              </div>
              <div className="flex flex-wrap gap-2">
                <button className="btn-secondary" type="button" onClick={handleSave} disabled={loading}>
                  <Save className="h-4 w-4" />
                  Save
                </button>
                <button className="btn-primary" type="button" onClick={handleGenerate} disabled={loading}>
                  {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Sparkles className="h-4 w-4" />}
                  Generate Resume
                </button>
                <button className="btn-secondary" type="button" onClick={() => downloadResume(resume.id)} disabled={!resume.id || !resume.pdfPath}>
                  <Download className="h-4 w-4" />
                  Download
                </button>
              </div>
            </div>
            {status && <p className="mt-4 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-steel">{status}</p>}
          </section>

          <BuilderSection title="About">
            <div className="grid gap-4 md:grid-cols-3">
              <TextField label="First Name" value={resume.firstName} onChange={(value) => patch(["firstName"], value)} required />
              <TextField label="Middle Name" value={resume.middleName} onChange={(value) => patch(["middleName"], value)} />
              <TextField label="Last Name" value={resume.lastName} onChange={(value) => patch(["lastName"], value)} required />
              <label className="grid gap-2">
                <span className="field-label">Profile Image</span>
                <span className="btn-secondary justify-start">
                  <Upload className="h-4 w-4" />
                  Upload
                  <input className="hidden" type="file" accept="image/*" onChange={(event) => handleProfileImage(event.target.files?.[0])} />
                </span>
              </label>
              <TextField label="Designation" value={resume.designation} onChange={(value) => patch(["designation"], value)} />
              <TextField label="Email" type="email" value={resume.email} onChange={(value) => patch(["email"], value)} />
              <TextField label="Phone" value={resume.phone} onChange={(value) => patch(["phone"], value)} />
              <TextField label="LinkedIn" value={resume.linkedIn} onChange={(value) => patch(["linkedIn"], value)} />
              <TextField label="GitHub" value={resume.github} onChange={(value) => patch(["github"], value)} />
              <TextField label="Portfolio" value={resume.portfolio} onChange={(value) => patch(["portfolio"], value)} />
            </div>
          </BuilderSection>

          <BuilderSection title="Education" onAdd={() => addItem("education", emptyResume.education[0])}>
            {resume.education.map((item, index) => (
              <RepeatBlock key={index} onRemove={() => removeItem("education", index)} removable={resume.education.length > 1}>
                <div className="grid gap-4 md:grid-cols-2">
                  <TextField label="School" value={item.school} onChange={(value) => patch(["education", index, "school"], value)} />
                  <TextField label="Degree" value={item.degree} onChange={(value) => patch(["education", index, "degree"], value)} />
                  <TextField label="Start Year" type="number" value={item.startYear} onChange={(value) => patch(["education", index, "startYear"], value)} />
                  <TextField label="End Year" type="number" value={item.endYear} onChange={(value) => patch(["education", index, "endYear"], value)} />
                  <ScoreToggle value={item.scoreType} onChange={(value) => patch(["education", index, "scoreType"], value)} />
                  <TextField label="Score" value={item.score} onChange={(value) => patch(["education", index, "score"], value)} />
                </div>
              </RepeatBlock>
            ))}
          </BuilderSection>

          <SimpleList title="Skills" items={resume.skills} keyName="skills" field="name" addItem={addItem} removeItem={removeItem} patch={patch} placeholder="Java, Spring Boot, React" />

          <BulletedSection title="Projects" items={resume.projects} keyName="projects" addItem={addItem} removeItem={removeItem} patch={patch} kind="project" />

          <BulletedSection title="Experience" items={resume.experiences} keyName="experiences" addItem={addItem} removeItem={removeItem} patch={patch} kind="experience" />

          <BuilderSection title="Certificates" onAdd={() => addItem("certificates", emptyResume.certificates[0])}>
            {resume.certificates.map((item, index) => (
              <RepeatBlock key={index} onRemove={() => removeItem("certificates", index)} removable={resume.certificates.length > 1}>
                <div className="grid gap-4 md:grid-cols-2">
                  <TextField label="Certificate Name" value={item.name} onChange={(value) => patch(["certificates", index, "name"], value)} />
                  <TextField label="Organization" value={item.organization} onChange={(value) => patch(["certificates", index, "organization"], value)} />
                </div>
              </RepeatBlock>
            ))}
          </BuilderSection>

          <SimpleList title="Languages" items={resume.languages} keyName="languages" field="name" addItem={addItem} removeItem={removeItem} patch={patch} placeholder="English" />

          <BuilderSection title="Job Description">
            <TextArea label="Paste Job Description" value={resume.jobDescription} onChange={(value) => patch(["jobDescription"], value)} placeholder="Paste the role description here so Gemini can tailor your objective and bullet points." rows={8} />
          </BuilderSection>
        </div>

        <aside className="min-w-0 h-max xl:sticky xl:top-6">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-black text-ink">Preview</h2>
          </div>
          <div className="resume-preview-frame">
            <div className="resume-preview-space">
              <div className="resume-preview-scale">
                <ResumePreview resume={previewResume} />
              </div>
            </div>
          </div>
        </aside>
      </div>
    </DashboardLayout>
  );
}

function BuilderSection({ title, children, onAdd }) {
  return (
    <section className="panel overflow-hidden">
      <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
        <h2 className="text-lg font-black text-ink">{title}</h2>
        {onAdd && (
          <button className="icon-btn" type="button" onClick={onAdd} title={`Add ${title}`}>
            <Plus className="h-4 w-4" />
          </button>
        )}
      </div>
      <div className="grid gap-4 p-5">{children}</div>
    </section>
  );
}

function RepeatBlock({ children, onRemove, removable }) {
  return (
    <div className="rounded-md border border-slate-200 bg-slate-50 p-4">
      <div className="flex justify-end">
        <button className="icon-btn mb-3" type="button" onClick={onRemove} disabled={!removable} title="Remove">
          <Trash2 className="h-4 w-4" />
        </button>
      </div>
      {children}
    </div>
  );
}

function ScoreToggle({ value, onChange }) {
  return (
    <div className="grid gap-2">
      <span className="field-label">Percentage / CGPA</span>
      <div className="grid h-11 grid-cols-2 rounded-md border border-slate-300 bg-white p-1">
        {["PERCENTAGE", "CGPA"].map((option) => (
          <button
            key={option}
            className={`rounded text-sm font-bold ${value === option ? "bg-mint text-white" : "text-steel"}`}
            type="button"
            onClick={() => onChange(option)}
          >
            {option === "PERCENTAGE" ? "Percentage" : "CGPA"}
          </button>
        ))}
      </div>
    </div>
  );
}

function SimpleList({ title, items, keyName, field, addItem, removeItem, patch, placeholder }) {
  return (
    <BuilderSection title={title} onAdd={() => addItem(keyName, { [field]: "" })}>
      {items.map((item, index) => (
        <div key={index} className="grid grid-cols-[1fr_auto] gap-3">
          <TextField label={`${title.slice(0, -1)} ${index + 1}`} value={item[field]} onChange={(value) => patch([keyName, index, field], value)} placeholder={placeholder} />
          <div className="pt-6">
            <button className="icon-btn" type="button" onClick={() => removeItem(keyName, index)} disabled={items.length === 1} title="Remove">
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        </div>
      ))}
    </BuilderSection>
  );
}

function BulletedSection({ title, items, keyName, addItem, removeItem, patch, kind }) {
  const empty = kind === "project" ? emptyResume.projects[0] : emptyResume.experiences[0];
  return (
    <BuilderSection title={title} onAdd={() => addItem(keyName, empty)}>
      {items.map((item, index) => (
        <RepeatBlock key={index} onRemove={() => removeItem(keyName, index)} removable={items.length > 1}>
          <div className="grid gap-4 md:grid-cols-2">
            {kind === "project" ? (
              <TextField label="Title" value={item.title} onChange={(value) => patch([keyName, index, "title"], value)} />
            ) : (
              <>
                <TextField label="Role" value={item.role} onChange={(value) => patch([keyName, index, "role"], value)} />
                <TextField label="Organization" value={item.organization} onChange={(value) => patch([keyName, index, "organization"], value)} />
              </>
            )}
            <SelectField label="Start Month" value={item.startMonth} onChange={(value) => patch([keyName, index, "startMonth"], value)} options={months} />
            <TextField label="Start Year" type="number" value={item.startYear} onChange={(value) => patch([keyName, index, "startYear"], value)} />
            <SelectField label="End Month" value={item.endMonth || "Dec"} onChange={(value) => patch([keyName, index, "endMonth"], value)} options={months} />
            <TextField label="End Year" type="number" value={item.endYear || ""} onChange={(value) => patch([keyName, index, "endYear"], value)} />
          </div>
          <label className="mt-4 flex items-center gap-2 text-sm font-semibold text-steel">
            <input type="checkbox" checked={item.present} onChange={(event) => patch([keyName, index, "present"], event.target.checked)} />
            Present
          </label>
          <div className="mt-4 grid gap-3">
            <div className="flex items-center justify-between">
              <span className="field-label">Description Bullets</span>
              <button className="icon-btn" type="button" onClick={() => patch([keyName, index, "bullets"], [...item.bullets, ""])} title="Add bullet">
                <Plus className="h-4 w-4" />
              </button>
            </div>
            {item.bullets.map((bullet, bulletIndex) => (
              <div key={bulletIndex} className="grid grid-cols-[1fr_auto] gap-3">
                <input className="field-input" value={bullet} onChange={(event) => patch([keyName, index, "bullets", bulletIndex], event.target.value)} placeholder="Describe your contribution" />
                <button
                  className="icon-btn"
                  type="button"
                  onClick={() => patch([keyName, index, "bullets"], item.bullets.filter((_, idx) => idx !== bulletIndex))}
                  disabled={item.bullets.length === 1}
                  title="Remove bullet"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            ))}
          </div>
        </RepeatBlock>
      ))}
    </BuilderSection>
  );
}
