import { FEATURE_CATALOG } from "../constants/featureCatalog";

function formatTimestamp(value) {
  if (!value) {
    return "Not yet evaluated";
  }

  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "medium",
  }).format(new Date(value));
}

export default function EvaluationSummary({
  environment,
  error,
  evaluations,
  lastUpdatedAt,
  loading,
  userId,
}) {
  return (
    <aside className="summary panel">
      <div className="summary-header">
        <div>
          <p className="eyebrow">Evaluation Summary</p>
          <h2>Live GateKeeper decisions</h2>
        </div>
        <span className={`status-pill ${loading ? "pending" : "ready"}`}>
          {loading ? "Refreshing" : "Live"}
        </span>
      </div>

      <dl className="context-list">
        <div>
          <dt>User</dt>
          <dd>{userId}</dd>
        </div>
        <div>
          <dt>Environment</dt>
          <dd>{environment}</dd>
        </div>
        <div>
          <dt>Last updated</dt>
          <dd>{formatTimestamp(lastUpdatedAt)}</dd>
        </div>
      </dl>

      {error ? <p className="error-banner compact">{error}</p> : null}

      <ul className="summary-list">
        {FEATURE_CATALOG.map((feature) => {
          const enabled = evaluations[feature.key]?.enabled ?? false;
          const unresolved = !evaluations[feature.key] && !error;

          return (
            <li key={feature.key} className="summary-item">
              <div>
                <strong>{feature.label}</strong>
                <p>{feature.key}</p>
              </div>
              <span
                className={`feature-status ${
                  unresolved ? "unknown" : enabled ? "enabled" : "disabled"
                }`}
              >
                {unresolved ? "Pending" : enabled ? "Enabled" : "Disabled"}
              </span>
            </li>
          );
        })}
      </ul>
    </aside>
  );
}
