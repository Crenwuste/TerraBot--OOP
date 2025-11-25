package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents soil in a territory section
 */
@Data
public class Soil implements EnvironmentEntity {

    // Constants for clamping and rounding
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;
    private static final double ROUNDING_FACTOR = 100;

    // Constants for soil quality calculation multipliers
    // ForestSoil
    private static final double FOREST_NITROGEN_MUL = 1.2;
    private static final double FOREST_ORGANIC_MATTER_MUL = 2;
    private static final double FOREST_WATER_RETENTION_MUL = 1.5;
    private static final double FOREST_LEAF_LITTER_MUL = 0.3;
    // SwampSoil
    private static final double SWAMP_NITROGEN_MUL = 1.1;
    private static final double SWAMP_ORGANIC_MATTER_MUL = 2.2;
    private static final double SWAMP_WATER_LOGGING_MUL = 5;
    // DesertSoil
    private static final double DESERT_NITROGEN_MUL = 0.5;
    private static final double DESERT_WATER_RETENTION_MUL = 0.3;
    private static final double DESERT_SALINITY_MUL = 2;
    // GrasslandSoil
    private static final double GRASSLAND_NITROGEN_MUL = 1.3;
    private static final double GRASSLAND_ORGANIC_MATTER_MUL = 1.5;
    private static final double GRASSLAND_ROOT_DENSITY_MUL = 0.8;
    // TundraSoil
    private static final double TUNDRA_NITROGEN_MUL = 0.7;
    private static final double TUNDRA_ORGANIC_MATTER_MUL = 0.5;
    private static final double TUNDRA_PERMAFROST_MUL = 1.5;

    // Constants for soil quality thresholds
    private static final double GOOD_SOIL_QUALITY = 70;
    private static final double MODERATE_SOIL_QUALITY = 40;

    // Constants for robot damage calculation
    private static final double FOREST_WATER_RETENTION_DAMAGE_MUL = 0.6;
    private static final double FOREST_LEAF_LITTER_DAMAGE_MUL = 0.4;
    private static final double FOREST_DAMAGE_DIV = 80;
    private static final double SWAMP_WATER_LOGGING_DAMAGE_MUL = 10;
    private static final double DESERT_DAMAGE_DIV = 100;
    private static final double GRASSLAND_ROOT_DENSITY_BASE = 50;
    private static final double GRASSLAND_WATER_RETENTION_DAMAGE_MUL = 0.5;
    private static final double GRASSLAND_DAMAGE_DIV = 75;
    private static final double TUNDRA_PERMAFROST_BASE = 50;
    private static final double TUNDRA_DAMAGE_DIVISOR = 50;

    private String type;
    private String name;
    private double mass;
    private double nitrogen;
    private double waterRetention;
    private double soilpH;
    private double organicMatter;
    private double leafLitter;
    private double waterLogging;
    private double permafrostDepth;
    private double rootDensity;
    private double salinity;

    private double soilQuality;

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
     * Computes the quality score of the soil based on its type and attributes
     */
    public void calculateQuality() {
        soilQuality = switch (type) {
            case "ForestSoil" -> (nitrogen * FOREST_NITROGEN_MUL)
                    + (organicMatter * FOREST_ORGANIC_MATTER_MUL)
                    + (waterRetention * FOREST_WATER_RETENTION_MUL)
                    + (leafLitter * FOREST_LEAF_LITTER_MUL);
            case "SwampSoil" -> (nitrogen * SWAMP_NITROGEN_MUL)
                    + (organicMatter * SWAMP_ORGANIC_MATTER_MUL)
                    - (waterLogging * SWAMP_WATER_LOGGING_MUL);
            case "DesertSoil" -> (nitrogen * DESERT_NITROGEN_MUL)
                    + (waterRetention * DESERT_WATER_RETENTION_MUL)
                    - (salinity * DESERT_SALINITY_MUL);
            case "GrasslandSoil" -> (nitrogen * GRASSLAND_NITROGEN_MUL)
                    + (organicMatter * GRASSLAND_ORGANIC_MATTER_MUL)
                    + (rootDensity * GRASSLAND_ROOT_DENSITY_MUL);
            case "TundraSoil" -> (nitrogen * TUNDRA_NITROGEN_MUL)
                    + (organicMatter * TUNDRA_ORGANIC_MATTER_MUL)
                    - (permafrostDepth * TUNDRA_PERMAFROST_MUL);
            default -> 0;
        };
        soilQuality = clampAndRound(soilQuality);
    }

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
        switch (type) {
            case "ForestSoil" -> entities.put("leafLitter", leafLitter);
            case "SwampSoil" -> entities.put("waterLogging", waterLogging);
            case "DesertSoil" -> entities.put("salinity", salinity);
            case "GrasslandSoil" -> entities.put("rootDensity", rootDensity);
            case "TundraSoil" -> entities.put("permafrostDepth", permafrostDepth);
            default -> { }
        }

        return entities;
    }

    /**
     * Computes the probability that the soil will block the TerraBot
     *
     * @return damage score
     */
    @Override
    public double calculateBlockingProbability() {
        return switch (type) {
            case "ForestSoil" -> (waterRetention * FOREST_WATER_RETENTION_DAMAGE_MUL
                    + leafLitter * FOREST_LEAF_LITTER_DAMAGE_MUL) / FOREST_DAMAGE_DIV * MAX_VALUE;
            case "SwampSoil" -> waterLogging * SWAMP_WATER_LOGGING_DAMAGE_MUL;
            case "DesertSoil" -> (MAX_VALUE - waterRetention + salinity)
                    / DESERT_DAMAGE_DIV * MAX_VALUE;
            case "GrasslandSoil" -> ((GRASSLAND_ROOT_DENSITY_BASE - rootDensity)
                    + waterRetention * GRASSLAND_WATER_RETENTION_DAMAGE_MUL)
                    / GRASSLAND_DAMAGE_DIV * MAX_VALUE;
            case "TundraSoil" -> (TUNDRA_PERMAFROST_BASE - permafrostDepth)
                    / TUNDRA_DAMAGE_DIVISOR * MAX_VALUE;
            default -> 0;
        };
    }
}
