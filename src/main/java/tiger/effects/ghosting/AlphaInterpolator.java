/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import javax.swing.UIManager;
import mesh.loaders.ObjLoader;
import scene.Scene;
import scene.surface.mesh.Mesh;
import tiger.core.OrthogonalExaminerViewer;
import tiger.core.Window;

/**
 *
 * @author cmolikl
 */
public class AlphaInterpolator {
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
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/head_anatomy3.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/male_anatomy5.obj
        Scene<Mesh> scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/drill/drill2.obj");

        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.setId(i);
            mesh.renderMethod = Mesh.VERTEX_BUFFER;
            System.out.println(i + ": " + mesh.getName());
            i++;
        }

        Grouping layerGroups = new Grouping();
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
        }

        GhostingCmolik peeling = new GhostingCmolik(scene, layerGroups, labelGroups);
        
        

        Window w = new Window(scene, 1600, 880);

        OrthogonalExaminerViewer viewer = new OrthogonalExaminerViewer(3);
        viewer.attach(w.canvas, scene);

        w.setControler(viewer);
        peeling.createUI(w);
        w.setEffect(peeling);
        w.runFastAsPosible = true;
        w.printFps = true;
        w.start();
        
    }
}
