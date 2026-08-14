#version 140
in vec2 texCoord;
in vec3 smokeColor;
in float smokeAlpha;
in vec3 worldPos;
in vec3 viewDir;

out vec4 FragColor;

uniform sampler2D smokeTexture;

// Lighting uniforms
uniform vec3 lightPos;
uniform vec3 lightColor;
uniform float lightIntensity;
uniform vec3 ambientLight;
uniform float lightSensitivity;

void main()
{
    // Sample smoke texture
    vec4 texColor = texture(smokeTexture, texCoord);

    // Calculate lighting
    vec3 lightDir = normalize(lightPos - worldPos);
    float distance = length(lightPos - worldPos);
    float attenuation = 1.0 / (1.0 + 0.01 * distance + 0.001 * distance * distance);

    // For volumetric smoke, use a more stable lighting approach
    // Use light distance for simple volumetric scattering (view-independent)
    float volumetricScatter = 1.0 / (1.0 + distance * 0.1);

    // Add some directional scattering based on light direction alone
    // Use a fixed "up" direction as pseudo-normal for consistent lighting
    vec3 pseudoNormal = vec3(0.0, 1.0, 0.0);
    float directionalLight = max(dot(pseudoNormal, lightDir), 0.0) * 0.5 + 0.5;

    // Simple scattering model that doesn't depend on view angle
    float scatterAmount = volumetricScatter * directionalLight;

    // Combine lighting contributions (view-independent)
    vec3 lighting = ambientLight + lightColor * lightIntensity * scatterAmount * attenuation;

    // Mix between original smoke color and lit color based on light sensitivity
    // lightSensitivity = 0: no lighting effect (original color)
    // lightSensitivity = 1: full lighting effect
    vec3 litColor = smokeColor * lighting;
    vec3 finalColor = mix(smokeColor, litColor, lightSensitivity);
    float finalAlpha = texColor.a * smokeAlpha;

    FragColor = vec4(finalColor, finalAlpha);

    // Discard fully transparent pixels
    if (FragColor.a < 0.01) {
        discard;
    }
}