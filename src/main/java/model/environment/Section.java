package model.environment;

import lombok.Data;
import lombok.Getter;
import model.entities.air.Air;
import model.entities.Animal;
import model.entities.Plant;
import model.entities.soil.Soil;
import model.entities.Water;

/**
 * Represents a single cell of the territory
 */
@Data
@Getter
public class Section {
    private Soil soil;
    private Plant plant;
    private Animal animal;
    private Water water;
    private Air air;
}
