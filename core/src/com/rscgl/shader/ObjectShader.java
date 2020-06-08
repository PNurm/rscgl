package com.rscgl.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.rscgl.Config;
import com.rscgl.Game;

/**
 * Simple shader for an object with a texture
 */
public class ObjectShader implements Shader {

    private final int u_cameraPosition;
    private final int u_lightDir;
    private final int u_lightColor;
    private final int u_fogColor;
    private ShaderProgram program;

    private int u_worldTrans;
    private int u_projViewTrans;
    private int u_normalMatrix;

    private int u_diffuseUVTransform;
    private int u_diffuseTexture;

    private int u_opacity;
    private int u_alphaTest;

    private RenderContext context;


    public ObjectShader() {
        String vertexShader = Gdx.files.internal("shaders/scene_v.glsl").readString();
        String fragmentShader = Gdx.files.internal("shaders/scene_f.glsl").readString();

        this.program = new ShaderProgram(vertexShader, fragmentShader);

        if (!program.isCompiled()) {
            System.err.println("Error with WorldShader: " + program.getLog());
        } else {
           // Gdx.app.log("init", "WorldShader compiled " + program.getLog() + "");
        }

        u_worldTrans = program.getUniformLocation("u_worldTrans");
        u_cameraPosition = program.getUniformLocation("u_cameraPosition");
        u_projViewTrans = program.getUniformLocation("u_projViewTrans");
        u_normalMatrix = program.getUniformLocation("u_normalMatrix");
        u_lightDir = program.getUniformLocation("u_lightDirection");
        u_lightColor = program.getUniformLocation("u_lightColor");

        u_diffuseTexture = program.getUniformLocation("u_diffuseTexture");
        u_diffuseUVTransform = program.getUniformLocation("u_diffuseUVTransform");

        u_alphaTest = program.getUniformLocation("u_alphaTest");
        u_opacity = program.getUniformLocation("u_opacity");
        u_fogColor = program.getUniformLocation("u_fogColor");
    }


    @Override
    public void begin(final Camera camera, final RenderContext context) {
        this.context = context;

        program.begin();
        program.setUniformMatrix(u_projViewTrans, camera.combined);
        program.setUniformf(u_cameraPosition, camera.position.x, camera.position.y, camera.position.z, 1.1881f / (camera.far * camera.far));
        program.setUniformf(u_lightDir, Game.inst.lightDirection);
        program.setUniformf(u_lightColor, Game.inst.lightColor);
        program.setUniformf(u_fogColor, Config.FOG_COLOR);

        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_NONE);
    }

    Matrix3 tmpM = new Matrix3();

    @Override
    public void render(final Renderable renderable) {
        if (!renderable.material.has(BlendingAttribute.Type))
            context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        program.setUniformMatrix(u_normalMatrix, tmpM.set(renderable.worldTransform).inv().transpose());
        configureMaterials(renderable.material);
        try {
            renderable.meshPart.render(program);
        } catch (Exception e) {
            System.out.println(renderable + " fucked up");
            e.printStackTrace();
        }
    }

    private void configureMaterials(Material material) {
        int depthFunc = GL20.GL_LEQUAL;
        int cullFace = GL20.GL_NONE;
        float depthRangeNear = 0f;
        float depthRangeFar = 1f;
        boolean depthMask = true;

        for (Attribute attr : material) {
            final long t = attr.type;
            if (BlendingAttribute.is(t)) {
                context.setBlending(true, ((BlendingAttribute) attr).sourceFunction, ((BlendingAttribute) attr).destFunction);
                program.setUniformf(u_opacity, ((BlendingAttribute) attr).opacity);

            } else if ((t & IntAttribute.CullFace) == IntAttribute.CullFace)
                cullFace = ((IntAttribute) attr).value;
            else if ((t & FloatAttribute.AlphaTest) == FloatAttribute.AlphaTest) {
                program.setUniformf(u_alphaTest, ((FloatAttribute) attr).value);
                program.setUniformf("alphaTesting", 1);
            } else if ((t & DepthTestAttribute.Type) == DepthTestAttribute.Type) {
                DepthTestAttribute dta = (DepthTestAttribute) attr;
                depthFunc = dta.depthFunc;
                depthRangeNear = dta.depthRangeNear;
                depthRangeFar = dta.depthRangeFar;
                depthMask = dta.depthMask;
            }
        }

        if (material.has(TextureAttribute.Diffuse)) {
            program.setUniformf("u_diffuse_texture", 1);

            TextureAttribute textureAttribute = (TextureAttribute) material.get(TextureAttribute.Diffuse);
            int unit = context.textureBinder.bind(textureAttribute.textureDescription);

            program.setUniformi(u_diffuseTexture, unit);
            program.setUniformf(u_diffuseUVTransform, textureAttribute.offsetU, textureAttribute.offsetV, textureAttribute.scaleU, textureAttribute.scaleV);
        } else {
            program.setUniformf("u_diffuse_texture", 0);
        }
        context.setCullFace(cullFace);
        context.setDepthTest(depthFunc, depthRangeNear, depthRangeFar);
        context.setDepthMask(depthMask);
    }

    @Override
    public void init() {
        if (!program.isCompiled()) {
            System.err.println("Error with WorldShader: " + program.getLog());
        }
        System.out.println(program.getLog());
    }

    @Override
    public int compareTo(final Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(final Renderable renderable) {
        return true;
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public void dispose() {
        program.dispose();
    }
}
