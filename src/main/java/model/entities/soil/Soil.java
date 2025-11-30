package model.entities.soil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import model.entities.EnvironmentEntity;

/**
 * Abstract base class representing soil in a territory section.
 */
@Data
public abstract class Soil implements EnvironmentEntity {

    // Constants for clamping and rounding
    protected static final int MIN_VALUE = 0;
    protected static final int MAX_VALUE = 100;
    protected static final double ROUNDING_FACTOR = 100;

    // Constants for soil quality thresholds
    protected static final double GOOD_SOIL_QUALITY = 70;
    protected static final double MODERATE_SOIL_QUALITY = 40;

    protected String type;
    protected String name;
    protected double mass;
    protected double nitrogen;
    protected double waterRetention;
    protected double soilpH;
    protected double organicMatter;
    protected double leafLitter;
    protected double waterLogging;
    protected double permafrostDepth;
    protected double rootDensity;
    protected double salinity;

    protected double soilQuality;

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
     * Computes the quality score of the soil based on its type and attributes.
     * This method delegates to the type-specific implementation.
     */
    public void calculateQuality() {
        soilQuality = calculateQualityInternal();
        soilQuality = clampAndRound(soilQuality);
    }

    /**
     * Type-specific soil quality calculation.
     *
     * @return raw soil quality score before clamping
     */
    protected abstract double calculateQualityInternal();

    /**
     * Type-specific blocking probability calculation.
     *
     * @return blocking probability/damage score
     */
    protected abstract double calculateBlockingProbabilityInternal();

    /**
     * Adds type-specific fields to the JSON representation.
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    protected abstract void addTypeSpecificFields(ObjectNode entities, ObjectMapper mapper);

    /**
     * Converts the numeric soil quality into a message
     *
     * @return "good", "moderate" or "poor"
     */
    public String soilQualityMessage() {
        calculateQuality();
        if (soilQuality > GOOD_SOIL_QUALITY) {
            return "good";
        } else if (soilQuality > MODERATE_SOIL_QUALITY) {
            return "moderate";
        } else {
            return "poor";
        }
    }

    /**
     * Serializes the soil entity for printing in outputs
     *
     * @param mapper jackson mapper
     * @return json node containing soil fields
     */
    @Override
    public ObjectNode getEntities(final ObjectMapper mapper) {
        ObjectNode entities = mapper.createObjectNode();

        entities.put("type", type);
        entities.put("name", name);
        entities.put("mass", mass);
        entities.put("nitrogen", nitrogen);
        entities.put("waterRetention", clampAndRound(waterRetention));
        entities.put("soilpH", soilpH);
        entities.put("organicMatter", clampAndRound(organicMatter));
        entities.put("soilQuality", soilQuality);

        // Add type-specific fields through polymorphism
        addTypeSpecificFields(entities, mapper);

        return entities;
    }

    /**
     * Computes the probability that the soil will block the TerraBot
     *
     * @return damage score
     */
    @Override
    public double calculateBlockingProbability() {
        return calculateBlockingProbabilityInternal();
    }
}

