#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D screenTexture;
// When true, apply the sRGB transfer function manually before output.
// Used by the final present pass so we don't rely on GL_FRAMEBUFFER_SRGB
// being honoured by the default framebuffer (it is silently ignored on
// many Linux/GLX drivers that allocate a linear default framebuffer).
uniform bool applyGammaCorrection;

// Proper piecewise sRGB transfer function (IEC 61966-2-1),
// identical to what GL_FRAMEBUFFER_SRGB applies automatically.
vec3 linearToSrgb(vec3 c) {
    vec3 lo = c * 12.92;
    vec3 hi = pow(max(c, vec3(0.0)), vec3(1.0 / 2.4)) * 1.055 - 0.055;
    return mix(lo, hi, step(vec3(0.0031308), c));
}

void main()
{
    vec4 color = texture(screenTexture, TexCoords);
    if (applyGammaCorrection) {
        color.rgb = linearToSrgb(color.rgb);
    }
    FragColor = color;
}
