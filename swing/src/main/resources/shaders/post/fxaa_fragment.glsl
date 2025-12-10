#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D screenTexture;
uniform float rt_w; // reciprocal of screen width
uniform float rt_h; // reciprocal of screen height

#define FXAA_REDUCE_MIN   (1.0/128.0)
#define FXAA_REDUCE_MUL   (1.0/8.0)
#define FXAA_SPAN_MAX     8.0

vec4 fxaa(sampler2D tex, vec2 fragCoord, vec2 rcpFrame) {
    vec3 rgbNW = texture(tex, fragCoord + vec2(-1.0, -1.0) * rcpFrame).xyz;
    vec3 rgbNE = texture(tex, fragCoord + vec2(1.0, -1.0) * rcpFrame).xyz;
    vec3 rgbSW = texture(tex, fragCoord + vec2(-1.0, 1.0) * rcpFrame).xyz;
    vec3 rgbSE = texture(tex, fragCoord + vec2(1.0, 1.0) * rcpFrame).xyz;
    vec3 rgbM  = texture(tex, fragCoord).xyz;

    vec3 luma = vec3(0.299, 0.587, 0.114);
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM  = dot(rgbM,  luma);

    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

    vec2 dir = vec2(
        -((lumaNW + lumaNE) - (lumaSW + lumaSE)),
        ((lumaNW + lumaSW) - (lumaNE + lumaSE))
    );

    float dirReduce = max(
        (lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL),
        FXAA_REDUCE_MIN
    );

    float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);

    dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX),
          max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
          dir * rcpDirMin)) * rcpFrame;

    vec3 rgbA = 0.5 * (
        texture(tex, fragCoord + dir * (1.0/3.0 - 0.5)).xyz +
        texture(tex, fragCoord + dir * (2.0/3.0 - 0.5)).xyz);
    vec3 rgbB = rgbA * 0.5 + 0.25 * (
        texture(tex, fragCoord + dir * -0.5).xyz +
        texture(tex, fragCoord + dir * 0.5).xyz);

    float lumaB = dot(rgbB, luma);
    if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
        return vec4(rgbA, 1.0);
    } else {
        return vec4(rgbB, 1.0);
    }
}

void main()
{
    vec2 rcpFrame = vec2(1.0/rt_w, 1.0/rt_h);
    FragColor = fxaa(screenTexture, TexCoords, rcpFrame);
}
