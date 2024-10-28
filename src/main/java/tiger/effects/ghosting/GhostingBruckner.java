/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.io.InputStream;
import com.jogamp.opengl.GL;
import scene.Scene;
import tiger.core.GlslProgramFloatParameter;
import tiger.core.RenderState;
import tiger.effects.labeling.AbstractExternalLabeling;
import tiger.util.saq.Saq;

/**
 *
 * @author cmolikl
 */
public class GhostingBruckner extends Ghosting {
    
    public GhostingBruckner(Scene scene) {
        super(scene, null);
    }
    
    public GhostingBruckner(Scene scene, AbstractExternalLabeling labeling) {
        super(scene, labeling);
    }

    public GhostingBruckner(Scene scene, Grouping layerGroups, Grouping labelGroups) {
        super(scene, layerGroups, labelGroups, null);
    }
    
    protected void createCompositionPass() {
        RenderState rs = new RenderState();
        rs.clearBuffers(true);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        offsetX = new GlslProgramFloatParameter("offsetX", 1f);
        offsetY = new GlslProgramFloatParameter("offsetY", 1f);

        InputStream fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/Composition_Bruckner.frag");
        composeLayers = new Saq(fragment);
        composeLayers.addTexture(color, "color");
        composeLayers.addTexture(depth, "depth");
        composeLayers.addTexture(invColor, "nextColor");
        composeLayers.addTexture(invDepth, "nextDepth");
        composeLayers.addTexture(accumTexture, "accumTexture");
        composeLayers.addTexture(finalColor, "finalColor");
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
