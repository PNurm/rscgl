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

attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat3 u_normalMatrix;
uniform vec4 u_fogColor;

uniform vec3 u_lightDirection;
uniform vec3 u_lightColor;
uniform vec4 u_diffuseUVTransform;
uniform vec4 u_cameraPosition;

uniform float u_diffuse_texture;
uniform float u_diffuse_color;

varying vec2 v_diffuseUV;
varying vec4 v_color;

uniform float u_alphaTest;
varying float v_alphaTest;
uniform float u_opacity;
varying float v_opacity;
varying float v_fog;
varying vec4 v_fogColor;

varying vec3 v_normal;

void main() {
    vec4 pos = u_worldTrans * vec4(a_position, 1.0);
    vec3 normal = normalize(u_normalMatrix * a_normal);

	if(u_diffuse_texture == 1.0) {
		v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;
    }
    vec3 distance = u_cameraPosition.xyz - pos.xyz;
    float fog = dot(distance, distance) * u_cameraPosition.w;
    v_fog = min(fog, 1.0);
    v_fogColor = u_fogColor;
    v_normal = normal;
    v_alphaTest = u_alphaTest;
    v_opacity = u_opacity;

    gl_Position = u_projViewTrans * pos;
}
