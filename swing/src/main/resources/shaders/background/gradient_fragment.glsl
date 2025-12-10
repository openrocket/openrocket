#version 330 core
out vec4 FragColor;

in vec2 v_pos;

uniform vec3 topColor;
uniform vec3 bottomColor;

void main()
{
    // Interpolate between top and bottom color based on the fragment's
    // normalized Y-coordinate (v\_pos.y goes from -1 to 1).
    float t = (v_pos.y + 1.0) / 2.0;
    vec3 finalColor = mix(bottomColor, topColor, t);
    FragColor = vec4(finalColor, 1.0);
}