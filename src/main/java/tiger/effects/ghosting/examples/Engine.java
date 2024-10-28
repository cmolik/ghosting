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
import tiger.effects.ghosting.GhostingCmolik;

/**
 *
 * @author cmolikl
 */
public class Engine {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("The atempt to set system Look&Feel failed. Continuing with default.");
        }

        ObjLoader loader = new ObjLoader();
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/u_mug_3.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/bike_wheel_fork_triangles.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/anatomy1.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/curved_shape.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/hydrant.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/blue_sphere.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/hand_anatomy.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/human_hand.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/sphere_cylinder.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/suspension/suspension.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/gearbox/gearbox.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/cow_triangles.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/heart.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/gas_engine_simple2.obj");
        Scene<Mesh> scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/one_cylinder_engine2.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/head_anatomy3.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/male_anatomy5.obj");

        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.setId(i);
            mesh.renderMethod = Mesh.VERTEX_BUFFER;
            System.out.println(i + ": " + mesh.getName());
            i++;
        }

        /*Grouping layerGroups = new Grouping();
        layerGroups.addToNewGroup(scene.getMesh("Original.020"), scene.getMesh("Original.023"), scene.getMesh("Original.044"), scene.getMesh("Original.033"), scene.getMesh("Original.034"), scene.getMesh("Original.036"));
        for(Mesh mesh : scene.getAllMeshes()) {
            if(layerGroups.getGroup(mesh) < 0) {
                layerGroups.addToNewGroup(mesh);
            }
        }*/

        GhostingCmolik peeling = new GhostingCmolik(scene);//, layerGroups, layerGroups);

        Window w = new Window(scene, 512, 512);
        peeling.createUI(w);
        w.setEffect(peeling);
        w.runFastAsPosible = true;
        w.printFps = true;
        w.start();
    }
}
