package model.environment;

import lombok.Data;
import model.entities.*;

/**
 * Represents a single cell of the territory
 */
@Data
public class Section {
    private Soil soil;
    private Plant plant;
    private Animal animal;
    private Water water;
    private Air air;
}
