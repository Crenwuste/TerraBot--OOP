package model.robot;

import lombok.Data;
import model.position.Position;

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
     * Creates a robot with the given starting position and energy
     *
     * @param position     initial position
     * @param energyPoints initial energy points
     */
    public TerraBot(final Position position, final int energyPoints) {
        this.position = position;
        this.energyPoints = energyPoints;
        this.knowledgeBase = new KnowledgeBase();
    }
}
