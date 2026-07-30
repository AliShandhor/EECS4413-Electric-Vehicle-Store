import React from "react";

type View = "signin" | "register" | "catalogue" | "detail" | "cart" | "checkout" | "confirmed" | "admin" | "compare" | "hotdeals";

interface SignInProps {
  form: { email: string; password: string };
  setForm: React.Dispatch<React.SetStateAction<{ email: string; password: string }>>;
  onSubmit: (e: React.FormEvent) => void;
  error: string;
  setView: React.Dispatch<React.SetStateAction<View>>;
}

const SignInForm: React.FC<SignInProps> = React.memo(({ form, setForm, onSubmit, error, setView }) => {
  return (
    <form
      className="bg-card border border-border rounded-sm p-6 flex flex-col gap-4"
      onSubmit={onSubmit}
    >
      {error && <p className="text-xs text-destructive bg-destructive/10 px-3 py-2 rounded-sm">{error}</p>}
      <div>
        <label className="text-xs text-muted-foreground block mb-1">Email Address</label>
        <input
          required
          type="email"
          placeholder="john.smith@example.com"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          className="w-full border border-border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent"
        />
      </div>
      <div>
        <div className="flex justify-between mb-1">
          <label className="text-xs text-muted-foreground">Password</label>
          <button type="button" className="text-xs text-accent hover:underline">Forgot password?</button>
        </div>
        <input
          required
          type="password"
          placeholder="••••••••"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          className="w-full border border-border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent"
        />
      </div>
      <button
        type="submit"
        className="w-full py-2.5 bg-foreground text-white rounded-sm font-semibold hover:opacity-90 transition-opacity mt-1"
      >
        SIGN IN
      </button>
      <p className="text-center mt-3">
        <button
          onClick={() => setView("register")}
          className="text-xs text-muted-foreground hover:text-foreground underline"
        >
          Create account
        </button>
      </p>
    </form>
  );
});

export default SignInForm;