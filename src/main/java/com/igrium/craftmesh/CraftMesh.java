package com.igrium.craftmesh;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igrium.craftmesh.mat.TextureExtractor;
import com.igrium.craftmesh.test.CraftMeshCommand;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjWriter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class CraftMesh implements ClientModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("craftmesh");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(CraftMeshCommand::register);
    }
    
    public static Path getExportDir(MinecraftClient client) {
        return client.runDirectory.toPath().resolve("craftmesh");
    }

    public static String export(BlockRenderView world, BlockPos minPos, BlockPos maxPos, String name) throws Exception {
        // Path exportDir = getExportDir(MinecraftClient.getInstance());
        // Files.createDirectories(exportDir);
        // Path target = exportDir.resolve(name);
        Path target = getExportDir(MinecraftClient.getInstance()).resolve(name);
        Files.createDirectories(target);

        Path objFile = target.resolve("world.obj");

        Exporter exporter = new Exporter();
        exporter.getConfig().setMinPos(minPos).setMaxPos(maxPos);

        Obj mesh = exporter.exportMesh(world);

        try(BufferedWriter writer = Files.newBufferedWriter(objFile)) {
            ObjWriter.write(mesh, writer);
        }

        NativeImage blockAtlasTexture = TextureExtractor.getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        blockAtlasTexture.writeTo(target.resolve("world.png"));

        return target.toString();
    }
}