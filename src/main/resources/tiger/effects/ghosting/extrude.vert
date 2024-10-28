#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

const float extrudeDist = 0.1;

void main() { 
    // Vertex transformation 
    vec4 v = ftransform();
    vec3 n = normalize(gl_NormalMatrix * gl_Normal);
    gl_Position = v + vec4(n * extrudeDist, 0.0);
    
}