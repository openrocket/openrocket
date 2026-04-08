#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D screenTexture;
uniform sampler2D depthTexture;
uniform mat4 projection;
uniform mat4 inverseProjection;
uniform vec2 screenSize;
uniform float radius;
uniform float strength;
uniform float bias;
uniform int sampleCount;

// 32 Fibonacci-hemisphere samples biased toward the surface center.
// Closer samples catch fine contact shadows; distant ones catch broader occlusion.
const int MAX_SAMPLES = 32;
const vec3 SAMPLE_KERNEL[MAX_SAMPLES] = vec3[](
    vec3( 0.017763,  0.000000,  0.099303),
    vec3(-0.023095, -0.021157,  0.098663),
    vec3( 0.003656,  0.041654,  0.099480),
    vec3( 0.031559, -0.041163,  0.101587),
    vec3(-0.061417,  0.010864,  0.104820),
    vec3( 0.062261,  0.039605,  0.109015),
    vec3(-0.022438, -0.083469,  0.114006),
    vec3(-0.046327,  0.089201,  0.119629),
    vec3( 0.109145, -0.039859,  0.125719),
    vec3(-0.123495, -0.050977,  0.132111),
    vec3( 0.064778,  0.138427,  0.138640),
    vec3( 0.052065, -0.165993,  0.145142),
    vec3(-0.170498,  0.098807,  0.151451),
    vec3( 0.216973,  0.047701,  0.157404),
    vec3(-0.143370, -0.203930,  0.162834),
    vec3(-0.035786,  0.276156,  0.167578),
    vec3( 0.236815, -0.199588,  0.171471),
    vec3(-0.342705, -0.014172,  0.174347),
    vec3( 0.268174,  0.266869,  0.176042),
    vec3(-0.019201, -0.415242,  0.176392),
    vec3(-0.291537,  0.349359,  0.175230),
    vec3( 0.491870, -0.066180,  0.172394),
    vec3(-0.442830, -0.308110,  0.167717),
    vec3( 0.128281,  0.570220,  0.161035),
    vec3( 0.313836, -0.547685,  0.152184),
    vec3(-0.647516,  0.206575,  0.140997),
    vec3( 0.662415,  0.306053,  0.127312),
    vec3(-0.301599, -0.720656,  0.110962),
    vec3(-0.282312,  0.784899,  0.091783),
    vec3( 0.786307, -0.413261,  0.069611),
    vec3(-0.912424, -0.240512,  0.044279),
    vec3( 0.540772,  0.841024,  0.015625)
);

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 reconstructViewPosition(vec2 uv, float depth) {
    float z = depth * 2.0 - 1.0;
    vec4 clip = vec4(uv * 2.0 - 1.0, z, 1.0);
    vec4 view = inverseProjection * clip;
    return view.xyz / max(view.w, 1e-6);
}

vec3 sampleViewPosition(vec2 uv, vec3 fallback) {
    float depth = texture(depthTexture, uv).r;
    if (depth >= 0.9999) {
        return fallback;
    }
    return reconstructViewPosition(uv, depth);
}

vec3 reconstructNormal(vec2 uv, vec3 centerPos) {
    vec2 texel = 1.0 / screenSize;
    vec3 rightPos = sampleViewPosition(clamp(uv + vec2(texel.x, 0.0), 0.0, 1.0), centerPos);
    vec3 leftPos = sampleViewPosition(clamp(uv - vec2(texel.x, 0.0), 0.0, 1.0), centerPos);
    vec3 upPos = sampleViewPosition(clamp(uv + vec2(0.0, texel.y), 0.0, 1.0), centerPos);
    vec3 downPos = sampleViewPosition(clamp(uv - vec2(0.0, texel.y), 0.0, 1.0), centerPos);

    vec3 dx = abs(rightPos.z - centerPos.z) < abs(centerPos.z - leftPos.z)
        ? rightPos - centerPos
        : centerPos - leftPos;
    vec3 dy = abs(upPos.z - centerPos.z) < abs(centerPos.z - downPos.z)
        ? upPos - centerPos
        : centerPos - downPos;

    if (length(dx) < 1e-4 || length(dy) < 1e-4) {
        return vec3(0.0, 0.0, 1.0);
    }

    vec3 normal = normalize(cross(dx, dy));
    if (normal.z < 0.0) {
        normal = -normal;
    }
    return normal;
}

void main() {
    vec4 sceneColor = texture(screenTexture, TexCoords);
    float centerDepth = texture(depthTexture, TexCoords).r;
    if (centerDepth >= 0.9999) {
        FragColor = sceneColor;
        return;
    }

    vec3 centerPos = reconstructViewPosition(TexCoords, centerDepth);
    vec3 normal = reconstructNormal(TexCoords, centerPos);

    float angle = hash12(TexCoords * screenSize) * 6.2831853;
    vec2 rotation = vec2(cos(angle), sin(angle));
    vec3 randomVec = normalize(vec3(rotation, 0.0));

    vec3 tangent = randomVec - normal * dot(randomVec, normal);
    if (length(tangent) < 0.001) {
        tangent = cross(normal, vec3(0.0, 1.0, 0.0));
        if (length(tangent) < 0.001) {
            tangent = cross(normal, vec3(1.0, 0.0, 0.0));
        }
    }
    tangent = normalize(tangent);
    vec3 bitangent = normalize(cross(normal, tangent));
    mat3 tbn = mat3(tangent, bitangent, normal);

    float occlusion = 0.0;
    int validSamples = 0;
    for (int i = 0; i < MAX_SAMPLES; i++) {
        if (i >= sampleCount) {
            break;
        }

        vec3 samplePos = centerPos + (tbn * SAMPLE_KERNEL[i]) * radius;
        vec4 sampleClip = projection * vec4(samplePos, 1.0);
        if (sampleClip.w <= 0.0) {
            continue;
        }

        vec3 sampleNdc = sampleClip.xyz / sampleClip.w;
        vec2 sampleUv = sampleNdc.xy * 0.5 + 0.5;
        if (sampleUv.x < 0.0 || sampleUv.x > 1.0 || sampleUv.y < 0.0 || sampleUv.y > 1.0) {
            continue;
        }

        float sampleDepth = texture(depthTexture, sampleUv).r;
        if (sampleDepth >= 0.9999) {
            continue;
        }

        vec3 sampleViewPos = reconstructViewPosition(sampleUv, sampleDepth);

        // Smooth range attenuation — reject occluders that are too far away in depth
        float depthDiff = abs(centerPos.z - sampleViewPos.z);
        float rangeWeight = 1.0 - smoothstep(0.0, radius * 1.5, depthDiff);

        // Smooth occlusion falloff instead of hard 0/1 cutoff
        float occlusionDepth = sampleViewPos.z - samplePos.z - bias;
        float blocked = smoothstep(0.0, bias * 2.0, occlusionDepth);

        occlusion += blocked * rangeWeight;
        validSamples++;
    }

    float divisor = float(max(validSamples, 1));
    float ao = 1.0 - strength * (occlusion / divisor);
    ao = clamp(ao, 0.15, 1.0);

    FragColor = vec4(sceneColor.rgb * ao, sceneColor.a);
}
