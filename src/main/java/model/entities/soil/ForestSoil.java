package model.entities.soil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents forest soil
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ForestSoil extends Soil {

    private static final double FOREST_NITROGEN_MUL = 1.2;
    private static final double FOREST_ORGANIC_MATTER_MUL = 2;
    private static final double FOREST_WATER_RETENTION_MUL = 1.5;
    private static final double FOREST_LEAF_LITTER_MUL = 0.3;
    private static final double FOREST_WATER_RETENTION_DAMAGE_MUL = 0.6;
    private static final double FOREST_LEAF_LITTER_DAMAGE_MUL = 0.4;
    private static final double FOREST_DAMAGE_DIV = 80;

    /**
     * Calculates soil quality for forest soil
     */
    @Override
    protected double calculateQualityInternal() {
        return (nitrogen * FOREST_NITROGEN_MUL)
                + (organicMatter * FOREST_ORGANIC_MATTER_MUL)
                + (waterRetention * FOREST_WATER_RETENTION_MUL)
                + (leafLitter * FOREST_LEAF_LITTER_MUL);
    }

    /**
     * Calculates the blocking probability for forest soil
     *
     * @return blocking probability/damage score for forest soil
     */
    @Override
    protected double calculateBlockingProbabilityInternal() {
        return (waterRetention * FOREST_WATER_RETENTION_DAMAGE_MUL
                + leafLitter * FOREST_LEAF_LITTER_DAMAGE_MUL) / FOREST_DAMAGE_DIV * MAX_VALUE;
    }

    /**
     * Adds forest soil specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("leafLitter", leafLitter);
    }
}
