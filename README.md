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



## 📁 Project Structure

TransitIQ/
├── pom.xml
├── .gitignore
├── README.md
├── data/
│   ├── nodes.csv                 (20-node test graph)
│   ├── edges.csv
│   ├── nodes_500.csv             (benchmark graph)
│   └── edges_500.csv
└── src/
    ├── main/java/com/transitiq/
    │   ├── App.java                      // entry point, boots CLI
    │   ├── cli/
    │   │   └── CLIController.java        // command parser, orchestrator
    │   ├── graph/
    │   │   ├── NodeType.java             // enum: JUNCTION, BUS_STOP, TRAIN_STATION
    │   │   ├── TransportMode.java        // enum: CAR, BUS, TRAIN, WALK
    │   │   ├── TransitNode.java          // record (id, lat, lon, type)
    │   │   ├── TransitEdge.java          // record (from, to, mode, baseTime, maxSpeed, district, cost, co2)
    │   │   ├── CityGraph.java            // immutable nodes + adjacency list
    │   │   └── GraphLoader.java          // parses CSV, returns CityGraph
    │   ├── routing/
    │   │   ├── RoutingStrategy.java      // interface (computeEdgeCost, heuristic)
    │   │   ├── RouteSegment.java         // record (edge, actual time, cost, co2)
    │   │   ├── RouteResult.java          // list<RouteSegment>, total metrics
    │   │   ├── AStarRouter.java          // generic A* using strategy + snapshot
    │   │   ├── FastestStrategy.java
    │   │   ├── CheapestStrategy.java
    │   │   └── EcoStrategy.java
    │   ├── state/
    │   │   ├── TrafficObserver.java      // functional interface
    │   │   ├── GraphStateManager.java    // AtomicReference<Map<String,Double>>
    │   │   └── TrafficSimulator.java     // scheduled updater, notifies observers
    │   ├── io/
    │   │   ├── TripRecord.java           // record to serialize
    │   │   └── TripLogger.java           // async JSON appender
    │   ├── validation/
    │   │   └── InputValidator.java       // static regex validators
    │   └── exception/
    │       ├── TransitIQException.java   // root checked
    │       ├── NoRouteException.java
    │       ├── InvalidNodeException.java
    │       ├── StaleSnapshotException.java
    │       └── DataCorruptionException.java // unchecked
    └── test/java/com/transitiq/          (mirrors main with *Test suffix)
        ├── cli/
        │   └── CLIControllerTest.java
        ├── graph/
        │   ├── GraphLoaderTest.java
        │   └── CityGraphTest.java
        ├── routing/
        │   ├── FastestStrategyTest.java
        │   ├── CheapestStrategyTest.java
        │   ├── EcoStrategyTest.java
        │   └── AStarRouterTest.java
        ├── state/
        │   ├── GraphStateManagerTest.java
        │   ├── TrafficSimulatorTest.java
        │   └── ConcurrencyStressTest.java   // integration: many threads + updates
        ├── io/
        │   └── TripLoggerTest.java
        ├── validation/
        │   └── InputValidatorTest.java
        └── PerformanceTest.java            // A* benchmark on 500-node graph



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
