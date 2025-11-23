package model.robot;

import lombok.Getter;

/**
 * Represents movement directions for TerraBot
 */
@Getter
public enum Direction {
    UP(0, 1),
    RIGHT(1, 0),
    DOWN(0, -1),
    LEFT(-1, 0);

    private final int deltaX;
    private final int deltaY;

    Direction(final int deltaX, final int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    /**
     * Calculates the new x coordinate after moving in this direction
     *
     * @param currentX the current x coordinate
     * @return the new x coordinate
     */
    public int getNewX(final int currentX) {
        return currentX + deltaX;
    }

    /**
     * Calculates the new y coordinate after moving in this direction
     *
     * @param currentY the current y coordinate
     * @return the new y coordinate
     */
    public int getNewY(final int currentY) {
        return currentY + deltaY;
    }
}
