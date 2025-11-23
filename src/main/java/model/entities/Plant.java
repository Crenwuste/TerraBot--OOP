package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents a plant in a territory section
 */
@Data
public class Plant implements EnvironmentEntity {

    private String type;
    private String name;
    private double mass;

    private double growth = 0;
    private double ageSurplus = 0.2;
    private boolean isActive = false;

    @Override
    public ObjectNode getEntities(final ObjectMapper mapper) {
        ObjectNode entities = mapper.createObjectNode();

        entities.put("type", type);
        entities.put("name", name);
        entities.put("mass", mass);

        return entities;
    }

    @Override
    public double giveRobotDamage() {
        int possibilityStuck = switch (type) {
            case "FloweringPlants" -> 90;
            case "GymnospermsPlants" -> 60;
            case "Ferns" -> 30;
            case "Mosses" -> 40;
            case "Algae" -> 20;
            default -> 0;
        };
        return possibilityStuck / 100.0;
    }

    public double oxygenProduced() {
        return switch (type) {
            case "FloweringPlants" -> 6 + ageSurplus;
            case "GymnospermsPlants" -> ageSurplus;
            case "Ferns" -> ageSurplus;
            case "Mosses" -> 0.8 + ageSurplus;
            case "Algae" -> 0.5 + ageSurplus;
            default -> 0;
        };
    }

    public void increaseGrowth() {
        growth += 0.2;
        if (growth == 1) {
            if (ageSurplus == 0.2) {
                ageSurplus = 0.7; // mature
            } else if (ageSurplus == 0.7) {
                ageSurplus = 0.4; // old
            } else if (ageSurplus == 0.4) {
                ageSurplus = 0;
            }
        }
    }
}
