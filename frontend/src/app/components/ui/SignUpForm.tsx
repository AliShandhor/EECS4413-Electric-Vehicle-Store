import React from "react";

type View = "signin" | "register" | "catalogue" | "detail" | "cart" | "checkout" | "confirmed" | "admin" | "compare" | "hotdeals";

interface SignUpProps {
  form: { name: string; email: string; password: string; confirm: string };
  setForm: React.Dispatch<React.SetStateAction<{ name: string; email: string; password: string; confirm: string }>>;
  onSubmit: (e: React.FormEvent) => void;
  error: string;
  setView: React.Dispatch<React.SetStateAction<View>>;
}

const SignUpForm: React.FC<SignUpProps> = React.memo(({ form, setForm, onSubmit, error, setView }) => {
  return (
    <form
      className="bg-card border border-border rounded-sm p-6 flex flex-col gap-4"
      onSubmit={onSubmit}
    >
      {error && <p className="text-xs text-destructive bg-destructive/10 px-3 py-2 rounded-sm">{error}</p>}
      {[
        { label: "Full Name", key: "name", type: "text", placeholder: "Ali Shandhor" },
        { label: "Email Address", key: "email", type: "email", placeholder: "ali@example.com" },
        { label: "Password", key: "password", type: "password", placeholder: "Min. 6 characters" },
        { label: "Confirm Password", key: "confirm", type: "password", placeholder: "Repeat password" },
      ].map(({ label, key, type, placeholder }) => (
        <div key={key}>
          <label className="text-xs text-muted-foreground block mb-1">{label}</label>
          <input
            required
            type={type}
            placeholder={placeholder}
            value={form[key as keyof typeof form]}
            onChange={(e) => setForm({ ...form, [key]: e.target.value })}
            className="w-full border border-border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent"
          />
        </div>
      ))}
      <button
        type="submit"
        className="w-full py-2.5 bg-foreground text-white rounded-sm font-semibold hover:opacity-90 transition-opacity mt-1"
      >
        CREATE ACCOUNT
      </button>
      <p className="text-center mt-3">
        Already have an account?{" "}
        <button
          onClick={() => { setView("signin"); }}
          className="text-xs text-muted-foreground hover:text-foreground underline"
        >
          Sign in
        </button>
      </p>
    </form>
  );
});

export default SignUpForm;