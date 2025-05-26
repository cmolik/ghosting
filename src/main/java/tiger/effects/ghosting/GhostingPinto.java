/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.io.InputStream;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import scene.Scene;
import scene.surface.mesh.Mesh;
import tiger.core.FrameBuffer;
import tiger.core.GlslProgramFloatParameter;
import tiger.core.GlslProgramIntParameter;
import tiger.core.Pass;
import tiger.core.RenderState;
import tiger.core.Texture2D;
import tiger.effects.labeling.AbstractExternalLabeling;
import tiger.util.saq.Saq;

/**
 *
 * @author cmolikl
 */
public class GhostingPinto extends Ghosting {
    
    Texture2D importanceTexture;
    Texture2D finalImportance;
    
    FrameBuffer finalImportanceBuffer;
    
    Pass importanceBlend;
    
    GlslProgramIntParameter firstComposition;
    
    public GhostingPinto(Scene scene) {
        super(scene, null);
    }
    
    public GhostingPinto(Scene scene, AbstractExternalLabeling labeling) {
        super(scene, labeling);
    }

    public GhostingPinto(Scene scene, Grouping layerGroups, Grouping labelGroups) {
        super(scene, layerGroups, labelGroups, null);
    }
    
    @Override
    protected void createCompositionPass() {
        
        firstComposition = new GlslProgramIntParameter("firstComposition", 1);
        
        importanceTexture = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        compositionBuffer.targets.add(importanceTexture);
        
        finalImportance = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        finalImportanceBuffer = new FrameBuffer(false, finalImportance);
        
        RenderState rs = new RenderState();
        rs.clearBuffers(true);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        offsetX = new GlslProgramFloatParameter("offsetX", 1f);
        offsetY = new GlslProgramFloatParameter("offsetY", 1f);

        InputStream fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/Composition_Pinto.frag");
        composeLayers = new Saq(fragment);
        composeLayers.addTexture(color, "color");
        composeLayers.addTexture(depth, "depth");
        composeLayers.addTexture(invColor, "nextColor");
        composeLayers.addTexture(invDepth, "nextDepth");
        composeLayers.addTexture(accumTexture, "accumTexture");
        composeLayers.addTexture(finalImportance, "finalImportance");
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
        composeLayers.glslVaryingParameters.add(firstComposition);    
    }
    
    protected void createBlendPass() {
        RenderState rs = new RenderState();
        rs.clearBuffers(false);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_BLEND);
        rs.setBlendFuncSeparate(GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_SRC_ALPHA, GL.GL_ZERO, GL.GL_ZERO);

        InputStream fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/blend.frag");
        blendLayers = new Saq(fragment);
        blendLayers.addTexture(compositionTexture, "composition");
        blendLayers.addTexture(finalLabelingId, "labelingBitmask");
        blendLayers.addTexture(labelingId, "labelingId");
        blendLayers.setTarget(finalBuffer);
        blendLayers.renderState = rs;
        
