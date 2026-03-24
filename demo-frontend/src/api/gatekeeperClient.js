const baseUrl = (import.meta.env.VITE_GATEKEEPER_BASE_URL || "").replace(/\/$/, "");
const username = import.meta.env.VITE_GATEKEEPER_USERNAME;
const password = import.meta.env.VITE_GATEKEEPER_PASSWORD;

function buildHeaders() {
  const headers = {
    Accept: "application/json",
  };

  if (username && password) {
    headers.Authorization = `Basic ${btoa(`${username}:${password}`)}`;
  }

  return headers;
}

export function getGatekeeperBaseUrl() {
  return baseUrl;
}

export async function evaluateFlag({ flagKey, userId, environment, signal }) {
  if (!baseUrl) {
    throw new Error("Missing VITE_GATEKEEPER_BASE_URL.");
  }

  const params = new URLSearchParams({
    flagKey,
    userId,
    environment,
  });

  const response = await fetch(`${baseUrl}/api/evaluate?${params.toString()}`, {
    method: "GET",
    headers: buildHeaders(),
    signal,
  });

  if (!response.ok) {
    const detail = await response.text();
    throw new Error(
      `GateKeeper evaluation failed (${response.status}). ${detail || "Check the API URL and credentials."}`,
    );
  }

  return response.json();
}
