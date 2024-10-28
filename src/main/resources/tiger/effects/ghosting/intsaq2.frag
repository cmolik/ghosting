#version 330
#extension GL_EXT_gpu_shader4: enable

uniform usampler2D id;
uniform usampler2D labelingId;
uniform sampler2D composition;
uniform sampler2D finalColor;

uniform int considerTransparency;
uniform float layerTransparencyTreshold;
uniform float accumulatedTransparencyTreshold;

in vec2 texCoord;

layout(location=0) out uvec4 bitmask;
layout(location=1) out uvec4 labelingBitmask;

void main(void) {
   bitmask = texture2D(id, texCoord);
   labelingBitmask = texture2D(labelingId, texCoord);

   if(considerTransparency == 1) {
       float layer_transparency = texture2D(composition, texCoord).a;
       float accumulated_transparency = texture2D(finalColor, texCoord).a;
       if(layer_transparency > layerTransparencyTreshold || accumulated_transparency < accumulatedTransparencyTreshold) {
            labelingBitmask = uvec4(0u, 0u, 0u, 0u);
       }
   }
}