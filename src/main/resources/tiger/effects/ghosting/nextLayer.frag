#version 330
#extension GL_EXT_gpu_shader4: enable

uniform sampler2D depth;
uniform usampler2D id;

uniform float imp;
uniform int index;
uniform int bit;
uniform int labelingIndex;
uniform int labelingBit;
//uniform int noLabel;

uniform int distanceOpacity;
uniform int importanceFiltering;
uniform int layering;
uniform int attention;

in vec2 texCoord;
in float D;
in vec3 N;
in vec4 C;

const vec3 L = vec3(0.0, 0.0, 1.0);
const float specularExp = 128.0;
const float ambientLight = 0.2;
const vec3 lightCol = vec3(1.0, 1.0, 1.0);

layout(location=0) out vec4  fragData0;
layout(location=1) out vec4  fragData1;
layout(location=2) out uvec4 fragData2;
layout(location=3) out uvec4 fragData3;

vec4 phong() {
    // Init fragment color
    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);
    // Normalize normal vector
    vec3 NN = normalize(N * 2.0 - 1.0);
    // Normalize light vector
    vec3 NL = normalize(L);

    // Cos of angle between normal and light vector
    float NdotL = abs(dot(NN, NL));
    if(attention == 1)
        NdotL = pow(NdotL, imp);

    // Ambient component
    color.rgb += C.rgb * ambientLight;
    // Diffuse component
    color.rgb += C.rgb * lightCol * NdotL;

    return color;
}

void main() {

    float prevD = texture2D(depth, texCoord).a;
    if(prevD == 0.0 || D <= prevD + 0.0005){
        discard;
    }

    uvec4 prevI = texture2D(id, texCoord);
    //if(importanceFiltering == 1 && imp < prevI.b) {
    //    discard;
    //}

    uint bitmask = 1u << uint(bit);
    uint mv = bitmask & prevI[index];
    if(layering == 1 && mv != 0u){
       discard;
    }

    fragData0 = phong();
    fragData0.a = imp;

    fragData1 = vec4(N, D);

    fragData2 = uvec4(0u);
    fragData2[index] = bitmask;

    fragData3 = uvec4(0u);
    uint labelingBitmask = 1u << uint(labelingBit);
    fragData3[labelingIndex] = labelingBitmask;

    //if(noLabel == 1) fragData3 = uvec4(0u);
}
