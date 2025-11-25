package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents water in a territory section
 */
@Data
public class Water implements EnvironmentEntity {

    // Constants for water quality calculation
    private static final double MAX_PERCENTAGE = 100;
    private static final double IDEAL_PH = 7.5;
    private static final double MAX_SALINITY = 350;

    // Constants for water quality weights
    private static final double PURITY_WEIGHT = 0.3;
    private static final double PH_WEIGHT = 0.2;
    private static final double SALINITY_WEIGHT = 0.15;
    private static final double TURBIDITY_WEIGHT = 0.1;
    private static final double CONTAMINANT_WEIGHT = 0.15;
    private static final double FROZEN_WEIGHT = 0.2;

    private String type;
    private String name;
    private double mass;
    private double purity;
    private double salinity;
    private double turbidity;
    private double contaminantIndex;
    private double pH;
    private boolean frozen;

    private boolean isActive = false;
    private int lastIterTimestamp;

    /**
     * Serializes the water entity for map/env outputs.
     *
     * @param mapper jackson mapper
     * @return json node describing water
     */
    @Override
    public ObjectNode getEntities(final ObjectMapper mapper) {
        ObjectNode entities = mapper.createObjectNode();

        entities.put("type", type);
        entities.put("name", name);
        entities.put("mass", mass);

        return entities;
    }

    /**
     * Water does not directly damage TerraBot, so this always returns 0.
     *
     * @return zero damage
     */
    @Override
    public double calculateBlockingProbability() {
        return 0;
    }

    /**
     * Computes the quality score of the water
     *
     * @return quality score between 0 and 100
     */
    public double waterQuality() {
        // Normalize factors
        double purityScore = purity / MAX_PERCENTAGE;
        double phScore = 1 - Math.abs(pH - IDEAL_PH) / IDEAL_PH;
        double salinityScore = 1 - (salinity / MAX_SALINITY);
        double turbidityScore = 1 - (turbidity / MAX_PERCENTAGE);
        double contaminantScore = 1 - (contaminantIndex / MAX_PERCENTAGE);
        double frozenScore = frozen ? 0 : 1;

        return (PURITY_WEIGHT * purityScore + PH_WEIGHT * phScore + SALINITY_WEIGHT * salinityScore
                + TURBIDITY_WEIGHT * turbidityScore + CONTAMINANT_WEIGHT * contaminantScore
                + FROZEN_WEIGHT * frozenScore) * MAX_PERCENTAGE;
    }
}
