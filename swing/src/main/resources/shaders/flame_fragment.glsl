#version 330 core
in vec2 texCoord;
in vec3 flameColor;
in float flameAlpha;
in vec3 worldPos;
in float age;

out vec4 FragColor;

uniform sampler2D flameTexture;
uniform float flickerIntensity;
uniform float time;
uniform float exposureScale;

void main()
{
    // Texture is used only as a soft shape mask.
    vec4 texColor = texture(flameTexture, texCoord);

    // --- Temperature calculation ---
    // Radial: 1.0 at particle center, 0.0 at edge.
    float radial = 1.0 - clamp(length(texCoord - 0.5) * 2.0, 0.0, 1.0);

    // Longitudinal: young particles (age~0) are hottest, old ones (age~1) coolest.
    // The curve keeps the base very hot and only starts cooling noticeably past the midpoint.
    float ageCool = 1.0 - age * age * 0.6;

    // Combine into a single temperature value [0, 1].
    float temp = radial * ageCool;

    // Subtle turbulent flicker driven by world position and time.
    float flicker = sin(time * 5.0 + worldPos.y * 3.0 + worldPos.x * 1.7) * 0.04
                  + sin(time * 8.0 + worldPos.z * 2.5 + worldPos.y * 1.3) * 0.03;
    temp = clamp(temp + flicker * flickerIntensity, 0.0, 1.0);

    // Use per-particle color brightness as additional turbulence so particles
    // that received a brighter random color burn a little hotter.
    float particleBrightness = dot(flameColor, vec3(0.299, 0.587, 0.114));
    temp = clamp(temp * (0.75 + particleBrightness * 0.5), 0.0, 1.0);

    // --- Hue extraction ---
    // Normalize the user/particle color to extract its hue direction.
    // This ensures even very dark user picks produce a bright, vivid flame.
    float tintMax = max(max(flameColor.r, flameColor.g), max(flameColor.b, 0.001));
    vec3 hue = flameColor / tintMax;

    // --- Heat ramp with user-color tinting ---
    // The flame should always look like fire: bright core trending toward white,
    // mid-range dominated by the user's color, cool edges fading out.
    // smoothstep gives a nice S-curve for each zone.
    float brightness = pow(temp, 0.55) * 2.8;
    float whiteShift = smoothstep(0.55, 1.0, temp);

    // Mix hue toward white at the hot core.  At whiteShift=1 we're ~75 % white.
    vec3 color = mix(hue, vec3(1.0), whiteShift * 0.75) * brightness;

    // Add a warm "ember" glow at cool edges so the flame doesn't cut off abruptly.
    float emberMask = smoothstep(0.25, 0.0, temp);
    vec3 emberColor = hue * 0.12;
    color += emberColor * emberMask;

    // --- Alpha ---
    // Use the texture alpha for soft particle silhouette, modulated by the
    // per-vertex alpha from the renderer (controls overall opacity along the plume).
    // A slight alpha fade at low temperature avoids hard edges.
    float alphaFade = smoothstep(0.0, 0.12, temp);
    float finalAlpha = texColor.a * flameAlpha * alphaFade * exposureScale;

    FragColor = vec4(color * exposureScale, finalAlpha);

    if (FragColor.a < 0.005) {
        discard;
    }
}
