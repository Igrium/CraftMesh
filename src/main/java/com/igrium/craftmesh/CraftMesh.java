package com.igrium.craftmesh;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igrium.craftmesh.mat.MeshMaterials;
import com.igrium.craftmesh.mat.TextureExtractor;
import com.igrium.craftmesh.mesh.BlockMeshBuilder;
import com.igrium.craftmesh.test.CraftMeshCommand;
import com.igrium.meshlib.ConcurrentMeshBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

import de.javagl.obj.MtlWriter;
import de.javagl.obj.ObjWriter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class CraftMesh implements ClientModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("craftmesh");

    private static final int NUM_THREADS = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(CraftMeshCommand::register);
    }
    
    public static Path getExportDir(MinecraftClient client) {
        return client.runDirectory.toPath().resolve("craftmesh");
    }

    public static CompletableFuture<?> export(BlockRenderView world, BlockPos minPos, BlockPos maxPos, BlockPos offset, String name, Consumer<Text> feedbackConsumer) {
        try {
            Path exportDir = getExportDir(MinecraftClient.getInstance());

            Path target = exportDir.resolve(name);
            Files.createDirectories(target);

            CompletableFuture<?>[] futures = new CompletableFuture[2];

            // var worldCompileExecutor = WORLD_COMPILE_EXECUTORS.getHandle();

            feedbackConsumer.accept(Text.translatable("misc.craftmesh.world"));
            ConcurrentMeshBuilder mesh = ConcurrentMeshBuilder.create(true);
            mesh.setPrioritizeNewFaces(false);
            
            futures[0] = BlockMeshBuilder.buildThreaded(mesh, minPos, maxPos, offset, world, true, Util.getMainWorkerExecutor(), NUM_THREADS)
                    .thenApplyAsync(m -> {
                        // worldCompileExecutor.close();
                        feedbackConsumer.accept(Text.translatable("misc.craftmesh.mesh"));
                        return mesh.toObj(true);
                    }, Util.getMainWorkerExecutor()).thenAcceptAsync(obj -> {
                        feedbackConsumer.accept(Text.translatable("misc.craftmesh.save"));

                        obj.setMtlFileNames(Collections.singleton("world.mtl"));

                        try(BufferedWriter writer = Files.newBufferedWriter(target.resolve("world.mtl"))) {
                            MtlWriter.write(MeshMaterials.createMtls("world.png"), writer);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }

                        try (BufferedWriter writer = Files.newBufferedWriter(target.resolve("world.obj"))) {
                            ObjWriter.write(obj, writer);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }, Util.getIoWorkerExecutor());

            // Extract mesh on render thread but save it on worker thread
            futures[1] = CompletableFuture.supplyAsync(() -> {
                return TextureExtractor.getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

            }, CraftMesh::executeOnRenderThread).thenAcceptAsync(image -> {

                try {
                    image.writeTo(target.resolve("world.png"));
                } catch (IOException e) {
                    throw new CompletionException(e);
                }

            }, Util.getIoWorkerExecutor());

            return CompletableFuture.allOf(futures);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static void executeOnRenderThread(Runnable r) {
        if (RenderSystem.isOnRenderThread()) {
            r.run();
        } else {
            RenderSystem.recordRenderCall(r::run);
        }
    }
}