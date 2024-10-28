#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform float imp;
uniform int redBlue;
uniform int labelingIndex;
uniform int labelingBit;

uniform uvec4 roi; //= uvec4(7u, 0u, 0u, 0u);
uniform int roiVis; // = 1;
uniform vec3 roiColor; 

out float D;
out vec3 N;
out vec4 C;

layout(location=0) in vec3 vertex;
layout(location=1) in vec3 normal;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * vec4(vertex, 1.0);
    if(redBlue == 1) {
        // blue/red vizualization used during selection
        if(imp > 0.3) {
            C.rgb = vec3(1.0, 0.5, 0.5);
        }
        else {
            C.rgb = vec3(0.8, 0.8, 1.0);
            //C = gl_Color;
        }
    }
    else if(roiVis == 1) {
        uvec4 id = uvec4(0u);
        id[labelingIndex] = 1u << uint(labelingBit);
        if((id & roi) != uvec4(0u)) {
            C.rgb = roiColor;
        }
        else {
            C = gl_Color;
        }
    }
    else {
        C = gl_Color;
    }

    D = (gl_Position.z/gl_Position.w + 1.0) * 0.5;
    N = (normalize(gl_NormalMatrix * normal) + 1.0) * 0.5;
}