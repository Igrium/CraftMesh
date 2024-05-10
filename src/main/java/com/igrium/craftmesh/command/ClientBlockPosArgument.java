package com.igrium.craftmesh.command;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.CommandSource.RelativePosition;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;

public class ClientBlockPosArgument implements ArgumentType<ClientPosArgument> {

    public static ClientBlockPosArgument blockPos() {
        return new ClientBlockPosArgument();
    }

    public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, ClientPosArgument.class).toAbsoluteBlockPos(context.getSource());
    }
    
    @Override
    public ClientPosArgument parse(StringReader reader) throws CommandSyntaxException {
        return DefaultClientPosArgument.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSource source) {
            String remaining = builder.getRemaining();
            Collection<RelativePosition> positions = source.getBlockPositionSuggestions();
            return CommandSource.suggestPositions(remaining, positions, builder, CommandManager.getCommandValidator(this::parse));
        }
        return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return DefaultClientPosArgument.EXAMPLES;
    }
}
