package com.igrium.craftmesh.test;

import com.igrium.craftmesh.CraftMesh;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class CraftMeshCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
    
        dispatcher.register(literal("craftmesh").then(
            literal("export").then(
                argument("radius", IntegerArgumentType.integer(0)).then(
                    argument("name", StringArgumentType.string()).executes(CraftMeshCommand::export)
                )
            )
        ));
    }

    private static int export(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        int radius = IntegerArgumentType.getInteger(context, "radius");
        String name = StringArgumentType.getString(context, "name");

        BlockPos center = context.getSource().getEntity().getBlockPos();
        BlockPos minPos = center.add(-radius, -radius, -radius);
        BlockPos maxPos = center.add(radius, radius, radius);

        long startTime = Util.getMeasuringTimeMs();

        CraftMesh.export(context.getSource().getWorld(), minPos, maxPos, name, context.getSource()::sendFeedback)
                .thenRun(() -> {
                    long time = Util.getMeasuringTimeMs() - startTime;
                    context.getSource().sendFeedback(Text.literal("Exported mesh in %.2f seconds.".formatted(time / 1000f)));
                }).exceptionally(e -> {
                    context.getSource().sendError(Text.literal("Error exporting mesh. See console for details."));

                    CraftMesh.LOGGER.error("Error exporting mesh.", e);

                    return null;
                });

        return 1;
    }
}
