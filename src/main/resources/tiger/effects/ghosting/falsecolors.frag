#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform sampler2D texture;
//uniform usampler2D id;

in vec2 texCoord;

//const float treshold = 3.0/512.0;

void main() {
  
    gl_FragColor = texture2D(texture, texCoord);
    //uvec4 i = texture2D(id, texCoord);

    //if(i != uvec4(0u)) discard;
    //if(t.b <= treshold) {
    //    gl_FragColor = vec4(1.0, 1.0, 1.0, 0.8 * t.b/treshold);
    //}
    //else {
    //    discard;
    //}
}

