#version 330
#extension GL_EXT_gpu_shader4: enable

uniform usampler2D texture;

in vec2 texCoord;

layout(location=0) out vec3 count;

void main(void) {
   uvec4 bitmask = texture2D(texture, texCoord);
   count = vec3(0.0);
   for(uint i = 0u; i < 4u; i++) {
        for(uint j = 0u; j < 32u; j++) {
            uint mask = 1u << j;
            if((mask & bitmask[i]) != 0u) {
                count.r++;
            }
        }
   }
}