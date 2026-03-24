export default function CheckoutSection({ enabled }) {
  if (enabled) {
    return (
      <section className="feature-card checkout checkout-new">
        <div className="section-header">
          <div>
            <p className="eyebrow">beta-checkout enabled</p>
            <h3>Fast checkout experience</h3>
          </div>
          <span className="badge">15% launch offer</span>
        </div>

        <div className="checkout-grid">
          <div className="checkout-item">
            <span>Team plan</span>
            <strong>$49</strong>
            <p>Billed monthly with fast checkout support.</p>
          </div>
          <div className="checkout-highlight">
            <strong>One-click confirmation</strong>
            <p>Saved billing profile, instant confirmation, and reduced checkout friction.</p>
            <button type="button">Start fast checkout</button>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="feature-card checkout checkout-old">
      <div className="section-header">
        <div>
          <p className="eyebrow">beta-checkout disabled</p>
          <h3>Classic checkout</h3>
        </div>
      </div>

      <div className="legacy-checkout">
        <p>Plan: Team</p>
        <p>Price: $59/month</p>
        <button type="button">Continue</button>
      </div>
    </section>
  );
}
