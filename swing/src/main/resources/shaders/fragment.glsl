#version 330 core

// Output color
out vec4 FragColor;

// Inputs from vertex shader
in mediump vec3 v_fragPos;
in mediump vec3 v_normal;
in mediump vec2 v_texCoord;
flat in int v_surfaceID;
in mediump float v_eyeSpaceZ;
in mediump vec4 v_lightSpacePos;

// Uniforms
uniform int renderStyle; // 0:COLOR_ONLY, 1:TEXTURE_ONLY, 2:WIREFRAME
uniform mediump vec3 objectColor;
uniform int hasTexture;
uniform sampler2D textureSampler;
uniform mat4 textureTransformMatrix;
uniform mat4 decalTransformMatrix;

// Material
uniform float shine;
uniform float roughnessScale;
uniform float roughnessStrength;
uniform mediump vec3 viewPos;
uniform mediump vec3 materialSpecular;
uniform float specularTintFactor;
uniform bool isUnlit;
uniform float ambientLightFactor;
uniform float opacity;  // 0.0 = fully transparent, 1.0 = fully opaque
uniform int textureMode;

// Lighting
#define MAX_LIGHTS 10

struct Light {
    int type;       // 0 = Directional, 1 = Point
    mediump vec3 position;
    mediump vec3 direction;
    mediump vec3 color;
};

uniform Light lights[MAX_LIGHTS];
uniform int numLights;

// Decal
uniform bool hasDecal;
uniform sampler2D decalSampler;
uniform vec2 decalPosition;
uniform vec2 decalScale;
uniform int decalSurfaceMask;

// Selection
uniform bool isSelected;
uniform mediump vec4 selectionColor;

// Fog uniforms
uniform mediump vec3 fogColor;
uniform float fogDensity;
uniform bool fogEnabled;

uniform bool forceWhite;
uniform bool enableRoughnessBump;
uniform bool hideInnerSurfaces;
uniform bool xrayMode;
uniform sampler2D shadowMap;
uniform bool shadowsEnabled;
uniform int shadowLightIndex;
uniform float shadowStrength;

// --- Simplex Noise (from simplex_noise.glsl) ---
vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
  return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v)
  {
  const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
                      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
                     -0.577350269189626,  // -1.0 + 2.0 * C.x
                      0.024390243902439); // 1.0 / 41.0
// First corner
  vec2 i  = floor(v + dot(v, C.yy) );
  vec2 x0 = v -   i + dot(i, C.xx);

// Other corners
  vec2 i1;
  i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
  vec4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;

// Permutations
  i = mod289(i); // Avoid truncation effects in permutation
  vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
		+ i.x + vec3(0.0, i1.x, 1.0 ));

  vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
  m = m*m;
  m = m*m;

// Gradients
  vec3 x = 2.0 * fract(p * C.www) - 1.0;
  vec3 h = abs(x) - 0.5;
  vec3 ox = floor(x + 0.5);
  vec3 a0 = x - ox;
  m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

// Compute final noise value
  vec3 g;
  g.x  = a0.x  * x0.x  + h.x  * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}

// --- FBM (Fractional Brownian Motion) for more complex noise ---
float fbm(vec2 v) {
    float total = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;
    int octaves = 2;
    float persistence = 0.5;

    for (int i = 0; i < octaves; i++) {
        total += snoise(v * frequency) * amplitude;
        frequency *= 2.0;
        amplitude *= persistence;
    }
    return total;
}

