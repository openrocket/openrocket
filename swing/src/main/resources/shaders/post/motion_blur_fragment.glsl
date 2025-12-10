#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D screenTexture;
uniform float blurFactor;

void main()
{
    vec4 color = vec4(0.0);
    int numSamples = 15;
    float blurStrength = 0.003 * blurFactor;
    
    // Horizontal motion blur samples
    for (int i = 0; i < numSamples; i++) {
        float offset = (float(i) / float(numSamples - 1) - 0.5) * 2.0;
        vec2 samplePos = TexCoords + vec2(offset * blurStrength, 0.0);
        
        // Weight samples to reduce aberration
        float weight = 1.0 - abs(offset);
        color += texture(screenTexture, samplePos) * weight;
    }
    
    // Normalize by total weight
    float totalWeight = 0.0;
    for (int i = 0; i < numSamples; i++) {
        float offset = (float(i) / float(numSamples - 1) - 0.5) * 2.0;
        totalWeight += 1.0 - abs(offset);
    }
    color /= totalWeight;
    
    FragColor = color;
}
