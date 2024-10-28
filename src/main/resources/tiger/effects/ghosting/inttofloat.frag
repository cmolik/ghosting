#version 330
#extension GL_EXT_gpu_shader4: enable

uniform usampler2D texture;

in vec2 texCoord;

uniform int showId;

layout(location=0) out vec4 fragColor;

void main() {
    uvec4 X = texture2D(texture, texCoord);
    uint bit = uint(showId) % 32u;
    uint index = uint(showId) / 32u;
    uint mask = 1u << bit;
    if((X[index] & mask) == 0u) {
        discard;
    }
    else {
        fragColor = vec4(0.0, 1.0, 0.0, 1.0);
    }
}

