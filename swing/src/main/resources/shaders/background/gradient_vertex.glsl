#version 140

// A quad with vertices at (-1,-1), (1,-1), (-1,1), (1,1)
in vec2 aPos;

// Pass the vertex position to the fragment shader
out vec2 v_pos;

void main()
{
    v_pos = aPos;
    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
}
