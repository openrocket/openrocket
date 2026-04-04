#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D screenTexture;
uniform sampler2D depthTexture;
uniform float blurFactor;
uniform vec2 blurDirection;  // Normalized screen-space direction of the rocket axis

void main()
{
    vec4 color = vec4(0.0);
    int numSamples = 15;
    float blurStrength = 0.003 * blurFactor;
    float totalWeight = 0.0;
    bool hasRocketSample = false;

    for (int i = 0; i < numSamples; i++) {
        float offset = (float(i) / float(numSamples - 1) - 0.5) * 2.0;
        vec2 samplePos = TexCoords + blurDirection * (offset * blurStrength);
        float weight = 1.0 - abs(offset);

        float sampleDepth = texture(depthTexture, samplePos).r;
        if (sampleDepth < 0.9999) {
            hasRocketSample = true;
        }

        color += texture(screenTexture, samplePos) * weight;
        totalWeight += weight;
    }

    if (!hasRocketSample) {
        // No rocket pixels along the blur axis — keep background sharp
        FragColor = texture(screenTexture, TexCoords);
    } else {
        // Rocket pixels found — apply directional blur (edges bleed naturally)
        FragColor = color / totalWeight;
    }
}
