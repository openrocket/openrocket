#version 330 core

// A quad with vertices at (-1,-1), (1,-1), (-1,1), (1,1)
layout (location = 0) in vec2 aPos;

// Pass the vertex position to the fragment shader
out vec2 v_pos;

void main()
{
    v_pos = aPos;
    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
}
