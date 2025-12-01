package model.entities.air;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents mountain air conditions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MountainAir extends Air {

    private static final double MOUNTAIN_ALTITUDE_DIV = 1000;
    private static final double MOUNTAIN_ALTITUDE_MUL = 0.5;
    private static final double MOUNTAIN_HUMIDITY_MUL = 0.6;
    private static final int MOUNTAIN_MAX_SCORE = 78;

    /**
     * Calculates air quality for mountain air
     */
    @Override
    protected double calculateQualityInternal() {
        return (oxygenLevel - (altitude / MOUNTAIN_ALTITUDE_DIV
                * MOUNTAIN_ALTITUDE_MUL)) * OXYGEN_MUL + humidity * MOUNTAIN_HUMIDITY_MUL;
    }

    /**
     * Returns the maximum possible score for mountain air type
     *
     * @return maximum score for mountain air
     */
    @Override
    protected int getMaxScore() {
        return MOUNTAIN_MAX_SCORE;
    }

    /**
     * Adds mountain air specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("altitude", altitude);
    }

    /**
     * Applies mountain-specific weather change and updates the air quality
     *
     * @param command weather-changing command
     */
    @Override
    protected void applyWeatherChange(final CommandInput command) {
        airQuality -= command.getNumberOfHikers() * HIKERS_PENALTY_MUL;
    }
}
