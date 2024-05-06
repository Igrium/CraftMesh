package com.igrium.craftmesh.mesh;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector3f;

import com.igrium.meshlib.ConcurrentMeshBuilder;
import com.igrium.meshlib.FaceBuilder;
import com.igrium.meshlib.Vertex;
import com.igrium.meshlib.math.Vector2;
import com.igrium.meshlib.math.Vector3;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;

public final class MeshVertexConsumer implements VertexConsumer {
    public final ConcurrentMeshBuilder mesh;
    private Vector3f vec = new Vector3f();

    public MeshVertexConsumer(ConcurrentMeshBuilder mesh) {
        this.mesh = mesh;
    }

    public final MatrixStack matrices = new MatrixStack();

    private String material;

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    private final Set<String> activeGroups = new HashSet<>();

    public Set<String> getActiveGroups() {
        return activeGroups;
    }

    Vector3[] vertCache = new Vector3[4];
    Vector3[] colorCache = new Vector3[4];
    Vector2[] texCache = new Vector2[4];
    Vector3[] normalCache = new Vector3[4];

    private int head = 0;

    private boolean normalEnabled = true;

    public void setNormalEnabled(boolean normalEnabled) {
        this.normalEnabled = normalEnabled;
    }

    public boolean isNormalEnabled() {
        return normalEnabled;
    }
    
    @Override
    public void quad(Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue,
            int[] lights, int overlay, boolean useQuadColorData) {
        
        VertexConsumer.super.quad(matrixEntry, quad, new float[] { 1, 1, 1, 1 }, red, green, blue, lights, overlay,
                useQuadColorData);
    }

    public MeshVertexConsumer vertex(double x, double y, double z) {
        // posCache.set(x, y, z).mulPosition(matrices.peek().getPositionMatrix());
        // vertCache[head] = new float[] { posCache.x, posCache.y, posCache.z };
        // return this;
        vec.set(x, y, z).mulPosition(matrices.peek().getPositionMatrix());
        vertCache[head] = new Vector3(vec.x, vec.y, vec.z);
        return this;
    }

    @Override
    public MeshVertexConsumer color(int red, int green, int blue, int alpha) {
        colorCache[head] = new Vector3(red / 255f, green / 255f, blue / 255f);
        return this;
    }

    @Override
    public MeshVertexConsumer texture(float u, float v) {
        texCache[head] = new Vector2(u, 1 - v);
        return this;
    }

    @Override
    public MeshVertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public MeshVertexConsumer light(int u, int v) {
        return this;
    }

    @Override
    public MeshVertexConsumer normal(float x, float y, float z) {
        vec.set(x, y, z).mulDirection(matrices.peek().getPositionMatrix());
        vec.normalize();

        normalCache[head] = new Vector3(vec.x, vec.y, vec.z);
        return this;
    }

    @Override
    public void next() {
        if (head >= 3) {
            Vertex[] vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                vertices[i] = new Vertex(vertCache[i], colorCache[i]);
            }
            new FaceBuilder(vertices).build(mesh);
            head = 0;
        } else {
            head++;
        }
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {

    }

    @Override
    public void unfixColor() {

    }
    
}
