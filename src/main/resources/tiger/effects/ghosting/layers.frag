#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

flat in vec3 index1;
layout(location=0) out uvec4 fragColor;

void main() {
    uint bit = uint(index1.g);
    uint mask = 1u << bit;
    fragColor = uvec4(mask, bit, 0.0, 0.0);
}