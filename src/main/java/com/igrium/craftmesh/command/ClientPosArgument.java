package com.igrium.craftmesh.command;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface ClientPosArgument {
    public Vec3d toAbsolutePos(FabricClientCommandSource source);

    public Vec2f toAbsoluteRotation(FabricClientCommandSource source);

    public default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
        return BlockPos.ofFloored(toAbsolutePos(source));
    }

    public boolean isXRelative();

    public boolean isYRelative();

    public boolean isZRelative();
}
