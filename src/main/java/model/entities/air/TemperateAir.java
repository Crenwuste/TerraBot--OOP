package model.entities.air;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents temperate air conditions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TemperateAir extends Air {

    private static final double TEMPERATE_HUMIDITY_MUL = 0.7;
    private static final double TEMPERATE_POLLEN_MUL = 0.1;
    private static final int TEMPERATE_MAX_SCORE = 84;

    /**
     * Calculates air quality for temperate air
     */
    @Override
    protected double calculateQualityInternal() {
        return oxygenLevel * OXYGEN_MUL + humidity * TEMPERATE_HUMIDITY_MUL
                - pollenLevel * TEMPERATE_POLLEN_MUL;
    }

    /**
     * Returns the maximum possible score for temperate air type
     *
     * @return maximum score for temperate air
     */
    @Override
    protected int getMaxScore() {
        return TEMPERATE_MAX_SCORE;
    }

    /**
     * Adds temperate air specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("pollenLevel", pollenLevel);
    }

    /**
     * Applies temperate-specific weather change and updates the air quality
     *
     * @param command weather-changing command
     */
    @Override
    protected void applyWeatherChange(final CommandInput command) {
        if (command.getSeason() == null) {
            return;
        }
        airQuality -= command.getSeason().equalsIgnoreCase("Spring") ? SPRING_SEASON_PENALTY : 0;
    }
}
