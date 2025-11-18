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

    @Override
    public ObjectNode getEntities(final ObjectMapper mapper) {
        ObjectNode entities = mapper.createObjectNode();

        entities.put("type", type);
        entities.put("name", name);
        entities.put("mass", mass);

        return entities;
    }

}
