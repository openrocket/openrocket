#version 330 core

layout (location = 0) in vec3 position;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 center; // The world-space center of the billboard

void main()
{
    // Transform the world-space center of the billboard into view space
    vec4 centerView = viewMatrix * vec4(center, 1.0);

    // Add the vertex's model-space position as an offset in view space
    vec4 finalPosView = centerView + vec4(position, 0.0);

    // Project the final view-space position
    gl_Position = projectionMatrix * finalPosView;
}