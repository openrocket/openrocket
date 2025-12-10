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
    // Sample flame texture (reuse smoke texture)
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
    
    // Create realistic flame color gradient based on temperature
    vec3 hotCore = vec3(1.2, 1.2, 1.0);      // Very bright white-yellow (hottest)
    vec3 mediumFlame = vec3(1.0, 0.7, 0.2);  // Orange (medium)
    vec3 coolEdge = vec3(1.0, 0.1, 0.0);     // Bright red (coolest)
    
    vec3 temperatureColor;
    if (temperature > 0.8) {
        // Very hot core region - blend from medium to extremely hot
        float t = (temperature - 0.8) / 0.2;
        temperatureColor = mix(mediumFlame, hotCore, t);
    } else if (temperature > 0.4) {
        // Medium region - blend from cool to medium
        float t = (temperature - 0.4) / 0.4;
        temperatureColor = mix(coolEdge, mediumFlame, t);
    } else {
        // Cool edges - pure red with some variation
        temperatureColor = coolEdge;
    }
    
    // Use mostly temperature-based color with minimal original color influence
    vec3 finalColor = mix(temperatureColor, flameColor, 0.05);
    
    // Smoother brightness gradient for more cohesive appearance
    float coreBrightness = pow(temperature, 0.5) * 3.0 + 0.4;
    finalColor *= coreBrightness;
    
    float finalAlpha = texColor.a * flameAlpha;
    
    FragColor = vec4(finalColor, finalAlpha);
    
    // Discard fully transparent pixels
    if (FragColor.a < 0.01) {
        discard;
    }
}