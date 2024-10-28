#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform usampler2D id;

uniform float xOffset;
uniform float yOffset;

in vec2 texCoord;

void main() {

    uvec4 X = texture2D(id, texCoord);
    uvec4 A = texture2D(id, texCoord + vec2(xOffset, 0.0));
    uvec4 B = texture2D(id, texCoord + vec2(0.0, -yOffset));
    uvec4 C = texture2D(id, texCoord + vec2(-xOffset, 0.0));
    uvec4 D = texture2D(id, texCoord + vec2(0.0, yOffset));

    uvec4 Z = uvec4(0u);

    if(X != Z && (A == Z || B == Z || C == Z || D == Z)) {
        gl_FragColor = vec4(texCoord, 0.0, 0.0);
    }
    else {
        gl_FragColor = vec4(0.0, 0.0, 1.0, 0.0);
    }
}

