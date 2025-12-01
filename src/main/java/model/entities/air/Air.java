package model.entities.air;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;
import model.entities.EnvironmentEntity;

/**
 * Abstract base class representing air conditions in a territory section.
 */
@Data
public abstract class Air implements EnvironmentEntity {

    // Constants for clamping and rounding
    protected static final int MIN_VALUE = 0;
    protected static final int MAX_VALUE = 100;
    protected static final double ROUNDING_FACTOR = 100;

    // Constants for air quality calculation multipliers
    protected static final double OXYGEN_MUL = 2;

    // Constants for air quality thresholds
    protected static final double GOOD_AIR_QUALITY = 70;
    protected static final double MODERATE_AIR_QUALITY = 40;
    protected static final double TOXICITY_THRESHOLD_FACTOR = 0.8;

    // Constants for toxicity calculation
    protected static final int TOXICITY_MUL = 100;

    // Constants for weather change effects
    protected static final double RAINFALL_MUL = 0.3;
    protected static final double POLAR_STORM_WIND_MUL = 0.2;
    protected static final double SPRING_SEASON_PENALTY = 15;
    protected static final double DESERT_STORM_PENALTY = 30;
    protected static final double HIKERS_PENALTY_MUL = 0.1;

    protected String type;
    protected String name;
    protected double mass;
    protected double humidity;
    protected double temperature;
    protected double oxygenLevel;
    protected double altitude;
    protected double pollenLevel;
    protected double co2Level;
    protected double iceCrystalConcentration;
    protected double dustParticles;

    protected boolean desertStorm = false;
    protected double airQuality;
    protected double changedAirQuality;

    /**
     * Clamps a numeric value within {@link #MIN_VALUE} and {@link #MAX_VALUE} and rounds it
     *
     * @param value raw value
     * @return clamped and rounded value
     */
    protected double clampAndRound(final double value) {
        double aux = Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
        return Math.round(aux * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    /**
     * Computes the current air quality score based on the air type and its properties.
     * This method delegates to the type-specific implementation.
     */
    public void calculateQuality() {
        airQuality = calculateQualityInternal();
        airQuality = clampAndRound(airQuality);
    }

    /**
     * Type-specific air quality calculation.
     *
     * @return raw air quality score before clamping
     */
    protected abstract double calculateQualityInternal();

    /**
     * Returns the maximum possible score for this air type.
     * Used in toxicity calculations.
     *
     * @return maximum score for this air type
     */
    protected abstract int getMaxScore();

    /**
     * Adds type-specific fields to the JSON representation.
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    protected abstract void addTypeSpecificFields(ObjectNode entities, ObjectMapper mapper);

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

        // Add type-specific fields through polymorphism
        addTypeSpecificFields(entities, mapper);

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
        double oldQuality = airQuality;
        applyWeatherChange(cmd);

        // false -> if airQuality doesn't change; true -> if it changes
        return oldQuality != airQuality;
    }

    /**
     * Applies a type-specific weather change and updates the air quality
     *
     * @param cmd weather-changing command
     */
    protected abstract void applyWeatherChange(CommandInput cmd);
}
