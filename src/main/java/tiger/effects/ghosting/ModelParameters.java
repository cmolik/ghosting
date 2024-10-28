/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import com.jogamp.opengl.GLAutoDrawable;
import mesh.loaders.ObjLoader;
import scene.Scene;
import scene.surface.mesh.Mesh;
import tiger.core.GlslProgramFloatParameter;
import tiger.effects.labeling.AbstractExternalLabeling;

/**
 *
 * @author cmolikl
 */
public class ModelParameters {
    public static void saveParameters(Scene<Mesh> scene, Grouping layerGroups, Grouping labelGroups, GlslProgramFloatParameter[] imp, BufferedWriter w) throws IOException {
        w.write(scene.path + "\\" + scene.fileName + "." + scene.extension);
        w.newLine();
        w.write("// Importances");
        w.newLine();
        for(Mesh mesh : scene.getAllMeshes()) {
            w.write(mesh.getName());
            w.write(" ");
            w.write("" + imp[mesh.getId()].getValue());
            w.newLine();
        }
        w.write("// Layer group ids and names");
        w.newLine();
        for(Mesh mesh : scene.getAllMeshes()) {
            w.write(mesh.getName());
            w.write(" ");
            int layerGroupId = layerGroups.getGroup(mesh);
            w.write("" + layerGroupId);
            w.newLine();
        }
        w.write("// Label group ids and names");
        w.newLine();
        for(Mesh mesh : scene.getAllMeshes()) {
            w.write(mesh.getName());
            w.write(" ");
            int labelGroupId = labelGroups.getGroup(mesh);
            w.write("" + labelGroupId);
            w.write(" ");
            w.write("" + labelGroups.getGroupName(labelGroupId));
            w.newLine();
        }
    }

