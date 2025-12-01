package model.robot;

import lombok.Data;
import model.environment.Section;
import model.environment.Territory;
import model.position.Position;
import java.util.ArrayList;

/**
 * Represents the TerraBot robot exploring the territory
 */
@Data
public class TerraBot {

    /**
     * Current position of the robot on the territory grid
     */
    private Position position;

    /**
     * Current energy points of the robot
     */
    private int energyPoints;

    /**
     * Knowledge base storing learned facts
     */
    private KnowledgeBase knowledgeBase;

    /**
     * Objects that have been scanned
     */
    private ArrayList<String> scannedObjects;

    /**
     * Creates a robot with the given starting position and energy
     *
     * @param position     initial position
     * @param energyPoints initial energy points
     */
    public TerraBot(final Position position, final int energyPoints) {
        this.position = position;
        this.energyPoints = energyPoints;
        this.knowledgeBase = new KnowledgeBase();
        this.scannedObjects = new ArrayList<>();
    }

    /**
     * Gets the current section where the robot is located
     *
     * @param territory the territory to get the section from
     * @return the current section
     */
    public Section getCurrentSection(final Territory territory) {
        return territory.getSections()[position.getX()][position.getY()];
    }
}
