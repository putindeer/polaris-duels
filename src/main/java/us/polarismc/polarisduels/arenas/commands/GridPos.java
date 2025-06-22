package us.polarismc.polarisduels.arenas.commands;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a position in a grid system used for arena placement in the world.
 * Each grid position corresponds to a 1000x1000 block area in a specific world.
 * This class provides utility methods to convert between grid coordinates and
 * world coordinates, making it easier to manage arena placement and organization.
 * 
 * <p>The grid system is used to prevent arena overlap and ensure proper spacing
 * between different arena instances in the world.</p>
 * 
 * @param world The Minecraft world this grid position belongs to
 * @param x The x-coordinate in the grid (not world coordinates)
 * @param z The z-coordinate in the grid (not world coordinates)
 */
public record GridPos(World world, int x, int z) {
    /**
     * Calculates the world X-coordinate for the center of this grid cell.
     * 
     * @return The X-coordinate of the center point of this grid cell in world coordinates
     */
    public double getCenterWorldX() {
        return x * 1000.0 + 500.0;
    }

    /**
     * Calculates the world Z-coordinate for the center of this grid cell.
     * 
     * @return The Z-coordinate of the center point of this grid cell in world coordinates
     */
    public double getCenterWorldZ() {
        return z * 1000.0 + 500.0;
    }

    /**
     * Gets the minimum X-coordinate of this grid cell in world coordinates.
     * 
     * @return The minimum X-coordinate of this grid cell
     */
    public int getMinWorldX() {
        return x * 1000;
    }

    /**
     * Gets the minimum Z-coordinate of this grid cell in world coordinates.
     * 
     * @return The minimum Z-coordinate of this grid cell
     */
    public int getMinWorldZ() {
        return z * 1000;
    }

    /**
     * Gets the maximum X-coordinate of this grid cell in world coordinates.
     * 
     * @return The maximum X-coordinate of this grid cell
     */
    public int getMaxWorldX() {
        return x * 1000 + 999;
    }

    /**
     * Gets the maximum Z-coordinate of this grid cell in world coordinates.
     * 
     * @return The maximum Z-coordinate of this grid cell
     */
    public int getMaxWorldZ() {
        return z * 1000 + 999;
    }

    public boolean equals(GridPos gridPos) {
        return x == gridPos.x && z == gridPos.z && world.equals(gridPos.world());
    }
    
    @Override
    public @NotNull String toString() {
        return String.format("GridPos[world=%s, x=%d, z=%d] (World Coords: %d,%d to %d,%d)",
                world.getName(), x, z, getMinWorldX(), getMinWorldZ(), getMaxWorldX(), getMaxWorldZ());
    }
}