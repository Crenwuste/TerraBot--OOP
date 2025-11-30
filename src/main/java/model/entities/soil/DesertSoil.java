package model.entities.soil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents desert soil
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DesertSoil extends Soil {

    private static final double DESERT_NITROGEN_MUL = 0.5;
    private static final double DESERT_WATER_RETENTION_MUL = 0.3;
    private static final double DESERT_SALINITY_MUL = 2;
    private static final double DESERT_DAMAGE_DIV = 100;

    /**
     * Calculates soil quality for desert soil:
     */
    @Override
    protected double calculateQualityInternal() {
        return (nitrogen * DESERT_NITROGEN_MUL)
                + (waterRetention * DESERT_WATER_RETENTION_MUL)
                - (salinity * DESERT_SALINITY_MUL);
    }

    /**
     * Calculates the blocking probability for desert soil
     *
     * @return blocking probability/damage score for desert soil
     */
    @Override
    protected double calculateBlockingProbabilityInternal() {
        return (MAX_VALUE - waterRetention + salinity)
                / DESERT_DAMAGE_DIV * MAX_VALUE;
    }

    /**
     * Adds desert soil specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("salinity", salinity);
    }
}
