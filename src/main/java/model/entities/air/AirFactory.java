package model.entities.air;

import fileio.AirInput;

/**
 * Factory class for creating Air instances based on type.
 */
public final class AirFactory {

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private AirFactory() {
        // Utility class
    }

    /**
     * Creates an Air instance based on the type string.
     *
     * @param type the type of air to create
     * @return a new Air instance of the appropriate subclass
     * @throws IllegalArgumentException if the type is not recognized
     */
    public static Air createAir(final String type) {
        if (type == null) {
            throw new IllegalArgumentException("Air type cannot be null");
        }

        return switch (type) {
            case "TropicalAir" -> new TropicalAir();
            case "PolarAir" -> new PolarAir();
            case "TemperateAir" -> new TemperateAir();
            case "DesertAir" -> new DesertAir();
            case "MountainAir" -> new MountainAir();
            default -> throw new IllegalArgumentException("Unknown air type: " + type);
        };
    }

    /**
     * Creates an Air instance from AirInput data.
     * This method handles the creation and initialization of Air objects.
     *
     * @param airInput input data containing air properties
     * @return a fully initialized Air instance
     */
    public static Air createAirFromInput(final AirInput airInput) {
        if (airInput == null) {
            throw new IllegalArgumentException("AirInput cannot be null");
        }

        Air air = createAir(airInput.getType());

        // Set common properties
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

        return air;
    }
}
