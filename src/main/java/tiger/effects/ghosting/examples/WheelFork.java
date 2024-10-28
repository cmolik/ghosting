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
public class WheelFork {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("The atempt to set system Look&Feel failed. Continuing with default.");
        }

        ObjLoader loader = new ObjLoader();
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/u_mug_3.obj");
        Scene<Mesh> scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/bike_wheel_fork_triangles.obj");
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
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/head_anatomy4.obj");
        //Scene scene = loader.loadFile("C:/Users/cmolikl/Projects/Data/male_anatomy5.obj");

        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.setId(i);
            mesh.renderMethod = Mesh.VERTEX_BUFFER;
            System.out.println(i + ": " + mesh.getName());
            i++;
        }

        /*Grouping layerGroups = new Grouping();
        layerGroups.addToNewGroup(scene.getMesh("fork_body"), scene.getMesh("holder"), scene.getMesh("left_bottom_cover"), scene.getMesh("left_bottom_fork_cover"), scene.getMesh("left_top_cover"), scene.getMesh("left_spring"), scene.getMesh("left_suspension"), scene.getMesh("left_piston"), scene.getMesh("left_top_fork_cover"));
        for(Mesh mesh : scene.getAllMeshes()) {
            if(layerGroups.getGroup(mesh) < 0) {
                layerGroups.addToNewGroup(mesh);
            }
        }

        Grouping labelGroups = new Grouping();
        labelGroups.addToNewGroup("bottom_cover", scene.getMesh("right_bottom_cover"), scene.getMesh("left_bottom_cover"));
        labelGroups.addToNewGroup("bottom_fork_cover", scene.getMesh("right_bottom_fork_cover"), scene.getMesh("left_bottom_fork_cover"));
        labelGroups.addToNewGroup("top_cover", scene.getMesh("right_top_cover"), scene.getMesh("left_top_cover"));
        labelGroups.addToNewGroup("spring", scene.getMesh("right_spring"), scene.getMesh("left_spring"));
        labelGroups.addToNewGroup("suspension", scene.getMesh("right_suspension"), scene.getMesh("left_suspension"));
        labelGroups.addToNewGroup("piston", scene.getMesh("right_piston"), scene.getMesh("left_piston"));
        labelGroups.addToNewGroup("top_fork_cover", scene.getMesh("right_top_fork_cover"), scene.getMesh("left_top_fork_cover"));
        for(Mesh mesh : scene.getAllMeshes()) {
            if(labelGroups.getGroup(mesh) < 0) {
                labelGroups.addToNewGroup(mesh);
            }
        }*/
        
        String workDir = "C:/Users/cmolikl/Projects/Private/Labeling_selection/parameters/";

        GhostingCmolik peeling = new GhostingCmolik(scene);//, layerGroups, labelGroups);
        peeling.setParametersPath(workDir + "wheel_fork/parameters.txt");
        
        //GhostingBruckner peeling = new GhostingBruckner(scene);
        //peeling.setParametersPath(workDir + "wheel_fork - Bruckner/parameters.txt");
        
        //GhostingPinto peeling = new GhostingPinto(scene);
        //peeling.setParametersPath(workDir + "wheel_fork - Pinto/parameters.txt");
        

        Window w = new Window(scene, 1600, 800);
        w.setCameraPath(workDir + "wheel_fork/camera.txt");
        peeling.createUI(w);
        w.setEffect(peeling);
        w.runFastAsPosible = true;
        w.printFps = true;
        w.start();
    }
}
