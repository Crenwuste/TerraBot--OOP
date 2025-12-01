package simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;
import lombok.Getter;
import model.entities.Water;
import model.entities.air.Air;
import model.entities.Animal;
import model.entities.Plant;
import model.entities.soil.Soil;
import model.environment.Section;
import model.environment.Territory;
import model.position.Position;
import model.robot.Direction;
import model.robot.KnowledgeBase;
import model.robot.TerraBot;

import java.util.ArrayList;

/**
 * Represents a single simulation run for TerraBot
 */
@Getter
@Data
public class Simulation {

    private static final int SCAN_ENERGY_COST = 7;
    private static final int LEARN_FACT_ENERGY_COST = 2;
    private static final int IMPROVEMENT_ENERGY_COST = 10;
    private static final double WATER_HUMIDITY_INCREMENT = 0.1;
    private static final double SOIL_WATER_RETENTION_INCREMENT = 0.1;
    private static final double ORGANIC_MATTER_BOTH_INCREMENT = 0.8;
    private static final double ORGANIC_MATTER_SINGLE_INCREMENT = 0.5;
    private static final double WATER_INTAKE_RATE = 0.08;
    private static final double PLANT_VEGETATION_OXYGEN_INCREMENT = 0.3;
    private static final double FERTILIZE_SOIL_INCREMENT = 0.3;
    private static final double HUMIDITY_INCREASE_INCREMENT = 0.2;
    private static final double MOISTURE_INCREASE_INCREMENT = 0.2;
    private static final int WEATHER_COOLDOWN_INTERVAL = 2;

    /**
     * The territory on which the simulation takes place
     */
    private final Territory territory;

    /**
     * The TerraBot instance controlled during the simulation
     */
    private final TerraBot terraBot;

    /**
     * Indicating the timestamp at witch the charging ends
     */
    private int charging = 0;

    /**
     * Timestamp until which weather changes are locked
     */
    private int changeWeather = 0;

    /**
     * Last timestamp for which entities were updated
     */
    private int lastUpdatedTimestamp = 0;

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

        // Update entities for all timestamps between last update and current command
        for (int timestamp = lastUpdatedTimestamp + 1; timestamp <= command.getTimestamp();) {
            updateActiveEntities(section, timestamp);
            timestamp++;
        }
        lastUpdatedTimestamp = command.getTimestamp();

        Section currentSection = terraBot.getCurrentSection(territory);
        Soil soil = currentSection.getSoil();
        Air air = currentSection.getAir();

        soil.calculateQuality();
        if (changeWeather <= command.getTimestamp()) {
            air.calculateQuality();
            air.setDesertStorm(false);
        }


        String name = command.getCommand();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("timestamp", command.getTimestamp());

        if (charging > command.getTimestamp()) {
            node.put("message", "ERROR: Robot still charging. Cannot perform action");
            output.add(node);
            return;
        }

