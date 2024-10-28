#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform sampler2D texture;
uniform sampler2D color;
uniform usampler2D layerId;
uniform usampler2D id;
uniform usampler2D labelingId;

uniform int background;
uniform int showComposition;

varying vec2 texCoord;

void main() {
  vec4 t = texture2D(texture, texCoord);
  vec4 c = texture2D(color, texCoord);
  uvec4 i = texture2D(layerId, texCoord);
  uvec4 objectId = texture2D(id, texCoord);
  uvec4 labelId = texture2D(labelingId, texCoord);
  if(showComposition == 1) {
    if(background == 1 && objectId == uvec4(0u)) {
      gl_FragColor = vec4(1.0);
    }
    else {
      gl_FragColor = t;
    }
  } 
  /*else {
    gl_FragColor = vec4(1.0 - c.a, 1.0 - c.a, 1.0 - c.a, 1.0);
  }*/
  else {
    uint bit = 0u;
    for(bit = 0u; bit < 32u; bit++) { 
        uint mask = 1u << bit;
        uint test = i.r & mask & labelId[0];
        if(test != 0u) break;
    }
    gl_FragColor = vec4(float(bit+1u)/32.0, 0.0, 0.0, 1.0);
  }
}

