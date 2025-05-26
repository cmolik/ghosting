#version 330 compatibility
#extension GL_EXT_gpu_shader4: enable

uniform sampler2D texture;
uniform usampler2D id;
uniform usampler2D labelingId;

uniform int background;
uniform int showId;
uniform int idToShow;

in vec2 texCoord;

void main() {
  vec4 t = texture2D(texture, texCoord);
  uvec4 objectId = texture2D(id, texCoord);
  uvec4 labelId = texture2D(labelingId, texCoord);
  if(showId == 1) {
    uint bit = uint(idToShow) % 32u;
    uint index = uint(idToShow) / 32u;
    uint mask = 1u << bit;
    if((labelId[index] & mask) != 0u) {
        gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
        return;
    }
  }

  if(background == 1 && objectId == uvec4(0u)) {
    gl_FragColor = vec4(1.0);
  }
  else {
    gl_FragColor = t;
  }
}

