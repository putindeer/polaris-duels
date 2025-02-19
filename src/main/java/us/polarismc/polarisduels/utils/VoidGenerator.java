package us.polarismc.polarisduels.utils;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.block.data.BlockData;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class VoidGenerator extends ChunkGenerator {
    @Override
    public void generateSurface(@NotNull WorldInfo info, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData data) {
        if (chunkX == 0 && chunkZ == 0) {
            BlockData glass = Material.GLASS.createBlockData();
            data.setBlock(0, 64, 0, glass);
        }
    }
}

