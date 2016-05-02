varying vec2 v_texCoords;
uniform sampler2D u_actorTexture;
uniform vec4 u_actorColor;
uniform float u_actorAngle;

void main()
{
  vec4 texColor = texture2D(u_actorTexture, v_texCoords.xy).rgba;
  gl_FragColor = vec4(texColor.rgba * u_actorColor.rgba);
}
