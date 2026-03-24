import { useEffect, useState } from "react";
import { evaluateFlag } from "../api/gatekeeperClient";
import { FEATURE_CATALOG } from "../constants/featureCatalog";

function normalizeResults(results) {
  return Object.fromEntries(results.map((item) => [item.flagKey, item]));
}

export function useFlagEvaluations({ userId, environment }) {
  const [evaluations, setEvaluations] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [lastUpdatedAt, setLastUpdatedAt] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    async function loadEvaluations() {
      setLoading(true);
      setError("");

      try {
        const results = await Promise.all(
          FEATURE_CATALOG.map((feature) =>
            evaluateFlag({
              flagKey: feature.key,
              userId,
              environment,
              signal: controller.signal,
            }),
          ),
        );

        setEvaluations(normalizeResults(results));
        setLastUpdatedAt(new Date().toISOString());
      } catch (loadError) {
        if (loadError.name === "AbortError") {
          return;
        }

        setEvaluations({});
        setError(loadError.message || "Unable to evaluate feature flags.");
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadEvaluations();

    return () => controller.abort();
  }, [environment, userId]);

  return {
    evaluations,
    loading,
    error,
    lastUpdatedAt,
  };
}
