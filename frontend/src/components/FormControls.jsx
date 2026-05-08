export function TextField({ label, value, onChange, type = "text", placeholder, required = false }) {
  return (
    <label className="grid gap-2">
      <span className="field-label">{label}</span>
      <input
        className="field-input"
        type={type}
        value={value ?? ""}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        required={required}
      />
    </label>
  );
}

export function TextArea({ label, value, onChange, placeholder, rows = 5 }) {
  return (
    <label className="grid gap-2">
      <span className="field-label">{label}</span>
      <textarea className="field-area" value={value ?? ""} onChange={(event) => onChange(event.target.value)} placeholder={placeholder} rows={rows} />
    </label>
  );
}

export function SelectField({ label, value, onChange, options }) {
  return (
    <label className="grid gap-2">
      <span className="field-label">{label}</span>
      <select className="field-input" value={value ?? ""} onChange={(event) => onChange(event.target.value)}>
        {options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </label>
  );
}
