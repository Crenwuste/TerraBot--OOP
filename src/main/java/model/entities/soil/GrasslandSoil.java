package model.entities.soil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents grassland soil
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GrasslandSoil extends Soil {

    private static final double GRASSLAND_NITROGEN_MUL = 1.3;
    private static final double GRASSLAND_ORGANIC_MATTER_MUL = 1.5;
    private static final double GRASSLAND_ROOT_DENSITY_MUL = 0.8;
    private static final double GRASSLAND_ROOT_DENSITY_BASE = 50;
    private static final double GRASSLAND_WATER_RETENTION_DAMAGE_MUL = 0.5;
    private static final double GRASSLAND_DAMAGE_DIV = 75;

    /**
     * Calculates soil quality for grassland soil:
     */
    @Override
    protected double calculateQualityInternal() {
        return (nitrogen * GRASSLAND_NITROGEN_MUL)
                + (organicMatter * GRASSLAND_ORGANIC_MATTER_MUL)
                + (rootDensity * GRASSLAND_ROOT_DENSITY_MUL);
    }

    /**
     * Calculates the blocking probability for grassland soil
     *
     * @return blocking probability score for grassland soil
     */
    @Override
    protected double calculateBlockingProbabilityInternal() {
        return ((GRASSLAND_ROOT_DENSITY_BASE - rootDensity)
                + waterRetention * GRASSLAND_WATER_RETENTION_DAMAGE_MUL)
                / GRASSLAND_DAMAGE_DIV * MAX_VALUE;
    }

    /**
     * Adds grassland soil specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("rootDensity", rootDensity);
    }
}
