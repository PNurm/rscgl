package com.rscgl.util.builder;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.WallObjectDef;
import com.rscgl.model.RSMesh;
import com.rscgl.model.Vertex;

import static com.rscgl.GameWorld.TSIZE;

public class RSModelBuilder implements Disposable {

    public static RSMesh createWall(int id, int dir, int x, int y) {
        WallObjectDef def = RSCache.WALLS[id];

        float height = (def.getHeight() / 128f) * TSIZE;// CacheValues.wallObjectHeight[var1];
        int frontTex = def.getFrontTex();
        int backTex = def.getBackTex();

        float x1 = x;
        float x2 = x;
        float z1 = y;
        float z2 = y;

        if (dir == 1) {
            z2 = 1 + y;
        }
        if (dir == 0) {
            x2 = x + 1;
        }
        if (dir == 2) {
            z2 = 1 + y;
            x1 = 1 + x;
        }
        if (dir == 3) {
            z2 = 1 + y;
            x2 = x + 1;
        }

        x1 *= -TSIZE;
        x2 *= -TSIZE;
        z1 *= TSIZE;
        z2 *= TSIZE;

        float yv00 = Game.world().getInterpolatedElevation(x1, z1);
        float yv10 = Game.world().getInterpolatedElevation(x1, z1) + height;
        float yv11 = Game.world().getInterpolatedElevation(x2, z2) + height;
        float yv01 = Game.world().getInterpolatedElevation(x1, z2);

        Vertex v00 = new Vertex()
                .pos(x1, yv00, z1)
                .uv(0, 1)
                .setNor(0, 1, 0);

        Vertex v10 = new Vertex()
                .pos(x1, yv10, z1)
                .uv(1, 1)
                .setNor(0, 1, 0);

        Vertex v11 = new Vertex()
                .pos(x2, yv11, z2)
                .uv(1, 0)
                .setNor(0, 1, 0);

        Vertex v01 = new Vertex()
                .pos(x2, yv01, z2)
                .uv(0, 0)
                .setNor(0, 1, 0);

        RSMesh mesh = new RSMesh(4, 16);

        short i00 = mesh.vertex(v00);
        short i10 = mesh.vertex(v10);
        short i11 = mesh.vertex(v11);
        short i01 = mesh.vertex(v01);

        mesh.triangle(frontTex, i00, i10, i11);
        mesh.triangle(backTex, i11, i01, i00);
        mesh.update();

        return mesh;
    }


    private final Vertex[] vertexData;
    private boolean disposed;
    private ModelBuilder builder;

    public RSModelBuilder(int vertexCount, int indexCount) {
        vertexData = new Vertex[vertexCount];
        for (int x = 0; x < vertexData.length; x++) {
            vertexData[x] = new Vertex();
        }
    }

    public void calculateNormal(int aID, int bID, int cID) {
        Vertex v1 = vertexData[aID];
        Vertex v2 = vertexData[bID];
        Vertex v3 = vertexData[cID];

        Vector3 a = new Vector3(v1.position.cpy().sub(v2.position));
        Vector3 b = new Vector3(v1.position.cpy().sub(v3.position));

        Vector3 surfaceNormal = a.crs(b);

        v1.setNor(surfaceNormal);
        v2.setNor(surfaceNormal);
        v3.setNor(surfaceNormal);
    }

    public void prebuild() {
        builder = new ModelBuilder();
        TextureAttribute texture = TextureAttribute.createDiffuse(Assets.inst.getMaterial(0).getTexture());
        Material material = new Material();
        material.set(texture);
        material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));

        builder.begin();
        MeshPartBuilder part = builder.part("mesh", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position
                        | VertexAttributes.Usage.Normal
                        | VertexAttributes.Usage.ColorUnpacked
                        | VertexAttributes.Usage.TextureCoordinates,
                material);

/*
        TextureRegion textureRegion = Assets.inst.getMaterial(group.getMaterialID());
        part.setUVRange(textureRegion);
        for (int i = 0; i < group.indices.length; i += 3) {
            part.triangle(vertexData[group.indices[i]].toVertexInfo(), vertexData[group.indices[i + 1]].toVertexInfo(), vertexData[group.indices[i + 2]].toVertexInfo());
        }*/

    }

    public Model end() {
        if (builder == null)
            prebuild();
        return builder.end();
    }


    public void transform(Matrix4 matrix4) {
        for (int i = 0; i < vertexData.length; i++) {
            Vertex v = getVertex(i);
            v.position.mul(matrix4);
        }
    }

    public Vertex getVertex(int vID) {
        return vertexData[vID];
    }

    public Vector3 getVertexPosition(Vector3 tmpPos, int vID) {
        return tmpPos.set(vertexData[vID].position);
    }

    public void dispose() {
        disposed = true;
    }

    public short setVertexData(int id, float x, float y, float z, int u, int v) {
        vertexData[id].pos(x, y, z).uv(u, v);
        return (short) id;
    }

    public boolean isDisposed() {
        return disposed;
    }
}