        switch (name) {
            case "startSimulation" ->
                    startSimulation(output, node);
            case "endSimulation" ->
                    endSimulation(output, node);
            case "printEnvConditions" ->
                    printEnvConditions(output, mapper, node);
            case "printMap" ->
                    printMap(output, mapper, node);
            case "moveRobot" ->
                    moveRobot(output, node);
            case "scanObject" ->
                    scanObject(command, output, node);
            case "learnFact" ->
                    learnFact(command, output, node);
            case "improveEnvironment" ->
                    improveEnvironment(command, output, node);
            case "changeWeatherConditions" ->
                    changeWeatherConditions(command, output, node);
            case "rechargeBattery" ->
                    rechargeBattery(command, output, node);
            case "getEnergyStatus" ->
                    getEnergyStatus(output, node);
            case "printKnowledgeBase" ->
                    printKnowledgeBase(output, mapper, node);
            default ->  throw new IllegalArgumentException("Invalid command");
        }
    }

    /**
     * Handles the start of the simulation.
     */
    public void startSimulation(final ArrayNode output,
                                final ObjectNode node) {
        node.put("message", "Simulation has started.");
        output.add(node);
    }

    /**
     * Handles the end of the simulation.
     */
    public void endSimulation(final ArrayNode output,
                              final ObjectNode node) {
        node.put("message", "Simulation has ended.");
        output.add(node);
    }

    /**
     * Prints environmental conditions at the robot's current position.
     */
    public void printEnvConditions(final ArrayNode output,
                                   final ObjectMapper mapper,
                                   final ObjectNode node) {
        Section currentSection = terraBot.getCurrentSection(territory);

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


        node.set("output", entities);
        output.add(node);
    }

    /**
     * Prints the map overview (objects and quality per section).
     */
    public void printMap(final ArrayNode output,
                         final ObjectMapper mapper,
                         final ObjectNode node) {
        ArrayNode outputArray = mapper.createArrayNode();

        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getWidth(); j++) {
                int objCount = 0;
                ObjectNode sectionNode = mapper.createObjectNode();

                ArrayNode sectionCoords = mapper.createArrayNode();
                sectionCoords.add(j);
                sectionCoords.add(i);
                sectionNode.set("section", sectionCoords);

                Section currentSection = territory.getSections()[j][i];

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

        node.set("output", outputArray);
        output.add(node);
    }

    /**
     * Moves the robot on the territory according to the command.
     */
    public void moveRobot(final ArrayNode output,
                          final ObjectNode node) {
        Position currentPos = terraBot.getPosition();
        int x = currentPos.getX();
        int y = currentPos.getY();

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
            int newX = bestDirection.getNewX(x);
            int newY = bestDirection.getNewY(y);
            currentPos.setX(newX);
            currentPos.setY(newY);
            terraBot.setEnergyPoints(terraBot.getEnergyPoints() - minCost);
            msg = "The robot has successfully moved to position (" + newX + ", " + newY + ").";
        } else {
            msg = "ERROR: Not enough battery left. Cannot perform action";
        }

        node.put("message", msg);
        output.add(node);
    }

    private int moveRobotHelper(final int x, final int y) {
        if (x < 0 || y < 0 || x >= territory.getWidth() || y >= territory.getHeight()) {
            return Integer.MAX_VALUE;
        }

        Section[][] section = territory.getSections();
        Section currentSection = section[x][y];

        return currentSection.movementCost();
    }

    /**
     * Scans the object at the robot's current position.
     */
    public void scanObject(final CommandInput command,
                           final ArrayNode output,
                           final ObjectNode node) {
        if (SCAN_ENERGY_COST > terraBot.getEnergyPoints()) {
            node.put("message", "ERROR: Not enough energy to perform action");
            output.add(node);

            return;
        }

        Section currentSection = terraBot.getCurrentSection(territory);

        // Check what object is at current position and activate it
        if (currentSection.getWater() != null && command.getSound().equals("none")
                && command.getColor().equals("none") && command.getSmell().equals("none")) {
            // Scan water - activate it
            Water water = currentSection.getWater();
            water.setActive(true);
            water.setLastIterTimestamp(command.getTimestamp());
            terraBot.getScannedObjects().add(water.getName());
            node.put("message", "The scanned object is water.");
        } else if (currentSection.getPlant() != null && command.getSound().equals("none")
                && !command.getColor().equals("none")) {
            // Scan plant - activate it
            Plant plant = currentSection.getPlant();
            plant.setActive(true);
            plant.setLastIteration(command.getTimestamp());
            terraBot.getScannedObjects().add(plant.getName());
            node.put("message", "The scanned object is a plant.");
        } else if (currentSection.getAnimal() != null && !command.getSound().equals("none")) {
            // Scan animal - activate it
            Animal animal = currentSection.getAnimal();
            animal.setActive(true);
            animal.setLastMoveTimestamp(command.getTimestamp());
            terraBot.getScannedObjects().add(animal.getName());
            node.put("message", "The scanned object is an animal.");
        } else {
            node.put("message", "ERROR: Object not found. Cannot perform action");
            output.add(node);

            return;
        }

        terraBot.setEnergyPoints(terraBot.getEnergyPoints() - SCAN_ENERGY_COST);

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
                Water water = currentSection.getWater();
                Animal animal = currentSection.getAnimal();

                boolean hasPlant = plant != null && plant.isActive();
                boolean hasWater = water != null && water.isActive();
                boolean hasAnimal = animal != null && animal.isActive();

                // Update active water
                if (hasWater) {
                    // Update waterRetention and humidity after 2 iterations
                    if (currentTimestamp - water.getLastIterTimestamp() >= 2) {
                        air.setHumidity(air.getHumidity() + WATER_HUMIDITY_INCREMENT);
                        soil.setWaterRetention(soil.getWaterRetention()
                                + SOIL_WATER_RETENTION_INCREMENT);
                        water.setLastIterTimestamp(water.getLastIterTimestamp() + 2);
                    }
                }

                // Update active plants
                if (hasPlant) {
                    if (currentTimestamp > plant.getLastIteration()) {
                        plant.increaseGrowth();
                        if (hasWater) {
                            plant.increaseGrowth();
                        }
                        if (plant.getAgeSurplus() == 0) {
                            currentSection.setPlant(null);
                        } else {
                            // Update air
                            double oxygenProduced = plant.oxygenProduced();
                            air.setOxygenLevel(air.getOxygenLevel() + oxygenProduced);
                        }
                        plant.setLastIteration(currentTimestamp);
                    }
                }

                // Update active animals
                if (hasAnimal) {
                    // Check if air is toxic
                    boolean isToxic = air.isToxicForAnimals();
                    animal.setSick(isToxic);

                    // Produce organic matter if animal was well-fed in previous timestamp
                    boolean wasWellFed = animal.isAtePlant() || animal.isDrankWater()
                            || animal.isAteAnimal();
                    if (wasWellFed && !animal.isSick()) {
                        // Calculate organic matter to add
                        boolean ateBoth = (animal.isAtePlant() && animal.isDrankWater())
                                || (animal.isDrankWater() && animal.isAteAnimal());
                        double organicMatterToAdd = ateBoth
                                ? ORGANIC_MATTER_BOTH_INCREMENT
                                : ORGANIC_MATTER_SINGLE_INCREMENT;
                        soil.setOrganicMatter(soil.getOrganicMatter() + organicMatterToAdd);
                    }

                    // Feed animal (this sets flags for next timestamp)
                    currentSection.feedAnimal(WATER_INTAKE_RATE);

                    // Animal moves every 2 iterations
                    // Check if at least 2 timestamps have passed since last move
                    if (currentTimestamp - animal.getLastMoveTimestamp() >= 2) {
                        Section targetSection = moveAnimal(animal, i, j, sections);
                        targetSection.feedAnimal(WATER_INTAKE_RATE);
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
     * @return the section where the animal moved
     */
    private Section moveAnimal(final Animal animal, final int currentX, final int currentY,
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

        boolean isCarnivoreOrParasite = animal.getType().equals("Carnivores")
                || animal.getType().equals("Parasites");

        for (Direction dir : Direction.values()) {
            int newX = dir.getNewX(currentX);
            int newY = dir.getNewY(currentY);

            // Check bounds
            if (newX < 0 || newY < 0 || newX >= territory.getWidth()
                    || newY >= territory.getHeight()) {
                continue;
            }

            Section neighborSection = sections[newX][newY];

            if (!isCarnivoreOrParasite && neighborSection.getAnimal() != null) {
                continue;
            }

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
        } else if (firstSectionWithPlant != null) {
            // Priority 2a: First section with plant
            targetSection = firstSectionWithPlant;
        } else if (bestSectionWithWater != null) {
            // Priority 2b: Best section with water
            targetSection = bestSectionWithWater;
        }

        if (isCarnivoreOrParasite) {
            if (targetSection.getAnimal() != null) {
                animal.setMass(animal.getMass() + targetSection.getAnimal().getMass());
                animal.setAteAnimal(true);

                targetSection.setAnimal(null);
            }
        }

        // Move animal to new section
        sections[currentX][currentY].setAnimal(null);
        targetSection.setAnimal(animal);
        return targetSection;
    }

    /**
     * Saves a fact into the robot's knowledge base.
     */
    public void learnFact(final CommandInput command,
                          final ArrayNode output,
                          final ObjectNode node) {
        if (LEARN_FACT_ENERGY_COST > terraBot.getEnergyPoints()) {
            node.put("message", "ERROR: Not enough battery left. Cannot perform action");
            output.add(node);

            return;
        }

        if (terraBot.getScannedObjects().contains(command.getComponents())) {
            terraBot.getKnowledgeBase().addFact(command.getComponents(), command.getSubject());
            node.put("message", "The fact has been successfully saved in the database.");
        } else {
            node.put("message", "ERROR: Subject not yet saved. Cannot perform action");
            output.add(node);

            return;
        }

        terraBot.setEnergyPoints(terraBot.getEnergyPoints() - LEARN_FACT_ENERGY_COST);

        output.add(node);
    }

    /**
     * Applies an improvement to the environment.
     */
    public void improveEnvironment(final CommandInput command,
                                   final ArrayNode output,
                                   final ObjectNode node) {
        String msg;

        if (IMPROVEMENT_ENERGY_COST > terraBot.getEnergyPoints()) {
            node.put("message", "ERROR: Not enough battery left. Cannot perform action");
            output.add(node);

            return;
        }

        String improvementType = command.getImprovementType();
        String componentName = command.getName();

        if (!terraBot.getScannedObjects().contains(componentName)) {
            node.put("message", "ERROR: Subject not yet saved. Cannot perform action");
            output.add(node);

            return;
        }

        if (!terraBot.getKnowledgeBase().hasImprovementFact(componentName)) {
            node.put("message", "ERROR: Fact not yet saved. Cannot perform action");
            output.add(node);

            return;
        }

        Section currentSection = terraBot.getCurrentSection(territory);
        Air air = currentSection.getAir();
        Soil soil = currentSection.getSoil();

        switch (improvementType) {
            case "plantVegetation" -> {
                air.setOxygenLevel(air.getOxygenLevel() + PLANT_VEGETATION_OXYGEN_INCREMENT);
                msg = "The " + componentName + " was planted successfully.";
            }
            case "fertilizeSoil" -> {
                soil.setOrganicMatter(soil.getOrganicMatter() + FERTILIZE_SOIL_INCREMENT);
                msg = "The soil was successfully fertilized using " + componentName;
            }
            case "increaseHumidity" -> {
                air.setHumidity(air.getHumidity() + HUMIDITY_INCREASE_INCREMENT);
                msg = "The humidity was successfully increased using " + componentName;
            }
            case "increaseMoisture" -> {
                soil.setWaterRetention(soil.getWaterRetention() + MOISTURE_INCREASE_INCREMENT);
                msg = "The moisture was successfully increased using " + componentName;
            }
            default -> {
                msg = "ERROR: Improvement not supported. Cannot perform action";
                node.put("message", msg);
                output.add(node);

                return;
            }
        }

        terraBot.setEnergyPoints(terraBot.getEnergyPoints() - IMPROVEMENT_ENERGY_COST);
        terraBot.getScannedObjects().remove(componentName);

        node.put("message", msg);
        output.add(node);
    }

    /**
     * Changes the weather conditions in the environment.
     */
    public void changeWeatherConditions(final CommandInput command,
                                        final ArrayNode output,
                                        final ObjectNode node) {
        Section[][] sections = territory.getSections();
        String msg = "";
        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getWidth(); j++) {
                Air air = sections[i][j].getAir();
                if (air.changeWeather(command)) {
                    msg = "The weather has changed.";
                    changeWeather = command.getTimestamp() + WEATHER_COOLDOWN_INTERVAL;
                }
            }
        }
        if (msg.isEmpty()) {
            msg = "ERROR: The weather change does not affect the environment."
                    + " Cannot perform action";
        }

        node.put("message", msg);
        output.add(node);
    }

    /**
     * Recharges the robot's battery.
     */
    public void rechargeBattery(final CommandInput command,
                                final ArrayNode output,
                                final ObjectNode node) {
        charging = command.getTimestamp() + command.getTimeToCharge();
        terraBot.setEnergyPoints(terraBot.getEnergyPoints() + command.getTimeToCharge());

        node.put("message", "Robot battery is charging.");
        output.add(node);
    }

    /**
     * Reports the current energy status of the robot.
     */
    public void getEnergyStatus(final ArrayNode output,
                                final ObjectNode node) {
        node.put("message", "TerraBot has " + terraBot.getEnergyPoints() + " energy points left.");
        output.add(node);
    }

    /**
     * Prints the robot's knowledge base
     */
    public void printKnowledgeBase(final ArrayNode output,
                                   final ObjectMapper mapper,
                                   final ObjectNode node) {
        KnowledgeBase kb = terraBot.getKnowledgeBase();
        ArrayNode outputArray = mapper.createArrayNode();

        ArrayList<String> allTopics = kb.getTopics();
        for (String topic : allTopics) {
            ObjectNode topicNode = mapper.createObjectNode();
            topicNode.put("topic", topic);

            ArrayNode factsArray = mapper.createArrayNode();
            ArrayList<String> facts = kb.getFacts(topic);
            for (String fact : facts) {
                factsArray.add(fact);
            }
            topicNode.set("facts", factsArray);
            outputArray.add(topicNode);
        }

        node.set("output", outputArray);
        output.add(node);
    }
}
