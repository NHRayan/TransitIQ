<!--
  ██████╗   ██████╗  ██╗     ███████╗ ██╗  ██╗
  ██╔══██╗ ██╔═══██╗ ██║     ██╔════╝ ██║  ██║
  ██████╔╝ ██║   ██║ ██║     █████╗   ███████║
  ██╔══██╗ ██║   ██║ ██║     ██╔══╝   ██╔══██║
  ██████╔╝ ╚██████╔╝ ███████╗███████╗ ██║  ██║
  ╚═════╝   ╚═════╝  ╚══════╝╚══════╝ ╚═╝  ╚═╝
  
  Author : NAHID HASAN RAYAN (NHR)
  Group  : 4 – BOLEH
  Course : SCSE1224 Advanced Programming
  Project: TransitIQ – Lock‑Free Multi‑Modal Routing Engine
  Date   : 23 April 2026
-->

# TransitIQ – Smart City Traffic & Public Transit Router

**Lock‑Free Multi‑Modal Routing Engine**  
*Built for SCSE1224 Advanced Programming, Universiti Teknologi Malaysia (UTM)*

**Group 4 – BOLEH**  
*Nahid Hasan Rayan (NHR), Jobayer Alam, Khalid Waleed Issa, Zay Yar Shin*

---

## 🚀 Overview

TransitIQ is a high‑performance, thread‑safe route planning engine for a smart city.
It loads a graph of roads, bus corridors, and train lines from CSV files,
then finds optimal multi‑modal routes using an **admissible A\*** search while
**live traffic updates are applied concurrently — without a single lock**.

All concurrency is powered by **AtomicReference** and **virtual threads** from JDK 21,
giving you zero‑blocking snapshots of the traffic state.

---

## 🧰 Tech Stack

| Layer          | Technology                |
| -------------- | ------------------------- |
| Language       | Java 21 (LTS)             |
| Build Tool     | Apache Maven 3.9+         |
| Testing        | JUnit 5 + AssertJ         |
| Coverage       | JaCoCo (85% minimum)      |
| Code Style     | Google Java Style (Checkstyle) |
| Concurrency    | Virtual Threads, `AtomicReference`, `CompletableFuture` |

---

## 📁 Project Structure
TransitIQ/
├── pom.xml
├── .gitignore
├── README.md
├── data/
│ ├── nodes.csv (sample graph)
│ └── edges.csv
└── src/
├── main/java/com/transitiq/
│ ├── App.java # entry point
│ ├── cli/
│ │ └── CLIController.java
│ ├── graph/
│ │ ├── NodeType.java
│ │ ├── TransportMode.java
│ │ ├── TransitNode.java
│ │ ├── TransitEdge.java
│ │ ├── CityGraph.java
│ │ └── GraphLoader.java
│ ├── routing/
│ │ ├── RoutingStrategy.java
│ │ ├── FastestStrategy.java
│ │ ├── CheapestStrategy.java
│ │ ├── EcoStrategy.java
│ │ ├── AStarRouter.java
│ │ └── RouteResult.java
│ ├── state/
│ │ ├── TrafficObserver.java
│ │ ├── GraphStateManager.java
│ │ └── TrafficSimulator.java
│ ├── io/
│ │ ├── TripRecord.java
│ │ └── TripLogger.java
│ ├── validation/
│ │ └── InputValidator.java
│ └── exception/
│ ├── TransitIQException.java
│ ├── NoRouteException.java
│ ├── InvalidNodeException.java
│ ├── StaleSnapshotException.java
│ └── DataCorruptionException.java
└── test/java/com/transitiq/ (mirrors main for unit tests)


---

## ⚙️ Build & Run

### Prerequisites
- Java 21 (or later)
- Maven 3.9+ (wrapper included if you run `mvnw`)

### Compile and test
```bash
mvn clean verify

🧪 Running Tests
All tests (unit + integration) run with mvn test.
A special ConcurrencyStressTest launches 20 virtual threads querying routes
while the TrafficSimulator continuously updates the congestion map — proving
that reads never block writes.

🔒 Thread‑Safety Proof
TransitNode and TransitEdge are immutable Java records.

The traffic weight map is stored in an AtomicReference<Map<String,Double>>; a
snapshot is taken once at the start of each A* search — zero locks.

TrafficSimulator replaces the map atomically using Map.copyOf().

Each concurrent route query works with its own PriorityQueue and HashSet —
no shared mutable state.

📊 Performance Requirement
Non‑functional requirement NFR‑2:
A route search on a 500‑node, 2000‑edge graph must complete in < 150 ms on
commodity JDK 21 hardware.
The PerformanceTest class benchmarks this automatically and fails the build if
the 99th percentile exceeds the limit.



<!-- ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ ░ NAHID HASAN RAYAN (NHR) – SCSE1224 ░ ░ GROUP 4 – BOLEH ░ ░ FINGERPRINT: 4BOLEH::TRANSITIQ::2026 ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ -->