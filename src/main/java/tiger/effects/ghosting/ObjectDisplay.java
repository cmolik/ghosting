/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import gleem.BSphere;
import gleem.linalg.Vec3f;
import java.io.InputStream;
import com.jogamp.opengl.GL;
import javax.vecmath.Point4d;
import scene.surface.mesh.Mesh;
import scene.surface.mesh.MeshUtils;
import scene.Scene;
import mesh.loaders.ObjLoader;
import tiger.core.Effect;
import tiger.core.Pass;
import tiger.core.RenderState;
import tiger.core.ViewPort;
import tiger.core.Window;
import tiger.util.saq.Saq;

/**
 *
 * @author cmolikl
 */
public class ObjectDisplay {

    final static int ROWS = 3;
    final static int COLLS = 3;

    final static int OBJECT_OFFSET = 0;

    public static void main(String... args) {

        Effect effect = new Effect();

        ObjLoader loader = new ObjLoader();
        Scene<Mesh> scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/head_anatomy3.obj");

        RenderState rs = new RenderState();
        rs.clearBuffers(true);
        rs.setClearColor(1f, 1f, 1f, 1f);
        rs.enable(GL.GL_DEPTH_TEST);
        effect.addGLEventListener(rs);

        Mesh[] mesh = scene.getAllMeshes().toArray(new Mesh[scene.getSize()]);
        int i = OBJECT_OFFSET;

        float cellWidth = 1f / (float) (COLLS);
        float cellHeight = 1f / (float) (ROWS);

        float cellSize = Math.min(cellWidth, cellHeight);

        float rowOffset = 0f;
        float colOffset = 0f;

        stop_all:
        for(int row = 0; row < ROWS; row++) {
            for(int col = 0; col < COLLS; col++) {
                
                Scene meshScene = new Scene();
                meshScene.addMesh(mesh[i]);
                MeshUtils.calculateBoundingShapes(mesh[i]);
                Point4d c1 = mesh[i].boundingSphere.center;
                Vec3f c2 = new Vec3f((float)c1.x, (float)c1.y, (float)c1.z);
                BSphere sphere = new BSphere(c2, (float) mesh[i].boundingSphere.radius);
                meshScene.setBoundingSphere(sphere);

                InputStream vertexStream = ClassLoader.getSystemResourceAsStream("tiger/example/Phong2.vert");
                InputStream fragmentStream = ClassLoader.getSystemResourceAsStream("tiger/example/Phong.frag");
                Pass pass = new Pass(vertexStream, fragmentStream);
                pass.scene = meshScene;

                ViewPort viewPort = new ViewPort(col*cellWidth, row*cellHeight, cellWidth, cellHeight, pass);

                effect.addGLEventListener(viewPort);

                if(i >= scene.getSize() - 1) break stop_all;
                i++;
            }
        }

        Window w = new Window(scene, 1024, 512);
        w.setEffect(effect);
        w.runFastAsPosible = true;
        //w.printFps = true;
        w.start();
    }
}
