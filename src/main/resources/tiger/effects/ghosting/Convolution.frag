#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform sampler2D texture;
const float widthInversed = 1.0/512.0;
const float heightInversed = 1.0/512.0;

//const mat3 k = mat3(-0.125, -0.125, -0.125,  -0.125, 1.0, -0.125,  -0.125, -0.125, -0.125);
//const int channel = 0;

varying vec2 texCoord;

void main(void) {
   
    vec4 X = texture2D(texture, texCoord); 
    //vec4 A = texture2D(texture, texCoord + vec2(-widthInversed, -heightInversed));
    vec4 B = texture2D(texture, texCoord + vec2(0, -heightInversed));
    //vec4 C = texture2D(texture, texCoord + vec2(widthInversed, -heightInversed));
    vec4 D = texture2D(texture, texCoord + vec2(-widthInversed, 0));
    vec4 E = texture2D(texture, texCoord + vec2(widthInversed, 0));
    //vec4 F = texture2D(texture, texCoord + vec2(-widthInversed, heightInversed));
    vec4 G = texture2D(texture, texCoord + vec2(0, heightInversed));
    //vec4 H = texture2D(texture, texCoord + vec2(widthInversed, heightInversed));

    //vec4 result = abs(k[1][1]*X + k[0][0]*A + k[1][0]*B + k[2][0]*C + k[0][1]*D + k[2][1]*E + k[0][2]*F + k[1][2]*G + k[2][2]*H);

    if(X != B || X != D || X != E || X != G) {
        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    }
    else {
        discard;
    }
}