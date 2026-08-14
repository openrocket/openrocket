#version 140
in vec3 aPos;
in vec2 aTexCoord;
in vec4 aColorAlpha;

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

    // Calculate view direction for lighting. The view rotation is orthonormal,
    // so its inverse is its transpose; spell that multiplication out because
    // GLSL 1.40 does not provide the matrix inverse built-in.
    vec3 inverseTranslation = -view[3].xyz;
    vec3 cameraPos = vec3(
        dot(view[0].xyz, inverseTranslation),
        dot(view[1].xyz, inverseTranslation),
        dot(view[2].xyz, inverseTranslation));
    viewDir = normalize(cameraPos - aPos);
}
