attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_worldView;
uniform float u_cyclingTime;
uniform float u_time;
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec3 v_lightPos;
varying vec2 v_timeOffset;

void main()
{
   v_color = vec4(1, 1, 1, 1);
   v_texCoords = a_texCoord0;
   v_lightPos = normalize(vec3((sin(u_cyclingTime) + 1.0) / 2.0, 0.1, 1.0));
   v_timeOffset = vec2(-mod(u_time / 20.0, 3.0), mod(u_time / 40.0, 3.0));

   gl_Position =  u_worldView * a_position;
}