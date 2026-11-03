import { ArrowUpRight, Zap } from "lucide-react";
import heroImage from "../../assets/vehicles/ev-sedan.png";
import "../../styles/landing.css";
import type { ReactNode } from "react";
import VehicleSkeletons from "./VehicleSkeletons";

export default function LandingPage({ featured, loading }: { featured: ReactNode; loading: boolean }) {
  return (
    <div className="ev-landing">
      <a className="ev-landing__skip" href="#landing-main">Skip to content</a>
      <header className="ev-landing__header">
        <a className="ev-landing__brand" href="#/home" aria-label="EV Store home">
          <Zap aria-hidden="true" size={24} fill="currentColor" />
          <span>EV STORE<span className="ev-landing__dot">.</span></span>
        </a>
        <nav aria-label="Account navigation">
          <a className="ev-landing__nav-signin" href="#/signin">Sign In</a>
          <a className="ev-landing__button ev-landing__button--small" href="#/register">Sign Up <ArrowUpRight size={16} aria-hidden="true" /></a>
        </nav>
      </header>

      <main id="landing-main" tabIndex={-1}>
        <section className="ev-landing__hero" aria-labelledby="landing-title">
          <div className="ev-landing__copy">
            <p className="ev-landing__eyebrow"><span /> A new way forward</p>
            <h1 id="landing-title">Your next chapter.<br /><span>All electric.</span></h1>
            <p className="ev-landing__intro">Find the electric vehicle that fits your life. Explore the EV Store collection, compare your favourites, and take the next step toward your next drive.</p>
            <div className="ev-landing__actions">
              <a className="ev-landing__button" href="#/register">Sign Up <ArrowUpRight size={20} aria-hidden="true" /></a>
              <a className="ev-landing__button ev-landing__button--outline" href="#/signin">Sign In</a>
            </div>
            <p className="ev-landing__note">A fresh start, or your next great drive. Start here.</p>
          </div>
          <figure className="ev-landing__visual">
            <div className="ev-landing__image-label"><Zap size={15} aria-hidden="true" /> Electric by design</div>
            <img src={heroImage} alt="Silver electric sedan viewed from the front and side" width={1672} height={941} fetchPriority="high" />
            <figcaption><span>The future looks good on you.</span><span aria-hidden="true">01 / EV STORE</span></figcaption>
          </figure>
        </section>

        <section className="landing-featured" aria-labelledby="featured-heading">
          <div className="landing-featured__heading"><div><p className="ev-landing__eyebrow">Find your next drive</p><h2 id="featured-heading">Electric possibilities.</h2></div><a className="ev-landing__button" href="#/catalogue">Explore Vehicles <ArrowUpRight size={18} aria-hidden="true" /></a></div>
          {loading ? <VehicleSkeletons count={3} /> : <div className="vehicle-grid">{featured}</div>}
          <p className="landing-featured__note">*Range figures are illustrative sample specifications. Images may illustrate the vehicle body type.</p>
        </section>

        <section className="ev-landing__introduction" aria-labelledby="landing-introduction">
          <p className="ev-landing__eyebrow">Meet EV Store</p>
          <h2 id="landing-introduction">A better fit for<br />the road ahead.</h2>
          <p>From city-ready hatchbacks to spacious SUVs, discover electric vehicles in one place. Compare options, explore vehicle details, and make your next move with confidence.</p>
        </section>
      </main>

      <footer className="ev-landing__footer"><span>EV STORE</span><span>Your journey starts electric.</span></footer>
    </div>
  );
}
