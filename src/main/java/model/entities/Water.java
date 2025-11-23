package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents water in a territory section
 */
@Data
public class Water implements EnvironmentEntity {

    private String type;
    private String name;
    private double mass;
    private double purity;
    private double salinity;
    private double turbidity;
    private double contaminantIndex;
    private double pH;
    private boolean frozen;

    private boolean isActive = false;
    private int lastIterTimestamp = 0;

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
        return 0;
    }

    public double waterQuality() {
        // Normalize factors
        double purity_score = purity / 100;
        double pH_score = 1 - Math.abs(pH - 7.5) / 7.5;
        double salinity_score = 1 - (salinity / 350);
        double turbidity_score = 1 - (turbidity / 100);
        double contaminant_score = 1 - (contaminantIndex / 100);
        double frozen_score = frozen ? 0 : 1;

        return (0.3 * purity_score + 0.2 * pH_score + 0.15 * salinity_score
                + 0.1 * turbidity_score + 0.15 * contaminant_score + 0.2 * frozen_score) * 100;
    }
}
