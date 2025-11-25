package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents a plant in a territory section
 */
@Data
public class Plant implements EnvironmentEntity {

    // Constants for attack possibility percentages
    private static final int FLOWERING_PLANTS_STUCK_POSSIBILITY = 90;
    private static final int GYMNOSPERMS_STUCK_POSSIBILITY = 60;
    private static final int FERNS_STUCK_POSSIBILITY = 30;
    private static final int MOSSES_STUCK_POSSIBILITY = 40;
    private static final int ALGAE_STUCK_POSSIBILITY = 20;
    private static final double POSSIBILITY_DIV = 100;

    // Constants for oxygen production
    private static final double FLOWERING_PLANTS_BASE_OXYGEN = 6.0;
    private static final double MOSSES_BASE_OXYGEN = 0.8;
    private static final double ALGAE_BASE_OXYGEN = 0.5;

    // Constants for growth and age
    private static final double INITIAL_AGE_SURPLUS = 0.2;
    private static final double MATURE_AGE_SURPLUS = 0.7;
    private static final double OLD_AGE_SURPLUS = 0.4;
    private static final double DEAD_AGE_SURPLUS = 0;
    private static final double GROWTH_INCREMENT = 0.2;
    private static final double GROWTH_THRESHOLD = 1;
    private static final double GROWTH_RESET = 0;

    private String type;
    private String name;
    private double mass;

    private double growth = GROWTH_RESET;
    private double ageSurplus = INITIAL_AGE_SURPLUS;
    private boolean isActive = false;
    private int lastIteration;

    /**
     * Serializes the plant entity to JSON for printing in outputs.
     *
     * @param mapper jackson mapper
     * @return json node containing plant fields
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
     * Returns the chance that TerraBot gets stuck by the plant
     *
     * @return stuck probability
     */
    @Override
    public double calculateBlockingProbability() {
        int possibilityStuck = switch (type) {
            case "FloweringPlants" -> FLOWERING_PLANTS_STUCK_POSSIBILITY;
            case "GymnospermsPlants" -> GYMNOSPERMS_STUCK_POSSIBILITY;
            case "Ferns" -> FERNS_STUCK_POSSIBILITY;
            case "Mosses" -> MOSSES_STUCK_POSSIBILITY;
            case "Algae" -> ALGAE_STUCK_POSSIBILITY;
            default -> 0;
        };
        return possibilityStuck / POSSIBILITY_DIV;
    }

    /**
     * Computes how much oxygen the plant produces during the current timestep
     *
     * @return produced oxygen
     */
    public double oxygenProduced() {
        return switch (type) {
            case "FloweringPlants" -> FLOWERING_PLANTS_BASE_OXYGEN + ageSurplus;
            case "GymnospermsPlants", "Ferns" -> ageSurplus;
            case "Mosses" -> MOSSES_BASE_OXYGEN + ageSurplus;
            case "Algae" -> ALGAE_BASE_OXYGEN + ageSurplus;
            default -> 0;
        };
    }

    /**
     * Advances the growth stage of the plant, updating its age surplus
     */
    public void increaseGrowth() {
        growth += GROWTH_INCREMENT;
        if (growth >= GROWTH_THRESHOLD) {
            if (ageSurplus == INITIAL_AGE_SURPLUS) {
                ageSurplus = MATURE_AGE_SURPLUS; // mature
            } else if (ageSurplus == MATURE_AGE_SURPLUS) {
                ageSurplus = OLD_AGE_SURPLUS; // old
            } else if (ageSurplus == OLD_AGE_SURPLUS) {
                ageSurplus = DEAD_AGE_SURPLUS; // dead
            }
            growth = GROWTH_RESET;
        }
    }
}
