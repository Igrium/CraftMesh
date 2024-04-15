package com.igrium.craftmesh.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.igrium.meshlib.AbstractConcurrentMesh;
import com.igrium.meshlib.OverlapCheckingMesh;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class BlockMeshBuilder {

    public static AbstractConcurrentMesh build(BlockPos minPos, BlockPos maxPos, BlockRenderView world) {
        MeshVertexConsumer mesh = new MeshVertexConsumer(new OverlapCheckingMesh());
        mesh.setNormalEnabled(false);

        Random random = Random.create();
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        MatrixStack matrixStack = new MatrixStack();

        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            BlockState state = world.getBlockState(pos);
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                RenderLayer fluidLayer = RenderLayers.getFluidLayer(fluidState);

                mesh.matrices.push();
                mesh.matrices.translate(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
                blockRenderManager.renderFluid(pos, world, mesh, state, fluidState);
                mesh.matrices.pop();
            }

            if (state.getRenderType() == BlockRenderType.INVISIBLE)
                continue;

            RenderLayer renderLayer = RenderLayers.getBlockLayer(state);

            matrixStack.push();
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlock(state, pos, world, matrixStack, mesh, true, random);
            matrixStack.pop();
        }
        return mesh.mesh;
    }

    public static void build(BlockPos minPos, BlockPos maxPos, AbstractConcurrentMesh mesh, BlockRenderView world,
            Random random) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        MatrixStack matrixStack = new MatrixStack();

        MeshVertexConsumer vertexConsumer = new MeshVertexConsumer(mesh);
        vertexConsumer.setNormalEnabled(false);

        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            BlockState state = world.getBlockState(pos);
            FluidState fluidState = state.getFluidState();

            if (!fluidState.isEmpty()) {
                vertexConsumer.matrices.push();
                vertexConsumer.matrices.translate(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
                blockRenderManager.renderFluid(pos, world, vertexConsumer, state, fluidState);
                vertexConsumer.matrices.pop();
            }

            if (state.getRenderType() == BlockRenderType.INVISIBLE)
                continue;

            matrixStack.push();
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlock(state, pos, world, matrixStack, vertexConsumer, true, random);
            matrixStack.pop();
        }
    }

    public static <T extends AbstractConcurrentMesh> CompletableFuture<T> buildThreaded(T targetMesh, BlockPos minPos,
            BlockPos maxPos, BlockRenderView world, Executor threadExecutor) {

        ChunkSectionPos minChunk = ChunkSectionPos.from(minPos);
        ChunkSectionPos maxChunk = ChunkSectionPos.from(maxPos);

        Vec3i size = maxChunk.subtract(minChunk);
        List<CompletableFuture<?>> futures = new ArrayList<>(size.getX() * size.getY() * size.getZ());

        Random random = Random.createLocal();

        for (int y = minChunk.getY(); y <= maxChunk.getY(); y++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
                    ChunkSectionPos chunkPos = ChunkSectionPos.from(x, y, z);

                    futures.add(CompletableFuture.runAsync(() -> {
                        int minX = Math.max(minPos.getX(), chunkPos.getMinX());
                        int minY = Math.max(minPos.getY(), chunkPos.getMinY());
                        int minZ = Math.max(minPos.getZ(), chunkPos.getMinZ());

                        int maxX = Math.min(maxPos.getX(), chunkPos.getMaxX());
                        int maxY = Math.min(maxPos.getY(), chunkPos.getMaxY());
                        int maxZ = Math.min(maxPos.getZ(), chunkPos.getMaxZ());

                        build(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ),
                                targetMesh, world, random);

                    }, threadExecutor));
                }
            }
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new)).thenApply(v -> targetMesh);
    }

    // public static Map<RenderLayer, Obj> build(BlockPos minPos, BlockPos maxPos,
    // BlockRenderView world, Function<RenderLayer, Obj> objFactory) {
    // Map<RenderLayer, ObjVertexConsumer> layers = new HashMap<>();
    // Random random = Random.create();
    // BlockRenderManager blockRenderManager =
    // MinecraftClient.getInstance().getBlockRenderManager();
    // MatrixStack matrixStack = new MatrixStack();

    // for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
    // BlockState state = world.getBlockState(pos);
    // FluidState fluidState = state.getFluidState();

    // if (!fluidState.isEmpty()) {
    // RenderLayer fluidLayer = RenderLayers.getFluidLayer(fluidState);
    // ObjVertexConsumer obj = layers.computeIfAbsent(fluidLayer, l -> new
    // ObjVertexConsumer(objFactory.apply(l)));

    // obj.matrices.push();
    // obj.matrices.translate(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ()
    // >> 4 << 4);
    // blockRenderManager.renderFluid(pos, world, obj, state, fluidState);
    // obj.matrices.pop();
    // }

    // if (state.getRenderType() == BlockRenderType.INVISIBLE) continue;
    // RenderLayer renderLayer = RenderLayers.getBlockLayer(state);
    // ObjVertexConsumer obj = layers.computeIfAbsent(renderLayer, l -> new
    // ObjVertexConsumer(objFactory.apply(l)));

    // matrixStack.push();
    // matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
    // blockRenderManager.renderBlock(state, pos, world, matrixStack, obj, true,
    // random);
    // matrixStack.pop();
    // }

    // return ImmutableMap.copyOf(Maps.transformValues(layers, obj -> obj.baseObj));
    // }

    // public static Obj build(BlockPos minPos, BlockPos maxPos, BlockRenderView
    // world, Obj dest) {
    // ObjVertexConsumer vertexConsumer = new ObjVertexConsumer(dest);
    // Random random = Random.create();
    // BlockRenderManager blockRenderManager =
    // MinecraftClient.getInstance().getBlockRenderManager();
    // MatrixStack matrixStack = new MatrixStack();

    // for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
    // BlockState blockState = world.getBlockState(pos);
    // FluidState fluidState = blockState.getFluidState();

    // matrixStack.push();
    // matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
    // blockRenderManager.renderBlock(blockState, pos, world, matrixStack,
    // vertexConsumer, true, random);
    // matrixStack.pop();
    // }

    // return dest;
    // }

    // public static void testExport(BlockPos minPos, BlockPos maxPos,
    // BlockRenderView world) throws Exception {
    // Obj obj = build(minPos, maxPos, world, Objs.create());
    // MinecraftClient client = MinecraftClient.getInstance();
    // Path targetFile = client.runDirectory.toPath().resolve("testexport.obj");

    // try (BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
    // ObjWriter.write(obj, writer);
    // }
    // }
}
