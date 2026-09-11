#version 140
out vec4 FragColor;
in vec3 WorldPos;

uniform sampler2D equirectangularMap;
uniform float exposure = 1.0;

const vec2 invAtan = vec2(0.1591, 0.3183);

// Converts a 3D direction vector to a 2D UV coordinate on an equirectangular map
vec2 SampleSphere(vec3 v)
{
    vec2 uv = vec2(atan(v.z, v.x), asin(v.y));
    uv *= invAtan;
    uv += 0.5;
    return uv;
}

// ACES tone mapping
vec3 ACESFilm(vec3 x)
{
    float a = 2.51;
    float b = 0.03;
    float c = 2.43;
    float d = 0.59;
    float e = 0.14;
    return clamp((x*(a*x+b))/(x*(c*x+d)+e), 0.0, 1.0);
}

void main()
{
    vec2 uv = SampleSphere(normalize(WorldPos));
    uv.y = 1.0 - uv.y;
    vec3 color = texture(equirectangularMap, uv).rgb;

    // Apply exposure
    color *= exposure;

    // Tone mapping
    color = ACESFilm(color);

    // Keep the offscreen result linear. GL_FRAMEBUFFER_SRGB encodes this target,
    // and the final presentation pass performs the display transfer explicitly.
    FragColor = vec4(color, 1.0);
}
