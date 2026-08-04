#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D accumulationTexture;
uniform sampler2D revealageTexture;

const float EPSILON = 1e-5;

void main()
{
    float revealage = texture(revealageTexture, TexCoords).r;
    if (revealage >= 1.0 - EPSILON) {
        discard;
    }

    vec4 accumulation = texture(accumulationTexture, TexCoords);
    // Guard against floating-point attachment saturation on unusually deep stacks.
    if (any(isinf(abs(accumulation.rgb)))) {
        accumulation.rgb = vec3(accumulation.a);
    }

    vec3 averageColor = accumulation.rgb / max(accumulation.a, EPSILON);
    FragColor = vec4(averageColor, 1.0 - revealage);
}