vec3 getSurfaceNormal(vec3 normal, vec3 fragPos, vec2 texCoord) {
    if (roughnessStrength <= 0.0) return normal;

    vec3 tangent = normalize(cross(normal, vec3(0.0, 1.0, 0.0)));
    if (length(tangent) < 0.1) tangent = normalize(cross(normal, vec3(1.0, 0.0, 0.0)));
    vec3 bitangent = normalize(cross(normal, tangent));

    // Use the texture coordinates to drive the noise, making it independent of object position
    vec2 noiseCoord = texCoord * roughnessScale;
    
    // Use FBM for more detailed noise
    float noise = fbm(noiseCoord);

    // Use central difference to get the gradient of the noise
    float offset = 0.01;
    float noise_dx = fbm(noiseCoord + vec2(offset, 0.0)) - noise;
    float noise_dy = fbm(noiseCoord + vec2(0.0, offset)) - noise;

    // The bump vector is the gradient of the noise
    vec2 bumpVec = vec2(noise_dx, noise_dy);

    return normalize(normal + (tangent * bumpVec.x + bitangent * bumpVec.y) * roughnessStrength);
}

float calculateShadow(vec3 normal, vec3 lightDir) {
    if (!shadowsEnabled) {
        return 0.0;
    }

    vec3 projCoords = v_lightSpacePos.xyz / v_lightSpacePos.w;
    projCoords = projCoords * 0.5 + 0.5;

    if (projCoords.z > 1.0 || projCoords.x < 0.0 || projCoords.x > 1.0 || projCoords.y < 0.0 || projCoords.y > 1.0) {
        return 0.0;
    }

    float bias = max(0.0015 * (1.0 - dot(normal, lightDir)), 0.0004);
    float currentDepth = projCoords.z - bias;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    vec2 poissonDisk[12] = vec2[](
        vec2(-0.326, -0.406),
        vec2(-0.840, -0.074),
        vec2(-0.696,  0.457),
        vec2(-0.203,  0.621),
        vec2( 0.962, -0.195),
        vec2( 0.473, -0.480),
        vec2( 0.519,  0.767),
        vec2( 0.185, -0.893),
        vec2( 0.507,  0.064),
        vec2( 0.896,  0.412),
        vec2(-0.322, -0.933),
        vec2(-0.792, -0.598)
    );
    float poissonWeights[12] = float[](
        1.00, 0.92, 0.88, 0.84,
        0.82, 0.80, 0.78, 0.74,
        0.72, 0.68, 0.64, 0.60
    );
    float shadow = 0.0;
    float totalWeight = 0.0;
    float filterRadius = 1.8;
    for (int i = 0; i < 12; ++i) {
        vec2 sampleUv = projCoords.xy + poissonDisk[i] * texelSize * filterRadius;
        float closestDepth = texture(shadowMap, sampleUv).r;
        float sampleShadow = currentDepth > closestDepth ? 1.0 : 0.0;
        shadow += sampleShadow * poissonWeights[i];
        totalWeight += poissonWeights[i];
    }
    shadow /= totalWeight;
    return shadow * shadowStrength;
}


