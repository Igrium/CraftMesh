package com.igrium.craftmesh;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igrium.craftmesh.test.CraftMeshCommand;

public class CraftMesh implements ClientModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("craftmesh");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(CraftMeshCommand::register);
    }
    
}