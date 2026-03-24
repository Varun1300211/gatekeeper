export default function HomepageSection({ enabled }) {
  if (enabled) {
    return (
      <section className="feature-card hero hero-new">
        <div className="feature-copy">
          <p className="eyebrow">new-homepage enabled</p>
          <h3>Launch new checkout experiences with confidence</h3>
          <p>
            The redesigned hero highlights progressive delivery, instant rollback,
            and environment-aware releases for higher confidence launches.
          </p>
          <div className="chip-row">
            <span>Environment targeting</span>
            <span>Percentage rollout</span>
            <span>Immediate rollback</span>
          </div>
        </div>
        <div className="hero-metrics">
          <div>
            <strong>99.95%</strong>
            <span>release confidence</span>
          </div>
          <div>
            <strong>4 flags</strong>
            <span>evaluated live</span>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="feature-card hero hero-old">
      <div className="feature-copy">
        <p className="eyebrow">new-homepage disabled</p>
        <h3>Feature flag management for safer releases</h3>
        <p>
          This is the legacy homepage treatment. It is simpler, flatter, and less
          visually expressive than the new hero experience.
        </p>
      </div>
      <div className="legacy-box">
        <strong>Legacy Homepage</strong>
        <span>Static hero content</span>
      </div>
    </section>
  );
}
