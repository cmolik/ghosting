#version 150 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform sampler2D id;
flat out vec3 index;
flat out vec3 check;

void main() {
    gl_Position = 2.0*gl_Vertex - 1.0;
    vec4 texel = texture2D(id, gl_Vertex.xy);
    index = texel.rgb;
    if(index.g > 0.0) check = vec3(0.0, 1.0, 0.0);
    else check = vec3(1.0, 0.0, 0.0);
    if(texel.a != 1.0) gl_Position = vec4(2.0, 0.0, 0.0, 1.0);
}