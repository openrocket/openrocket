#version 140
in vec3 particleColor;
in vec2 trailCoord;
in float particleAlpha;
out vec4 FragColor;

void main()
{
    float across = abs(trailCoord.y);

    // A crisp incandescent center wrapped in a much softer halo.
    float core = 1.0 - smoothstep(0.10, 0.45, across);
    float glow = 1.0 - smoothstep(0.05, 1.0, across);

    // Round off both ends of the ribbon.  The per-vertex alpha makes the tail
    // faint and leaves the leading fragment as the dominant point of light.
    float tailFade = smoothstep(0.0, 0.06, trailCoord.x);
    float headFade = 1.0 - smoothstep(0.97, 1.0, trailCoord.x);
    float intensity = (core + 0.50 * glow) * tailFade * headFade * particleAlpha;

    FragColor = vec4(particleColor, clamp(intensity, 0.0, 1.0));
}
