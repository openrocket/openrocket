#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTrailCoord;
layout (location = 2) in vec3 aColor;
layout (location = 3) in float aAlpha;

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
