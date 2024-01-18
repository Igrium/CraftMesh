package com.igrium.craftmesh;

import java.util.Objects;

import net.minecraft.util.math.BlockPos;

public class ExportConfig {
    private BlockPos minPos = BlockPos.ORIGIN;

    public BlockPos getMinPos() {
        return minPos;
    }

    public ExportConfig setMinPos(BlockPos minPos) {
        this.minPos = Objects.requireNonNull(minPos);
        return this;
    }

    private BlockPos maxPos = BlockPos.ORIGIN;

    public BlockPos getMaxPos() {
        return maxPos;
    }

    public ExportConfig setMaxPos(BlockPos maxPos) {
        this.maxPos = Objects.requireNonNull(maxPos);
        return this;
    }

    /**
     * Copy the values from another export config into this.
     * @param other Config to copy from.
     * @return <code>this</code>
     */
    public ExportConfig copy(ExportConfig other) {
        this.minPos = other.minPos;
        this.maxPos = other.maxPos;
        return this;
    }
}
