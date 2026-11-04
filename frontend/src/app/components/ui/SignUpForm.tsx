import React from "react";
import { ArrowRight } from "lucide-react";
import AuthField from "./AuthField";

interface SignUpProps {
  form: { name: string; email: string; password: string; confirm: string };
  setForm: React.Dispatch<React.SetStateAction<{ name: string; email: string; password: string; confirm: string }>>;
  onSubmit: (e: React.FormEvent) => void;
  error: string;
  loading: boolean;
  setView: (view: "signin" | "register") => void;
}

const SignUpForm: React.FC<SignUpProps> = React.memo(({ form, setForm, onSubmit, error, loading, setView }) => (
  <form className="ev-auth__form" onSubmit={onSubmit} aria-busy={loading}>
    {error && <p className="ev-auth__error" role="alert">{error}</p>}
    <AuthField id="signup-name" label="Full name" type="text" placeholder="Alex Morgan" autoComplete="name"
      value={form.name} onChange={(name) => setForm({ ...form, name })} />
    <AuthField id="signup-email" label="Email address" type="email" placeholder="you@example.com" autoComplete="email"
      value={form.email} onChange={(email) => setForm({ ...form, email })} />
    <AuthField id="signup-password" label="Password" type="password" placeholder="Create a password" autoComplete="new-password"
      minLength={8} hint="Use at least 8 characters." value={form.password} onChange={(password) => setForm({ ...form, password })} />
    <AuthField id="signup-confirm" label="Confirm password" type="password" placeholder="Re-enter your password" autoComplete="new-password"
      minLength={8} value={form.confirm} onChange={(confirm) => setForm({ ...form, confirm })} />
    <button type="submit" className="ev-auth__submit" disabled={loading}>
      {loading ? "Creating your account…" : "Sign Up"}<ArrowRight size={18} aria-hidden="true" />
    </button>
    <p className="ev-auth__switch">Already have an account? <button type="button" disabled={loading} onClick={() => setView("signin")}>Sign in</button></p>
  </form>
));

export default SignUpForm;
