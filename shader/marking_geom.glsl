/* GEOMETRY SHADER */

#version 330

layout(lines) in;
layout(line_strip, max_vertices = 2) out;

uniform float width;
uniform float height;

bool isInBounds(vec4 p){
    return p.x >= -width && p.x <= width && p.y >= -height && p.y <= height;
}


void main() {
    bool is_visible = isInBounds(gl_in[0].gl_Position) || isInBounds(gl_in[1].gl_Position);
    if(is_visible){
        gl_Position = gl_in[0].gl_Position;
        EmitVertex();
        gl_Position = gl_in[1].gl_Position;
        EmitVertex();
    }

    EndPrimitive();
}
