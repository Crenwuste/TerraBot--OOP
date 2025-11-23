package simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Getter;
import model.entities.Water;
import model.entities.Air;
import model.entities.Animal;
import model.entities.Plant;
import model.entities.Soil;
import model.environment.Section;
import model.environment.Territory;
import model.robot.Direction;
import model.robot.TerraBot;

/**
 * Represents a single simulation run for TerraBot
 */
@Getter
public class Simulation {

    /**
     * The territory on which the simulation takes place
     */
    private final Territory territory;

    /**
     * The TerraBot instance controlled during the simulation
     */
    private final TerraBot terraBot;

    /**
     * Flag indicating whether the simulation is currently running
     */
    private boolean running;

    /**
     * Indicating the timestamp at witch the charging ends
     */
    private int charging = 0;

    private int changeWeather = 0;

    /**
     * Creates a new simulation for the given territory and robot
     *
     * @param territory the simulated territory
     * @param terraBot  the robot exploring the territory
     */
    public Simulation(final Territory territory, final TerraBot terraBot) {
        this.territory = territory;
        this.terraBot = terraBot;
    }

    /**
     * Executes a single command within this simulation
     *
     * @param command the command to execute
     * @param output  the output array where the result node should be added
     * @param mapper  the Jackson mapper used to create JSON nodes
     */
    public void executeCommand(final CommandInput command,
                               final ArrayNode output,
                               final ObjectMapper mapper) {
        Section[][] section = territory.getSections();

        updateActiveEntities(section, command.getTimestamp());

        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getWidth(); j++) {
                Soil soil = section[i][j].getSoil();
                Air air = section[i][j].getAir();
                Animal animal = section[i][j].getAnimal();
                Plant plant = section[i][j].getPlant();
                Water water = section[i][j].getWater();

                soil.calculateQuality();
                if (changeWeather <= command.getTimestamp()) {
                    air.calculateQuality();
                }
            }
        }


        String name = command.getCommand();
        if (charging > command.getTimestamp()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("command", command.getCommand());
            node.put("message", "ERROR: Robot still charging. Cannot perform action");
            node.put("timestamp", command.getTimestamp());

            output.add(node);
            return;
        }

        switch (name) {
            case "startSimulation" ->
                    startSimulation(command, output, mapper);
            case "endSimulation" ->
                    endSimulation(command, output, mapper);
            case "printEnvConditions"  ->
                    printEnvConditions(command, output, mapper);
            case "printMap"  ->
                    printMap(command, output, mapper);
            case "moveRobot" ->
                    moveRobot(command, output, mapper);
            case "scanObject"  ->
                    scanObject(command, output, mapper);
            case "learnFact"  ->
                    learnFact(command, output, mapper);
            case "improveEnvironment"   ->
                    improveEnvironment(command, output, mapper);
            case "changeWeatherConditions"   ->
                    changeWeatherConditions(command, output, mapper);
            case "rechargeBattery"   ->
                    rechargeBattery(command, output, mapper);
            case "getEnergyStatus" ->
                    getEnergyStatus(command, output, mapper);
            case "printKnowledgeBase"   ->
                    printKnowledgeBase(command, output, mapper);
            default ->  throw new IllegalArgumentException("Invalid command");
        }
    }

    /**
     * Handles the start of the simulation.
     */
    public void startSimulation(final CommandInput command,
                                final ArrayNode output,
                                final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("message", "Simulation has started.");
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    /**
     * Handles the end of the simulation.
     */
    public void endSimulation(final CommandInput command,
                              final ArrayNode output,
                              final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("message", "Simulation has ended.");
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    /**
     * Prints environmental conditions at the robot's current position.
     */
    public void printEnvConditions(final CommandInput command,
                                   final ArrayNode output,
                                   final ObjectMapper mapper) {
        Section[][] section = territory.getSections();
        int x = terraBot.getPosition().getX();
        int y = terraBot.getPosition().getY();
        Section currentSection = section[x][y];

        ObjectNode entities = mapper.createObjectNode();

        entities.set("soil", currentSection.getSoil().getEntities(mapper));
        if (currentSection.getPlant() != null) {
            entities.set("plants", currentSection.getPlant().getEntities(mapper));
        }
        if (currentSection.getAnimal() != null) {
            entities.set("animals", currentSection.getAnimal().getEntities(mapper));
        }
        if (currentSection.getWater() != null) {
            entities.set("water", currentSection.getWater().getEntities(mapper));
        }
        entities.set("air", currentSection.getAir().getEntities(mapper));

        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.set("output", entities);
        node.put("timestamp", command.getTimestamp());

        output.add(node);

    }

    /**
     * Prints the map overview (objects and quality per section).
     */
    public void printMap(final CommandInput command,
                         final ArrayNode output,
                         final ObjectMapper mapper) {
        ArrayNode outputArray = mapper.createArrayNode();

        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getWidth(); j++) {
                int objCount = 0;
                ObjectNode sectionNode = mapper.createObjectNode();

                ArrayNode sectionCoords = mapper.createArrayNode();
                sectionCoords.add(j);
                sectionCoords.add(i);
                sectionNode.set("section", sectionCoords);

                Section[][] section = territory.getSections();
                Section currentSection = section[j][i];

                if (currentSection.getPlant() != null) {
                    objCount++;
                }
                if (currentSection.getAnimal() != null) {
                    objCount++;
                }
                if (currentSection.getWater() != null) {
                    objCount++;
                }

                Soil soil = currentSection.getSoil();
                Air air = currentSection.getAir();

                sectionNode.put("totalNrOfObjects", objCount);
                sectionNode.put("airQuality", air.airQualityMessage());
                sectionNode.put("soilQuality", soil.soilQualityMessage());

                outputArray.add(sectionNode);
            }
        }

        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.set("output", outputArray);
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    /**
     * Moves the robot on the territory according to the command.
     */
    public void moveRobot(final CommandInput command,
                          final ArrayNode output,
                          final ObjectMapper mapper) {
        int x = terraBot.getPosition().getX();
        int y = terraBot.getPosition().getY();

        // Calculate costs for all directions
        Direction bestDirection = null;
        int minCost = Integer.MAX_VALUE;

        for (Direction dir : Direction.values()) {
            int newX = dir.getNewX(x);
            int newY = dir.getNewY(y);
            int cost = moveRobotHelper(newX, newY);

            if (cost < minCost) {
                minCost = cost;
                bestDirection = dir;
            }
        }

        String msg;
        if (minCost <= terraBot.getEnergyPoints()) {
            int xNew = bestDirection.getNewX(x);
            int yNew = bestDirection.getNewY(y);
            terraBot.getPosition().setX(xNew);
            terraBot.getPosition().setY(yNew);
            terraBot.setEnergyPoints(terraBot.getEnergyPoints() - minCost);
            msg = "The robot has successfully moved to position (" + xNew + ", " + yNew + ").";
        } else {
            msg = "ERROR: Not enough battery left. Cannot perform action";
        }

        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("message", msg);
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    private int moveRobotHelper(final int x, final int y) {
        if (x < 0 || y < 0 || x >= territory.getWidth() || y >= territory.getHeight()) {
            return Integer.MAX_VALUE;
        }

        Section[][] section = territory.getSections();
        Section currentSection = section[x][y];

        double soil = currentSection.getSoil().giveRobotDamage();
        double air = currentSection.getAir().giveRobotDamage();

        int count = 2;

        double animal = 0;
        if (currentSection.getAnimal() != null) {
            animal = currentSection.getAnimal().giveRobotDamage();
            count++;
        }
        double plant = 0;
        if (currentSection.getPlant() != null) {
            plant = currentSection.getPlant().giveRobotDamage();
            count++;
        }

        double sum = soil + air + animal + plant;
        double mean = Math.abs(sum / count);
        return (int) Math.round(mean);
    }

    /**
     * Scans the object at the robot's current position.
     */
    public void scanObject(final CommandInput command,
                           final ArrayNode output,
                           final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        String msg = "";

        if (7 > terraBot.getEnergyPoints()) {
            node.put("command", command.getCommand());
            node.put("message", "ERROR: Not enough battery left. Cannot perform action");
            node.put("timestamp", command.getTimestamp());
            output.add(node);
            return;
        }

        int x = terraBot.getPosition().getX();
        int y = terraBot.getPosition().getY();
        Section[][] sections = territory.getSections();
        Section currentSection = sections[x][y];
        boolean success = true;

        // Check what object is at current position and activate it
        if (currentSection.getWater() != null && command.getSound().equals("none") && command.getColor().equals("none") && command.getSmell().equals("none")) {
            // Scan water - activate it
            Water water = currentSection.getWater();
            water.setActive(true);
            water.setLastIterTimestamp(command.getTimestamp());
            msg = "The scanned object is water.";
        } else if (currentSection.getPlant() != null && command.getSound().equals("none") &&  !command.getColor().equals("none")) {
            // Scan plant - activate it
            Plant plant = currentSection.getPlant();
            plant.setActive(true);
            msg = "The scanned object is a plant.";
        } else if (currentSection.getAnimal() != null) {
            // Scan animal - activate it
            Animal animal = currentSection.getAnimal();
            animal.setActive(true);
            animal.setLastMoveTimestamp(command.getTimestamp());
            msg = "The scanned object is an animal.";
        } else {
            msg = "ERROR: Object not found. Cannot perform action";
            success = false;
        }

        if (success) {
            terraBot.setEnergyPoints(terraBot.getEnergyPoints() - 7);
        }

        node.put("command", command.getCommand());
        node.put("message", msg);
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    /**
     * Updates all active entities at each iteration.
     *
     * @param sections the territory sections
     * @param currentTimestamp the current timestamp (iteration number)
     */
    private void updateActiveEntities(final Section[][] sections, final int currentTimestamp) {
        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getWidth(); j++) {
                Section currentSection = sections[i][j];

                Air air = currentSection.getAir();
                Soil soil = currentSection.getSoil();
                Plant plant = currentSection.getPlant();

                // Update active water
                Water water = currentSection.getWater();
                if (water != null && water.isActive()) {
                    // Update waterRetention and humidity after 2 iterations
                    while (currentTimestamp - water.getLastIterTimestamp() >= 2) {
                        air.setHumidity(air.getHumidity() + 0.1);
                        soil.setWaterRetention(soil.getWaterRetention() + 0.1);
                        water.setLastIterTimestamp(water.getLastIterTimestamp() + 2);
                    }
                    if (plant != null && plant.isActive()) {
                        plant.increaseGrowth();
                        if (plant.getAgeSurplus() == 0) {
                            currentSection.setPlant(null);
                        }
                    }
                }

                // Update active plants
                if (plant != null && plant.isActive()) {
                    plant.increaseGrowth();
                    if (plant.getAgeSurplus() == 0) {
                        currentSection.setPlant(null);
                    }

                    // Update air
                    double oxygenProduced = plant.oxygenProduced();
                    air.setOxygenLevel(air.getOxygenLevel() + oxygenProduced);
                }

                // Update active animals
                Animal animal = currentSection.getAnimal();
                if (animal != null && animal.isActive()) {
                    // Animal moves every 2 iterations (timestamps)
                    // Check if at least 2 timestamps have passed since last move
                    while (currentTimestamp - animal.getLastMoveTimestamp() >= 2) {
                        moveAnimal(animal, i, j, sections);
                        animal.setLastMoveTimestamp(animal.getLastMoveTimestamp() + 2);
                    }
                }

            }
        }
    }

    /**
     * Moves an animal to a neighboring section based on the feeding algorithm.
     *
     * @param animal the animal to move
     * @param currentX current x coordinate
     * @param currentY current y coordinate
     * @param sections the territory sections
     */
    private void moveAnimal(final Animal animal, final int currentX, final int currentY,
                            final Section[][] sections) {
        // Priority 1: Section with both plant AND water
        Section bestSectionWithBoth = null;
        double bestWaterQuality = -1;

        // Priority 2: Sections with plant OR water
        Section firstSectionWithPlant = null;

        Section bestSectionWithWater = null;
        double bestWaterQualityAlone = -1;

        // Priority 3: First available section
        Section firstAvailableSection = null;

        for (Direction dir : Direction.values()) {
            int newX = dir.getNewX(currentX);
            int newY = dir.getNewY(currentY);

            // Check bounds
            if (newX < 0 || newY < 0 || newX >= territory.getWidth()
                    || newY >= territory.getHeight()) {
                continue;
            }

            Section neighborSection = sections[newY][newX];

            Plant neighborPlant = neighborSection.getPlant();
            Water neighborWater = neighborSection.getWater();
            boolean hasPlant = neighborPlant != null && neighborPlant.isActive();
            boolean hasWater = neighborWater != null && neighborWater.isActive();

            // Priority 1: Both plant and water
            if (hasPlant && hasWater) {
                double waterQuality = neighborWater.waterQuality();
                if (waterQuality > bestWaterQuality) {
                    bestWaterQuality = waterQuality;
                    bestSectionWithBoth = neighborSection;
                }
            }

            // Priority 2: Plant or water
            if (hasPlant && firstSectionWithPlant == null) {
                firstSectionWithPlant = neighborSection;
            }

            if (hasWater) {
                double waterQuality = neighborWater.waterQuality();
                if (waterQuality > bestWaterQualityAlone) {
                    bestWaterQualityAlone = waterQuality;
                    bestSectionWithWater = neighborSection;
                }
            }

            // Priority 3: First available
            if (firstAvailableSection == null) {
                firstAvailableSection = neighborSection;
            }
        }

        // Choose destination based on priority
        Section targetSection = firstAvailableSection;

        if (bestSectionWithBoth != null) {
            // Priority 1: Section with both plant and water
            targetSection = bestSectionWithBoth;
            sectionWithPlant(targetSection, animal);
            sectionWithWater(targetSection, animal);
        } else if (firstSectionWithPlant != null) {
            // Priority 2a: First section with plant
            targetSection = firstSectionWithPlant;
            sectionWithPlant(targetSection, animal);
        } else if (bestSectionWithWater != null) {
            // Priority 2b: Best section with water
            targetSection = bestSectionWithWater;
            sectionWithWater(targetSection, animal);
        }

        if (animal.getType().equals("Carnivore") || animal.getType().equals("Parasite")) {
            if (targetSection.getAnimal() != null) {
                animal.setMass(animal.getMass() + targetSection.getAnimal().getMass());
                targetSection.setAnimal(null);
            }
        }

        // Move animal to new section
        sections[currentY][currentX].setAnimal(null);
        targetSection.setAnimal(animal);
    }

    private void sectionWithPlant(final Section sectionWithPlant, final Animal animal) {
        Plant plant = sectionWithPlant.getPlant();

        animal.setMass(animal.getMass() + plant.getMass());
        sectionWithPlant.setPlant(null);
    }

    private void sectionWithWater(final Section sectionWithWater, final Animal animal) {
        Water water = sectionWithWater.getWater();

        double intakeRate = 0.08;
        double waterToDrink = Math.min(animal.getMass() * intakeRate, water.getMass());

        if (waterToDrink > water.getMass()) {
            waterToDrink = water.getMass();
        }
        animal.setMass(animal.getMass() + waterToDrink);
        water.setMass(water.getMass() - waterToDrink);
        if (water.getMass() == 0) {
            sectionWithWater.setWater(null);
        }
    }

    /**
     * Saves a fact into the robot's knowledge base.
     */
    public void learnFact(final CommandInput command,
                          final ArrayNode output,
                          final ObjectMapper mapper) {
    }

    /**
     * Applies an improvement to the environment.
     */
    public void improveEnvironment(final CommandInput command,
                                   final ArrayNode output,
                                   final ObjectMapper mapper) {
    }

    /**
     * Changes the weather conditions in the environment.
     */
    public void changeWeatherConditions(final CommandInput command,
                                        final ArrayNode output,
                                        final ObjectMapper mapper) {
        String msg = "";
        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getWidth(); j++) {
                Air air = territory.getSections()[i][j].getAir();
                if (changeWeatherHelper(command, air)) {
                    if (air.changeWeather(command)) {
                        msg = "The weather has changed.";
                        changeWeather = command.getTimestamp() + 2;
                    } else {
                        msg = "ERROR: The weather change does not affect the environment. Cannot perform action";
                        break;
                    }
                }
            }
        }
        if (msg.isEmpty()) {
            msg = "ERROR: The weather change does not affect the environment. Cannot perform action";
        }

        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("message", msg);
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    private boolean changeWeatherHelper(final CommandInput command, final Air air) {
        String event = command.getType();
        String expectedAir = switch (event) {
            case "rainfall" -> "TropicalAir";
            case "windfall" -> "PolarAir";
            case "newSeason" -> "TemperateAir";
            case "desertStorm" -> "DesertAir";
            case "peopleHiking" -> "MountainAir";
            default -> "Unknown";
        };

        return air.getType().equals(expectedAir);
    }

    /**
     * Recharges the robot's battery.
     */
    public void rechargeBattery(final CommandInput command,
                                final ArrayNode output,
                                final ObjectMapper mapper) {
        charging = command.getTimestamp() + command.getTimeToCharge();
        terraBot.setEnergyPoints(terraBot.getEnergyPoints() + command.getTimeToCharge());

        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("message", "Robot battery is charging.");
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    /**
     * Reports the current energy status of the robot.
     */
    public void getEnergyStatus(final CommandInput command,
                                final ArrayNode output,
                                final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("message", "TerraBot has " + terraBot.getEnergyPoints() + " energy points left.");
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

    /**
     * Prints the robot's knowledge base.
     */
    public void printKnowledgeBase(final CommandInput command,
                                   final ArrayNode output,
                                   final ObjectMapper mapper) {
    }

}
