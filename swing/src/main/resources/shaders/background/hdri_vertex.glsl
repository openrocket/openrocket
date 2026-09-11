#version 140
in vec3 aPos;

out vec3 WorldPos;

uniform mat4 projection;
uniform mat4 view;

void main() {
    WorldPos = aPos;
    mat4 viewNoTranslation = mat4(mat3(view)); // remove translation from the view matrix
    vec4 pos = projection * viewNoTranslation * vec4(WorldPos, 1.0);
    gl_Position = pos.xyww;
}