package model.environment;

import lombok.Data;
import lombok.Getter;
import model.entities.air.Air;
import model.entities.Animal;
import model.entities.Plant;
import model.entities.EnvironmentEntity;
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

    /**
     * Calculates the movement cost for TerraBot entering this section based on
     * the blocking probabilities of the contained entities
     *
     * @return movement cost as an integer value
     */
    public int movementCost() {
        int count = 0;
        double sum = 0;

        sum += addEntityBlockingProbability(soil);
        if (soil != null) {
            count++;
        }

        sum += addEntityBlockingProbability(air);
        if (air != null) {
            count++;
        }

        sum += addEntityBlockingProbability(plant);
        if (plant != null) {
            count++;
        }

        sum += addEntityBlockingProbability(animal);
        if (animal != null) {
            count++;
        }

        double mean = Math.abs(sum / count);
        return (int) Math.round(mean);
    }

    private double addEntityBlockingProbability(final EnvironmentEntity entity) {
        return entity != null ? entity.calculateBlockingProbability() : 0;
    }

    /**
     * Feeds the given animal using the resources available in this section.
     *
     * @param waterIntakeRate  the fraction of the animal's mass it can drink as water
     */
    public void feedAnimal(final double waterIntakeRate) {
        // Reset flags at the beginning of each timestamp
        animal.setAtePlant(false);
        animal.setDrankWater(false);

        boolean drankWater = drinkWater(waterIntakeRate);
        boolean atePlant = eatPlant();

        if (drankWater) {
            animal.setDrankWater(true);
        }
        if (atePlant) {
            animal.setAtePlant(true);
        }
    }

    private boolean eatPlant() {
        if (plant == null || !plant.isActive()) {
            return false;
        }

        animal.setMass(animal.getMass() + plant.getMass());
        plant = null;
        return true;
    }

    private boolean drinkWater(final double waterIntakeRate) {
        if (water == null || !water.isActive() || water.getMass() == 0) {
            return false;
        }

        double waterToDrink = Math.min(animal.getMass() * waterIntakeRate, water.getMass());

        animal.setMass(animal.getMass() + waterToDrink);
        water.setMass(water.getMass() - waterToDrink);

        if (water.getMass() == 0) {
            water = null;
        }

        return true;
    }
}
