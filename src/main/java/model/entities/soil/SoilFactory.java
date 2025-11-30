package model.entities.soil;

import fileio.SoilInput;

/**
 * Factory class for creating Soil instances based on type
 */
public final class SoilFactory {

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private SoilFactory() {
        // Utility class
    }

    /**
     * Creates a Soil instance based on the type string.
     *
     * @param type the type of soil to create
     * @return a new Soil instance of the appropriate subclass
     * @throws IllegalArgumentException if the type is not recognized
     */
    public static Soil createSoil(final String type) {
        if (type == null) {
            throw new IllegalArgumentException("Soil type cannot be null");
        }

        return switch (type) {
            case "ForestSoil" -> new ForestSoil();
            case "SwampSoil" -> new SwampSoil();
            case "DesertSoil" -> new DesertSoil();
            case "GrasslandSoil" -> new GrasslandSoil();
            case "TundraSoil" -> new TundraSoil();
            default -> throw new IllegalArgumentException("Unknown soil type: " + type);
        };
    }

    /**
     * Creates a Soil instance from SoilInput data.
     * This method handles the creation and initialization of Soil objects.
     *
     * @param soilInput input data containing soil properties
     * @return a fully initialized Soil instance
     */
    public static Soil createSoilFromInput(final SoilInput soilInput) {
        if (soilInput == null) {
            throw new IllegalArgumentException("SoilInput cannot be null");
        }

        Soil soil = createSoil(soilInput.getType());

        // Set common properties
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

        return soil;
    }
}
