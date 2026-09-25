#version 140
in vec3 aPos;
in vec2 aTrailCoord;
in vec3 aColor;
in float aAlpha;

out vec3 particleColor;
out vec2 trailCoord;
out float particleAlpha;

uniform mat4 projection;
uniform mat4 view;

void main()
{
    gl_Position = projection * view * vec4(aPos, 1.0);
    particleColor = aColor;
    trailCoord = aTrailCoord;
    particleAlpha = aAlpha;
}
