package com.igrium.craftmesh.test;

import com.igrium.craftmesh.CraftMesh;
import com.igrium.craftmesh.mesh.SimpleChunkBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class CraftMeshCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
    
        dispatcher.register(literal("craftmesh").then(
            literal("test").then(
                argument("radius", IntegerArgumentType.integer(0)).executes(CraftMeshCommand::doTest)
            )
        ));
    }

    private static int doTest(CommandContext<FabricClientCommandSource> context) {
        int radius = IntegerArgumentType.getInteger(context, "radius");
        BlockPos center = context.getSource().getEntity().getBlockPos();
        ClientWorld world = context.getSource().getWorld();
        
        BlockPos minPos = center.add(-radius, 0, -radius).withY(world.getBottomY());
        BlockPos maxPos = center.add(radius, 0, radius).withY(world.getTopY());

        try {
            SimpleChunkBuilder.testExport(minPos, maxPos, world);
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error exporting world. See console for details."));
            CraftMesh.LOGGER.error("Error exporting world.", e);
        }

        context.getSource().sendFeedback(Text.literal("Exported world."));

        return 1;
    }
}
