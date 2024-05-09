package com.igrium.craftmesh.mat;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import de.javagl.obj.Mtl;
import de.javagl.obj.Mtls;

public class MeshMaterials {
    public static final String WORLD = "world";
    public static final String WORLD_TRANS = "world_trans";
    public static final String WORLD_TINTED = "world_tinted";
    public static final String WORLD_TRANS_TINTED = "world_trans_tinted";

    public static String getMaterialName(boolean transparent, boolean tinted) {
        if (transparent && !tinted) {
            return WORLD_TRANS;
        } else if (!transparent && tinted) {
            return WORLD_TINTED;
        } else if (transparent && tinted) {
            return WORLD_TRANS_TINTED;
        } else {
            return WORLD;
        }
    }

    public static Collection<Mtl> createMtls(String worldTexture) {
        Mtl world = Mtls.create(WORLD);
        world.setMapKd(worldTexture);

        Mtl worldTrans = Mtls.create(WORLD_TRANS);
        worldTrans.setMapKd(worldTexture);
        worldTrans.setMapD(worldTexture);
        worldTrans.getMapDOptions().setImfchan("m");

        return ImmutableList.of(world, worldTrans);
    }
}
