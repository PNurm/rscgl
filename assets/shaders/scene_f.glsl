#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform float u_diffuse_texture;
uniform float u_diffuse_color;
uniform float alphaTesting;

uniform vec4 u_diffuseColor;
uniform sampler2D u_diffuseTexture;

uniform vec3 u_lightDirection;
uniform vec3 u_lightColor;
varying MED vec2 v_diffuseUV;

varying vec3 v_normal;

varying float v_opacity;
varying float v_alphaTest;
varying float v_fog;

varying vec4 v_fogColor;

void main()
{
    vec4 diffuse = vec4(1.0);
    if(u_diffuse_color == 1.0 && u_diffuse_texture == 1.0)
        diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
    else if(u_diffuse_color == 1.0)
        diffuse = u_diffuseColor;
    else if(u_diffuse_texture == 1.0)
        diffuse = texture2D(u_diffuseTexture, v_diffuseUV);

    vec3 normal = v_normal;

    vec3 lightDiffuse = vec3(0.25);
    vec3 lightDir = -u_lightDirection;
    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    vec3 value = u_lightColor * NdotL;
    lightDiffuse += value;

    diffuse.rgb = diffuse.rgb * lightDiffuse;
	gl_FragColor = diffuse;

    gl_FragColor.rgb = mix(gl_FragColor.rgb, v_fogColor.rgb, v_fog);

    if(alphaTesting == 1.0) {
        if (gl_FragColor.a <= v_alphaTest)
            discard;
    }
}
