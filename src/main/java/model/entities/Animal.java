package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents an animal in a territory section
 */
@Data
public class Animal implements EnvironmentEntity {

    // Constants for attack possibility percentages
    private static final int HERBIVORES_ATT_POSSIBILITY = 85;
    private static final int CARNIVORES_ATT_POSSIBILITY = 30;
    private static final int OMNIVORES_ATT_POSSIBILITY = 60;
    private static final int DETRITIVORES_ATT_POSSIBILITY = 90;
    private static final int PARASITES_ATT_POSSIBILITY = 10;
    private static final int DEFAULT_ATT_POSSIBILITY = 0;

    // Constants for damage calculation
    private static final int MAX_POSSIBILITY = 100;
    private static final double DMG_DIV = 10;

    // Constants for mass rounding
    private static final double ROUNDING_FACTOR = 100;

    private String type;
    private String name;
    private double mass;

    private boolean isActive = false;
    private int lastMoveTimestamp;

    // States for animal feeding and organic matter production
    private boolean atePlant = false;
    private boolean drankWater = false;
    private boolean ateAnimal = false;
    private boolean isSick = false;
    private boolean producedOrganicMatter = false;

    /**
     * Builds a JSON representation of the animal to be printed in outputs
     *
     * @param mapper jackson mapper used to create json nodes
     * @return json node describing this animal
     */
    @Override
    public ObjectNode getEntities(final ObjectMapper mapper) {
        ObjectNode entities = mapper.createObjectNode();

        entities.put("type", type);
        entities.put("name", name);
        entities.put("mass", Math.round(mass * ROUNDING_FACTOR) / ROUNDING_FACTOR);

        return entities;
    }

    /**
     * Computes the damage dealt to TerraBot if this animal attacks it
     *
     * @return damage measured on a 0-10 scale
     */
    @Override
    public double calculateBlockingProbability() {
        int possibilityAttack = switch (type) {
            case "Herbivores" -> HERBIVORES_ATT_POSSIBILITY;
            case "Carnivores" -> CARNIVORES_ATT_POSSIBILITY;
            case "Omnivores" -> OMNIVORES_ATT_POSSIBILITY;
            case "Detritivores" -> DETRITIVORES_ATT_POSSIBILITY;
            case "Parasites" -> PARASITES_ATT_POSSIBILITY;
            default -> DEFAULT_ATT_POSSIBILITY;
        };
        return (MAX_POSSIBILITY - possibilityAttack) / DMG_DIV;
    }
}
