#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D hudTexture;

void main() {
    vec4 texColor = texture(hudTexture, TexCoords);
    // Discard fragments that are fully transparent
    if(texColor.a < 0.1)
    discard;
    FragColor = texColor;
}