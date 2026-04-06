#!/bin/bash

echo "Running Smoke Test..."
k6 run smoke.js

echo "Running Load Test..."
k6 run load.js

echo "Running Spike Test..."
k6 run spike.js

echo "Running Soak Test..."
k6 run soak.js

echo "Running End-to-End Test..."
k6 run e2e.js