#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec4 aColorAlpha;

out vec2 texCoord;
out vec3 smokeColor;
out float smokeAlpha;
out vec3 worldPos;
out vec3 viewDir;

uniform mat4 projection;
uniform mat4 view;

void main()
{
    // Simple pass-through - no billboard effect, just render as regular quads
    gl_Position = projection * view * vec4(aPos, 1.0);
    
    texCoord = aTexCoord;
    smokeColor = aColorAlpha.rgb;
    smokeAlpha = aColorAlpha.a;
    worldPos = aPos;
    
    // Calculate view direction for lighting
    vec3 cameraPos = inverse(mat3(view)) * (-view[3].xyz);
    viewDir = normalize(cameraPos - aPos);
}