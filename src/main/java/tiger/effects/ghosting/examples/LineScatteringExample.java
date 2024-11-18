package tiger.effects.ghosting.examples;

import java.io.InputStream;
import java.nio.FloatBuffer;

import javax.swing.UIManager;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mesh.loaders.ObjLoader;
import scene.Scene;
import scene.surface.mesh.Mesh;
import tiger.core.Effect;
import tiger.core.FrameBuffer;
import tiger.core.Pass;
import tiger.core.RenderState;
import tiger.core.Texture2D;
import tiger.core.Window;
import tiger.effects.ghosting.GhostingCmolik;
import tiger.effects.ghosting.Grouping;
import tiger.util.saq.Saq;
import tiger.util.scattering.LineScatteringPassVBO;

public class LineScatteringExample extends Pass {

    FrameBuffer bufferToReadFrom;
    int attachment = GL.GL_COLOR_ATTACHMENT0;
    FloatBuffer readBuffer;
    boolean readFromBuffer = true;

    public LineScatteringExample() {
        super();
        readBuffer = FloatBuffer.allocate(4 * 32);
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.setSwapInterval(0);
    }

    public void display(GLAutoDrawable drawable) {
        if(readFromBuffer) {
            GL2 gl = drawable.getGL().getGL2();
            bufferToReadFrom.bind(gl);
            gl.glReadBuffer(attachment);
            gl.glReadPixels(0, 0, 32, 1, GL2.GL_RED, GL.GL_FLOAT, readBuffer);
            System.out.println("ID_SUM");
            float[] readArray = readBuffer.array();
            for(int i = 0; i < 32; i++) {
                System.out.println(i + ": " + readArray[i]);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String... args) {

        int width = 512;
        int height = 512;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("The atempt to set system Look&Feel failed. Continuing with default.");
        }

        ObjLoader loader = new ObjLoader();
        Scene<Mesh> scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/Ghosting/digestive.obj");

        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.setId(i);
            mesh.renderMethod = Mesh.VERTEX_BUFFER;
            System.out.println(i + ": " + mesh.getName());
            i++;
        }

        Grouping layerGroups = new Grouping();
        layerGroups.addToNewGroup(scene.getMesh("larynx_1"), scene.getMesh("larynx_2"));
        for(Mesh mesh : scene.getAllMeshes()) {
            if(layerGroups.getGroup(mesh) < 0) {
                layerGroups.addToNewGroup(mesh);
            }
        }

        GhostingCmolik ghosting = new GhostingCmolik(scene, layerGroups, null);

        Texture2D idTexture = ghosting.finalLabelingId;
       
        Texture2D idSumTexture = new Texture2D(32, 1);
        FrameBuffer idSumBuffer = new FrameBuffer(false, idSumTexture);

        RenderState rs = new RenderState();
        rs.clearBuffers(true);
        rs.setClearColor(0f, 0f, 0f, 0f);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        InputStream fragmentStream = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/inttofloat.frag");
        Pass displayId = new Saq(fragmentStream, idTexture);
        displayId.glslVaryingParameters.add(ghosting.idToShow);
        displayId.renderState = rs;

        rs = new RenderState();
        rs.clearBuffers(true);
        rs.setClearColor(0f, 0f, 0f, 0f);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_BLEND);
        rs.setBlendFunc(GL.GL_ONE, GL.GL_ONE);

        InputStream vertexStream = ClassLoader.getSystemResourceAsStream("tiger/example/scattering/idSum.vert");
        fragmentStream = ClassLoader.getSystemResourceAsStream("tiger/example/scattering/idSum.frag");
        Pass lineScattering = new LineScatteringPassVBO(vertexStream, fragmentStream, 32, 1);
        lineScattering.addTexture(idTexture, "idTexture");
        lineScattering.setTarget(idSumBuffer);
        lineScattering.renderState = rs;

        LineScatteringExample readFromBuffer = new LineScatteringExample();
        readFromBuffer.bufferToReadFrom = idSumBuffer;
        readFromBuffer.attachment = GL2.GL_COLOR_ATTACHMENT0;

        Effect effect = new Effect();
        effect.addTexture(idSumTexture);
        effect.addTarget(idSumBuffer);
        effect.addGLEventListener(ghosting);
        effect.addGLEventListener(displayId);
        effect.addGLEventListener(lineScattering);
        effect.addGLEventListener(readFromBuffer);

        Window w = new Window(scene, width, height);
        ghosting.createUI(w);
        w.debug = true;
        w.setEffect(effect);
        w.runFastAsPosible = false;
        w.printFps = false;
        w.start();

    }
}
