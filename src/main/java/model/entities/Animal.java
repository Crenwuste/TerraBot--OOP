package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents an animal in a territory section
 */
@Data
public class Animal implements EnvironmentEntity {

    private String type;

    private String name;

    private double mass;

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
        int possibilityAttack = switch (type) {
            case "Herbivores" -> 85;
            case "Carnivores" -> 30;
            case "Omnivores" -> 60;
            case "Detritivores" -> 90;
            case "Parasites" -> 10;
            default -> 0;
        };
        return (100 - possibilityAttack) / 10.0;
    }
}
