import { ENVIRONMENT_OPTIONS, USER_OPTIONS } from "../constants/featureCatalog";

export default function ControlBar({
  environment,
  onEnvironmentChange,
  onUserChange,
  userId,
}) {
  return (
    <section className="control-bar panel">
      <div>
        <p className="eyebrow">Demo Context</p>
        <h2>Evaluate the same UI for different users and environments</h2>
        <p className="muted">
          Change the identity below and the app will re-check GateKeeper for each
          feature on the page.
        </p>
      </div>

      <div className="control-grid">
        <label className="field">
          <span>User ID</span>
          <select value={userId} onChange={(event) => onUserChange(event.target.value)}>
            {USER_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>Environment</span>
          <select
            value={environment}
            onChange={(event) => onEnvironmentChange(event.target.value)}
          >
            {ENVIRONMENT_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
      </div>
    </section>
  );
}
