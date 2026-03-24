import { getGatekeeperBaseUrl } from "../api/gatekeeperClient";
import { FEATURE_CATALOG } from "../constants/featureCatalog";

function getFrontendOrigin() {
  if (typeof window === "undefined") {
    return "Unavailable";
  }

  return window.location.origin;
}

function detectDeploymentTarget() {
  if (typeof window === "undefined") {
    return "Static frontend hosting";
  }

  const { hostname } = window.location;

  if (hostname === "localhost" || hostname === "127.0.0.1") {
    return "Local development";
  }

  if (hostname.endsWith(".netlify.app")) {
    return "Netlify";
  }

  if (hostname.endsWith(".vercel.app")) {
    return "Vercel";
  }

  return `Custom static host (${hostname})`;
}

export default function ArchitectureNote() {
  return (
    <section className="architecture-note panel">
      <p className="eyebrow">Consumer App Architecture</p>
      <h2>This UI is a thin client of the GateKeeper data plane</h2>
      <p>
        The demo app does not own flag configuration. It asks GateKeeper for each
        feature decision through <code>/api/evaluate</code>, then renders the
        interface based on the returned flag state.
      </p>
      <dl className="context-list">
        <div>
          <dt>GateKeeper API</dt>
          <dd>{getGatekeeperBaseUrl() || "Not configured"}</dd>
        </div>
        <div>
          <dt>Features evaluated</dt>
          <dd>{FEATURE_CATALOG.length} live flags</dd>
        </div>
        <div>
          <dt>Deployment target</dt>
          <dd>{detectDeploymentTarget()}</dd>
        </div>
        <div>
          <dt>Frontend origin</dt>
          <dd>{getFrontendOrigin()}</dd>
        </div>
      </dl>
    </section>
  );
}
