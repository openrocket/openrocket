#version 140
in vec3 aPos;
in vec2 aTexCoord;
in vec3 aColor;

out vec2 texCoord;
out vec3 particleColor;

uniform mat4 projection;
uniform mat4 view;

void main()
{
    gl_Position = projection * view * vec4(aPos, 1.0);
    texCoord = aTexCoord;
    particleColor = aColor;
}