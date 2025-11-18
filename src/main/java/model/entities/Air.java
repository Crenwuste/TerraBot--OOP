package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/**
 * Represents air conditions in a territory section.
 */
@Data
public class Air implements EnvironmentEntity {

    private String type;
    private String name;
    private double mass;
    private double humidity;
    private double temperature;
    private double oxygenLevel;
    private double altitude;
    private double pollenLevel;
    private double co2Level;
    private double iceCrystalConcentration;
    private double dustParticles;

    private double airQuality = 0;

    private double clampAndRound(final double value) {
        double aux = Math.max(0, Math.min(100, value));
        return Math.round(aux * 100) / 100.0;
    }

    public void calculateQuality() {
        if (co2Level != 0) {
            airQuality = oxygenLevel * 2 + humidity * 0.5 - co2Level * 0.01;
        } else if (iceCrystalConcentration != 0) {
            airQuality = oxygenLevel * 2 + (100 - Math.abs(temperature)) - iceCrystalConcentration * 0.05;
        } else if (pollenLevel != 0) {
            airQuality = oxygenLevel * 2 + humidity * 0.7 - pollenLevel * 0.1;
        } else if (dustParticles != 0) {
            airQuality = oxygenLevel * 2 - dustParticles * 0.2 - temperature * 0.3;
        } else if (altitude != 0) {
            double oxygenFactor = oxygenLevel - (altitude / 1000 * 0.5);
            airQuality = oxygenFactor * 2 + humidity * 0.6;
        }
        airQuality = clampAndRound(airQuality);
    }

    public String airQualityMessage() {
        calculateQuality();
        if (airQuality > 70) {
            return "good";
        } else if (airQuality > 40) {
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
        entities.put("humidity", humidity);
        entities.put("temperature", temperature);
        entities.put("oxygenLevel", oxygenLevel);
        entities.put("airQuality", airQuality);
        if (co2Level != 0) {
            entities.put("co2Level", co2Level);
        } else if (iceCrystalConcentration != 0) {
            entities.put("iceCrystalConcentration", iceCrystalConcentration);
        } else if (pollenLevel != 0) {
            entities.put("pollenLevel", pollenLevel);
        } else if (dustParticles != 0) {
            entities.put("dustParticles", dustParticles);
        } else if (altitude != 0) {
            entities.put("altitude", altitude);
        }

        return entities;
    }

}
