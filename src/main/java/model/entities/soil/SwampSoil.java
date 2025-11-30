package model.entities.soil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents swamp soil
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SwampSoil extends Soil {

    private static final double SWAMP_NITROGEN_MUL = 1.1;
    private static final double SWAMP_ORGANIC_MATTER_MUL = 2.2;
    private static final double SWAMP_WATER_LOGGING_MUL = 5;
    private static final double SWAMP_WATER_LOGGING_DAMAGE_MUL = 10;

    /**
     * Calculates soil quality for swamp soil
     */
    @Override
    protected double calculateQualityInternal() {
        return (nitrogen * SWAMP_NITROGEN_MUL)
                + (organicMatter * SWAMP_ORGANIC_MATTER_MUL)
                - (waterLogging * SWAMP_WATER_LOGGING_MUL);
    }

    /**
     * Calculates the blocking probability for swamp soil
     *
     * @return blocking probability score for swamp soil
     */
    @Override
    protected double calculateBlockingProbabilityInternal() {
        return waterLogging * SWAMP_WATER_LOGGING_DAMAGE_MUL;
    }

    /**
     * Adds swamp soil specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("waterLogging", waterLogging);
    }
}
