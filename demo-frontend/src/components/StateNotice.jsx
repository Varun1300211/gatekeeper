export default function StateNotice({ error, loading }) {
  if (loading) {
    return (
      <section className="state-banner panel">
        <div className="spinner" aria-hidden="true" />
        <div>
          <strong>Fetching GateKeeper evaluations</strong>
          <p>The demo is checking all configured feature flags for this user and environment.</p>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="state-banner panel error-state">
        <div>
          <strong>Unable to reach GateKeeper</strong>
          <p>{error}</p>
        </div>
      </section>
    );
  }

  return null;
}
