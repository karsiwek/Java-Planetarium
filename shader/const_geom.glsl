#version 330

layout(lines) in;
layout(line_strip, max_vertices = 2) out;

uniform float width;
uniform float height;

in float draw[];

bool isInBounds(vec4 p){
    return p.x >= -width && p.x <= width && p.y >= -height && p.y <= height;
}

void main(){
    vec4 p1 = gl_in[0].gl_Position;
    vec4 p2 = gl_in[1].gl_Position;

    bool isVisible = draw[0] > 0.5 && draw[1] > 0.5;

    if(isVisible){
        gl_Position = p1;
        EmitVertex();

        gl_Position = p2;
        EmitVertex();

    }

    EndPrimitive();
}