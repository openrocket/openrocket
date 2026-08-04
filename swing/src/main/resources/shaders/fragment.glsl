#version 330 core

// Output color
out vec4 FragColor;

// Inputs from vertex shader
in mediump vec3 v_fragPos;
in mediump vec3 v_normal;
in mediump vec3 v_objectPos;
in mediump vec3 v_objectNormal;
in mediump vec2 v_texCoord;
flat in int v_surfaceID;
in mediump float v_eyeSpaceZ;
in mediump vec4 v_lightSpacePos;

// Uniforms
uniform int renderStyle; // 0:COLOR_ONLY, 1:TEXTURE_ONLY, 2:WIREFRAME
uniform mediump vec3 objectColor;
uniform int hasTexture;
uniform sampler2D textureSampler;
uniform mat4 model;
// transpose(inverse(mat3(model))), supplied by the CPU rather than derived here:
// a matrix inverse per fragment recomputes a value that is constant for the draw call.
uniform mat3 normalMatrix;
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
uniform bool textureOpacityAffectsAlpha;
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
// 0 = regular scene color, 1 = weighted transparency accumulation,
// 2 = weighted transparency revealage, 3 = opaque texture coverage only.
uniform int transparencyOutputMode;
uniform sampler2DShadow shadowMap;
uniform bool shadowsEnabled;
uniform int shadowLightIndex;
uniform float shadowStrength;

