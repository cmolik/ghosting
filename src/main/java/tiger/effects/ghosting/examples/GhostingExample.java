/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting.examples;

import java.io.File;
import java.io.IOException;
import javax.swing.UIManager;
import mesh.loaders.ObjLoader;
import scene.Scene;
import scene.surface.mesh.Mesh;
import tiger.core.Window;
import tiger.effects.ghosting.Grouping;
import tiger.effects.ghosting.GhostingCmolik;

/**
 *
 * @author cmolikl
 */
public class GhostingExample {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("The atempt to set system Look&Feel failed. Continuing with default.");
        }

        ObjLoader loader = new ObjLoader();
        Scene<Mesh> scene = loader.loadFile(args[0]);

        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.renderMethod = Mesh.VERTEX_BUFFER;
            System.out.println(mesh.id + ": " + mesh.getName());
        }

        GhostingCmolik ghosting = new GhostingCmolik(scene);

        Window w = new Window(scene, 512, 512);
        ghosting.createUI(w);
        w.setEffect(ghosting);
        w.runFastAsPosible = true;
        w.printFps = true;
        w.start();
        
        if(args.length > 1) {
            File file = new File(args[1]);
            ghosting.loadState(file);
        }
    }
}
