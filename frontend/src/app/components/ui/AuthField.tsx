import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";

type Props = {
  id: string; label: string; type: "text" | "email" | "password";
  value: string; onChange: (value: string) => void;
  placeholder: string; autoComplete: string; hint?: string; minLength?: number;
};

export default function AuthField({ id, label, type, value, onChange, placeholder, autoComplete, hint, minLength }: Props) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="ev-auth__field">
      <label htmlFor={id}>{label}</label>
      <div className="ev-auth__input-wrap">
        <input id={id} name={id} required type={type === "password" && visible ? "text" : type}
          value={value} onChange={(event) => onChange(event.target.value)} placeholder={placeholder}
          autoComplete={autoComplete} minLength={minLength} aria-describedby={hint ? `${id}-hint` : undefined}
          className={type === "password" ? "ev-auth__password" : undefined} />
        {type === "password" && (
          <button className="ev-auth__reveal" type="button" onClick={() => setVisible(!visible)}
            aria-label={`${visible ? "Hide" : "Show"} ${label.toLowerCase()}`} aria-pressed={visible} aria-controls={id}>
            {visible ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
          </button>
        )}
      </div>
      {hint && <p className="ev-auth__hint" id={`${id}-hint`}>{hint}</p>}
    </div>
  );
}
