#version 150 compatibility
#extension GL_EXT_geometry_shader4 : enable
#extension GL_EXT_texture_array: enable

flat in vec3[] index;
flat out vec3 index1;

// a passthrough geometry shader for color and position
void main(void) {    
    //for(int i = 0; i < gl_VerticesIn; i++) {
        index1 = index[0];
        // copy position
        gl_Position = gl_PositionIn[0];
        // set layer
        gl_Layer = int(index[0].r);
        // done with the vertex
        EmitVertex();

    //}
    EndPrimitive();

}