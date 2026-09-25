#version 140

in vec3 position;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 center; // The world-space center of the billboard
uniform float scaleWithView; // 1.0 = scale with zoom, 0.0 = fixed size
uniform float fixedScaleFactor; // Scale factor for fixed-size carets
uniform float viewportHeight; // Framebuffer height in pixels (used for fixed-size normalization)

void main()
{
    // Transform the world-space center of the billboard into view space
    vec4 centerView = viewMatrix * vec4(center, 1.0);

    // Add the vertex's model-space position as an offset in view space
    float distance = max(0.001, -centerView.z);
    vec3 offset = position;
    if (scaleWithView < 0.5) {
        // Divide by viewportHeight so the screen-pixel size is independent of window dimensions.
        // Without this, the apparent size scales linearly with viewport height.
        offset *= distance * fixedScaleFactor / max(1.0, viewportHeight);
    }
    vec4 finalPosView = centerView + vec4(offset, 0.0);

    // Project the final view-space position
    gl_Position = projectionMatrix * finalPosView;
}
