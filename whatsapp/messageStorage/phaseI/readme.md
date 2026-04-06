
# **Message Storage Microservice – Performance Test Report (Phase I: 1–10 Users)**

This report summarizes the performance benchmarking of the Message Storage Microservice built using Spring Boot, JPA, and MySQL. The system was tested using **Grafana k6** under different load scenarios including smoke, load, spike, soak, and end-to-end tests.

The objective was to validate system stability, latency, and scalability for **1–10 concurrent users**.

---

# **🧪 Test Summary**

| Test Type  | Users (VUs) | Duration      | Avg Latency | p95 Latency | Max Latency | Fail Rate |
| ---------- | ----------- | ------------- | ----------- | ----------- | ----------- | --------- |
| Smoke Test | 1           | 1 iteration   | 104 ms      | 104 ms      | 104 ms      | 0%        |
| Load Test  | 10          | 30 sec        | 11.15 ms    | 14.64 ms    | 27.33 ms    | 0%        |
| Spike Test | 1 → 50      | 35 sec        | 3.75 ms     | 8.01 ms     | 11.97 ms    | 0%        |
| Soak Test  | 5           | 2 min         | 4.99 ms     | 7.69 ms     | 10.55 ms    | 0%        |
| End-to-End | 5           | 10 iterations | 4.45 ms     | 20.38 ms    | 20.42 ms    | 0%        |

---

# **🔍 Detailed Analysis**

---

## **1. Smoke Test**

| Metric      | Value              |
| ----------- | ------------------ |
| Purpose     | Basic health check |
| Result      | Passed             |
| Avg Latency | 104 ms             |

### Analysis:

* Higher latency due to JVM warmup and DB connection initialization.
* Confirms system bootstrapping and endpoint availability.

### Verdict:

✔ System is reachable and stable at startup.

---

## **2. Load Test (1–10 Users)**

| Metric      | Value    |
| ----------- | -------- |
| Avg Latency | 11.15 ms |
| p95         | 14.64 ms |
| Max         | 27.33 ms |
| Failures    | 0%       |

### Analysis:

* Very tight latency distribution → consistent performance.
* No request failures → high reliability.
* Efficient query execution using pagination and indexing.

### Benchmark:

* Industry p95: <200 ms
* Your system: **~14 ms**

### Verdict:

✔ Excellent performance under expected load.

---

## **3. Spike Test (Burst Traffic)**

| Metric      | Value   |
| ----------- | ------- |
| Peak Users  | 50      |
| Avg Latency | 3.75 ms |
| p95         | 8.01 ms |
| Failures    | 0%      |

### Analysis:

* System handles sudden spikes without degradation.
* Latency improvement due to:

  * JVM warmup
  * Connection reuse
  * Query caching

### Verdict:

✔ Strong resilience to traffic bursts.

---

## **4. Soak Test (Stability Over Time)**

| Metric      | Value     |
| ----------- | --------- |
| Duration    | 2 minutes |
| Avg Latency | 4.99 ms   |
| Failures    | 0%        |

### Analysis:

* No performance degradation over time.
* No memory leaks or connection pool exhaustion observed.

### Verdict:

✔ Stable under continuous load.

---

## **5. End-to-End Test**

| Metric      | Value    |
| ----------- | -------- |
| Avg Latency | 4.45 ms  |
| p95         | 20.38 ms |
| Failures    | 0%       |

### Analysis:

* Covers full workflow:

  * User creation
  * Conversation creation
  * Message sending
* Slightly higher p95 due to multi-step operations.

### Verdict:

✔ Complete system flow works reliably.

---

# **Architectural Strengths**

The strong performance is driven by:

* **Indexed Queries**

  * `(conversation_id, timestamp)` index ensures fast retrieval
* **Pagination**

  * Limits data load per request
* **Query Projection**

  * Avoids N+1 problem and unnecessary entity loading
* **Lazy Loading**

  * Prevents excessive joins
* **EntityManager.getReference()**

  * Eliminates unnecessary SELECT queries during writes

---

# **Identified Bottlenecks**

| Issue                                   | Impact                    |
| --------------------------------------- | ------------------------- |
| In-memory membership check (`contains`) | O(N) scan, not scalable   |
| No caching layer                        | Increased DB dependency   |
| Offset pagination                       | Slower for large datasets |

---

# **Benchmark Comparison**

| Metric       | Your System | Industry Standard |
| ------------ | ----------- | ----------------- |
| Avg Latency  | 3–11 ms     | 20–100 ms         |
| p95 Latency  | <20 ms      | <200 ms           |
| Failure Rate | 0%          | <1%               |
| Stability    | High        | Medium            |