// Object-space value noise avoids UV seams and stretching across different mappings.
float hash13(vec3 p) {
    vec3 p3 = fract(p * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float valueNoise3D(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);

    float n000 = hash13(i + vec3(0.0, 0.0, 0.0));
    float n100 = hash13(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash13(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash13(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash13(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash13(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash13(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash13(i + vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, u.x);
    float nx10 = mix(n010, n110, u.x);
    float nx01 = mix(n001, n101, u.x);
    float nx11 = mix(n011, n111, u.x);
    float nxy0 = mix(nx00, nx10, u.y);
    float nxy1 = mix(nx01, nx11, u.y);
    return mix(nxy0, nxy1, u.z);
}

float ridgedNoise3D(vec3 p) {
    return 1.0 - abs(2.0 * valueNoise3D(p) - 1.0);
}

vec3 rotateNoise3D(vec3 p) {
    return mat3(
         0.00,  0.80,  0.60,
        -0.80,  0.36, -0.48,
        -0.60, -0.48,  0.64
    ) * p;
}

/**
 * Layered surface grain.
 *
 * footprint is how much of this noise's own coordinate space one pixel covers. Each octave
 * is roughly twice as fine as the one before, so the later ones fall below the size of a
 * pixel long before the loop ends. Those cannot be resolved: they contribute no visible
 * texture, only a crawling sparkle as the view turns. Fading each octave out as it reaches
 * that limit keeps the surface steady under rotation, and lets the loop stop early once
 * nothing further can be seen.
 */
float granularHeight(vec3 noisePos, float footprint) {
    vec3 warped = noisePos;
    warped += vec3(
        valueNoise3D(rotateNoise3D(noisePos * 0.55 + vec3(2.3, 5.1, 7.4))),
        valueNoise3D(rotateNoise3D(noisePos.yzx * 0.50 + vec3(9.2, 1.8, 4.6))),
        valueNoise3D(rotateNoise3D(noisePos.zxy * 0.60 + vec3(3.7, 8.9, 6.1)))
    ) - 0.5;
    warped += (vec3(
        valueNoise3D(rotateNoise3D(noisePos * 1.3 + vec3(13.1, 4.7, 11.5))),
        valueNoise3D(rotateNoise3D(noisePos.zxy * 1.2 + vec3(6.3, 12.8, 2.4))),
        valueNoise3D(rotateNoise3D(noisePos.yzx * 1.4 + vec3(15.4, 9.6, 5.7)))
    ) - 0.5) * 0.28;

    vec3 octavePos = warped;
    float amplitude = 0.50;
    float total = 0.0;
    float weight = 0.0;

    float octaveFootprint = footprint;

    for (int octave = 0; octave < 7; ++octave) {
        float octaveFade = 1.0 - smoothstep(0.40, 0.90, octaveFootprint);
        if (octaveFade <= 0.001) {
            break;
        }

        float base = valueNoise3D(octavePos + vec3(17.0, 9.0, 13.0));
        float ridge = ridgedNoise3D(octavePos * 1.17 + vec3(5.0, 23.0, 31.0));
        float layer = mix(base, ridge, octave < 2 ? 0.18 : 0.38);
        total += layer * amplitude * octaveFade;
        weight += amplitude * octaveFade;

        octavePos = rotateNoise3D(octavePos * 2.05 + vec3(11.3, 17.1, 7.7));
        amplitude *= 0.58;
        octaveFootprint *= 2.05;
    }

    float sparkleFade = 1.0 - smoothstep(0.40, 0.90, octaveFootprint);
    if (sparkleFade > 0.001) {
        float sparkle = valueNoise3D(rotateNoise3D(octavePos * 1.9 + vec3(29.0, 47.0, 19.0)));
        total += sparkle * 0.08 * sparkleFade;
        weight += 0.08 * sparkleFade;
    }

    return pow(clamp(total / max(weight, 1e-4), 0.0, 1.0), 1.22);
}

vec2 getMaterialTexCoord() {
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

    return (textureTransformMatrix * vec4(finalTexCoord, 0.0, 1.0)).xy;
}

float adjustTextureCoverage(float alpha) {
    // Thin line-art textures lose contrast quickly under mip filtering in the linear pipeline.
    // Bias alpha slightly upward so narrow dark features stay closer to the legacy look.
    return clamp(pow(alpha, 0.82), 0.0, 1.0);
}

vec3 getSurfaceNormal(vec3 normal) {
    if (roughnessStrength <= 0.0 || roughnessScale <= 0.0) return normal;

    vec3 objectNormal = normalize(v_objectNormal);
    vec3 objectAxis = abs(objectNormal.y) < 0.85 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 objectTangent = normalize(cross(objectAxis, objectNormal));
    if (length(objectTangent) < 1e-4) {
        objectTangent = normalize(cross(vec3(0.0, 0.0, 1.0), objectNormal));
    }
    vec3 objectBitangent = normalize(cross(objectNormal, objectTangent));

    vec3 transformedTangent = normalMatrix * objectTangent;
    transformedTangent -= normal * dot(normal, transformedTangent);
    vec3 tangent;
    if (length(transformedTangent) < 1e-4) {
        vec3 worldAxis = abs(normal.y) < 0.85 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
        tangent = normalize(cross(worldAxis, normal));
    } else {
        tangent = normalize(transformedTangent);
    }

    vec3 transformedBitangent = normalMatrix * objectBitangent;
    transformedBitangent -= normal * dot(normal, transformedBitangent);
    vec3 bitangent = length(transformedBitangent) < 1e-4
        ? normalize(cross(normal, tangent))
        : normalize(transformedBitangent);
    if (dot(cross(tangent, bitangent), normal) < 0.0) {
        bitangent = -bitangent;
    }

    // roughnessScale is the grain frequency in cells per world unit, set from the
    // component's surface finish.
    vec3 noisePos = v_objectPos * roughnessScale;

    // granularHeight is by far the most expensive thing this shader does — seven
    // octaves plus two warp stages, each octave a pair of 3D value-noise lookups —
    // and sampling it once per axis to difference the height made it the single
    // largest cost in the frame. One sample is enough: the hardware already
    // computes derivatives across each 2x2 quad, so the same slope can be recovered
    // from how the height and the sample position vary across the quad.
    //
    // dFdx(height) alone would be a screen-space slope, which would strengthen the
    // bump as the surface is magnified. Mapping it back through the derivatives of
    // noisePos removes that dependency and leaves the same surface-space gradient
    // the differencing produced.
    vec3 noiseDx = dFdx(noisePos);
    vec3 noiseDy = dFdy(noisePos);

    // Fade the grain out once a pixel spans an appreciable part of a noise cell. Below that
    // size the detail cannot be resolved, so it stops reading as texture and instead crawls
    // and sparkles as the view turns. Fading it is both steadier and cheaper than drawing
    // detail that is smaller than the pixels showing it.
    float pixelFootprint = max(length(noiseDx), length(noiseDy));
    float detailFade = 1.0 - smoothstep(0.35, 1.0, pixelFootprint);
    if (detailFade <= 0.0) {
        return normal;
    }

    float height = granularHeight(noisePos, pixelFootprint);
    mat2 screenToSurface = mat2(
        dot(noiseDx, objectTangent), dot(noiseDy, objectTangent),
        dot(noiseDx, objectBitangent), dot(noiseDy, objectBitangent));

    vec2 bumpVec = vec2(0.0);
    if (abs(determinant(screenToSurface)) > 1e-12) {
        bumpVec = inverse(screenToSurface) * vec2(dFdx(height), dFdy(height));
    }
    bumpVec = clamp(bumpVec, vec2(-1.5), vec2(1.5));

    float bumpAmplitude = roughnessStrength * 0.06 * detailFade;
    return normalize(normal - (tangent * bumpVec.x + bitangent * bumpVec.y) * bumpAmplitude);
}

float calculateShadow(vec3 normal, vec3 lightDir) {
    if (!shadowsEnabled) {
        return 0.0;
    }

    vec3 projCoords = v_lightSpacePos.xyz / v_lightSpacePos.w;
    projCoords = projCoords * 0.5 + 0.5;

    if (projCoords.z <= 0.0 || projCoords.z >= 1.0 ||
        projCoords.x <= 0.0 || projCoords.x >= 1.0 ||
        projCoords.y <= 0.0 || projCoords.y >= 1.0) {
        return 0.0;
    }

    float ndotl = max(dot(normal, lightDir), 0.0);
    float bias = max(0.0012 * (1.0 - ndotl), 0.00015);
    float receiverDepth = projCoords.z - bias;
    vec2 texelSize = 1.0 / vec2(textureSize(shadowMap, 0));
    vec2 poissonDisk[5] = vec2[](
        vec2( 0.000,  0.000),
        vec2( 0.557,  1.409),
        vec2(-1.602,  0.331),
        vec2( 1.287, -0.878),
        vec2(-0.462, -1.514)
    );
    float poissonWeights[5] = float[](
        0.38, 0.16, 0.16, 0.15, 0.15
    );
    float visibility = 0.0;
    float filterRadius = 1.35;
    for (int i = 0; i < 5; ++i) {
        vec2 sampleUv = projCoords.xy + poissonDisk[i] * texelSize * filterRadius;
        visibility += texture(shadowMap, vec3(sampleUv, receiverDepth)) * poissonWeights[i];
    }

    return (1.0 - visibility) * shadowStrength;
}

void writeFragment(vec4 color) {
    const float opaqueThreshold = 1.0 - 1e-5;
    if (transparencyOutputMode == 3) {
        if (color.a < opaqueThreshold) {
            discard;
        }
        FragColor = vec4(color.rgb, 1.0);
        return;
    }
    // Fully opaque texture coverage was already written to the regular scene target.
    // Excluding it here prevents WBOIT from averaging surfaces that it should occlude.
    if (transparencyOutputMode == 1 || transparencyOutputMode == 2) {
        if (color.a >= opaqueThreshold) {
            discard;
        }
    }
    if (transparencyOutputMode == 1) {
        float alpha = clamp(color.a, 0.0, 1.0);
        float colorImportance = max(
            min(1.0, max(max(color.r, color.g), color.b) * alpha),
            alpha);
        // Weighted, blended order-independent transparency. Eye-space depth is
        // positive and measured in OpenRocket world units (one unit is 50 mm). The weight
        // favours the nearer layer without requiring a fragile object/triangle sort.
        float depthWeight = clamp(
            0.03 / (1e-5 + pow(max(v_eyeSpaceZ, 0.0) / 20.0, 4.0)),
            1e-2,
            3e3);
        float weight = colorImportance * depthWeight;
        FragColor = vec4(color.rgb * alpha, alpha) * weight;
        return;
    }
    if (transparencyOutputMode == 2) {
        // The revealage attachment uses (ZERO, ONE_MINUS_SRC_ALPHA), so only
        // the source alpha matters here.
        FragColor = vec4(0.0, 0.0, 0.0, clamp(color.a, 0.0, 1.0));
        return;
    }
    FragColor = color;
}


void main()
{
    if (forceWhite) {
        writeFragment(vec4(1.0)); // Output solid white
        return;
    }

    // --- Handle Wireframe as a separate, early-exit case ---
    if (renderStyle == 2) { // 2 is the ordinal for WIREFRAME
        vec4 finalColor = vec4(objectColor, opacity);
        if (isSelected) {
            finalColor = mix(finalColor, selectionColor, 0.1);
        }
        writeFragment(finalColor);
        return; // Exit immediately
    }

    // 1 = RenderingConstants.SURFACE_ID_INSIDE
    if (hideInnerSurfaces && v_surfaceID == 1) {
        discard;
    }
    // ----------------------------------------------------------------

    // --- Logic for solid (non-wireframe) objects ---

    // 1. Get base surface color
    vec4 surfaceColor = vec4(objectColor, 1.0);
    vec2 materialTexCoord = getMaterialTexCoord();
    float materialAlpha = opacity;

    // Texture handling
    if (renderStyle == 1 && hasTexture == 1) { // 1 = TEXTURED
        vec4 texColor = texture(textureSampler, materialTexCoord);
        float textureAlpha = adjustTextureCoverage(texColor.a);
        if (textureOpacityAffectsAlpha) {
            surfaceColor.rgb = mix(surfaceColor.rgb, texColor.rgb, textureAlpha);
        } else {
            // Treat the texture as a layer over the translucent component paint. This
            // keeps both its alpha and its colour independent of component opacity.
            float baseContribution = opacity * (1.0 - textureAlpha);
            materialAlpha = textureAlpha + baseContribution;
            if (materialAlpha > 1e-5) {
                surfaceColor.rgb = (texColor.rgb * textureAlpha
                    + surfaceColor.rgb * baseContribution) / materialAlpha;
            }
        }
    }

    // 2. Apply decal
    if (hasDecal) {
        int currentSurfaceBit = 1 << v_surfaceID;
        if ((decalSurfaceMask & currentSurfaceBit) > 0) {
            vec2 transformedDecalCoord = (decalTransformMatrix * vec4(v_texCoord, 0.0, 1.0)).xy;
            vec4 decalColor = texture(decalSampler, transformedDecalCoord);
            float decalAlpha = adjustTextureCoverage(decalColor.a);
            surfaceColor.rgb = mix(surfaceColor.rgb, decalColor.rgb, decalAlpha);
        }
    }

    // 3. Apply lighting
    vec3 finalColor;
    if (isUnlit) {
        finalColor = surfaceColor.rgb;
    } else {
        vec3 norm = normalize(v_normal);
        if (enableRoughnessBump) {
            norm = getSurfaceNormal(norm);
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
    float finalAlpha = surfaceColor.a * materialAlpha;
    vec4 finalColorRGBA = vec4(finalColor, finalAlpha);

    if (xrayMode && !gl_FrontFacing) {
        finalColorRGBA.rgb *= 0.78;
    }

    // Selection feedback for shaded geometry is handled by OutlinePass.

    // 5. Apply fog
    if (fogEnabled) {
        float fogFactor = exp(-pow(v_eyeSpaceZ * fogDensity, 2.0));
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        finalColorRGBA.rgb = mix(fogColor, finalColorRGBA.rgb, fogFactor);
    }

    writeFragment(finalColorRGBA);
}
