/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.io.InputStream;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import scene.surface.mesh.Mesh;
import scene.Scene;
import mesh.loaders.ObjLoader;
import tiger.core.Effect;
import tiger.core.FrameBuffer;
import tiger.core.Pass;
import tiger.core.RenderState;
import tiger.core.Texture2D;
import tiger.core.Window;
import tiger.util.saq.Saq;

/**
 *
 * @author cmolikl
 */
public class IntegerTextureTest {
    public static void main(String[] args) {
        ObjLoader loader = new ObjLoader();
        Scene<Mesh> scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/cow_triangles.obj");
        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.setId(i);
            System.out.println(i + ": " + mesh.getName());
            i++;
        }

        Effect effect = new Effect();

        Texture2D fTexture1 = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        effect.addTexture(fTexture1);

        Texture2D fTexture2 = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        effect.addTexture(fTexture2);

        Texture2D iTexture = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);
        effect.addTexture(iTexture);

        FrameBuffer fbo = new FrameBuffer(true, fTexture1, fTexture2);//, iTexture);
        effect.addTarget(fbo);

        Texture2D finalId = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);
        effect.addTexture(finalId);

        FrameBuffer bitmaskBuffer = new FrameBuffer(false, finalId);
        effect.addTarget(bitmaskBuffer);

        RenderState state = new RenderState();
        state.enable(GL.GL_DEPTH_TEST);
        state.disable(GL.GL_COLOR_LOGIC_OP);
        state.setClearDepth(1f);
        state.setClearColor(0f, 0f, 0f, 1f);
        state.setBuffersToClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        state.clearBuffers(true);

        InputStream vertex = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/firstLayer.vert");
        InputStream fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/firstLayer.frag");
        Pass draw = new Pass(vertex, fragment);
        draw.scene = scene;
        draw.setTarget(fbo);
        draw.renderState = state;
        effect.addGLEventListener(draw);

        state = new RenderState();
        state.disable(GL.GL_DEPTH_TEST);
        state.enable(GL.GL_COLOR_LOGIC_OP);
        state.setLogicOp(GL.GL_OR);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/intsaq.frag");
        Pass bitmask = new Saq(fragment, iTexture);
        bitmask.setTarget(bitmaskBuffer);
        bitmask.renderState = state;
        effect.addGLEventListener(bitmask);
        
        state = new RenderState();
        state.disable(GL.GL_DEPTH_TEST);
        state.disable(GL.GL_COLOR_LOGIC_OP);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/inttofloat.frag");
        Pass saq = new Saq(fragment, finalId);
        saq.renderState = state;
        effect.addGLEventListener(saq);

        Window w = new Window(scene, 512, 512, true);
        w.setEffect(effect);
        w.runFastAsPosible = true;
        w.start();
    }
}
