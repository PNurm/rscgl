package com.rscgl.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Vertex {

    public final Vector3 position = new Vector3();
    public boolean hasPosition;
    public final Vector3 normal = new Vector3(0, 1, 0);
    public boolean hasNormal;
    public final Color color = new Color(0,0,0,0);
    public boolean hasColor;
    public final Vector2 uv = new Vector2();
    public boolean hasUV;

    public void reset() {
        position.set(0, 0, 0);
        normal.set(0, 1, 0);
        color.set(1, 1, 1, 1);
        uv.set(0, 0);
    }

    public Vertex set(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
        reset();
        if ((hasPosition = pos != null) == true) position.set(pos);
        if ((hasNormal = nor != null) == true) normal.set(nor);
        if ((hasColor = col != null) == true) color.set(col);
        if ((hasUV = uv != null) == true) this.uv.set(uv);
        return this;
    }

    public Vertex set(final Vertex other) {
        if (other == null) return set(null, null, null, null);
        hasPosition = other.hasPosition;
        position.set(other.position);
        hasNormal = other.hasNormal;
        normal.set(other.normal);
        hasColor = other.hasColor;
        color.set(other.color);
        hasUV = other.hasUV;
        uv.set(other.uv);
        return this;
    }

    public Vertex pos(float x, float y, float z) {
        position.set(x, y, z);
        hasPosition = true;
        return this;
    }

    public Vertex pos(Vector3 pos) {
        if ((hasPosition = pos != null) == true) position.set(pos);
        return this;
    }

    public Vertex setNor(float x, float y, float z) {
        normal.set(x, y, z);
        hasNormal = true;
        return this;
    }

    public Vertex setNor(Vector3 nor) {
        if ((hasNormal = nor != null) == true) normal.set(nor);
        return this;
    }

    public Vertex setCol(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        hasColor = true;
        return this;
    }

    public Vertex setCol(Color col) {
        if ((hasColor = col != null) == true) color.set(col);
        return this;
    }

    public Vertex uv(float u, float v) {
        uv.set(u, v);
        hasUV = true;
        return this;
    }

    public Vertex uv(Vector2 uv) {
        if ((hasUV = uv != null) == true) this.uv.set(uv);
        return this;
    }

    public Vertex lerp(final MeshPartBuilder.VertexInfo target, float alpha) {
        if (hasPosition && target.hasPosition) position.lerp(target.position, alpha);
        if (hasNormal && target.hasNormal) normal.lerp(target.normal, alpha);
        if (hasColor && target.hasColor) color.lerp(target.color, alpha);
        if (hasUV && target.hasUV) uv.lerp(target.uv, alpha);
        return this;
    }

    public MeshPartBuilder.VertexInfo toVertexInfo() {
        MeshPartBuilder.VertexInfo vertexInfo = new MeshPartBuilder.VertexInfo();
            vertexInfo.setPos(position);
            vertexInfo.setNor(normal);
            vertexInfo.setCol(color);
            vertexInfo.setUV(uv);
        return vertexInfo;
    }
}
