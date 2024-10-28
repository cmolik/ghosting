#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable
#extension GL_EXT_texture_array: enable

uniform sampler2D color;
uniform sampler2D depth;
uniform sampler2D nextColor;
uniform sampler2D nextDepth;
uniform sampler2D accumTexture;
uniform sampler2D finalColor;

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
    //vec4 E = texture2D(depth, texCoord + vec2(-offsetX, -offsetY));
    //vec4 F = texture2D(depth, texCoord + vec2(offsetX, -offsetY));
    //vec4 G = texture2D(depth, texCoord + vec2(offsetX, offsetY));
    //vec4 H = texture2D(depth, texCoord + vec2(-offsetX, offsetY));

    X.xyz = real(X.xyz);
    A.xyz = real(A.xyz);
    B.xyz = real(B.xyz);
    C.xyz = real(C.xyz);
    D.xyz = real(D.xyz);
    //E.xyz = real(E.xyz);
    //F.xyz = real(F.xyz);
    //G.xyz = real(G.xyz);
    //H.xyz = real(H.xyz);

    //float curvature = (dot(X, A) + dot(X, B) + dot(X, C) + dot(X, D)
    //    + dot(X, E) + dot(X, F) + dot(X, G) + dot(X, H)) * 0.125;

    //float curvature = (distance(X, A) + distance(X, B) + distance(X, C) + distance(X, D)
    //    + distance(X, E) + distance(X, F) + distance(X, G) + distance(X, H)) / 8;

    float curvature = min(1.0, (distance(X.xyz, A.xyz) + distance(X.xyz, B.xyz) + distance(X.xyz, C.xyz) + distance(X.xyz, D.xyz))/2.0); // 5.656854;
    float depth = min(10*abs(4*X.a - A.a - B.a - C.a - D.a), 1);

    float NdotL = 1.0 - abs(dot(X.xyz, vec3(0.0, 0.0, 1.0)));

    //return curvature + NdotL - curvature * NdotL;
    //return curvature * NdotL;
    return (curvature + depth - curvature * depth) * NdotL;
    //return ((curvature + depth - curvature * depth) + NdotL) * 0.5;
    //return NdotL;
}

void main() {
    
    vec4 C = texture2D(color, texCoord);
    vec4 D = texture2D(depth, texCoord);
    vec4 nextC = texture2D(nextColor, texCoord);
    vec4 nextD = texture2D(nextDepth, texCoord);
    float count = texture2D(accumTexture, texCoord).r;
    float accumTransp = texture2D(finalColor, texCoord).a;
   
    if(D.a > 0.0){
        
        float importance = C.a;
        importance = importancePower == 0.0 ? 1.0 : importance/(importancePower + importance - importancePower*importance);

        float gamma = importanceDecrease;

        float curvature = curvature(D);

        if(attention == 1) {
            float cm = max(0.0, 1.0 - 2.0*importance);
            C.xyz =  cm + (1.0 - cm) * C.xyz;
        }
        if(edges == 1) {
                C.xyz -= vec3(curvature);
        }
        
        if(selectiveTransparency == 1 && nextD.a == 0.0) {
            gl_FragColor.xyz = C.xyz;
            gl_FragColor.a = 0.0;
        }
        else {

            float exponent = pow(distancePower*abs(dot(real(D.xyz), vec3(0.0, 0.0, 1.0)))*(1.0 - D.a)*accumTransp, shapePower); 
            float alpha = pow(gamma, exponent);

            gl_FragColor.xyz = C.xyz * alpha;
            gl_FragColor.a = 1.0-alpha;
        }
        
    }
    else {
        discard;
    }
}

