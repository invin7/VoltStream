# ⚡ VoltStream

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java 16+](https://img.shields.io/badge/Java-16%2B-orange)

A console-based Java simulation of an EV charging network. Multiple charging bays share a capacity-limited grid, power is balanced between connected vehicles, and tariffs adjust with grid load.

Built as a showcase of object-oriented design: interfaces, inheritance, factories, the strategy and observer patterns, and custom checked and unchecked exceptions.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [How It Works](#how-it-works)
- [Architecture](#architecture)
- [Error Handling](#error-handling)
- [Project Structure](#project-structure)
- [Design Patterns and OOP Concepts](#design-patterns-and-oop-concepts)
- [License](#license)

## Features

- Four vehicle types (`VehicleType` enum: sedan, heavy truck, scooter, bus), each with its own battery capacity and maximum charging rate
- Two bay types (`BayType` enum): Ultra-Fast (350 kW) and Fast-DC (150 kW)
- Automatic dispatch of arriving vehicles to the first free bay
- Proportional power scaling when total demand exceeds grid capacity
- Dynamic tariffs: a per-bay-type multiplier plus a surcharge when grid utilization passes 75%
- Live console dashboard with per-bay status and running revenue
- Custom checked and unchecked exceptions for validation and misconfiguration

## Getting Started

### Prerequisites

- JDK 16 or newer (the project uses switch expressions)

### Build and run

```bash
git clone https://github.com/invin7/VoltStream.git
cd VoltStream

javac -d out $(find src -name '*.java')
java -cp out grid.Main
```

You can also import the project into any Java IDE (Eclipse, IntelliJ IDEA, VS Code) and run `grid.Main`.

## Usage

The simulator is driven by a menu:

```
1. Add Vehicle
2. Simulate 12-Minute Time Step
3. View Dashboard
4. Exit Simulation
```

1. **Add Vehicle** — pick a vehicle type, then enter a license plate and an initial charge (0–99%). The vehicle is docked at the first free bay.
2. **Simulate 12-Minute Time Step** — advances the simulation by 0.2 hours. Energy is delivered, revenue accrues, and fully charged vehicles are undocked.
3. **View Dashboard** — shows the current state without advancing time.

Default configuration (in `Main.java`): 400 kW grid capacity and three bays (2 × Ultra-Fast, 1 × Fast-DC).

## How It Works

```
Vehicle arrives
      │
      ▼
Validation (VehicleFactory)
      │
      ▼
Bay allocation (EVNetworkController)
      │
      ▼
Grid power distribution
      │
      ▼
Tariff calculation (TariffStrategy)
      │
      ▼
Charging progress + dashboard update (events)
```

Each bay requests `min(bay hardware output, vehicle max rate)` kW. If the total exceeds the grid capacity, every allocation is scaled down by the same factor. The tariff for a step is the base rate (8.50 per kWh) times the bay's multiplier, which rises when the grid load factor crosses the bay's threshold.

## Architecture

`EVNetworkController` is the central coordinator. It manages the bays, dispatches vehicles, allocates grid power, advances the simulation, and notifies listeners of events.

```
                       EVNetworkController
                     /          |           \
            Charging Bays   Event Listeners   Grid Capacity
                  |                |
     UltraFastBay / FastDCBay   ConsoleDashboardListener
                  |
      ElectricVehicle + TariffStrategy
```

UML class diagrams (PlantUML):

- [Skeletal diagram](voltstream-skeletal.puml): high-level relationships between components
- [Detailed diagram](voltstream-detailed.puml): attributes, operations, multiplicities and dependencies

## Error Handling

| Exception | Type | Raised when |
|-----------|------|-------------|
| `InvalidVehicleConfigurationException` | Checked | Missing vehicle type, empty plate, or charge outside 0–99% |
| `NoAvailableBayException` | Checked | Every charging bay is occupied |
| `BayDockingException` | Checked | A null or fully charged vehicle is docked, or the bay is occupied |
| `InvalidGridConfigurationException` | Unchecked | Grid capacity is not positive, or a time step is negative |

Checked exceptions cover problems caused by user input or runtime conditions that callers can recover from. Unchecked exceptions cover programming and configuration errors.

## Project Structure

```
VoltStream
├── LICENSE
├── README.md
├── voltstream-detailed.puml
├── voltstream-skeletal.puml
└── src/grid
    ├── Main.java
    ├── charging      ChargingBay, ChargingInterface, BayType, UltraFastBay, FastDCBay, ChargingBayFactory
    ├── core          EVNetworkController
    ├── events        ChargingEventListener, ConsoleDashboardListener
    ├── exceptions    custom checked and unchecked exceptions
    ├── tariff        TariffStrategy, FlatTariffStrategy, PeakSurchargeTariffStrategy
    ├── util          VisualUtility
    └── vehicles      ElectricVehicle, VehicleType, VehicleFactory
```

## Design Patterns and OOP Concepts

- **Strategy**: `TariffStrategy` implementations decide pricing. `FlatTariffStrategy` is not wired to a bay yet; it shows how a new pricing rule can be added without touching the bays or the controller (Open/Closed principle)
- **Observer**: `ChargingEventListener` receives controller events
- **Factory**: `VehicleFactory` and `ChargingBayFactory` centralize creation and validation
- **Enums**: `VehicleType` (with per-type battery capacity and charge rate) and `BayType` replace string identifiers
- Abstraction, encapsulation, inheritance, polymorphism and interfaces
- Composition (bays own a tariff strategy; the controller owns bays)
- Checked and unchecked custom exceptions
- Modular package design

## License

Released under the [MIT License](LICENSE).
