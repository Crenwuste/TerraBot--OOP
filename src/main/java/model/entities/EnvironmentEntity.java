package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base interface for every entity that TerraBot can encounter.
 * Any environment entity must be able to provide a JSON representation for output and
 * report the damage it can inflict on the robot.
 */
public interface EnvironmentEntity {
    /**
     * Builds a JSON representation of the entity so it can be printed in the outputs
     *
     * @param mapper jackson mapper used to create JSON nodes
     * @return node containing the entity data
     */
    ObjectNode getEntities(ObjectMapper mapper);

    /**
     * Calculates the probability that the robot will be blocked by entities
     *
     * @return blocking probability
     */
    double calculateBlockingProbability();
}
