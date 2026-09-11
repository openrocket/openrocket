#version 140

out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D backgroundImage;

void main() {
    FragColor = texture(backgroundImage, TexCoords);
}
