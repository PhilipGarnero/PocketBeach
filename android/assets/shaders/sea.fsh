varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_noiseTexture;
uniform sampler2D u_waveTexture;
uniform sampler2D u_waveBumpTexture;
uniform float u_time;

void main()
{
  vec2 displacement = texture2D(u_noiseTexture, v_texCoords).xy;
  float t = v_texCoords.x + displacement.y * 0.1 + (cos(v_texCoords.y * 50.0 + u_time) + sin(v_texCoords.y * 15.0 - u_time) * 2.0) * 0.03 - 0.1;
  gl_FragColor = v_color * texture2D(u_waveTexture, vec2(t, v_texCoords.y));
}
