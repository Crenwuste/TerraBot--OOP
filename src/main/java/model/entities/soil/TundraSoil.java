package model.entities.soil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents tundra soil
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TundraSoil extends Soil {

    private static final double TUNDRA_NITROGEN_MUL = 0.7;
    private static final double TUNDRA_ORGANIC_MATTER_MUL = 0.5;
    private static final double TUNDRA_PERMAFROST_MUL = 1.5;
    private static final double TUNDRA_PERMAFROST_BASE = 50;
    private static final double TUNDRA_DAMAGE_DIVISOR = 50;

    /**
     * Calculates soil quality for tundra soil:
     */
    @Override
    protected double calculateQualityInternal() {
        return (nitrogen * TUNDRA_NITROGEN_MUL)
                + (organicMatter * TUNDRA_ORGANIC_MATTER_MUL)
                - (permafrostDepth * TUNDRA_PERMAFROST_MUL);
    }

    /**
     * Calculates the blocking probability for tundra soil
     *
     * @return blocking probability score for tundra soil
     */
    @Override
    protected double calculateBlockingProbabilityInternal() {
        return (TUNDRA_PERMAFROST_BASE - permafrostDepth)
                / TUNDRA_DAMAGE_DIVISOR * MAX_VALUE;
    }

    /**
     * Adds tundra soil specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("permafrostDepth", permafrostDepth);
    }
}
