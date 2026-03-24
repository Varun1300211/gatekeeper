export default function BetaBanner({ enabled }) {
  if (!enabled) {
    return null;
  }

  return (
    <section className="beta-banner panel">
      <p className="eyebrow">Beta Experience</p>
      <h2>Beta features are enabled for this session</h2>
      <p>
        This banner is controlled by the <code>beta-banner</code> flag and appears
        only when GateKeeper evaluates it to <strong>true</strong>.
      </p>
    </section>
  );
}