    public static void loadParameters(Scene<Mesh> scene, Grouping layerGroups, Grouping labelGroups, GlslProgramFloatParameter[] imp, BufferedReader r) throws IOException {
        String path = r.readLine();
        if(!path.equals(scene.path + "\\" + scene.fileName + "." + scene.extension)) {
            System.out.println("Warning, loaded path does not equal to scene path.");
        }
        for(int i = 0; i < scene.getSize(); i++) {
            String line = r.readLine();
            if(line == null) return;
            if(line.startsWith("//")) {
                i--;
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            String meshName = tokenizer.nextToken();
            float meshImp = Float.parseFloat(tokenizer.nextToken());
            Mesh mesh = scene.getMesh(meshName);
            if(mesh == null) {
                System.out.println("Warning, mesh " + meshName + " not in scene.");
                continue;
            }
            imp[mesh.getId()].setValue(meshImp);
        }
        for(int i = 0; i < scene.getSize(); i++) {
            String line = r.readLine();
            if(line == null) return;
            if(line.startsWith("//")) {
                i--;
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            String meshName = tokenizer.nextToken();
            int meshLayerGroup = Integer.parseInt(tokenizer.nextToken());
            Mesh mesh = scene.getMesh(meshName);
            if(mesh == null) {
                System.out.println("Warning, mesh " + meshName + " not in scene.");
                continue;
            }
            layerGroups.addToGroup(meshLayerGroup, mesh);
        }
        for(int i = 0; i < scene.getSize(); i++) {
            String line = r.readLine();
            if(line == null) return;
            if(line.startsWith("//")) {
                i--;
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            String meshName = tokenizer.nextToken();
            int meshLabelGroup = Integer.parseInt(tokenizer.nextToken());
            String labelGroupName = null;
            if(tokenizer.hasMoreTokens()) {
                labelGroupName = tokenizer.nextToken();
            }
            Mesh mesh = scene.getMesh(meshName);
            if(mesh == null) {
                System.out.println("Warning, mesh " + meshName + " not in scene.");
                continue;
            }
            labelGroups.addToGroup(meshLabelGroup, mesh);
            labelGroups.setGroupName(meshLabelGroup, labelGroupName == null?meshName:labelGroupName);
        }
        /*for(String line = r.readLine(); line != null; line = r.readLine()) {
             if(line.startsWith("//")) continue;
             StringTokenizer tokenizer = new StringTokenizer(line, " ");
             String meshName = tokenizer.nextToken();
             float meshImp = Float.parseFloat(tokenizer.nextToken());
             int meshLayerGroup = Integer.parseInt(tokenizer.nextToken());
             int meshLabelGroup = Integer.parseInt(tokenizer.nextToken());
             String layerGroupName = null;
             String labelGroupName = null;
             if(tokenizer.hasMoreTokens()) {
                layerGroupName = tokenizer.nextToken();
                if(tokenizer.hasMoreTokens()) {
                    labelGroupName = tokenizer.nextToken();
                }
             }

             Mesh mesh = scene.getMesh(meshName);
             if(mesh == null) {
                 System.out.println("Warning, mesh " + meshName + " not in scene.");
                 continue;
             }
             imp[mesh.getId()].setValue(meshImp);
             layerGroups.addToGroup(meshLayerGroup, mesh);
             layerGroups.setGroupName(meshLayerGroup, layerGroupName == null?meshName:layerGroupName);
             labelGroups.addToGroup(meshLabelGroup, mesh);
             labelGroups.setGroupName(meshLabelGroup, labelGroupName == null?meshName:labelGroupName);
        }*/
    }
    
    public static void saveProperties(Ghosting ls, Properties properties) {
      
        properties.setProperty("scene", ls.scene.path + "\\" + ls.scene.fileName + "." + ls.scene.extension);
        
        for(Mesh mesh : ls.scene.getAllMeshes()) {
            properties.setProperty(mesh.name + ".importance", "" + ls.importance[mesh.getId()].getValue());
            properties.setProperty(mesh.name + ".layergroup", "" + ls.layerGroups.get().getGroup(mesh));
            int labelGroup = ls.labelGroups.get().getGroup(mesh);
            properties.setProperty(mesh.name + ".labelgroup", "" + labelGroup);
            properties.setProperty(mesh.name + ".label", "" + ls.labelGroups.get().getGroupName(labelGroup));
        }
    }
    
    public static void loadProperties(Ghosting ls, Properties properties) {
        Scene newScene = null;
        String p = properties.getProperty("scene");
        if(p != null && !p.equals(ls.scene.path + "\\" + ls.scene.fileName + "." + ls.scene.extension)) {
            /*ObjLoader loader = new ObjLoader();
            newScene = loader.loadFile(p);
            if(newScene != null) {
                ls.scene.replaceWith(newScene);
                int i = 0;
                ls.importance = new GlslProgramFloatParameter[ls.scene.getSize()];
                for(Mesh mesh : ls.scene.getAllMeshes()) {
                    mesh.setId(i);
                    mesh.renderMethod = Mesh.VERTEX_BUFFER;
                    System.out.println(i + ": " + mesh.getName());
                    ls.importance[i] = new GlslProgramFloatParameter("imp", 1f);
                    i++;
                }
                
                ls.layerGroups.set(new Grouping());
                ls.labelGroups.set(new Grouping());
            }*/
            System.out.println("Warning! The path to loaded scene is not equal to path in properties.");
        }
        for(Mesh mesh : ls.scene.getAllMeshes()) {
            p = properties.getProperty(mesh.name + ".importance");
            if(p != null) ls.importance[mesh.getId()].setValue(Float.parseFloat(p));
            p = properties.getProperty(mesh.name + ".layergroup");
            if(p != null) ls.layerGroups.get().addToGroup(Integer.parseInt(p), mesh);
            p = properties.getProperty(mesh.name + ".labelgroup");
            int labelGroup = Integer.parseInt(p);
            if(p != null) ls.labelGroups.get().addToGroup(labelGroup, mesh);
            p = properties.getProperty(mesh.name + ".label");
            ls.labelGroups.get().setGroupName(labelGroup, p==null ? mesh.name : p);
        }
        /*if(newScene != null) {
            ls.labeling = new ExternalLabeling(ls.finalLabelingId, ls.countTexture, ls.hullTexture, ls.labelGroups, ls.scene.getSize(), 512, ExternalLabeling.HORIZONTAL_LAYOUT, ls.importance);
        }*/
    }
}
