#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

flat in uvec4 id;

void main() {
    //calculate mask from position of the fragment
    uvec4 mask = uvec4(0u);
    uint objectId = uint(gl_FragCoord.x - 0.5);
    uint index = objectId / 32u;  
    uint bit = objectId - 32u * (objectId/32u);
    mask[index] = 1u << bit;
    //test if the the mask is in id - if no then discard
    if((id & mask) == uvec4(0u)) discard;
    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}


