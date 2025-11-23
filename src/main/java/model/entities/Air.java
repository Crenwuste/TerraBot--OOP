package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;

/**
 * Represents air conditions in a territory section
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

    private boolean desertStorm = false;
    private double airQuality = 0;
    private double changedAirQuality;

    private double clampAndRound(final double value) {
        double aux = Math.max(0, Math.min(100, value));
        return Math.round(aux * 100) / 100.0;
    }

    public void calculateQuality() {
        airQuality = switch (type) {
            case "TropicalAir" -> oxygenLevel * 2 + humidity * 0.5 - co2Level * 0.01;
            case "PolarAir" -> oxygenLevel * 2 + (100 - Math.abs(temperature))
                    - iceCrystalConcentration * 0.05;
            case "TemperateAir" -> oxygenLevel * 2 + humidity * 0.7 - pollenLevel * 0.1;
            case "DesertAir" -> oxygenLevel * 2 - dustParticles * 0.2 - temperature * 0.3;
            case "MountainAir" -> (oxygenLevel - (altitude / 1000 * 0.5)) * 2 + humidity * 0.6;
            default -> 0;
        };
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
        entities.put("humidity", clampAndRound(humidity));
        entities.put("temperature", temperature);
        entities.put("oxygenLevel", clampAndRound(oxygenLevel));
        entities.put("airQuality", airQuality);
        switch (type) {
            case "TropicalAir" -> entities.put("co2Level", clampAndRound(co2Level));
            case "PolarAir" ->entities.put("iceCrystalConcentration", iceCrystalConcentration);
            case "TemperateAir" -> entities.put("pollenLevel", pollenLevel);
            case "DesertAir" -> entities.put("desertStorm", desertStorm);
            case "MountainAir" -> entities.put("altitude", altitude);
            default -> { }
        }

        return entities;
    }

    @Override
    public double giveRobotDamage() {
        return toxicityAQ();
    }

    public double toxicityAQ() {
        calculateQuality();
        int maxScore = switch (type) {
            case "TropicalAir" -> 82;
            case "PolarAir" -> 142;
            case "TemperateAir" -> 84;
            case "DesertAir" -> 65;
            case "MountainAir" -> 78;
            default -> 0;
        };

        double toxicity = 100 * (1 - (airQuality / maxScore));
        toxicity = clampAndRound(toxicity);

        // true -> toxic, false -> non-toxic
        //return toxicity > 0.8 * maxScore;
        return toxicity;
    }

    public boolean changeWeather(final CommandInput cmd) {
        calculateQuality();
        double aux = airQuality;
        airQuality = switch (cmd.getType()) {
            case "rainfall" -> airQuality + (cmd.getRainfall() * 0.3);
            case "polarStorm" -> airQuality - (cmd.getWindSpeed() * 0.2);
            case "newSeason" ->
                    airQuality - (cmd.getSeason().equalsIgnoreCase("Spring") ? 15 : 0);
            case "desertStorm" -> {
                desertStorm = cmd.isDesertStorm();
                yield  airQuality - (desertStorm ? 30 : 0);
            }
            case "peopleHiking" -> airQuality - (cmd.getNumberOfHikers() * 0.1);
            default -> airQuality;
        };

        // false -> if airQuality doesn't change; true -> if it changes
        return aux != airQuality;
    }
}
