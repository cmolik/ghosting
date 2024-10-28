#version 330
#extension GL_EXT_gpu_shader4: enable

uniform sampler2D composition;

in vec2 texCoord;

layout(location=0) out vec4 fragColor;

void main(void) {
    fragColor = texture2D(composition, texCoord);
}