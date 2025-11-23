package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents soil in a territory section
 */
@Data
public class Soil implements EnvironmentEntity {

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

    private double clampAndRound(final double value) {
        double aux = Math.max(0, Math.min(100, value));
        return Math.round(aux * 100) / 100.0;
    }

    public void calculateQuality() {
        soilQuality = switch (type) {
            case "ForestSoil" -> (nitrogen * 1.2) + (organicMatter * 2)
                    + (waterRetention * 1.5) + (leafLitter * 0.3);
            case "SwampSoil" -> (nitrogen * 1.1) + (organicMatter * 2.2) - (waterLogging * 5);
            case "DesertSoil" -> (nitrogen * 0.5) + (waterRetention * 0.3) - (salinity * 2);
            case "GrasslandSoil" -> (nitrogen * 1.3) + (organicMatter * 1.5) + (rootDensity * 0.8);
            case "TundraSoil" -> (nitrogen * 0.7) + (organicMatter * 0.5) - (permafrostDepth * 1.5);
            default -> 0;
        };
        soilQuality = clampAndRound(soilQuality);
    }

    public String soilQualityMessage() {
        calculateQuality();
        if (soilQuality > 70) {
            return "good";
        } else if (soilQuality > 40) {
            return "moderate";
        } else {
            return "poor";
        }
    }

    @Override
    public ObjectNode getEntities(final ObjectMapper mapper) {
        ObjectNode entities = mapper.createObjectNode();

        entities.put("type", type);
        entities.put("name", name);
        entities.put("mass", mass);
        entities.put("nitrogen", nitrogen);
        entities.put("waterRetention", clampAndRound(waterRetention));
        entities.put("soilpH", soilpH);
        entities.put("organicMatter", organicMatter);
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

    @Override
    public double giveRobotDamage() {
        return switch (type) {
            case "ForestSoil" -> (waterRetention * 0.6 + leafLitter * 0.4) / 80 * 100;
            case "SwampSoil" -> waterLogging * 10;
            case "DesertSoil" -> (100 - waterRetention + salinity) / 100 * 100;
            case "GrasslandSoil" -> ((50 - rootDensity) + waterRetention * 0.5) / 75 * 100;
            case "TundraSoil" -> (50 - permafrostDepth) / 50 * 100;
            default -> 0;
        };
    }
}
