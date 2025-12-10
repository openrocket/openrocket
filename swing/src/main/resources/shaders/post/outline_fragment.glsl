#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D selectionTexture;
uniform vec4 outlineColor;
uniform float outlineWidth;
uniform vec2 screenSize;

void main()
{
    vec2 texelSize = 1.0 / screenSize;

    // Sample the selection texture
    float center = texture(selectionTexture, TexCoords).r;

    // If we're inside the object, don't draw outline
    if (center > 0.5) {
        discard;
    }

    // Simple box filter to detect edges
    float sum = 0.0;
    float samples = 0.0;

    for (float x = -outlineWidth; x <= outlineWidth; x += 1.0) {
        for (float y = -outlineWidth; y <= outlineWidth; y += 1.0) {
            if (x == 0.0 && y == 0.0) continue;

            vec2 offset = vec2(x, y) * texelSize;
            float s = texture(selectionTexture, TexCoords + offset).r;
            sum += s;
            samples += 1.0;
        }
    }

    float average = sum / samples;

    // If we're near a selected object, draw the outline
    if (average > 0.01) {
        FragColor = outlineColor;
    } else {
        discard;
    }
}