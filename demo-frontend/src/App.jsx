import { useState } from "react";
import ArchitectureNote from "./components/ArchitectureNote";
import BetaBanner from "./components/BetaBanner";
import CheckoutSection from "./components/CheckoutSection";
import ControlBar from "./components/ControlBar";
import EvaluationSummary from "./components/EvaluationSummary";
import HomepageSection from "./components/HomepageSection";
import PricingSection from "./components/PricingSection";
import StateNotice from "./components/StateNotice";
import { DEFAULT_ENVIRONMENT, DEFAULT_USER_ID } from "./constants/featureCatalog";
import { useFlagEvaluations } from "./hooks/useFlagEvaluations";

export default function App() {
  const [userId, setUserId] = useState(DEFAULT_USER_ID);
  const [environment, setEnvironment] = useState(DEFAULT_ENVIRONMENT);
  const { error, evaluations, lastUpdatedAt, loading } = useFlagEvaluations({
    userId,
    environment,
  });

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">GateKeeper Demo Consumer</p>
          <h1>Feature-flagged product surface backed by GateKeeper</h1>
          <p className="lead">
            A lightweight frontend app that proves feature flags are working by
            changing real UI sections across users and environments.
          </p>
        </div>
      </header>

      <main className="app-layout">
        <div className="main-column">
          <ControlBar
            environment={environment}
            onEnvironmentChange={setEnvironment}
            onUserChange={setUserId}
            userId={userId}
          />

          <StateNotice error={error} loading={loading} />

          <BetaBanner enabled={Boolean(evaluations["beta-banner"]?.enabled)} />
          <HomepageSection enabled={Boolean(evaluations["new-homepage"]?.enabled)} />

          <div className="content-grid">
            <CheckoutSection enabled={Boolean(evaluations["beta-checkout"]?.enabled)} />
            <PricingSection enabled={Boolean(evaluations["new-pricing"]?.enabled)} />
          </div>

          <ArchitectureNote />
        </div>

        <EvaluationSummary
          environment={environment}
          error={error}
          evaluations={evaluations}
          lastUpdatedAt={lastUpdatedAt}
          loading={loading}
          userId={userId}
        />
      </main>
    </div>
  );
}
