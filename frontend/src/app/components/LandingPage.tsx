import { useEffect, useRef, type ReactNode } from "react";
import { ArrowDown, ArrowUpRight } from "lucide-react";
import heroImage from "../../assets/landing/coastal-sedan.jpg";
import editorialImage from "../../assets/landing/modern-suv.jpg";
import VehicleSkeletons from "./VehicleSkeletons";
import "../../styles/landing.css";

export default function LandingPage({ featured, loading }: { featured: ReactNode; loading: boolean }) {
  const page = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches || !("IntersectionObserver" in window)) return;
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.remove("ev-reveal-pending");
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.08 });
    page.current?.querySelectorAll("[data-reveal]").forEach((element) => {
      element.classList.add("ev-reveal-pending");
      observer.observe(element);
    });
    return () => observer.disconnect();
  }, []);

  function scrollToCollection() {
    document.getElementById("ev-collection")?.scrollIntoView({ behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "instant" : "smooth" });
    document.getElementById("ev-collection")?.focus({ preventScroll: true });
  }

  return (
    <div className="ev-landing" ref={page}>
      <a className="ev-landing__skip" href="#landing-main" onClick={(event) => {
        event.preventDefault();
        document.getElementById("landing-main")?.focus();
      }}>Skip to content</a>
      <header className="ev-landing__nav">
        <a className="ev-landing__wordmark" href="/" aria-label="EV Store home">EV STORE<span aria-hidden="true">.</span></a>
        <nav aria-label="Main navigation">
          <a className="ev-landing__nav-browse" href="/#/catalogue">Our vehicles</a>
          <a href="/signin">Sign In</a>
          <a className="ev-landing__nav-signup" href="/signup">Sign Up <ArrowUpRight size={14} aria-hidden="true" /></a>
        </nav>
      </header>

      <main id="landing-main" tabIndex={-1}>
        <section className="ev-landing__cinema" aria-labelledby="landing-title">
          <img className="ev-landing__hero-photo" src={heroImage} alt="Illustrative silver electric sedan overlooking the coast at sunset" width={1672} height={941} fetchPriority="high" />
          <div className="ev-landing__hero-shade" aria-hidden="true" />
          <div className="ev-landing__hero-content">
            <p className="ev-landing__eyebrow">A new way forward</p>
            <h1 id="landing-title">The road ahead.<br /><em>Reimagined.</em></h1>
            <p className="ev-landing__hero-description">Discover an electric vehicle that feels like you.<br />For the everyday. And everything beyond.</p>
            <a className="ev-landing__cta" href="/#/catalogue">Browse Vehicles <ArrowUpRight size={18} aria-hidden="true" /></a>
          </div>
          <div className="ev-landing__hero-bottom"><span>Electric by nature. Individual by choice.</span><button onClick={scrollToCollection}>Discover the collection <ArrowDown size={15} aria-hidden="true" /></button></div>
        </section>

        <section className="ev-landing__statement" aria-labelledby="statement-title" data-reveal>
          <p className="ev-landing__eyebrow">Welcome to EV Store</p>
          <h2 id="statement-title">A different kind of drive.<br /><em>A world of possibilities.</em></h2>
          <p>From your morning commute to the long way home, find an electric vehicle that fits your life. Explore, compare, and make your next move with confidence.</p>
        </section>

        <section className="ev-landing__collection" id="ev-collection" tabIndex={-1} aria-labelledby="featured-heading" data-reveal>
          <div className="ev-landing__section-heading"><div><p className="ev-landing__eyebrow">The collection</p><h2 id="featured-heading">Find your next chapter.</h2></div><a className="ev-landing__text-link" href="/#/catalogue">View all vehicles <ArrowUpRight size={18} aria-hidden="true" /></a></div>
          {loading ? <VehicleSkeletons count={3} /> : <div className="vehicle-grid">{featured}</div>}
          <p className="ev-landing__fine-print">Range figures are illustrative sample specifications. Vehicle images may illustrate the body type.</p>
        </section>

        <section className="ev-landing__editorial" aria-labelledby="editorial-title" data-reveal>
          <figure><img src={editorialImage} alt="Illustrative forest-green electric SUV outside a warm stone house surrounded by olive trees" width={1536} height={1024} loading="lazy" /><figcaption>Electric living, imagined.</figcaption></figure>
          <div className="ev-landing__editorial-copy"><p className="ev-landing__eyebrow">Made for your everyday</p><h2 id="editorial-title">More room<br />for <em>possibility.</em></h2><p>City streets. Weekend escapes. Whatever your next chapter looks like, there’s a shape and style to explore.</p><a className="ev-landing__text-link" href="/#/catalogue">Explore the collection <ArrowUpRight size={18} aria-hidden="true" /></a></div>
        </section>

        <section className="ev-landing__journey" aria-labelledby="journey-title" data-reveal>
          <div className="ev-landing__section-heading"><div><p className="ev-landing__eyebrow">Your journey, simplified</p><h2 id="journey-title">A considered choice.<br />A confident next step.</h2></div></div>
          <div className="ev-landing__steps">
            <a href="/#/matchmaker"><span className="ev-landing__step-number">01 / Discover</span><h3>Find your electric match.</h3><p>A few questions about your budget and daily journeys. Possible fits from our actual inventory.</p><span className="ev-landing__step-link">Find my EV <ArrowUpRight size={16} aria-hidden="true" /></span></a>
            <a href="/#/compare"><span className="ev-landing__step-number">02 / Compare</span><h3>See the difference.</h3><p>Add up to three vehicles to compare their specifications side by side.</p><span className="ev-landing__step-link">Compare vehicles <ArrowUpRight size={16} aria-hidden="true" /></span></a>
            <a href="/signup"><span className="ev-landing__step-number">03 / Make it yours</span><h3>Take the next step.</h3><p>Create your account to build your cart and continue your journey with EV Store.</p><span className="ev-landing__step-link">Create an account <ArrowUpRight size={16} aria-hidden="true" /></span></a>
          </div>
        </section>
      </main>

      <footer className="ev-landing__footer">
        <div className="ev-landing__footer-top"><a className="ev-landing__wordmark" href="/">EV STORE<span aria-hidden="true">.</span></a><p>Your next chapter.<br /><em>All electric.</em></p><nav aria-label="Footer navigation"><a href="/#/catalogue">Browse Vehicles</a><a href="/signin">Sign In</a><a href="/signup">Sign Up</a></nav></div>
        <div className="ev-landing__footer-bottom"><span>EV Store · Electric possibilities.</span><span>Lifestyle imagery depicts illustrative vehicle concepts.</span></div>
      </footer>
    </div>
  );
}
