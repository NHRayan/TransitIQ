# TransitIQ
Lock‑free multi‑modal routing engine for smart city traffic. Built with Java 21, virtual threads, and admissible A* search. Group 4 – BOLEH | SCSE1224 Advanced Programming


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

## ✨ Features

- **Admissible A\* search** with Haversine heuristic – always finds the optimal route
- **Three optimisation strategies** – FASTEST (time), CHEAPEST (money), ECO (CO₂)
- **Lock‑free traffic simulation** – background thread updates congestion every 5 s, no locks
- **Multi‑modal graph** – CAR, BUS, TRAIN, WALK edges on a single network
- **Real‑world KL coordinates** – nodes include Masjid Jamek, KLCC, Bukit Bintang, UTM Semarak
- **Stream‑based congestion report** – per‑district delay averages using `groupingBy`
- **Async trip logging** – JSON‑lines history written via `CompletableFuture`
- **Robust error handling** – custom exception hierarchy for graceful failure

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
├── LICENSE
├── data/
│ ├── nodes.csv (25‑node KL graph)
│ ├── edges.csv
│ ├── nodes_500.csv (benchmark graph)
│ └── edges_500.csv
└── src/
├── main/java/com/transitiq/
│ ├── App.java # entry point
│ ├── cli/
│ │ └── CliController.java 
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
│ │ ├── RouteSegment.java
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
└── test/java/com/transitiq/ (unit & integration tests)


---

## ⚙️ Getting Started

### Prerequisites
- Java 21 (or later)
- Maven 3.9+ (or use the VS‑Code Maven wrapper)

### Build & Test
```bash
mvn clean verify

Run the Application
bash
mvn exec:java -Dexec.mainClass=com.transitiq.App

🕹️ Demo Commands

- route FASTEST J01 TS05
  Compute the quickest multi‑modal path.


- congestion
  Print a real‑time district congestion report.


- trips
  List saved trip history.


- exit

🔒 Thread‑Safety Proof
TransitNode and TransitEdge are immutable Java records.

The traffic weight map is stored in an AtomicReference<Map<String,Double>>; a snapshot is taken once at the start of each A* search — zero locks.

TrafficSimulator replaces the map atomically via Map.copyOf().

Each concurrent route search works on its own PriorityQueue and HashSet — no shared mutable state.

📊 Performance Requirement
Non‑functional requirement NFR‑2:
A route search on a 500‑node, 2000‑edge graph must complete in < 150 ms on commodity JDK 21 hardware.
The PerformanceTest class will enforce this once the test suite is added.


