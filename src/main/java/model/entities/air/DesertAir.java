package model.entities.air;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents desert air conditions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DesertAir extends Air {

    private static final double DESERT_DUST_MUL = 0.2;
    private static final double DESERT_TEMPERATURE_MUL = 0.3;
    private static final int DESERT_MAX_SCORE = 65;

    /**
     * Calculates air quality for desert air
     */
    @Override
    protected double calculateQualityInternal() {
        return oxygenLevel * OXYGEN_MUL - dustParticles * DESERT_DUST_MUL
                - temperature * DESERT_TEMPERATURE_MUL;
    }

    /**
     * Returns the maximum possible score for desert air type
     *
     * @return maximum score for desert air
     */
    @Override
    protected int getMaxScore() {
        return DESERT_MAX_SCORE;
    }

    /**
     * Adds desert air specific fields to the JSON representation
     *
     * @param entities the JSON object node to add fields to
     * @param mapper jackson mapper used for creating nodes
     */
    @Override
    protected void addTypeSpecificFields(final ObjectNode entities, final ObjectMapper mapper) {
        entities.put("desertStorm", desertStorm);
    }

    /**
     * Applies desert-specific weather change and updates the air quality
     *
     * @param command weather-changing command
     */
    @Override
    protected void applyWeatherChange(final CommandInput command) {
        desertStorm = command.isDesertStorm();
        airQuality -=  desertStorm ? DESERT_STORM_PENALTY : 0;
    }
}
