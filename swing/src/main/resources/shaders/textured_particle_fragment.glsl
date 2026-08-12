#version 140
in vec2 texCoord;
in vec3 particleColor;
out vec4 FragColor;

uniform sampler2D smokeTexture;

void main()
{
    vec4 texColor = texture(smokeTexture, texCoord);
    
    // Combine texture alpha with particle color
    FragColor = vec4(particleColor * texColor.rgb, texColor.a);
    
    // Discard fully transparent pixels
    if (FragColor.a < 0.01) {
        discard;
    }
}