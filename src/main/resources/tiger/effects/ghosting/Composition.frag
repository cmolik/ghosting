#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable
#extension GL_EXT_texture_array: enable

uniform sampler2D color;
uniform sampler2D depth;
uniform sampler2D nextColor;
uniform sampler2D nextDepth;
uniform sampler2D accumTexture;

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
   
    if(D.a > 0.0){
        
        float importance = C.a;
        if(importance < 0.5) {
            importance *= 2.0;
            importance = importancePower == 0.0 ? 1.0 : importance/(importancePower + importance - importancePower*importance);
            importance *= 0.5;
        }
        else {
            importance = importancePower == 0.0 ? 1.0 : importance/(importancePower + importance - importancePower*importance);
        }
        //float at = pow(importance, importancePower);
        float gamma = importanceDecrease;

        float curvature = curvature(D);

        if(attention == 1) {
            float cm = max(0.0, 1.0 - 2.0*importance);//*0.9;
            C.xyz =  cm + (1.0 - cm) * C.xyz;
        }
        if(edges == 1) {
            if(redBlue == 1) {
                // The code is for blue/red vizualization used during selection
                if(importance == 1.0) {
                    C.yz = max(vec2(0.0), C.yz - curvature);//*(1.0 - importance));
                    C.x  = max(0.0, C.x - curvature*0.5);//*(1.0 - importance));
                }
                else {
                    C.xy = max(vec2(0.0), C.xy - curvature);//*(1.0 - importance));
                }
            }
            else {
                C.xyz -= vec3(curvature);//*(1.0 - importance);
            }
        }
        
        if(selectiveTransparency == 1 && nextD.a == 0.0) {
            gl_FragColor.xyz = C.xyz;
            gl_FragColor.a = 0.0;
        }
        else {

            float shape = 0.0;
            if(shapeOpacity == 1) {
                shape = pow(curvature, shapePower);
            }

            float dist = 0.0;
            if(distanceOpacity == 1) {
                float depth = nextD.a;
                //TODO if(nextC.a <= 0.25) {
                //TODO    depth = 4.0*nextC.a*depth + (1 - 4.0*nextC.a)*nextNextD.a;
                //TODO }
                float space = depth - D.a;
                dist = 1.0 - pow( 1.0 - space, distancePower);
                //dist = pow(space, distancePower);
            }

            //float alpha = shape + dist - shape*dist;
            //if(importance == 0.0) {
                //alpha = (alpha + importance) * 0.5;
                //alpha = max(0, alpha * importance);
            //}
                //alpha = alpha + gamma - alpha*gamma;
            //float alpha = importance;
            //if(importance == 1.0)

            //float alpha = importance;
            float alpha = (importance + gamma - importance*gamma);
            if(redBlue == 0) {
                if(shapeOpacity == 1 || distanceOpacity == 1) {
                    float shapeDist = shape + dist - shape*dist;
                    if(alpha < 0.5) {
                        alpha *= 2.0;
                        alpha *= shapeDist;
                    }
                    else {
                        alpha = 2.0*alpha - 1.0;
                        alpha = shapeDist + alpha - shapeDist*alpha;
                    }
                    //alpha = 2*alpha*(1 - alpha)*shapeDist + alpha*alpha;
                }
                //alpha = (1.0 - gamma) * alpha + gamma * importance;
            }
            else {
                //alpha = shape;
                alpha = shape + alpha - shape*alpha;
            }

            /*if(count == 2u && nextC.a <= 0.25) {
                alpha = 4.0*nextC.a*alpha + (1 - 4.0*nextC.a);
            }*/

            gl_FragColor.xyz = C.xyz * alpha;
            gl_FragColor.a = 1.0 - alpha;
        }
        
    }
    else {
        discard;
    }
}

