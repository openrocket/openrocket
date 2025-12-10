#version 330 core

// Vertex attributes from the VBO
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;
layout (location = 3) in float aSurfaceID_float; // Receive the packed float

// Outputs to the fragment shader
out mediump vec3 v_fragPos;
out mediump vec3 v_normal;
out mediump vec2 v_texCoord;
flat out int v_surfaceID; // Use 'flat' for integer varyings
out float v_eyeSpaceZ;
out mediump vec4 v_lightSpacePos;

// Transformation matrices
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceMatrix;

void main()
{
    vec4 worldPos = model * vec4(aPos, 1.0);
    v_fragPos = worldPos.xyz;
    gl_Position = projection * view * worldPos;
    v_lightSpacePos = lightSpaceMatrix * worldPos;
    v_normal = mat3(transpose(inverse(model))) * aNormal;
    v_texCoord = aTexCoords;
    v_surfaceID = floatBitsToInt(aSurfaceID_float); // Unpack float to int

    // Calculate the vertex position in eye space
    vec4 vertexPosEyeSpace = view * worldPos;
    v_eyeSpaceZ = -vertexPosEyeSpace.z;
}
