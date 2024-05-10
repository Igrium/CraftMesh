package com.igrium.craftmesh.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.igrium.craftmesh.mat.MeshMaterials;
import com.igrium.craftmesh.util.FutureUtils;
import com.igrium.meshlib.ConcurrentMeshBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class BlockMeshBuilder {

    public static ConcurrentMeshBuilder build(BlockPos minPos, BlockPos maxPos, BlockRenderView world, boolean splitBlocks) {
        ConcurrentMeshBuilder mesh = ConcurrentMeshBuilder.create();
        mesh.setPrioritizeNewFaces(false);
        Random random = Random.create();
        build(mesh, minPos, maxPos, world, splitBlocks, b -> MeshMaterials.getMaterialName(!b.isOpaque(), splitBlocks), random);
        return mesh;
    }

    public static void build(ConcurrentMeshBuilder targetMesh, BlockPos minPos, BlockPos maxPos, BlockRenderView world,
            boolean splitBlocks, Function<BlockState, String> materialFactory, Random random) {
        
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        MatrixStack matrixStack = new MatrixStack();

        MeshVertexConsumer vertexConsumer = new MeshVertexConsumer(targetMesh);
        vertexConsumer.setNormalEnabled(false);

        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            BlockState state = world.getBlockState(pos);
            FluidState fluidState = state.getFluidState();

            boolean transparent = !state.isOpaque();
            vertexConsumer.setMaterial(MeshMaterials.getMaterialName(transparent, false));

            if (!fluidState.isEmpty()) {
                vertexConsumer.matrices.push();
                vertexConsumer.matrices.translate(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
                blockRenderManager.renderFluid(pos, world, vertexConsumer, state, fluidState);
                vertexConsumer.matrices.pop();
            }

            if (state.getRenderType() == BlockRenderType.INVISIBLE)
                continue;

            if (splitBlocks) {
                Identifier id = Registries.BLOCK.getId(state.getBlock());
                vertexConsumer.setActiveGroup(id.toString());
            }

            matrixStack.push();
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlock(state, pos, world, matrixStack, vertexConsumer, true, random);
            // renderBlock(state, pos, world, matrixStack, vertexConsumer, true, blockRenderManager, random);
            matrixStack.pop();
        }
    }

    public static CompletableFuture<ConcurrentMeshBuilder> buildThreaded(ConcurrentMeshBuilder targetMesh,
            BlockPos minPos, BlockPos maxPos, BlockRenderView world, boolean splitBlocks,
            Function<BlockState, String> materialFactory, Executor threadExecutor, int maxThreads) {
        
        ChunkSectionPos minChunk = ChunkSectionPos.from(minPos);
        ChunkSectionPos maxChunk = ChunkSectionPos.from(maxPos);

        Vec3i size = maxChunk.subtract(minChunk);
        
        List<Runnable> runnables = new ArrayList<>(size.getX() * size.getY() * size.getZ());

        ThreadLocal<Random> randoms = new ThreadLocal<>() {
            protected Random initialValue() {
                return Random.createLocal();
            };
        };

        for (int y = minChunk.getY(); y <= maxChunk.getY(); y++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
                    ChunkSectionPos chunkPos = ChunkSectionPos.from(x, y, z);

                    runnables.add(() -> {
                        int minX = Math.max(minPos.getX(), chunkPos.getMinX());
                        int minY = Math.max(minPos.getY(), chunkPos.getMinY());
                        int minZ = Math.max(minPos.getZ(), chunkPos.getMinZ());

                        int maxX = Math.min(maxPos.getX(), chunkPos.getMaxX());
                        int maxY = Math.min(maxPos.getY(), chunkPos.getMaxY());
                        int maxZ = Math.min(maxPos.getZ(), chunkPos.getMaxZ());

                        build(targetMesh, new BlockPos(minX, minY, minZ),
                                new BlockPos(maxX, maxY, maxZ), world, splitBlocks, materialFactory, randoms.get());

                    });
                }
            }
        }

        return CompletableFuture.allOf(FutureUtils.runAllAsync(runnables, threadExecutor, maxThreads)).thenApply(v -> targetMesh);

        // ChunkSectionPos minChunk = ChunkSectionPos.from(minPos);
        // ChunkSectionPos maxChunk = ChunkSectionPos.from(maxPos);

        // Vec3i size = maxChunk.subtract(minChunk);
        // List<CompletableFuture<?>> futures = new ArrayList<>(size.getX() * size.getY() * size.getZ());

        // Random random = Random.createLocal();

        // for (int y = minChunk.getY(); y <= maxChunk.getY(); y++) {
        //     for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
        //         for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
        //             ChunkSectionPos chunkPos = ChunkSectionPos.from(x, y, z);

        //             futures.add(CompletableFuture.runAsync(() -> {
        //                 int minX = Math.max(minPos.getX(), chunkPos.getMinX());
        //                 int minY = Math.max(minPos.getY(), chunkPos.getMinY());
        //                 int minZ = Math.max(minPos.getZ(), chunkPos.getMinZ());

        //                 int maxX = Math.min(maxPos.getX(), chunkPos.getMaxX());
        //                 int maxY = Math.min(maxPos.getY(), chunkPos.getMaxY());
        //                 int maxZ = Math.min(maxPos.getZ(), chunkPos.getMaxZ());

        //                 build(targetMesh, new BlockPos(minX, minY, minZ),
        //                         new BlockPos(maxX, maxY, maxZ), world, splitBlocks, materialFactory, random);

        //             }, threadExecutor));
        //         }
        //     }
        // }

        // return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new)).thenApply(v -> targetMesh);
    }
    
    public static CompletableFuture<ConcurrentMeshBuilder> buildThreaded(ConcurrentMeshBuilder targetMesh,
            BlockPos minPos, BlockPos maxPos, BlockRenderView world, boolean splitBlocks, Executor threadExecutor, int maxThreads) {
                
        return buildThreaded(targetMesh, minPos, maxPos, world, splitBlocks,
                b -> MeshMaterials.getMaterialName(!b.isOpaque(), false), threadExecutor, maxThreads);
    }
}
