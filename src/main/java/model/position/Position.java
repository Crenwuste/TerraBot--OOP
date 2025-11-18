package model.position;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a position on the territory grid
 */
@Data
@AllArgsConstructor
public final class Position {
    private int x;
    private int y;
}
