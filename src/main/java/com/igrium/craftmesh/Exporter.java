package com.igrium.craftmesh;

import java.util.Collections;
import java.util.Map;

import com.igrium.craftmesh.mesh.BlockMeshBuilder;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjUtils;
import de.javagl.obj.Objs;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.BlockRenderView;

public class Exporter {
    private final ExportConfig config = new ExportConfig();

    public final ExportConfig getConfig() {
        return config;
    }
    
    public Obj exportMesh(BlockRenderView world) {
        Map<RenderLayer, Obj> layers = BlockMeshBuilder.build(config.getMinPos(), config.getMaxPos(), world, l -> {
            Obj obj = Objs.create();
            obj.setActiveGroupNames(Collections.singleton(l.toString()));
            return obj;
        });
        Obj mesh = Objs.create();

        for (Obj obj : layers.values()) {
            ObjUtils.add(obj, mesh);
        }

        return mesh;
    }
}
