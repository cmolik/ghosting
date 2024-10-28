/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting.examples;

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
public class Drill {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("The atempt to set system Look&Feel failed. Continuing with default.");
        }

        ObjLoader loader = new ObjLoader();
        Scene<Mesh> scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/drill/drill2.obj");
        
        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.setId(i);
            mesh.renderMethod = Mesh.VERTEX_BUFFER;
            System.out.println(i + ": " + mesh.getName());
            i++;
        }

        /*Grouping layerGroups = new Grouping();
        layerGroups.addToNewGroup(scene.getMesh("Drill_REVISED_2.017"), scene.getMesh("Drill_REVISED_2.021"), scene.getMesh("Drill_REVISED_2.023"), scene.getMesh("Drill_REVISED_2.024"));
        for(Mesh mesh : scene.getAllMeshes()) {
            if(layerGroups.getGroup(mesh) < 0) {
                layerGroups.addToNewGroup(mesh);
            }
        }

        Grouping labelGroups = new Grouping();
        labelGroups.addToNewGroup("body", scene.getMesh("Drill_REVISED_2.022"), scene.getMesh("Drill_REVISED_2.017"), scene.getMesh("Drill_REVISED_2.021"), scene.getMesh("Drill_REVISED_2.023"), scene.getMesh("Drill_REVISED_2.024"));
        for(Mesh mesh : scene.getAllMeshes()) {
            if(labelGroups.getGroup(mesh) < 0) {
                labelGroups.addToNewGroup(mesh.getName(), mesh);
            }
        }*/

        GhostingCmolik peeling = new GhostingCmolik(scene);//, layerGroups, labelGroups);

        Window w = new Window(scene, 1600, 800);
        peeling.createUI(w);
        w.setEffect(peeling);
        w.runFastAsPosible = true;
        w.printFps = true;
        w.start();
    }
}
