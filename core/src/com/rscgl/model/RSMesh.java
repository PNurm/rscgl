package com.rscgl.model;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.rscgl.assets.Assets;
import com.rscgl.assets.model.RSArchiveModel;

import java.util.Arrays;

public class RSMesh implements RenderableProvider {

    public final Matrix4 worldTransform = new Matrix4();

    private final BoundingBox boundingBox = new BoundingBox();
    private final Vector3 halfExtents = new Vector3();
    private final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();

    public final Pixmap decoration;

    private final int uvRegion;
    private float radius = -1;

    private final int posPos;
    private final int norPos;
    private final int uvPos;
    private final int colPos;
    private final Material material;
    private final short[] indexData;
    private final int vertexSize;
    private final Mesh mesh;
    private final Renderable renderable;
    Vector3 tmp = new Vector3();
    private float[] vertexData;
    private Vertex[] vertexObject;
    private boolean meshDirty = false;
    public short vertexCount;
    private int indexCount;
    /**
     * The UV range used when building
     */
    private float uOffset = 0f, uScale = 1f, vOffset = 0f, vScale = 1f;
    private boolean hasUVTransform = false;

    public RSMesh(int maxVertexCount, int indexCount) {

        VertexAttributes attributes = new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "a_uvRegion")
        );

        this.vertexSize = attributes.vertexSize / 4;

        this.posPos = attributes.getOffset(VertexAttributes.Usage.Position, -1);
        this.norPos = attributes.getOffset(VertexAttributes.Usage.Normal, -1);
        this.uvPos = attributes.getOffset(VertexAttributes.Usage.TextureCoordinates, -1);
        this.uvRegion = attributes.getOffset(VertexAttributes.Usage.Generic, -1);
        this.colPos = attributes.getOffset(VertexAttributes.Usage.ColorUnpacked, -1);

        this.mesh = new Mesh(true, maxVertexCount, indexCount, attributes);

        this.vertexObject = new Vertex[maxVertexCount];
        this.vertexData = new float[maxVertexCount * vertexSize];
        this.indexData = new short[indexCount];

        material = new Material();
        material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        material.set(TextureAttribute.createDiffuse(Assets.inst.getTextureAtlas().getTextures().first()));
        material.set(FloatAttribute.createAlphaTest(0.1f));

        renderable = new Renderable();
        //renderable.worldTransform.set(worldTransform);
        renderable.material = material;
        renderable.meshPart.mesh = mesh;
        renderable.meshPart.offset = 0;
        renderable.meshPart.size = indexCount;
        renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;

        decoration = new Pixmap(48, 48, Pixmap.Format.RGBA8888);
    }

    private void updateUV(int offset) {
        Vertex vertex = vertexObject[offset];
        offset *= vertexSize;
        if (uvPos >= 0 && hasUVTransform) {
            vertexData[offset + uvPos] = uOffset + uScale * vertex.uv.x;
            vertexData[offset + uvPos + 1] = vOffset + vScale * vertex.uv.y;
        }
    }

    public void updateColor(int i) {
        Vertex v = vertexObject[i];
        i *= vertexSize;
        if (colPos >= 0) {
            vertexData[i + colPos + 0] = v.color.r;
            vertexData[i + colPos + 1] = v.color.g;
            vertexData[i + colPos + 2] = v.color.b;
            vertexData[i + colPos + 3] = v.color.a;
        }
    }

    private void setUVRange(float u1, float v1, float u2, float v2) {
        uOffset = u1;
        vOffset = v1;
        uScale = u2 - u1;
        vScale = v2 - v1;
        hasUVTransform = !(MathUtils.isZero(u1) && MathUtils.isZero(v1) && MathUtils.isEqual(u2, 1f) && MathUtils.isEqual(v2, 1f));
    }

    public void setUVRange(TextureRegion region) {
        if (!(hasUVTransform = (region != null))) {
            uOffset = vOffset = 0f;
            uScale = vScale = 1f;
        } else
            setUVRange(region.getU(), region.getV(), region.getU2(), region.getV2());
    }

    public void update() {
        if ((mesh.getMaxVertices() * vertexSize) < vertexData.length)
            throw new GdxRuntimeException("Mesh can't hold enough vertices: " + mesh.getMaxVertices() + " * " + vertexSize + " < "
                    + vertexData.length);
        if (mesh.getMaxIndices() < indexData.length)
            throw new GdxRuntimeException("Mesh can't hold enough indices: " + mesh.getMaxIndices() + " < " + indexData.length);

        mesh.setVertices(vertexData);
        mesh.setIndices(indexData);

        if (vertexCount > 0) {
            mesh.calculateBoundingBox(boundingBox, 0, vertexCount);

            boundingBox.getCenter(center);
            boundingBox.getDimensions(halfExtents).scl(0.5f);

            radius = halfExtents.len();
        }

        renderable.meshPart.update();
    }

    public int[] copyTriangle(int texture, short v1, short v2, short v3) {
        if (texture == RSArchiveModel.TRANSPARENT) {
            return null;
        }
        int newSize = vertexCount + 3;
        if (newSize >= Short.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("Too many vertices");
        }
        if (vertexObject.length < newSize) {
            vertexObject = Arrays.copyOf(vertexObject, newSize);
            vertexData = Arrays.copyOf(vertexData, newSize * vertexSize);
        }

        short c1 = vertex(new Vertex().set(vertexObject[v1]));
        short c2 = vertex(new Vertex().set(vertexObject[v2]));
        short c3 = vertex(new Vertex().set(vertexObject[v3]));

        triangle(texture, c1, c2, c3);

        meshDirty = true;

        return new int[]{c1, c2, c3};
    }

    public short vertex(Vertex vertex) {
        int newSize = vertexCount + 1;
        if (vertexObject.length < newSize) {
            vertexObject = Arrays.copyOf(vertexObject, newSize);
            vertexData = Arrays.copyOf(vertexData, newSize * vertexSize);
        }
        vertexObject[vertexCount] = vertex;

        int index = vertexSize * vertexCount;
        if (posPos >= 0) {
            vertexData[index + posPos + 0] = vertex.position.x;
            vertexData[index + posPos + 1] = vertex.position.y;
            vertexData[index + posPos + 2] = vertex.position.z;
        }
        if (norPos >= 0) {
            vertexData[index + norPos + 0] = vertex.normal.x;
            vertexData[index + norPos + 1] = vertex.normal.y;
            vertexData[index + norPos + 2] = vertex.normal.z;
        }
        if (colPos >= 0) {
            vertexData[index + colPos + 0] = vertex.color.r;
            vertexData[index + colPos + 1] = vertex.color.g;
            vertexData[index + colPos + 2] = vertex.color.b;
            vertexData[index + colPos + 3] = vertex.color.a;
        }
        if (uvPos >= 0) {
            vertexData[index + uvPos] = vertex.uv.x;
            vertexData[index + uvPos + 1] = vertex.uv.y;
        }

        return vertexCount++;
    }

    public void newQuad(int texture, Vector3 v0, Vector3 v1, Vector3 v2, Vector3 v3) {
        quad(texture,
                vertex(new Vertex().pos(v0).uv(0, 1)),
                vertex(new Vertex().pos(v1).uv(1, 1)),
                vertex(new Vertex().pos(v2).uv(0, 0)),
                vertex(new Vertex().pos(v3).uv(1, 0)));
    }


    private void drawDecor(short index) {
        int y = index / 48;

    }

    public void triangle(int texture, short v0, short v1, short v2) {
        setUVRange(null);

        TextureRegion textureRegion = Assets.inst.getMaterial(texture);
        setUVRange(textureRegion);

        updateUV(v0);
        updateUV(v1);
        updateUV(v2);

        this.indexData[indexCount++] = v0;
        this.indexData[indexCount++] = v1;
        this.indexData[indexCount++] = v2;
    }

    public void quad(int texture, short v0, short v1, short v2, short v3) {

        TextureRegion textureRegion = Assets.inst.getMaterial(texture);
        setUVRange(textureRegion);

        updateUV(v0);
        updateUV(v1);
        updateUV(v2);
        updateUV(v3);

        this.indexData[indexCount++] = v0;
        this.indexData[indexCount++] = v1;
        this.indexData[indexCount++] = v2;

        this.indexData[indexCount++] = v1;
        this.indexData[indexCount++] = v2;
        this.indexData[indexCount++] = v3;
        setUVRange(null);
    }

    public Vertex getVertex(int vID) {
        return vertexObject[vID];
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (meshDirty) {
            update();
            meshDirty = false;
        }

        renderable.worldTransform.set(worldTransform);

        renderables.add(renderable);
    }

    public void setDirty(boolean dirty) {
        this.meshDirty = dirty;
    }

    public void dispose() {
        mesh.dispose();
    }

    public Vector3 getVertexPosition(Vector3 tmpPos, int vID) {
        return tmpPos.set(vertexObject[vID].position).mul(worldTransform);
    }

    public BoundingBox getBounds() {
        return boundingBox;
    }

    public boolean rayHit(Ray ray) {
        if (Intersector.intersectRayBounds(ray, getBounds(), null)) {
            return true;
        }
        return false;
    }

    public boolean isVisible(Camera camera) {
        worldTransform.getTranslation(tmp);
        tmp.add(center);
        return camera.frustum.boundsInFrustum(tmp, dimensions);
    }

    public boolean inFrustum(Camera camera) {

        return true;
    }

    public void transformVertices(Matrix4 transform) {
        for (int i = 0; i < vertexCount; i++) {
            vertexObject[i].position.mul(transform);

            int index = i * vertexSize;
            if (posPos >= 0) {
                vertexData[index + posPos + 0] = vertexObject[i].position.x;
                vertexData[index + posPos + 1] = vertexObject[i].position.y;
                vertexData[index + posPos + 2] = vertexObject[i].position.z;
            }
        }
        update();
    }
}