        rs = new RenderState();
        rs.clearBuffers(false);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_BLEND);
        rs.setBlendEquation(GL2.GL_MAX);
        
        importanceBlend = new Saq(importanceTexture);
        importanceBlend.setTarget(finalImportanceBuffer);
        importanceBlend.renderState = rs;
    }
    
    @Override
    public void init(GLAutoDrawable glad) {
        importanceTexture.init(glad);
        finalImportance.init(glad);
        finalImportanceBuffer.init(glad);
        super.init(glad);
        importanceBlend.init(glad);
        
        for(GlslProgramFloatParameter param : importance) {
            param.setValue(0f);
        }
    }
    
    public void display(GLAutoDrawable glad) {
        GL2 gl = glad.getGL().getGL2();
        // if(selection.getValue() == 0) {
        //     draw.display(glad);
        // }
        // else {
            boolean isInteraction = interaction;

            countBuffer.get().bind(gl);
            gl.glClearColor(0f, 0f, 0f, 0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            //vyprazdnit prvni a druhy color buffer
            layer.get().bind(gl);
            gl.glClearColor(0f, 0f, 0f, 0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            firstLayer.prepare(glad);
            for(Mesh mesh : scene.getAllMeshes()) {
                bit.setValue(layerGroups.get().getGroup(mesh) % bitmaskBits);
                index.setValue(layerGroups.get().getGroup(mesh) / bitmaskBits);
                int labelGroup = labelGroups.get().getGroup(mesh);
                labelingBit.setValue(labelGroup % bitmaskBits);
                labelingIndex.setValue(labelGroup / bitmaskBits);

                bit.init(firstLayer.glslProgram);
                index.init(firstLayer.glslProgram);
                importance[mesh.getId()].init(firstLayer.glslProgram);
                labelingBit.init(firstLayer.glslProgram);
                labelingIndex.init(firstLayer.glslProgram);
                
                mesh.getRenderer().render(glad, mesh);              
            }

            bitmaskBuffer.get().bind(gl);
            gl.glClearColorIui(0, 0, 0, 0);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            bitmask.display(glad);
            gl.glDisable(GL.GL_COLOR_LOGIC_OP);

            layer.swap();

            finalImportanceBuffer.bind(gl);
            gl.glClearColor(0f, 0f, 0f, 0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            
            finalBuffer.bind(gl);
            gl.glClearColor(0f, 0f, 0f, 1f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
       
            firstComposition.setValue(1);
            int maxLayers = 0;
            // while there is geometry to render
            //for(int i = 0; i < 1; i++) {
            do {
                maxLayers++;
                layers++;

                // render next layer
                // - input is color1, depth1
                // - to layer2 (color2, depth2)
                layer.get().bind(gl);
                gl.glClearColor(0f, 0f, 0f, 0f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                nextLayer.prepare(glad);
                oq.beginQuery(glad);
                for(Mesh mesh : scene.getAllMeshes()) {
                    //if(importance[mesh.getId()].getValue() == 0f) continue;

                    bit.setValue(layerGroups.get().getGroup(mesh) % bitmaskBits);
                    index.setValue(layerGroups.get().getGroup(mesh) / bitmaskBits);
                    int labelGroup = labelGroups.get().getGroup(mesh);
                    labelingBit.setValue(labelGroup % bitmaskBits);
                    labelingIndex.setValue(labelGroup / bitmaskBits);

                    bit.init(nextLayer.glslProgram);
                    index.init(nextLayer.glslProgram);
                    importance[mesh.getId()].init(nextLayer.glslProgram);
                    labelingBit.init(nextLayer.glslProgram);
                    labelingIndex.init(nextLayer.glslProgram);
                    
                    mesh.getRenderer().render(glad, mesh);
                }
                oq.endQuery(glad);
                
                // compose layers
                // Note that here is not composited the layer generated with last call to nextLayer,
                // but the one created in previous pass (either previous nextLayer of firstLayer)
                // - input is color1, depth1, color2, depth2
                // - to layer1 = layer12
                composeLayers.display(glad);

                id.swap();
                
                bitmask2.display(glad);
                gl.glDisable(GL.GL_COLOR_LOGIC_OP);
                if(labeling != null) {
                    if(labeling.considerTransparency.getValue() == 1 || maxLayers <= 1) {
                        count.display(glad);
                    }
                    // if(labeling.considerTransparency.getValue() == 0 && maxLayers == 1) {
                    //     bitmaskBuffer2.set(bbuffer2);
                    // }
                }
                
                blendLayers.display(glad);
                importanceBlend.display(glad);
                gl.glBlendEquation(GL.GL_FUNC_ADD);
                if(firstComposition.getValue() == 1) {
                    firstComposition.setValue(0);
                }

                labelingId.swap();
                color.swap();
                invColor.swap();
                depth.swap();
                invDepth.swap();
                layer.swap();
                
            } while(oq.getResult(glad) > 0 && maxLayers < 100);

            counter++;
            long now = System.currentTimeMillis();
            if(now - time >= 5000) {
                System.out.println("Average number of layers is " + layers/counter);
                time = now;
                layers = 0;
                counter = 0;
            }

            //silhouette.display(glad);
            //hallo.display(glad);
            saq.display(glad);
            // if(showId.getValue() == 1) {
            //     showLabelingId.display(glad);
            // }
            //halloCompose.display(glad);


            // if needed then calculate and display labeling
            if(labeling != null) {
                // if(selection.getValue() == 1 && !isInteraction) {
                if(!isInteraction) {
                    //if(labeling.calculateLayout) {
                        hullPass.display(glad);
                        labeling.calculate(glad);
                        labeling.calculateLayout = false;
                    //}
                    if(showLabeling.getValue() == 1) {
                        labeling.display(glad);
                    }
                }
            }

            color.restart();
            invColor.restart();
            depth.restart();
            invDepth.restart();
            id.restart();
            labelingId.restart();
            //invId.restart();
            layer.restart();
            // if(labeling != null && labeling.considerTransparency.getValue() == 0) {
            //     bitmaskBuffer2.set(bitmaskBuffer);
            // }
        // }
    }
    
    @Override
    public void reshape(GLAutoDrawable glad, int x, int y, int width, int height) {
        importanceTexture.reshape(glad, x, y, width, height);
        finalImportance.reshape(glad, x, y, width, height);
        finalImportanceBuffer.reshape(glad, x, y, width, height);
        super.reshape(glad, x, y, width, height);
        importanceBlend.reshape(glad, x, y, width, height);
    }
}
