#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${GATEKEEPER_BASE_URL:-http://localhost:8080}"
USERNAME="${GATEKEEPER_USERNAME:-viewer}"
PASSWORD="${GATEKEEPER_PASSWORD:-viewer123}"
REQUESTS="${REQUESTS:-500}"
FLAGS="${FLAGS:-new-homepage,beta-checkout,beta-banner,new-pricing,redis-warmup}"
USERS="${USERS:-alice,bob,charlie,sdk-user,github-actions}"
ENVIRONMENTS="${ENVIRONMENTS:-test,uat,prod}"

IFS=',' read -r -a FLAG_ARRAY <<< "$FLAGS"
IFS=',' read -r -a USER_ARRAY <<< "$USERS"
IFS=',' read -r -a ENVIRONMENT_ARRAY <<< "$ENVIRONMENTS"

if [[ "$REQUESTS" -lt 1 ]]; then
  echo "REQUESTS must be at least 1"
  exit 1
fi

results_file="$(mktemp)"
latencies_file="$(mktemp)"
trap 'rm -f "$results_file" "$latencies_file"' EXIT

echo "Running GateKeeper evaluation load test"
echo "Base URL: ${BASE_URL}"
echo "Requests: ${REQUESTS}"
echo "Flags: ${FLAGS}"
echo "Users: ${USERS}"
echo "Environments: ${ENVIRONMENTS}"
echo

start_seconds="$(date +%s)"

for ((request_number = 1; request_number <= REQUESTS; request_number++)); do
  flag_key="${FLAG_ARRAY[$(((request_number - 1) % ${#FLAG_ARRAY[@]}))]}"
  user_id="${USER_ARRAY[$(((request_number - 1) % ${#USER_ARRAY[@]}))]}"
  environment="${ENVIRONMENT_ARRAY[$(((request_number - 1) % ${#ENVIRONMENT_ARRAY[@]}))]}"
  url="${BASE_URL}/api/evaluate?flagKey=${flag_key}&userId=${user_id}&environment=${environment}"

  if output="$(curl --silent --show-error --output /dev/null \
      --write-out "%{http_code} %{time_total}" \
      --user "${USERNAME}:${PASSWORD}" \
      --header "X-API-Key: load-test-client-${request_number}" \
      "${url}")"; then
    status_code="$(awk '{ print $1 }' <<< "$output")"
    time_total_seconds="$(awk '{ print $2 }' <<< "$output")"
  else
    status_code="000"
    time_total_seconds="0"
  fi

  latency_ms="$(awk -v seconds="$time_total_seconds" 'BEGIN { printf "%.0f", seconds * 1000 }')"
  printf "%s %s\n" "$status_code" "$latency_ms" >> "$results_file"
done

end_seconds="$(date +%s)"
duration_seconds="$((end_seconds - start_seconds))"

awk '{ print $2 }' "$results_file" | sort -n > "$latencies_file"
total_count="$(wc -l < "$results_file" | tr -d ' ')"
success_count="$(awk '$1 >= 200 && $1 < 300 { count++ } END { print count + 0 }' "$results_file")"
rate_limited_count="$(awk '$1 == 429 { count++ } END { print count + 0 }' "$results_file")"
failure_count="$((total_count - success_count))"

avg_ms="$(awk '{ sum += $2 } END { printf "%.1f", sum / NR }' "$results_file")"
min_ms="$(sed -n '1p' "$latencies_file")"
max_ms="$(sed -n "${total_count}p" "$latencies_file")"

percentile() {
  local percentile_value="$1"
  local rank
  rank="$(awk -v count="$total_count" -v percentile="$percentile_value" 'BEGIN {
    value = count * percentile
    position = int(value)
    if (position < value) {
      position++
    }
    if (position < 1) {
      position = 1
    }
    print position
  }')"
  sed -n "${rank}p" "$latencies_file"
}

p50_ms="$(percentile 0.50)"
p95_ms="$(percentile 0.95)"
p99_ms="$(percentile 0.99)"

echo "Load test summary"
echo "Requests sent: ${total_count}"
echo "Successful 2xx responses: ${success_count}"
echo "Rate limited 429 responses: ${rate_limited_count}"
echo "Non-2xx/failed responses: ${failure_count}"
echo "Wall-clock duration: ${duration_seconds}s"
echo "Latency min/avg/p50/p95/p99/max: ${min_ms}/${avg_ms}/${p50_ms}/${p95_ms}/${p99_ms}/${max_ms} ms"

if [[ "$success_count" -ne "$total_count" ]]; then
  echo
  echo "Some requests did not return 2xx. Check backend logs or reduce REQUESTS."
  exit 1
fi
