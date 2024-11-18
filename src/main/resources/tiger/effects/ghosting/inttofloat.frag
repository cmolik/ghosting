#version 330
#extension GL_EXT_gpu_shader4: enable

uniform usampler2D texture;

in vec2 texCoord;

uniform int idToShow;

layout(location=0) out vec4 fragColor;

void main() {
    uvec4 X = texture(texture, texCoord);
    uint bit = uint(idToShow) % 32u;
    uint index = uint(idToShow) / 32u;
    uint mask = 1u << bit;
    if((X[index] & mask) == 0u) {
        discard;
    }
    else {
        fragColor = vec4(0.0, 1.0, 0.0, 1.0);
    }
}

