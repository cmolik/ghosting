#version 330
#extension GL_EXT_gpu_shader4: enable

uniform usampler2D id;

in vec2 texCoord;

layout(location=0) out uvec4 bitmask;

void main(void) {
   bitmask = texture2D(id, texCoord);
}