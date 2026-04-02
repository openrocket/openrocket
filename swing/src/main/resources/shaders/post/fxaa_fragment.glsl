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
    vec4 rgbaNW = texture(tex, fragCoord + vec2(-1.0, -1.0) * rcpFrame);
    vec4 rgbaNE = texture(tex, fragCoord + vec2(1.0, -1.0) * rcpFrame);
    vec4 rgbaSW = texture(tex, fragCoord + vec2(-1.0, 1.0) * rcpFrame);
    vec4 rgbaSE = texture(tex, fragCoord + vec2(1.0, 1.0) * rcpFrame);
    vec4 rgbaM  = texture(tex, fragCoord);

    vec3 rgbNW = rgbaNW.xyz;
    vec3 rgbNE = rgbaNE.xyz;
    vec3 rgbSW = rgbaSW.xyz;
    vec3 rgbSE = rgbaSE.xyz;
    vec3 rgbM  = rgbaM.xyz;

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

    vec4 rgbaA = 0.5 * (
        texture(tex, fragCoord + dir * (1.0/3.0 - 0.5)) +
        texture(tex, fragCoord + dir * (2.0/3.0 - 0.5)));
    vec4 rgbaB = rgbaA * 0.5 + 0.25 * (
        texture(tex, fragCoord + dir * -0.5) +
        texture(tex, fragCoord + dir * 0.5));

    vec3 rgbA = rgbaA.xyz;
    vec3 rgbB = rgbaB.xyz;

    float lumaB = dot(rgbB, luma);
    if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
        return rgbaA;
    } else {
        return rgbaB;
    }
}

void main()
{
    vec2 rcpFrame = vec2(1.0/rt_w, 1.0/rt_h);
    FragColor = fxaa(screenTexture, TexCoords, rcpFrame);
}
