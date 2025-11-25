package simulation;

import fileio.AirInput;
import fileio.AnimalInput;
import fileio.PairInput;
import fileio.PlantInput;
import fileio.SimulationInput;
import fileio.SoilInput;
import fileio.TerritorySectionParamsInput;
import fileio.WaterInput;
import model.entities.Air;
import model.entities.Animal;
import model.entities.Plant;
import model.entities.Soil;
import model.entities.Water;
import model.environment.Section;
import model.environment.Territory;
import model.position.Position;
import model.robot.TerraBot;

import java.util.List;

/**
 * Builds Simulation instances from input data
 */
public class SimulationFactory {

    /**
     * Creates a {@link Simulation} instance based on the given input parameters
     *
     * @param input simulation parameters
     * @return a new Simulation instance
     */
    public Simulation build(final SimulationInput input) {
        if (input == null) {
            throw new IllegalArgumentException("SimulationInput must not be null");
        }

        // Parse territory dimensions
        String[] dimTokens = input.getTerritoryDim().split("x");
        int width = Integer.parseInt(dimTokens[0]);
        int height = Integer.parseInt(dimTokens[1]);

        Territory territory = new Territory(width, height);

        // The robot starts at position (0, 0)
        Position startingPosition = new Position(0, 0);
        TerraBot terraBot = new TerraBot(startingPosition, input.getEnergyPoints());

        // Populate territory sections with soil, plants, animals, water and air
        TerritorySectionParamsInput params = input.getTerritorySectionParams();
        if (params != null) {
            populateSoil(territory, params.getSoil());
            populatePlants(territory, params.getPlants());
            populateAnimals(territory, params.getAnimals());
            populateWater(territory, params.getWater());
            populateAir(territory, params.getAir());
        }

        return new Simulation(territory, terraBot);
    }

    private void populateSoil(final Territory territory,
                              final List<SoilInput> soils) {
        if (soils == null) {
            return;
        }
        for (SoilInput soilInput : soils) {
            for (PairInput sectionPos : soilInput.getSections()) {
                Section section = ensureSection(territory, sectionPos);
                Soil soil = new Soil();
                soil.setType(soilInput.getType());
                soil.setName(soilInput.getName());
                soil.setMass(soilInput.getMass());
                soil.setNitrogen(soilInput.getNitrogen());
                soil.setWaterRetention(soilInput.getWaterRetention());
                soil.setSoilpH(soilInput.getSoilpH());
                soil.setOrganicMatter(soilInput.getOrganicMatter());
                soil.setLeafLitter(soilInput.getLeafLitter());
                soil.setWaterLogging(soilInput.getWaterLogging());
                soil.setPermafrostDepth(soilInput.getPermafrostDepth());
                soil.setRootDensity(soilInput.getRootDensity());
                soil.setSalinity(soilInput.getSalinity());
                section.setSoil(soil);
            }
        }
    }

    private void populatePlants(final Territory territory,
                                final List<PlantInput> plants) {
        if (plants == null) {
            return;
        }
        for (PlantInput plantInput : plants) {
            for (PairInput sectionPos : plantInput.getSections()) {
                Section section = ensureSection(territory, sectionPos);
                Plant plant = new Plant();
                plant.setType(plantInput.getType());
                plant.setName(plantInput.getName());
                plant.setMass(plantInput.getMass());
                section.setPlant(plant);
            }
        }
    }

    private void populateAnimals(final Territory territory,
                                 final List<AnimalInput> animals) {
        if (animals == null) {
            return;
        }
        for (AnimalInput animalInput : animals) {
            for (PairInput sectionPos : animalInput.getSections()) {
                Section section = ensureSection(territory, sectionPos);
                Animal animal = new Animal();
                animal.setType(animalInput.getType());
                animal.setName(animalInput.getName());
                animal.setMass(animalInput.getMass());
                section.setAnimal(animal);
            }
        }
    }

    private void populateWater(final Territory territory,
                               final List<WaterInput> waters) {
        if (waters == null) {
            return;
        }
        for (WaterInput waterInput : waters) {
            for (PairInput sectionPos : waterInput.getSections()) {
                Section section = ensureSection(territory, sectionPos);
                Water water = new Water();
                water.setType(waterInput.getType());
                water.setName(waterInput.getName());
                water.setMass(waterInput.getMass());
                water.setPurity(waterInput.getPurity());
                water.setSalinity(waterInput.getSalinity());
                water.setTurbidity(waterInput.getTurbidity());
                water.setContaminantIndex(waterInput.getContaminantIndex());
                water.setPH(waterInput.getPH());
                water.setFrozen(waterInput.isFrozen());
                section.setWater(water);
            }
        }
    }

    private void populateAir(final Territory territory,
                             final List<AirInput> airs) {
        if (airs == null) {
            return;
        }
        for (AirInput airInput : airs) {
            for (PairInput sectionPos : airInput.getSections()) {
                Section section = ensureSection(territory, sectionPos);
                Air air = new Air();
                air.setType(airInput.getType());
                air.setName(airInput.getName());
                air.setMass(airInput.getMass());
                air.setHumidity(airInput.getHumidity());
                air.setTemperature(airInput.getTemperature());
                air.setOxygenLevel(airInput.getOxygenLevel());
                air.setAltitude(airInput.getAltitude());
                air.setPollenLevel(airInput.getPollenLevel());
                air.setCo2Level(airInput.getCo2Level());
                air.setIceCrystalConcentration(airInput.getIceCrystalConcentration());
                air.setDustParticles(airInput.getDustParticles());
                section.setAir(air);
            }
        }
    }

    /**
     * Ensures that a {@link Section} exists at the given position
     * in the territory, creating it if necessary
     */
    private Section ensureSection(final Territory territory, final PairInput pos) {
        Section[][] grid = territory.getSections();
        Section section = grid[pos.getX()][pos.getY()];
        if (section == null) {
            section = new Section();
            grid[pos.getX()][pos.getY()] = section;
        }
        return section;
    }
}
