#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoords;
layout (location = 2) in vec4 color; // RGB + alpha
layout (location = 3) in float aAgeRatio;

uniform mat4 projection;
uniform mat4 view;

out vec2 texCoord;
out vec3 flameColor;
out float flameAlpha;
out vec3 worldPos;
out float age;

void main()
{
    worldPos = position;
    gl_Position = projection * view * vec4(position, 1.0);
    texCoord = texCoords;
    flameColor = color.rgb;
    flameAlpha = color.a;
    age = aAgeRatio;
}
