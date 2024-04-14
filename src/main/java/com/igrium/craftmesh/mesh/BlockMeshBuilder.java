package com.igrium.craftmesh.mesh;

import com.igrium.meshlib.ConcurrentMesh;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class BlockMeshBuilder {

    public static ConcurrentMesh build(BlockPos minPos, BlockPos maxPos, BlockRenderView world) {
        MeshVertexConsumer mesh = new MeshVertexConsumer(new ConcurrentMesh());
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

            if (state.getRenderType() == BlockRenderType.INVISIBLE) continue;

            RenderLayer renderLayer = RenderLayers.getBlockLayer(state);

            matrixStack.push();
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlock(state, pos, world, matrixStack, mesh, true, random);
            matrixStack.pop();
        }
        return mesh.mesh;
    }

    // public static Map<RenderLayer, Obj> build(BlockPos minPos, BlockPos maxPos, BlockRenderView world, Function<RenderLayer, Obj> objFactory) {
    //     Map<RenderLayer, ObjVertexConsumer> layers = new HashMap<>();
    //     Random random = Random.create();
    //     BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
    //     MatrixStack matrixStack = new MatrixStack();

    //     for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
    //         BlockState state = world.getBlockState(pos);
    //         FluidState fluidState = state.getFluidState();

    //         if (!fluidState.isEmpty()) {
    //             RenderLayer fluidLayer = RenderLayers.getFluidLayer(fluidState);
    //             ObjVertexConsumer obj = layers.computeIfAbsent(fluidLayer, l -> new ObjVertexConsumer(objFactory.apply(l)));

    //             obj.matrices.push();
    //             obj.matrices.translate(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
    //             blockRenderManager.renderFluid(pos, world, obj, state, fluidState);
    //             obj.matrices.pop();
    //         }

    //         if (state.getRenderType() == BlockRenderType.INVISIBLE) continue;
    //         RenderLayer renderLayer = RenderLayers.getBlockLayer(state);
    //         ObjVertexConsumer obj = layers.computeIfAbsent(renderLayer, l -> new ObjVertexConsumer(objFactory.apply(l)));

    //         matrixStack.push();
    //         matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
    //         blockRenderManager.renderBlock(state, pos, world, matrixStack, obj, true, random);
    //         matrixStack.pop();
    //     }

    //     return ImmutableMap.copyOf(Maps.transformValues(layers, obj -> obj.baseObj));
    // }
    
    // public static Obj build(BlockPos minPos, BlockPos maxPos, BlockRenderView world, Obj dest) {
    //     ObjVertexConsumer vertexConsumer = new ObjVertexConsumer(dest);
    //     Random random = Random.create();
    //     BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
    //     MatrixStack matrixStack = new MatrixStack();

    //     for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
    //         BlockState blockState = world.getBlockState(pos);
    //         FluidState fluidState = blockState.getFluidState();


    //         matrixStack.push();
    //         matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
    //         blockRenderManager.renderBlock(blockState, pos, world, matrixStack, vertexConsumer, true, random);
    //         matrixStack.pop();
    //     }

    //     return dest;
    // }

    // public static void testExport(BlockPos minPos, BlockPos maxPos, BlockRenderView world) throws Exception {
    //     Obj obj = build(minPos, maxPos, world, Objs.create());
    //     MinecraftClient client = MinecraftClient.getInstance();
    //     Path targetFile = client.runDirectory.toPath().resolve("testexport.obj");

    //     try (BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
    //         ObjWriter.write(obj, writer);
    //     }
    // }
}
