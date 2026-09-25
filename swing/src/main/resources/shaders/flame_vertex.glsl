#version 140
in vec3 position;
in vec2 texCoords;
in vec4 color; // RGB + alpha
in float aAgeRatio;

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
