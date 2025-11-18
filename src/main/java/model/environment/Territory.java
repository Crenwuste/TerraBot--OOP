package model.environment;

import lombok.Getter;

/**
 * Represents the entire territory explored by TerraBot.
 */
@Getter
public class Territory {

    /**
     * Number of columns of the territory grid.
     */
    private final int width;

    /**
     * Number of rows of the territory grid.
     */
    private final int height;

    /**
     * Two-dimensional grid of sections.
     * Indexing convention: sections[x][y].
     */
    private final Section[][] sections;

    /**
     * Creates a territory with the given dimensions.
     *
     * @param width  number of columns
     * @param height number of rows
     */
    public Territory(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.sections = new Section[height][width];
    }
}
