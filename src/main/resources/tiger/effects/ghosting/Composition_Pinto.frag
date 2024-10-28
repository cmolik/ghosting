#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable
#extension GL_EXT_texture_array: enable

uniform sampler2D color;
uniform sampler2D depth;
uniform sampler2D nextColor;
uniform sampler2D nextDepth;
uniform sampler2D accumTexture;
uniform sampler2D finalImportance;

uniform float importancePower;
uniform float importanceDecrease;
uniform int selectiveTransparency;
uniform int distanceOpacity;
uniform float distancePower;
uniform int shapeOpacity;
uniform float shapePower;
uniform int attention;
uniform int edges;
uniform int redBlue;
uniform int firstComposition;

in vec2 texCoord;

uniform float offsetX;
uniform float offsetY;


vec3 real(vec3 normal) {
    return normalize(normal * 2.0 - 1.0);
}

vec4 linInt(vec4 a, vec4 b, float t) {
    return (1.0 - t)*a + t*b;
}

float curvature(vec4 X) {
    vec4 A = texture2D(depth, texCoord + vec2(0, -offsetY));
    vec4 B = texture2D(depth, texCoord + vec2(-offsetX, 0));
    vec4 C = texture2D(depth, texCoord + vec2(offsetX, 0));
    vec4 D = texture2D(depth, texCoord + vec2(0, offsetY));

    X.xyz = real(X.xyz);
    A.xyz = real(A.xyz);
    B.xyz = real(B.xyz);
    C.xyz = real(C.xyz);
    D.xyz = real(D.xyz);

    float curvature = min(1.0, (distance(X.xyz, A.xyz) + distance(X.xyz, B.xyz) + distance(X.xyz, C.xyz) + distance(X.xyz, D.xyz))/2.0); // 5.656854;
    float depth = min(10.0*abs(4.0*X.a - A.a - B.a - C.a - D.a), 1.0);

    float NdotL = 1.0 - abs(dot(X.xyz, vec3(0.0, 0.0, 1.0)));

    return (curvature + depth - curvature * depth) * NdotL;
}

void main() {
    
    vec4 C = texture2D(color, texCoord);
    vec4 D = texture2D(depth, texCoord);
    vec4 nextC = texture2D(nextColor, texCoord);
    vec4 nextD = texture2D(nextDepth, texCoord);
    float count = texture2D(accumTexture, texCoord).r;
    float accumImp = texture2D(finalImportance, texCoord).r;
   
    if(D.a > 0.0){
        
        float importance = C.a;
        //importance = importancePower == 0.0 ? 1.0 : importance/(importancePower + importance - importancePower*importance);

        float gamma = importanceDecrease;

        float curvature = curvature(D);

        if(attention == 1) {
            float cm = max(0.0, 1.0 - 2.0*importance);
            C.rgb =  cm + (1.0 - cm) * C.rgb;
        }
        if(edges == 1) {
            C.rgb -= vec3(curvature);
        }
        
        float m = 0.0;
        if(firstComposition == 0) {
            m = 1.0;
            if(importance > accumImp) {
                m = exp(accumImp - importance);
            }
        }

        gl_FragData[0].rgb = C.rgb;
        gl_FragData[0].a = m;
        gl_FragData[1] = vec4(importance, 0.0, 0.0, 0.0);
    }
    else {
        discard;
    }
}

