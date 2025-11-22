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

        for (int i = 0; i < territory.getHeight(); i++) {
            for (int j = 0; j < territory.getHeight(); j++) {
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

        ObjectNode entities = mapper.createObjectNode();

        entities.set("soil", section[x][y].getSoil().getEntities(mapper));
        if (section[x][y].getPlant() != null) {
            entities.set("plants", section[x][y].getPlant().getEntities(mapper));
        }
        if (section[x][y].getAnimal() != null) {
            entities.set("animals", section[x][y].getAnimal().getEntities(mapper));
        }
        if (section[x][y].getWater() != null) {
            entities.set("water", section[x][y].getWater().getEntities(mapper));
        }
        entities.set("air", section[x][y].getAir().getEntities(mapper));

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

                if (section[j][i].getPlant() != null) {
                    objCount++;
                }
                if (section[j][i].getAnimal() != null) {
                    objCount++;
                }
                if (section[j][i].getWater() != null) {
                    objCount++;
                }

                Soil soil = section[j][i].getSoil();
                Air air = section[j][i].getAir();

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

        int up = moveRobotHelper(x, y + 1);
        int right = moveRobotHelper(x + 1, y);
        int down = moveRobotHelper(x, y - 1);
        int left = moveRobotHelper(x - 1, y);

        int min = up;
        int xNew = x, yNew = y + 1;
        if (right < min) {
            min = right;
            xNew = x + 1;
            yNew = y;
        }
        if (down < min) {
            min = down;
            xNew = x;
            yNew = y - 1;
        }
        if (left < min) {
            min = left;
            xNew = x - 1;
            yNew = y;
        }

        String msg;
        if (min <= terraBot.getEnergyPoints()) {
            terraBot.getPosition().setX(xNew);
            terraBot.getPosition().setY(yNew);
            terraBot.setEnergyPoints(terraBot.getEnergyPoints() - min);
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
            return 999;
        }

        Section[][] section = territory.getSections();

        double soil = section[x][y].getSoil().giveRobotDamage();
        double air = section[x][y].getAir().giveRobotDamage();

        int count = 2;

        double animal = 0;
        if (section[x][y].getAnimal() != null) {
            animal = section[x][y].getAnimal().giveRobotDamage();
            count++;
        }
        double plant = 0;
        if (section[x][y].getPlant() != null) {
            plant = section[x][y].getPlant().giveRobotDamage();
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
        String msg = "";

        if (7 <= terraBot.getEnergyPoints()) {
            terraBot.setEnergyPoints(terraBot.getEnergyPoints() - 7);
        }

        ObjectNode node = mapper.createObjectNode();
        node.put("command", command.getCommand());
        node.put("message", msg);
        node.put("timestamp", command.getTimestamp());

        output.add(node);
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
