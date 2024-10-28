#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform usampler2D texture;
uniform  sampler2D composition;
uniform  sampler2D finalColor;

uniform float layerTransparencyTreshold;
uniform float accumulatedTransparencyTreshold;

in vec2 texCoord;

void main(void) {
   uvec4 id = texture2D(texture, texCoord);
   float layer_transparency = texture2D(composition, texCoord).a;
   float accumulated_transparency = texture2D(finalColor, texCoord).a;
   if(id == vec4(0u))
        discard;

   if(layer_transparency > layerTransparencyTreshold || accumulated_transparency < accumulatedTransparencyTreshold) {
        gl_FragData[0] = vec4(0.0, 0.0, 0.0, 0.0);
   }
   else {
        gl_FragData[0] = vec4(1.0, 0.0, 0.0, 0.0);
   }
   gl_FragData[1] = vec4(-1.0, 0.0, 0.0, 0.0);
}