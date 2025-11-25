package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;

/**
 * Represents air conditions in a territory section
 */
@Data
public class Air implements EnvironmentEntity {

    // Constants for clamping and rounding
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;
    private static final double ROUNDING_FACTOR = 100;

    // Constants for air quality calculation multipliers
    private static final double OXYGEN_MUL = 2;
    private static final double TROPICAL_HUMIDITY_MUL = 0.5;
    private static final double TROPICAL_CO2_MUL = 0.01;
    private static final double POLAR_ICE_CRYSTAL_MUL = 0.05;
    private static final double TEMPERATE_HUMIDITY_MUL = 0.7;
    private static final double TEMPERATE_POLLEN_MUL = 0.1;
    private static final double DESERT_DUST_MUL = 0.2;
    private static final double DESERT_TEMPERATURE_MUL = 0.3;
    private static final double MOUNTAIN_ALTITUDE_DIV = 1000;
    private static final double MOUNTAIN_ALTITUDE_MUL = 0.5;
    private static final double MOUNTAIN_HUMIDITY_MUL = 0.6;

    // Constants for air quality thresholds
    private static final double GOOD_AIR_QUALITY = 70;
    private static final double MODERATE_AIR_QUALITY = 40;
    private static final double TOXICITY_THRESHOLD_FACTOR = 0.8;

    // Constants for max scores by air type
    private static final int TROPICAL_MAX_SCORE = 82;
    private static final int POLAR_MAX_SCORE = 142;
    private static final int TEMPERATE_MAX_SCORE = 84;
    private static final int DESERT_MAX_SCORE = 65;
    private static final int MOUNTAIN_MAX_SCORE = 78;

    // Constants for toxicity calculation
    private static final int TOXICITY_MUL = 100;

    // Constants for weather change effects
    private static final double RAINFALL_MUL = 0.3;
    private static final double POLAR_STORM_WIND_MUL = 0.2;
    private static final double SPRING_SEASON_PENALTY = 15;
    private static final double DESERT_STORM_PENALTY = 30;
    private static final double HIKERS_PENALTY_MUL = 0.1;

    private String type;
    private String name;
    private double mass;
    private double humidity;
    private double temperature;
    private double oxygenLevel;
    private double altitude;
    private double pollenLevel;
    private double co2Level;
    private double iceCrystalConcentration;
    private double dustParticles;

    private boolean desertStorm = false;
    private double airQuality;
    private double changedAirQuality;

