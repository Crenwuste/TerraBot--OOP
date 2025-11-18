package model.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base contract for all environment entities.
 */
public interface EnvironmentEntity {
    ObjectNode getEntities(ObjectMapper mapper);
}


