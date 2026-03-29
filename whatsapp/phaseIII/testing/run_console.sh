#!/bin/bash

echo "=============================="
echo "Phase III k6 Console Test Run"
echo "=============================="

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
  echo ""
  echo "---------------------------------------"
  echo "Running $test (LIVE OUTPUT)"
  echo "---------------------------------------"

  k6 run "$test"

  echo "Completed $test"
  echo ""

  # cooldown to stabilize system
  sleep 10
done

echo "=============================="
echo "All tests completed (console mode)"
echo "=============================="