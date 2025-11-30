# TerraBot

TerraBot este un sistem de simulare a mediului inconjurator care modeleaza explorarea unui teritoriu de catre un robot. Proiectul implementeaza o arhitectura orientata pe obiecte pentru gestionarea entitatilor de mediu (aer, sol, apa, plante, animale) si procesarea comenzilor robotului.

## Features

- Modelare OOP completa cu ierarhii de mostenire pentru entitati de mediu
- Implementare a Factory Pattern pentru crearea instantelor de tipuri specifice
- Utilizarea polimorfismului
- Simulare a interactiunilor intre robot si mediul inconjurator
- Gestionare a comenzilor robotului (miscare, scanare, invatare, imbunatatire)
- Calculare dinamica a calitatii aerului si solului pe baza tipului specific
- Serializare JSON pentru input/output

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

- **fileio**: Clase responsabile pentru incarcarea si procesarea datelor JSON de intrare.

- **main**: Punctul de intrare al aplicatiei. Clasa Main gestioneaza fluxul principal de executie.

- **model.entities**: Pachetul principal pentru entitatile de mediu. Contine interfata EnvironmentEntity si implementarile concrete, organizate in subpachete pentru aer si sol.

- **model.entities.air**: Ierarhie OOP pentru tipurile de aer. Contine clasa abstracta Air, subclasele concrete si AirFactory.

- **model.entities.soil**: Ierarhie OOP pentru tipurile de sol. Contine clasa abstracta Soil, subclasele concrete si SoilFactory.

- **model.environment**: Gestioneaza structura teritoriului ca o grila bidimensionala de sectiuni.

- **model.position**: Reprezentare a pozitiei in teritoriu.

- **model.robot**: Componentele robotului TerraBot, inclusiv baza de cunostinte si logica de miscare.

- **simulation**: Logica de simulare care orchestreaza interactiunile intre robot si mediu.

## Architecture & Design

### Main Classes

- **TerraBot**: Robotul principal care exploreaza teritoriul. Gestioneaza pozitia, energia si baza de cunostinte.

- **Simulation**: Clasa centrala care orchestreaza simularea. Proceseaza comenzile, actualizeaza entitatile si calculeaza interactiunile.

- **Territory**: Reprezinta teritoriul ca o grila bidimensionala de sectiuni, fiecare continand entitati de mediu.

- **Section**: O celula individuala din teritoriu care poate contine sol, aer, apa, plante si animale.

### Role of Abstract Class Air

Clasa abstracta `Air` serveste ca baza pentru toate tipurile de aer din sistem. Ea defineste:

- **Campuri comune**: Toate tipurile de aer au proprietati comune precum `type`, `name`, `mass`, `humidity`, `temperature`, `oxygenLevel`, etc.

- **Metode abstracte**: Defineste contractul pentru metodele care trebuie implementate de subclase:
    - `calculateQualityInternal()`: Calculul specific al calitatii aerului
    - `getMaxScore()`: Scorul maxim pentru tipul respectiv de aer
    - `addTypeSpecificFields()`: Adaugarea campurilor specifice in JSON

- **Metode concrete**: Ofera implementari comune pentru:
    - `calculateQuality()`: Orchestreaza calculul si aplica rotunjire
    - `airQualityMessage()`: Traduce calitatea numerica in mesaje
    - `toxicityAQ()`: Calculeaza toxicitatea
    - `changeWeather()`: Gestioneaza schimbarile de vreme

- **Constante protejate**: Defineste constantele comune folosite de toate subclasele.

### Role of Subclasses (TropicalAir, PolarAir, etc.)

Fiecare subclasa incapsuleaza logica specifica si elimina necesitatea switch-urilor in cod.

### Role of AirFactory

`AirFactory` implementeaza Factory Pattern si are urmatoarele responsabilitati:

- **Creare instante**: Metoda `createAir(String type)` creeaza instanta corecta de Air pe baza string-ului de tip.

- **Initializare din input**: Metoda `createAirFromInput(AirInput)` creeaza si initializeaza complet o instanta de Air din datele de input.

- **Centralizare logica de creare**: Toata logica de creare este concentrata intr-un singur loc, facilitand mentenanta si extensibilitatea.

- **Validare**: Valideaza tipul primit si arunca exceptii clare pentru tipuri necunoscute.

Factory-ul elimina dependenta codului de clasele concrete si permite adaugarea de noi tipuri fara modificarea codului existent.

### Design Patterns Used

#### Factory Pattern
- **AirFactory** si **SoilFactory**: Centralizeaza crearea obiectelor si elimina dependentele directe de clasele concrete. Permite adaugarea de noi tipuri fara modificarea codului.

#### Template Method Pattern
- Clasa abstracta `Air` defineste scheletul algoritmului in `calculateQuality()`, iar subclasele implementeaza pasii specifici in `calculateQualityInternal()`. Acelasi pattern se aplica si pentru `Soil`.

#### Polymorphism
- Eliminarea switch-urilor prin utilizarea polimorfismului. Codul lucreaza cu tipul abstract `Air`, iar comportamentul specific este determinat la runtime.
