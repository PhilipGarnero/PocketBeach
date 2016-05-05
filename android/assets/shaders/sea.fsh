#ifdef GL_ES
precision mediump float;
#else
#define highp
#define mediump
#define lowp
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec3 v_lightPos;
varying vec2 v_timeOffset;
uniform sampler2D u_noiseTexture;
uniform highp float u_time;
uniform highp float u_cyclingTime;
uniform vec4 u_seaColor;

void main()
{
  vec3 normal = normalize(texture2D(u_noiseTexture, v_texCoords.xy + v_timeOffset.xy).rgb * 2.0 - 1.0);
  float diffuse = max(dot(normal, v_lightPos), 0.0);
  vec4 addedCol = vec4(clamp((v_color.rgb * diffuse) + u_seaColor.rgb, 0.0, 1.0), u_seaColor.a);

  vec2 displacement = texture2D(u_noiseTexture, v_texCoords).xy;
  float t = v_texCoords.x + displacement.y * 0.1 + (cos(v_texCoords.y * 8.0 + u_cyclingTime) * 3.0 + sin(v_texCoords.y * 2.5 - u_cyclingTime) * 5.0) * 0.03;
  if (t > 2.80) {
      addedCol.a = 0.0;
  }
  else if (t > 2.75) {
      addedCol = vec4(1.0, 1.0, 1.0, 0.5);
  }
  gl_FragColor = addedCol.rgba;
}
