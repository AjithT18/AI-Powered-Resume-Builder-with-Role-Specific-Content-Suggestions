function presentDate(item) {
  const start = [item.startMonth, item.startYear].filter(Boolean).join(" ");
  const end = item.present ? "Present" : [item.endMonth, item.endYear].filter(Boolean).join(" ");
  return [start, end].filter(Boolean).join(" - ");
}

function fullName(resume) {
  return [resume.firstName, resume.middleName, resume.lastName].filter(Boolean).join(" ") || "YOUR NAME";
}

function Section({ title, children, show = true }) {
  if (!show) return null;
  return (
    <section className="mt-[8px]">
      <h3 className="border-b border-black pb-[1px] text-[15px] font-black uppercase leading-[1.05]">{title}</h3>
      <div className="mt-[4px]">{children}</div>
    </section>
  );
}

export default function ResumePreview({ resume }) {
  const skills = resume.skills?.map((skill) => skill.name).filter(Boolean) || [];
  const languages = resume.languages?.map((language) => language.name).filter(Boolean) || [];

  return (
    <div className="resume-page mx-auto px-[48px] py-[30px] font-sans">
      <header className="text-center">
        <h2 className="text-[31px] font-black leading-[1.05] tracking-normal">{fullName(resume).toUpperCase()}</h2>
        {resume.designation && <p className="mt-[6px] text-[12px] leading-tight">{resume.designation}</p>}
        <p className="mx-auto mt-[8px] max-w-[650px] break-words text-[11px] leading-[1.25] text-blue-700">
          {[resume.phone, resume.email, resume.linkedIn, resume.github, resume.portfolio].filter(Boolean).join(" | ")}
        </p>
      </header>

      {resume.careerObjective && <p className="mt-[10px] text-[11px] leading-[1.45]">{resume.careerObjective}</p>}

      <Section title="Education" show={resume.education?.some((item) => item.school || item.degree)}>
        <div className="grid gap-[3px]">
          {resume.education.map((item, index) => (
            <div key={index}>
              <div className="flex justify-between gap-3 text-[13px] font-black leading-[1.15]">
                <span>{item.school}</span>
                <span className="shrink-0">{[item.startYear, item.endYear].filter(Boolean).join(" - ")}</span>
              </div>
              <div className="flex justify-between gap-3 text-[10.5px] leading-[1.25]">
                <span>{item.degree}</span>
                <span className="shrink-0">{item.score ? `${item.scoreType}: ${item.score}` : ""}</span>
              </div>
            </div>
          ))}
        </div>
      </Section>

      <Section title="Technical Skills" show={skills.length > 0}>
        <p className="text-[11px] leading-[1.45]">{skills.join(", ")}</p>
      </Section>

      <Section title="Projects" show={resume.projects?.some((item) => item.title || item.bullets?.some(Boolean))}>
        {resume.projects.map((project, index) => (
          <div key={index} className="mb-[5px]">
            <p className="text-[11.5px] font-black leading-[1.3]">{project.title} : {presentDate(project)}</p>
            <ul className="ml-8 list-disc text-[10.5px] leading-[1.45]">
              {project.bullets?.filter(Boolean).map((bullet, bulletIndex) => <li key={bulletIndex}>{bullet}</li>)}
            </ul>
          </div>
        ))}
      </Section>

      <Section title="Experience" show={resume.experiences?.some((item) => item.role || item.organization || item.bullets?.some(Boolean))}>
        {resume.experiences.map((experience, index) => (
          <div key={index} className="mb-[5px]">
            <p className="text-[11.5px] font-black leading-[1.3]">
              {[experience.role, experience.organization].filter(Boolean).join(" at ")} - {presentDate(experience)}
            </p>
            <ul className="ml-8 list-disc text-[10.5px] leading-[1.45]">
              {experience.bullets?.filter(Boolean).map((bullet, bulletIndex) => <li key={bulletIndex}>{bullet}</li>)}
            </ul>
          </div>
        ))}
      </Section>

      <Section title="Certificates" show={resume.certificates?.some((item) => item.name || item.organization)}>
        <ul className="ml-8 list-disc text-[10.5px] leading-[1.45]">
          {resume.certificates.map((certificate, index) => (
            <li key={index}>{[certificate.name, certificate.organization].filter(Boolean).join(" - ")}</li>
          ))}
        </ul>
      </Section>

      <Section title="Languages" show={languages.length > 0}>
        <p className="text-[11px] leading-[1.45]">{languages.join(", ")}</p>
      </Section>
    </div>
  );
}
