#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform float meshId;

//varying float D; 

void main() {
    gl_FragColor = vec4(meshId, 0.0, 0.0, 1.0);
}
