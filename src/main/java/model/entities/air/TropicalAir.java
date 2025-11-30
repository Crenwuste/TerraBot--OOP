package model.entities.air;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents tropical air conditions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TropicalAir extends Air {

    private static final double TROPICAL_HUMIDITY_MUL = 0.5;
    private static final double TROPICAL_CO2_MUL = 0.01;
    private static final int TROPICAL_MAX_SCORE = 82;

    /**
     * Calculates air quality for tropical air
     */
    @Override
    protected double calculateQualityInternal() {
        return oxygenLevel * OXYGEN_MUL + humidity * TROPICAL_HUMIDITY_MUL
                - co2Level * TROPICAL_CO2_MUL;
    }

    /**
     * Returns the maximum possible score for tropical air type
     *
     * @return maximum score for tropical air
     */
    @Override
    protected int getMaxScore() {
        return TROPICAL_MAX_SCORE;
    }

    /**
     * Adds tropical air specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("co2Level", clampAndRound(co2Level));
    }
}
