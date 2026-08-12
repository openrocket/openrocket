#version 140

in vec2 aPos;

out vec2 TexCoords;

void main() {
    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
    TexCoords = vec2((1.0 - aPos.x) * 0.5, (1.0 - aPos.y) * 0.5);
}