    /**
     * Clamps a numeric value within {@link #MIN_VALUE} and {@link #MAX_VALUE} and rounds it
     *
     * @param value raw value
     * @return clamped and rounded value
     */
    private double clampAndRound(final double value) {
        double aux = Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
        return Math.round(aux * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    /**
     * Computes the current air quality score based on the air type and its properties
     */
    public void calculateQuality() {
        airQuality = switch (type) {
            case "TropicalAir" -> oxygenLevel * OXYGEN_MUL + humidity * TROPICAL_HUMIDITY_MUL
                    - co2Level * TROPICAL_CO2_MUL;
            case "PolarAir" -> oxygenLevel * OXYGEN_MUL + (MAX_VALUE - Math.abs(temperature))
                    - iceCrystalConcentration * POLAR_ICE_CRYSTAL_MUL;
            case "TemperateAir" -> oxygenLevel * OXYGEN_MUL + humidity * TEMPERATE_HUMIDITY_MUL
                    - pollenLevel * TEMPERATE_POLLEN_MUL;
            case "DesertAir" -> oxygenLevel * OXYGEN_MUL - dustParticles * DESERT_DUST_MUL
                    - temperature * DESERT_TEMPERATURE_MUL;
            case "MountainAir" -> (oxygenLevel - (altitude / MOUNTAIN_ALTITUDE_DIV
                    * MOUNTAIN_ALTITUDE_MUL)) * OXYGEN_MUL + humidity * MOUNTAIN_HUMIDITY_MUL;
            default -> 0;
        };
        airQuality = clampAndRound(airQuality);
    }

    /**
     * Translates the numerical air quality into a message
     *
     * @return "good", "moderate" or "poor"
     */
    public String airQualityMessage() {
        calculateQuality();
        if (airQuality > GOOD_AIR_QUALITY) {
            return "good";
        } else if (airQuality > MODERATE_AIR_QUALITY) {
            return "moderate";
        } else {
            return "poor";
        }
    }

    /**
     * Serializes the air entity to JSON for printing purposes
     *
     * @param mapper jackson mapper used to build json nodes
     * @return json node containing air data
     */
    @Override
    public ObjectNode getEntities(final ObjectMapper mapper) {
        ObjectNode entities = mapper.createObjectNode();

        entities.put("type", type);
        entities.put("name", name);
        entities.put("mass", mass);
        entities.put("humidity", clampAndRound(humidity));
        entities.put("temperature", temperature);
        entities.put("oxygenLevel", Math.round(oxygenLevel * ROUNDING_FACTOR) / ROUNDING_FACTOR);
        entities.put("airQuality", clampAndRound(airQuality));
        switch (type) {
            case "TropicalAir" -> entities.put("co2Level", clampAndRound(co2Level));
            case "PolarAir" -> entities.put("iceCrystalConcentration", iceCrystalConcentration);
            case "TemperateAir" -> entities.put("pollenLevel", pollenLevel);
            case "DesertAir" -> entities.put("desertStorm", desertStorm);
            case "MountainAir" -> entities.put("altitude", altitude);
            default -> { }
        }

        return entities;
    }

    /**
     * Returns the impact that the air can have on TerraBot as the toxicity score
     *
     * @return toxicity score
     */
    @Override
    public double calculateBlockingProbability() {
        return toxicityAQ();
    }

    /**
     * Computes the toxicity score of the air
     *
     * @return toxicity value
     */
    public double toxicityAQ() {
        calculateQuality();
        int maxScore = getMaxScore();
        double toxicity = TOXICITY_MUL * (1 - (airQuality / maxScore));
        toxicity = clampAndRound(toxicity);

        return toxicity;
    }

    private int getMaxScore() {
        return switch (type) {
            case "TropicalAir" -> TROPICAL_MAX_SCORE;
            case "PolarAir" -> POLAR_MAX_SCORE;
            case "TemperateAir" -> TEMPERATE_MAX_SCORE;
            case "DesertAir" -> DESERT_MAX_SCORE;
            case "MountainAir" -> MOUNTAIN_MAX_SCORE;
            default -> 0;
        };
    }

    /**
     * Indicates whether the air is toxic for animals
     *
     * @return true if toxicity exceeds the threshold, false otherwise
     */
    public boolean isToxicForAnimals() {
        double toxicity = toxicityAQ();
        return toxicity > TOXICITY_THRESHOLD_FACTOR * getMaxScore();
    }

    /**
     * Applies the effect of a weather-changing command to the current air
     *
     * @param cmd command describing the requested change
     * @return true if the air quality changed, false otherwise
     */
    public boolean changeWeather(final CommandInput cmd) {
        calculateQuality();
        double aux = airQuality;
        airQuality = switch (cmd.getType()) {
            case "rainfall" -> airQuality + (cmd.getRainfall() * RAINFALL_MUL);
            case "polarStorm" -> airQuality - (cmd.getWindSpeed() * POLAR_STORM_WIND_MUL);
            case "newSeason" ->
                    airQuality - (cmd.getSeason().equalsIgnoreCase("Spring")
                            ? SPRING_SEASON_PENALTY : 0);
            case "desertStorm" -> {
                desertStorm = cmd.isDesertStorm();
                yield  airQuality - (desertStorm ? DESERT_STORM_PENALTY : 0);
            }
            case "peopleHiking" -> airQuality - (cmd.getNumberOfHikers() * HIKERS_PENALTY_MUL);
            default -> airQuality;
        };

        // false -> if airQuality doesn't change; true -> if it changes
        return aux != airQuality;
    }
}
