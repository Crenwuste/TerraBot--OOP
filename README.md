# TerraBot

TerraBot is an environmental simulation system that models the exploration of a territory by a robot. The project implements an object-oriented architecture for managing environment entities (air, soil, water, plants, animals) and processing robot commands.

## Features

- Complete OOP modeling with inheritance hierarchies for environment entities
- Factory Pattern implementation for creating instances of specific types
- Use of polymorphism
- Simulation of interactions between robot and the surrounding environment
- Management of robot commands (movement, scanning, learning, improvement)
- Dynamic calculation of air and soil quality based on specific type
- JSON serialization for input/output

## Project Structure
```
src/main/java/
├── fileio/                    # Input/Output handling
│   ├── AirInput.java
│   ├── AnimalInput.java
│   ├── CommandInput.java
│   ├── InputLoader.java
│   ├── PlantInput.java
│   ├── SimulationInput.java
│   ├── SoilInput.java
│   ├── WaterInput.java
│   └── ...
├── main/
│   └── Main.java              # Entry point
├── model/
│   ├── entities/              # Environment entities
│   │   ├── air/              # Air type hierarchy
│   │   │   ├── Air.java      # Abstract base class
│   │   │   ├── AirFactory.java
│   │   │   ├── TropicalAir.java
│   │   │   ├── PolarAir.java
│   │   │   ├── TemperateAir.java
│   │   │   ├── DesertAir.java
│   │   │   └── MountainAir.java
│   │   ├── soil/             # Soil type hierarchy
│   │   │   ├── Soil.java     # Abstract base class
│   │   │   ├── SoilFactory.java
│   │   │   ├── ForestSoil.java
│   │   │   ├── SwampSoil.java
│   │   │   ├── DesertSoil.java
│   │   │   ├── GrasslandSoil.java
│   │   │   └── TundraSoil.java
│   │   ├── Animal.java
│   │   ├── Plant.java
│   │   ├── Water.java
│   │   └── EnvironmentEntity.java  # Interface
│   ├── environment/          # Territory management
│   │   ├── Territory.java
│   │   └── Section.java
│   ├── position/             # Position representation
│   │   └── Position.java
│   └── robot/               # Robot components
│       ├── TerraBot.java
│       ├── KnowledgeBase.java
│       └── Direction.java
└── simulation/               # Simulation logic
    ├── Simulation.java
    ├── SimulationFactory.java
    └── SimulationManager.java
```

### Package Descriptions

- **fileio**: Classes responsible for loading and processing JSON input data.

- **main**: The application entry point. The Main class manages the main execution flow.

- **model.entities**: The main package for environment entities. Contains the EnvironmentEntity interface and concrete implementations, organized in subpackages for air and soil.

- **model.entities.air**: OOP hierarchy for air types. Contains the abstract Air class, concrete subclasses and AirFactory.

- **model.entities.soil**: OOP hierarchy for soil types. Contains the abstract Soil class, concrete subclasses and SoilFactory.

- **model.environment**: Manages the territory structure as a two-dimensional grid of sections.

- **model.position**: Position representation in the territory.

- **model.robot**: TerraBot robot components, including the knowledge base that manages learned facts and movement logic.

- **simulation**: Simulation logic that orchestrates interactions between robot and environment.

## Architecture & Design

### Main Classes

- **TerraBot**: The main robot that explores the territory. Manages position, energy and the knowledge base. Provides methods for accessing the current section.

- **Simulation**: The central class that orchestrates the simulation. Processes commands, updates entities and calculates interactions.

- **Territory**: Represents the territory as a two-dimensional grid of sections, each containing environment entities.

- **Section**: An individual cell in the territory that can contain soil, air, water, plants and animals. Provides methods for calculating movement cost and managing interactions with animals.

### Role of Abstract Class Air

The abstract class `Air` serves as the base for all air types in the system. It defines:

- **Common fields**: All air types have common properties such as `type`, `name`, `mass`, `humidity`, `temperature`, `oxygenLevel`, etc.

- **Abstract methods**: Defines the contract for methods that must be implemented by subclasses:
    - `calculateQualityInternal()`: Specific calculation of air quality
    - `getMaxScore()`: Maximum score for the respective air type
    - `addTypeSpecificFields()`: Adding specific fields to JSON
    - `applyWeatherChange(CommandInput cmd)`: Applying weather changes to air quality

- **Concrete methods**: Provides common implementations for:
    - `calculateQuality()`: Orchestrates the calculation and applies rounding
    - `airQualityMessage()`: Translates numerical quality into messages
    - `toxicityAQ()`: Calculates toxicity
    - `changeWeather()`: Manages weather changes

- **Protected constants**: Defines common constants used by all subclasses.

### Role of Subclasses (TropicalAir, PolarAir, etc.)

Each subclass encapsulates specific logic and eliminates the need for switches in code.

### Role of AirFactory

`AirFactory` implements the Factory Pattern and has the following responsibilities:

- **Instance creation**: The `createAir(String type)` method creates the correct Air instance based on the type string.

- **Input initialization**: The `createAirFromInput(AirInput)` method creates and fully initializes an Air instance from input data.

- **Creation logic centralization**: All creation logic is concentrated in one place, facilitating maintenance and extensibility.

- **Validation**: Validates the received type and throws clear exceptions for unknown types.

The factory eliminates code dependency on concrete classes and allows adding new types without modifying existing code.

### Design Patterns Used

#### Factory Pattern
- **AirFactory** and **SoilFactory**: Centralize object creation and eliminate direct dependencies on concrete classes. Allows adding new types without modifying code.

#### Template Method Pattern
- The abstract class `Air` defines the algorithm skeleton in `calculateQuality()`, and subclasses implement specific steps in `calculateQualityInternal()`. The same pattern applies to `Soil`.

#### Polymorphism
- Eliminating switches through the use of polymorphism. Code works with the abstract `Air` type, and specific behavior is determined at runtime.
