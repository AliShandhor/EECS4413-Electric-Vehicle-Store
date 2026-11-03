import type { ReactNode } from "react";
import { ArrowLeft } from "lucide-react";
import "../../styles/auth.css";

export default function AuthShell({ title, sub, children }: { title: string; sub: string; children: ReactNode }) {
  return (
    <div className="ev-auth">
      <main className="ev-auth__main">
        <a className="ev-auth__back" href="#/home"><ArrowLeft size={16} aria-hidden="true" /> Back to home</a>
        <div className="ev-auth__panel">
          <h1>{title}</h1>
          <p className="ev-auth__subtitle">{sub}</p>
          {children}
        </div>
      </main>
    </div>
  );
}
