#version 140

out vec4 FragColor;

uniform vec4 bgColor; // The user-defined background color (including alpha)
uniform float scale;  // The scale of the checkerboard squares

// Generates a standard grey-and-white checkerboard pattern.
vec4 checkerboard() {
    vec2 pos = gl_FragCoord.xy / scale;
    float check = mod(floor(pos.x) + floor(pos.y), 2.0);
    vec3 color1 = vec3(0.8, 0.8, 0.8); // Light grey
    vec3 color2 = vec3(0.6, 0.6, 0.6); // Dark grey
    return vec4(mix(color2, color1, check), 1.0);
}

void main() {
    vec4 checker = checkerboard();
    // Blend the user's background color over the checkerboard pattern.
    // The amount of blending is controlled by the alpha of the background color.
    FragColor = mix(checker, vec4(bgColor.rgb, 1.0), bgColor.a);
}