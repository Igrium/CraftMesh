package com.igrium.craftmesh.mesh;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjWriter;
import de.javagl.obj.Objs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class SimpleChunkBuilder {
    public static Obj buildChunk(BlockPos minPos, BlockPos maxPos, BlockRenderView world) {
        Obj obj = Objs.create();
        ObjVertexConsumer vertexConsumer = new ObjVertexConsumer(obj);
        Random random = Random.create();
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        MatrixStack matrixStack = new MatrixStack();

        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            matrixStack.push();
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlock(world.getBlockState(pos), pos, world, matrixStack, vertexConsumer, true, random);
            matrixStack.pop();
        }

        return obj;
    }

    public static void testExport(BlockPos minPos, BlockPos maxPos, BlockRenderView world) throws Exception {
        Obj obj = buildChunk(minPos, maxPos, world);
        MinecraftClient client = MinecraftClient.getInstance();
        Path targetFile = client.runDirectory.toPath().resolve("testexport.obj");

        try (BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
            ObjWriter.write(obj, writer);
        }
    }
}
