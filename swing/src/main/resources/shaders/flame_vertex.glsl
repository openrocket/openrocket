#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoords;
layout (location = 2) in vec4 color; // RGB + alpha

uniform mat4 projection;
uniform mat4 view;

out vec2 texCoord;
out vec3 flameColor;
out float flameAlpha;
out vec3 worldPos;
out vec3 viewDir;

void main()
{
    worldPos = position;
    
    // Calculate view direction
    vec3 cameraPos = inverse(view)[3].xyz;
    viewDir = normalize(cameraPos - worldPos);
    
    gl_Position = projection * view * vec4(position, 1.0);
    texCoord = texCoords;
    flameColor = color.rgb;
    flameAlpha = color.a;
}