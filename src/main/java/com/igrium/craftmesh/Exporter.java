package com.igrium.craftmesh;

import com.igrium.craftmesh.mesh.BlockMeshBuilder;

import de.javagl.obj.Obj;
import net.minecraft.world.BlockRenderView;

public class Exporter {
    private final ExportConfig config = new ExportConfig();

    public final ExportConfig getConfig() {
        return config;
    }
    
    public Obj exportMesh(BlockRenderView world) {
        // Map<RenderLayer, Obj> layers = BlockMeshBuilder.build(config.getMinPos(), config.getMaxPos(), world, l -> {
        //     Obj obj = Objs.create();
        //     obj.setActiveGroupNames(Collections.singleton(l.toString()));
        //     return obj;
        // });
        // Obj mesh = Objs.create();

        // for (Obj obj : layers.values()) {
        //     ObjUtils.add(obj, mesh);
        // }

        var mesh = BlockMeshBuilder.build(config.getMinPos(), config.getMaxPos(), world);
        

        return mesh.toObj();
    }
}
