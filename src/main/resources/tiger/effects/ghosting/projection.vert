#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

layout(location=0) in vec3 vertex;
layout(location=1) in vec3 normal;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * vec4(vertex, 1.0);
}