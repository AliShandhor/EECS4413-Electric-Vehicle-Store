import React from "react";
import { ArrowRight } from "lucide-react";
import AuthField from "./AuthField";

interface SignInProps {
  form: { email: string; password: string };
  setForm: React.Dispatch<React.SetStateAction<{ email: string; password: string }>>;
  onSubmit: (e: React.FormEvent) => void;
  error: string;
  loading: boolean;
  setView: (view: "signin" | "register") => void;
}

const SignInForm: React.FC<SignInProps> = React.memo(({ form, setForm, onSubmit, error, loading, setView }) => (
  <form className="ev-auth__form" onSubmit={onSubmit} aria-busy={loading}>
    {error && <p className="ev-auth__error" role="alert">{error}</p>}
    <AuthField id="signin-email" label="Email address" type="email" placeholder="you@example.com" autoComplete="email"
      value={form.email} onChange={(email) => setForm({ ...form, email })} />
    <AuthField id="signin-password" label="Password" type="password" placeholder="Enter your password" autoComplete="current-password"
      value={form.password} onChange={(password) => setForm({ ...form, password })} />
    <button type="submit" className="ev-auth__submit" disabled={loading}>
      {loading ? "Signing in…" : "Sign In"}<ArrowRight size={18} aria-hidden="true" />
    </button>
    <p className="ev-auth__switch">New to EV Store? <button type="button" disabled={loading} onClick={() => setView("register")}>Create an account</button></p>
  </form>
));

export default SignInForm;
