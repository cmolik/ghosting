#version 330 
#extension GL_EXT_gpu_shader4: enable

uniform int index;
uniform int bit;

layout(location=0) out uvec4 fragData0;

void main() {
    fragData0 = uvec4(0u);
    uint bitmask = 1u << uint(bit);
    fragData0[index] = bitmask;
}