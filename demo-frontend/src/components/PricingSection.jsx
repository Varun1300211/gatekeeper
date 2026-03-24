export default function PricingSection({ enabled }) {
  if (enabled) {
    return (
      <section className="feature-card pricing pricing-new">
        <div className="section-header">
          <div>
            <p className="eyebrow">new-pricing enabled</p>
            <h3>Updated pricing presentation</h3>
          </div>
          <span className="badge subtle">Most popular</span>
        </div>

        <div className="pricing-grid">
          <article>
            <span>Starter</span>
            <strong>$19</strong>
            <p>For small teams validating rollout workflows.</p>
          </article>
          <article className="featured-tier">
            <span>Growth</span>
            <strong>$49</strong>
            <p>Includes audit logs, metrics, and progressive delivery support.</p>
          </article>
          <article>
            <span>Enterprise</span>
            <strong>Contact us</strong>
            <p>For custom rollout policy, SSO, and operational support.</p>
          </article>
        </div>
      </section>
    );
  }

  return (
    <section className="feature-card pricing pricing-old">
      <div className="section-header">
        <div>
          <p className="eyebrow">new-pricing disabled</p>
          <h3>Legacy pricing table</h3>
        </div>
      </div>

      <div className="legacy-pricing">
        <div>
          <strong>Basic</strong>
          <span>$29</span>
        </div>
        <div>
          <strong>Pro</strong>
          <span>$69</span>
        </div>
      </div>
    </section>
  );
}
