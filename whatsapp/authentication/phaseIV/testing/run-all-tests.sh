#!/bin/bash

mkdir -p results

echo "Running smoke test..."
k6 run tests/smoke.js --out json=results/smoke.json

echo "Running login load..."
k6 run tests/login-load.js --out json=results/login.json

echo "Running spike test..."
k6 run tests/spike.js --out json=results/spike.json

echo "Running soak test..."
k6 run tests/soak.js --out json=results/soak.json

echo "Running auth flow..."
k6 run tests/auth-flow.js --out json=results/auth-flow.json

echo "Running brute force..."
k6 run tests/brute-force.js --out json=results/brute.json

echo "All tests completed"