package com.igrium.craftmesh.mat;

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
}
