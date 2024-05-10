package com.igrium.craftmesh.command;

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
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class CraftMeshCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
    
        // dispatcher.register(literal("craftmesh").then(
        //     literal("export").then(
        //         argument("from", ClientBlockPosArgument.blockPos()).then(
        //             argument("to", ClientBlockPosArgument.blockPos()).then(
        //                 argument("origin", ClientBlockPosArgument.blockPos()).then(
        //                     argument("name", StringArgumentType.string()).executes(CraftMeshCommand::exportBounds)
        //                 )
        //             )
        //         )
        //     )
        // ));

        dispatcher.register(literal("craftmesh").then(
            literal("export").then(
                argument("name", StringArgumentType.string()).then(
                    literal("bounds").then(
                        argument("from", ClientBlockPosArgument.blockPos()).then(
                            argument("to", ClientBlockPosArgument.blockPos()).then(
                                argument("origin", ClientBlockPosArgument.blockPos()).executes(CraftMeshCommand::exportBounds)
                            )
                        )
                    )
                ).then(
                    literal("radius").then(
                        argument("radius", IntegerArgumentType.integer(0)).executes(CraftMeshCommand::exportRadius).then(
                            argument("origin", ClientBlockPosArgument.blockPos()).executes(CraftMeshCommand::exportRadiusOrigin).then(
                                argument("center", ClientBlockPosArgument.blockPos()).executes(CraftMeshCommand::exportRadiusOriginCenter)
                            )
                        )
                    )
                )
            )
        ));
    }

    private static int exportBounds(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        BlockPos from = ClientBlockPosArgument.getBlockPos(context, "from");
        BlockPos to = ClientBlockPosArgument.getBlockPos(context, "to");
        BlockPos origin = ClientBlockPosArgument.getBlockPos(context, "origin");

        BlockPos minPos = new BlockPos(Math.min(from.getX(), to.getZ()),
                Math.min(from.getY(), to.getY()),
                Math.min(from.getZ(), to.getZ()));

        BlockPos maxPos = new BlockPos(Math.max(from.getX(), to.getZ()),
                Math.max(from.getY(), to.getY()),
                Math.max(from.getZ(), to.getZ()));

        return export(context, minPos, maxPos, origin);

    }

    public static int exportRadius(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        BlockPos origin = BlockPos.ofFloored(context.getSource().getPosition());
        origin = origin.withY(0);
        return exportRadius(context, origin, origin);
    }

    public static int exportRadiusOrigin(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        BlockPos origin = ClientBlockPosArgument.getBlockPos(context, "origin");
        return exportRadius(context, origin, origin);
    }

    public static int exportRadiusOriginCenter(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        BlockPos origin = ClientBlockPosArgument.getBlockPos(context, "origin");
        BlockPos center = ClientBlockPosArgument.getBlockPos(context, "center");
        return exportRadius(context, origin, center);
    }

    private static int exportRadius(CommandContext<FabricClientCommandSource> context, BlockPos origin, BlockPos center) throws CommandSyntaxException {
        int radius = IntegerArgumentType.getInteger(context, "radius");
        World world = context.getSource().getWorld();

        BlockPos minPos = new BlockPos(center.getX() - radius, world.getBottomY(), center.getZ() - radius);
        BlockPos maxPos = new BlockPos(center.getX() + radius, world.getTopY(), center.getZ() + radius);

        return export(context, minPos, maxPos, origin);
    }

    private static int export(CommandContext<FabricClientCommandSource> context, BlockPos minPos, BlockPos maxPos, BlockPos origin) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        context.getSource().sendFeedback(Text.translatable("commands.craftmesh.bounds", vec2String(minPos), vec2String(maxPos)));

        long startTime = Util.getMeasuringTimeMs();
        CraftMesh.export(context.getSource().getWorld(), minPos, maxPos, origin, name, context.getSource()::sendFeedback)
                .thenRun(() -> {
                    long time = Util.getMeasuringTimeMs() - startTime;
                    context.getSource().sendFeedback(Text.translatable("commands.craftmesh.success", time / 1000f));
                }).exceptionally(e -> {
                    context.getSource().sendError(Text.translatable("commands.craftmesh.error"));
                    CraftMesh.LOGGER.error("Error exporting mesh.", e);

                    return null;
                });

        return 1;
    }

    private static String vec2String(Vec3i vec) {
        return String.format("[%d, %d, %d]", vec.getX(), vec.getY(), vec.getZ());
    }
}
