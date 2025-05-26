/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.net.URL;

import com.jogamp.opengl.GL;

import scene.Scene;
import scene.surface.mesh.Mesh;
import tiger.core.GlslProgramFloatParameter;
import tiger.core.RenderState;
import tiger.effects.labeling.AbstractExternalLabeling;
import tiger.util.saq.Saq;
//import com.jogamp.opengl.util.awt.TextRenderer;

/**
 *
 * @author cmolikl
 */
public class GhostingCmolik extends Ghosting {
    
    public GhostingCmolik(Scene scene) {
        super(scene, null);
    }

    public GhostingCmolik(Scene<Mesh> scene, Grouping layerGroups, Grouping labelGroups) {
        super(scene, layerGroups, labelGroups, null);
    }   

    public GhostingCmolik(Scene scene, AbstractExternalLabeling labeling) {
        super(scene, labeling);
    }

    public GhostingCmolik(Scene<Mesh> scene, Grouping layerGroups, Grouping labelGroups, AbstractExternalLabeling labeling) {
        super(scene, layerGroups, labelGroups, labeling);
    }   

    protected void createCompositionPass() {
        RenderState rs = new RenderState();
        //rs.clearBuffers(false);
        rs.clearBuffers(true);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);
        //rs.enable(GL.GL_BLEND);
        //rs.setBlendFuncSeparate(GL.GL_DST_ALPHA, GL.GL_ONE, GL.GL_DST_ALPHA, GL.GL_ZERO);

        offsetX = new GlslProgramFloatParameter("offsetX", 1f);
        offsetY = new GlslProgramFloatParameter("offsetY", 1f);

        URL fragment = ClassLoader.getSystemResource("tiger/effects/ghosting/Composition.frag");
        composeLayers = new Saq(fragment);
        composeLayers.addTexture(color, "color");
        composeLayers.addTexture(depth, "depth");
        composeLayers.addTexture(invColor, "nextColor");
        composeLayers.addTexture(invDepth, "nextDepth");
        composeLayers.addTexture(accumTexture, "accumTexture");
        composeLayers.setTarget(compositionBuffer);
        composeLayers.renderState = rs;
        composeLayers.glslVaryingParameters.add(offsetX);
        composeLayers.glslVaryingParameters.add(offsetY);
        composeLayers.glslVaryingParameters.add(importancePower);
        composeLayers.glslVaryingParameters.add(importanceDecrease);
        composeLayers.glslVaryingParameters.add(selectiveTransparency);
        composeLayers.glslVaryingParameters.add(distanceOpacity);
        composeLayers.glslVaryingParameters.add(maxD);
        composeLayers.glslVaryingParameters.add(distancePower);
        composeLayers.glslVaryingParameters.add(shapeOpacity);
        composeLayers.glslVaryingParameters.add(shapePower);
        composeLayers.glslVaryingParameters.add(attention);
        composeLayers.glslVaryingParameters.add(edges);
        composeLayers.glslVaryingParameters.add(redBlueVis);
    }
}
