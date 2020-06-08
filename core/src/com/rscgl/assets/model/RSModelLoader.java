package com.rscgl.assets.model;

import com.badlogic.gdx.math.Vector3;
import com.rscgl.model.RSMesh;
import com.rscgl.model.Vertex;

public class RSModelLoader {

    public static RSMesh loadModel(RSArchiveModel model) {
        if (model.faceCount == 1) {
            return null;
        }

        RSMesh mesh = new RSMesh(Short.MAX_VALUE, Short.MAX_VALUE);
        for (int i = 0; i < model.vertexCount; i++) {
            Vertex vertex = new Vertex();
            vertex.pos(model.vertX[i], model.vertY[i], model.vertZ[i]);
            vertex.uv(-1, -1);

            mesh.vertex(vertex);
        }

        for (int face = 0; face < model.faceCount; face++) {
            int[] indices = model.faceIndices[face];

            int frontTexture = model.faceTextureFront[face];
            int backTexture = model.faceTextureBack[face];

            boolean[] suppress = new boolean[4];
            float[] nMap = {0, 0, 0, 1, 1, 1, 1, 0};
            int head = 0;

            for (int i = 0; i < indices.length; i++) {
                Vertex vertex = mesh.getVertex(indices[i]);
                for (int j = 0; j < suppress.length; j++) {
                    if (vertex.uv.x == nMap[j * 2] && vertex.uv.y == nMap[j * 2 + 1]) {
                        suppress[j] = true;
                        head = j + 1;
                    }
                }
            }

            for (int i = 0; i < indices.length; i++) {
                Vertex vertex = mesh.getVertex(indices[i]);
                if (vertex.uv.x < 0) {
                    while (suppress[head % 4] && head < 8)
                        head++;

                    int k = head % 4;
                    suppress[k] = true;
                    vertex.uv(nMap[k * 2], nMap[k * 2 + 1]);
                }
            }

            for (int i = 2; i < indices.length; i++) {
                calculateNormal(mesh.getVertex(indices[i - 1]), mesh.getVertex(indices[i]), mesh.getVertex(indices[0]));
                mesh.copyTriangle(backTexture, (short) indices[i - 1], (short) indices[i], (short) indices[0]);

                //modelBuilder.calculateNormal((short) indices[i - 1], (short) indices[i], (short) indices[0]);
                //modelBuilder.addTriangle(backTexture, (short) indices[i - 1], (short) indices[i], (short) indices[0]);
            }
            for (int i = 2; i < indices.length; i++) {
                calculateNormal(mesh.getVertex(indices[i - 1]), mesh.getVertex(indices[i]), mesh.getVertex(indices[0]));
                mesh.copyTriangle(frontTexture, (short) indices[i - 1], (short) indices[i], (short) indices[0]);

               // modelBuilder.calculateNormal((short) indices[0], (short) indices[i], (short) indices[i - 1]);
               // modelBuilder.addTriangle(frontTexture, (short) indices[0], (short) indices[i], (short) indices[i - 1]);
            }
        }
        mesh.update();

        return mesh;
    }

    private static void calculateNormal(Vertex v1, Vertex v2, Vertex v3) {
        Vector3 a = new Vector3(v1.position.cpy().sub(v2.position));
        Vector3 b = new Vector3(v1.position.cpy().sub(v3.position));
        Vector3 surfaceNormal = a.crs(b);
        v1.setNor(surfaceNormal);
        v2.setNor(surfaceNormal);
        v3.setNor(surfaceNormal);
    }

}