void main()
{
    if (forceWhite) {
        FragColor = vec4(1.0); // Output solid white
        return;
    }

    // --- Handle Wireframe as a separate, early-exit case ---
    if (renderStyle == 2) { // 2 is the ordinal for WIREFRAME
        vec4 finalColor = vec4(objectColor, opacity);
        if (isSelected) {
            finalColor = mix(finalColor, selectionColor, 0.1);
        }
        FragColor = finalColor;
        return; // Exit immediately
    }

    // 2 = DECAL_SURFACE_INSIDE (RenderingConstants.DECAL_SURFACE_INSIDE)
    if (hideInnerSurfaces && v_surfaceID == 2) {
        discard;
    }
    // ----------------------------------------------------------------

    // --- Logic for solid (non-wireframe) objects ---

    // 1. Get base surface color
    vec4 surfaceColor = vec4(objectColor, 1.0);

    // Texture handling
    if (renderStyle == 1 && hasTexture == 1) { // 1 = TEXTURED
        vec2 finalTexCoord = v_texCoord; 

        if (textureMode == 0) { // STRETCH
        } else if (textureMode == 1) { // REPEAT_AXIAL
            finalTexCoord.x = v_texCoord.x;
            finalTexCoord.y = v_texCoord.y;
        } else if (textureMode == 2) { // REPEAT_RADIAL
            finalTexCoord.x = v_texCoord.x;
            finalTexCoord.y = v_texCoord.y;
        } else if (textureMode == 3) { // REPEAT_BOTH
            finalTexCoord = v_texCoord;
        }

        vec2 transformedTexCoord = (textureTransformMatrix * vec4(finalTexCoord, 0.0, 1.0)).xy;
        vec4 texColor = texture(textureSampler, transformedTexCoord);

        surfaceColor.rgb = mix(surfaceColor.rgb, texColor.rgb, texColor.a);
    }

    // 2. Apply decal
    if (hasDecal) {
        int currentSurfaceBit = 1 << v_surfaceID;
        if ((decalSurfaceMask & currentSurfaceBit) > 0) {
            vec2 transformedDecalCoord = (decalTransformMatrix * vec4(v_texCoord, 0.0, 1.0)).xy;
            vec4 decalColor = texture(decalSampler, transformedDecalCoord);
            surfaceColor.rgb = mix(surfaceColor.rgb, decalColor.rgb, decalColor.a);
        }
    }

    // 3. Apply lighting
    vec3 finalColor;
    if (isUnlit) {
        finalColor = surfaceColor.rgb;
    } else {
        vec3 norm = normalize(v_normal);
        if (enableRoughnessBump) {
            norm = getSurfaceNormal(norm, v_fragPos, v_texCoord);
        }
        vec3 viewDir = normalize(viewPos - v_fragPos);

        vec3 ambient = ambientLightFactor * surfaceColor.rgb;
        vec3 totalLighting = vec3(0.0);

        for (int i = 0; i < numLights; i++) {
            Light currentLight = lights[i];
            vec3 lightDir;
            float attenuation = 1.0;

            if (currentLight.type == 0) { // Directional Light
                lightDir = normalize(-currentLight.direction);
            } else { // Point Light
                vec3 lightVector = currentLight.position - v_fragPos;
                float distance = length(lightVector);
                lightDir = lightVector / distance;
                
                attenuation = 1.0 / (1.0 + 0.09 * distance + 0.032 * distance * distance);
                
                if (distance > 50.0) {
                    attenuation = 0.0;
                }
            }

            // Diffuse
            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * currentLight.color * attenuation;

            // Specular
            vec3 specular = vec3(0.0);
            if (shine > 0.0) {
                vec3 reflectDir = reflect(-lightDir, norm);
                float spec_power = shine * 256.0 + 1.0;
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), spec_power);
            vec3 tintedSpecularColor = mix(materialSpecular, surfaceColor.rgb, specularTintFactor);
            specular = shine * spec * tintedSpecularColor * currentLight.color * attenuation;
        }

            vec3 lightContribution = diffuse * surfaceColor.rgb + specular;
            if (shadowsEnabled && shadowLightIndex == i && currentLight.type == 0) {
                float shadow = calculateShadow(norm, lightDir);
                lightContribution *= (1.0 - shadow);
            }

            totalLighting += lightContribution;
        }

        finalColor = ambient + totalLighting;
    }

    // Final color with opacity
    vec4 finalColorRGBA = vec4(finalColor, surfaceColor.a * opacity);

    if (xrayMode && !gl_FrontFacing) {
        finalColorRGBA.rgb *= 0.7;
        finalColorRGBA.a = 1.0;
    }

    // 4. Apply selection
    if (isSelected) {
        finalColorRGBA = mix(finalColorRGBA, selectionColor, 0);
    }

    // 5. Apply fog
    if (fogEnabled) {
        float fogFactor = exp(-pow(v_eyeSpaceZ * fogDensity, 2.0));
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        finalColorRGBA = mix(vec4(fogColor, 1.0), finalColorRGBA, fogFactor);
    }

    FragColor = finalColorRGBA;
}
