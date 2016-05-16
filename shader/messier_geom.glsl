/* GEOMETRY SHADER */

#version 330


layout(points) in;
layout(line_strip, max_vertices = 14) out;

uniform float width;
uniform float height;
uniform float zoom;
uniform float radius = 0.005;

in vec3 geom_color[];
out vec3 frag_color;

bool isInBounds(vec4 p){
    return p.x >= -width && p.x <= width && p.y >= -height && p.y <= height;
}

void main(){

    vec4 center = gl_in[0].gl_Position;
    if(isInBounds(center)){
       for(float i =  0; i <= 6.5; i += 0.5){
            float zoomed_radius = (zoom/2 + 1) * radius;
            float x = center.x + 1.3*zoomed_radius * cos(i) / width;
            float y = center.y + zoomed_radius * sin(i) / height;

            gl_Position = vec4(x, y, 0, 1);
            frag_color = geom_color[0];
            EmitVertex();
        }

    }
    EndPrimitive();
}