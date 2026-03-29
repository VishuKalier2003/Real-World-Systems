#!/bin/bash

echo "=============================="
echo "Phase III k6 Logged Test Run"
echo "=============================="

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BASE_DIR="results_$TIMESTAMP"

mkdir -p "$BASE_DIR"

tests=(
  "test_signup.js"
  "test_login_load.js"
  "test_token_flow.js"
  "test_multi_device.js"
  "test_refresh_storm.js"
  "test_spike.js"
  "test_drift.js"
  "test_soak.js"
  "test_invalid_login.js"
)

for test in "${tests[@]}"
do
  name=$(basename "$test" .js)
  test_dir="$BASE_DIR/$name"

  mkdir -p "$test_dir"

  echo ""
  echo "---------------------------------------"
  echo "Running $test (LOG MODE)"
  echo "---------------------------------------"

  k6 run "$test" \
    --summary-export="$test_dir/summary.json" \
    > "$test_dir/output.log" 2>&1

  echo "Completed $test"
  echo "Logs stored in $test_dir"

  sleep 10
done

echo "=============================="
echo "All logs stored in: $BASE_DIR"
echo "=============================="