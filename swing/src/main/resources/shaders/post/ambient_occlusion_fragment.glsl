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

const int MAX_SAMPLES = 16;
const vec3 SAMPLE_KERNEL[MAX_SAMPLES] = vec3[](
    vec3(0.141421, 0.000000, 0.989949),
    vec3(-0.099014, 0.171499, 0.980581),
    vec3(0.000000, -0.242536, 0.970143),
    vec3(0.281718, 0.281718, 0.917663),
    vec3(-0.359092, -0.130710, 0.924500),
    vec3(0.131876, 0.492996, 0.859971),
    vec3(-0.516398, 0.188982, 0.835629),
    vec3(0.566947, -0.206284, 0.797724),
    vec3(-0.221766, -0.607549, 0.762001),
    vec3(0.401610, 0.695608, 0.596550),
    vec3(-0.760885, -0.277124, 0.586443),
    vec3(0.786334, 0.286185, 0.547723),
    vec3(-0.294886, 0.810115, 0.506370),
    vec3(0.094916, -0.875205, 0.474579),
    vec3(-0.905255, 0.329442, 0.267261),
    vec3(0.930758, -0.338697, 0.137361)
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
        float rangeWeight = smoothstep(0.0, 1.0, radius / (abs(centerPos.z - sampleViewPos.z) + 1e-4));
        float normalWeight = max(dot(normal, normalize(sampleViewPos - centerPos)), 0.0);
        float blocked = sampleViewPos.z >= samplePos.z + bias ? 1.0 : 0.0;
        occlusion += blocked * rangeWeight * normalWeight;
    }

    float ao = 1.0 - strength * (occlusion / float(max(sampleCount, 1)));
    ao = clamp(ao, 0.20, 1.0);

    FragColor = vec4(sceneColor.rgb * ao, sceneColor.a);
}
