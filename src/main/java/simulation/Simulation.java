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
import model.position.Position;
import model.robot.Direction;
import model.robot.KnowledgeBase;
import model.robot.TerraBot;

import java.util.ArrayList;

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
     * Indicating the timestamp at witch the charging ends
     */
    private int charging = 0;

    private int changeWeather = 0;

    /**
     * Last timestamp for which entities were updated
     */
    private int lastUpdatedTimestamp = 0;

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

        // Update entities for all timestamps between last update and current command
        for (int timestamp = lastUpdatedTimestamp + 1; timestamp <= command.getTimestamp(); timestamp++) {
            updateActiveEntities(section, timestamp);
        }
        lastUpdatedTimestamp = command.getTimestamp();

        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getWidth(); j++) {
                Soil soil = section[i][j].getSoil();
                Air air = section[i][j].getAir();

                soil.calculateQuality();
                if (changeWeather <= command.getTimestamp()) {
                    air.calculateQuality();
                    air.setDesertStorm(false);
                }
            }
        }

        if (charging > command.getTimestamp()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("command", command.getCommand());
            node.put("message", "ERROR: Robot still charging. Cannot perform action");
            node.put("timestamp", command.getTimestamp());

            output.add(node);
            return;
        }

        String name = command.getCommand();
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
        node.put("command", command.getCommand());
        node.put("timestamp", command.getTimestamp());

        if (7 > terraBot.getEnergyPoints()) {
            node.put("message", "ERROR: Not enough battery left. Cannot perform action");
            output.add(node);

            return;
        }

        int x = terraBot.getPosition().getX();
        int y = terraBot.getPosition().getY();
        Section[][] sections = territory.getSections();
        Section currentSection = sections[x][y];

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

        int energyUsed = 7;
        terraBot.setEnergyPoints(terraBot.getEnergyPoints() - energyUsed);

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
                        air.setHumidity(air.getHumidity() + 0.1);
                        soil.setWaterRetention(soil.getWaterRetention() + 0.1);
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
                if (animal != null && animal.isActive()) {
                    // Animal moves every 2 iterations (timestamps)
                    // Check if at least 2 timestamps have passed since last move
                    feedAnimal(animal, currentSection);
                    if (currentTimestamp - animal.getLastMoveTimestamp() >= 2) {
                        moveAnimal(animal, i, j, sections);
                        animal.setLastMoveTimestamp(animal.getLastMoveTimestamp() + 2);
                    }
                }

            }
        }
    }

    private void feedAnimal(final Animal animal, final Section section) {
        if (animal.isFeedWithAnimal()) {
            animal.setFeedWithAnimal(false);
            return;
        }
        sectionWithWater(section, animal);
        sectionWithPlant(section, animal);
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
                animal.setFeedWithAnimal(true);

                targetSection.setAnimal(null);
            }
        }

        // Move animal to new section
        sections[currentX][currentY].setAnimal(null);
        targetSection.setAnimal(animal);
    }

    private void sectionWithPlant(final Section sectionWithPlant, final Animal animal) {
        Plant plant = sectionWithPlant.getPlant();
        if (plant == null || !plant.isActive()) {
            return;
        }

        animal.setMass(animal.getMass() + plant.getMass());
        sectionWithPlant.setPlant(null);
    }

    private void sectionWithWater(final Section sectionWithWater, final Animal animal) {
        Water water = sectionWithWater.getWater();
        if (water == null || !water.isActive()) {
            return;
        }

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
        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("timestamp", command.getTimestamp());


        if (2 > terraBot.getEnergyPoints()) {
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

        terraBot.setEnergyPoints(terraBot.getEnergyPoints() - 2);

        output.add(node);
    }


    private boolean hasImprovementFact(final String componentName,
                                       final String improvementType) {
        ArrayList<String> facts = terraBot.getKnowledgeBase().getFacts(componentName);
        if (facts.isEmpty()) {
            return false;
        }

        for (String fact : facts) {
            String firstWord = fact.split("\\s")[0];
            if (firstWord.equals("Method")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies an improvement to the environment.
     */
    public void improveEnvironment(final CommandInput command,
                                   final ArrayNode output,
                                   final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("timestamp", command.getTimestamp());
        String msg;

        if (10 > terraBot.getEnergyPoints()) {
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

        if (!hasImprovementFact(componentName, improvementType)) {
            node.put("message", "ERROR: Fact not yet saved. Cannot perform action");
            output.add(node);
            return;
        }

        Section[][] sections = territory.getSections();
        Position pos = terraBot.getPosition();
        Section currentSection = sections[pos.getX()][pos.getY()];
        Air air = currentSection.getAir();
        Soil soil = currentSection.getSoil();

        switch (improvementType) {
            case "plantVegetation" -> {
                air.setOxygenLevel(air.getOxygenLevel() + 0.3);
                msg = "The " + componentName + " was planted successfully.";
            }
            case "fertilizeSoil" -> {
                soil.setOrganicMatter(soil.getOrganicMatter() + 0.3);
                msg = "The soil was successfully fertilized using " + componentName;
            }
            case "increaseHumidity" -> {
                air.setHumidity(air.getHumidity() + 0.2);
                msg = "The humidity was successfully increased using " + componentName;
            }
            case "increaseMoisture" -> {
                soil.setWaterRetention(soil.getWaterRetention() + 0.2);
                msg = "The moisture was successfully increased using " + componentName;
            }
            default -> {
                msg = "ERROR: Improvement not supported. Cannot perform action";
                node.put("message", msg);
                output.add(node);
                return;
            }
        }

        terraBot.setEnergyPoints(terraBot.getEnergyPoints() - 10);
        terraBot.getScannedObjects().remove(componentName);

        node.put("message", msg);

        output.add(node);
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
        KnowledgeBase kb = terraBot.getKnowledgeBase();
        ArrayNode outputArray = mapper.createArrayNode();

        ArrayList<String> allTopics = kb.getAllTopics();
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

        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.set("output", outputArray);
        node.put("timestamp", command.getTimestamp());

        output.add(node);
    }

}
