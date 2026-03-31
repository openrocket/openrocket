#version 330 core
in vec2 texCoord;
in vec3 flameColor;
in float flameAlpha;
in vec3 worldPos;
in vec3 viewDir;

out vec4 FragColor;

uniform sampler2D flameTexture;
uniform float flickerIntensity;
uniform float time;

void main()
{
    // Treat the texture primarily as a shape mask, not as a color source.
    vec4 texColor = texture(flameTexture, texCoord);
    
    // Subtle flickering effect - much more gentle
    float flicker = sin(time * 4.0 + worldPos.y * 2.0) * 0.05 * flickerIntensity;
    flicker += sin(time * 6.0 + worldPos.x * 1.5 + worldPos.z * 1.5) * 0.03 * flickerIntensity;
    
    // Use a hybrid approach: texture coordinates for particle shape, but modulated by overall flame color
    float particleDistance = length(texCoord - 0.5) * 2.0; // 0 at center, 1 at edges of particle
    
    // Use the flame color's brightness as an indicator of where we are in the flame
    float flameBrightness = dot(flameColor, vec3(0.299, 0.587, 0.114)); // Luminance
    
    // Particles closer to flame core (brighter flame colors) get hotter centers
    float coreIntensity = flameBrightness * 1.5; // Boost based on particle's flame color
    
    // Calculate final temperature with smooth gradients
    float temperature = (1.0 - particleDistance) * coreIntensity;
    temperature = pow(temperature, 0.4) * (1.0 + flicker * 0.15); // Gentle flicker
    
    // Keep a bright physically-plausible heat ramp, then tint it with the selected
    // flame color using a screen/soft-light-like blend so dark user colors don't
    // collapse the flame luminance.
    vec3 coolEdge = vec3(1.0, 0.18, 0.02);
    vec3 mediumFlame = vec3(1.0, 0.72, 0.18);
    vec3 hotCore = vec3(1.0, 0.98, 0.90);

    vec3 heatColor;
    if (temperature > 0.8) {
        float t = (temperature - 0.8) / 0.2;
        heatColor = mix(mediumFlame, hotCore, t);
    } else if (temperature > 0.4) {
        float t = (temperature - 0.4) / 0.4;
        heatColor = mix(coolEdge, mediumFlame, t);
    } else {
        heatColor = coolEdge;
    }

    // Use the selected color as a hue tint only. Normalize by max channel so dark picks
    // still produce a bright flame, and light picks don't collapse to white.
    float tintMax = max(max(flameColor.r, flameColor.g), max(flameColor.b, 0.001));
    vec3 tintHue = clamp(flameColor / tintMax, 0.0, 1.25);
    vec3 tintedHeat = heatColor * mix(vec3(1.0), tintHue, 0.60);

    // Smoother brightness gradient for more cohesive appearance
    float coreBrightness = pow(temperature, 0.42) * 4.2 + 0.55;
    vec3 finalColor = tintedHeat * coreBrightness;

    // Use the source texture only for silhouette/soft falloff.
    float finalAlpha = texColor.a * flameAlpha;
    
    FragColor = vec4(finalColor, finalAlpha);
    
    // Discard fully transparent pixels
    if (FragColor.a < 0.01) {
        discard;
    }
}
