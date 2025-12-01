package model.entities.air;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents polar air conditions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PolarAir extends Air {

    private static final double POLAR_ICE_CRYSTAL_MUL = 0.05;
    private static final int POLAR_MAX_SCORE = 142;

    /**
     * Calculates air quality for polar air
     */
    @Override
    protected double calculateQualityInternal() {
        return oxygenLevel * OXYGEN_MUL + (MAX_VALUE - Math.abs(temperature))
                - iceCrystalConcentration * POLAR_ICE_CRYSTAL_MUL;
    }

    /**
     * Returns the maximum possible score for polar air type
     *
     * @return maximum score for polar air
     */
    @Override
    protected int getMaxScore() {
        return POLAR_MAX_SCORE;
    }

    /**
     * Adds polar air specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("iceCrystalConcentration", iceCrystalConcentration);
    }

    /**
     * Applies polar-specific weather change and updates the air quality
     *
     * @param command weather-changing command
     */
    @Override
    protected void applyWeatherChange(final CommandInput command) {
        airQuality -= command.getWindSpeed() * POLAR_STORM_WIND_MUL;
    }
}
