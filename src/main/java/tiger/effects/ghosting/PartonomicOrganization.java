package tiger.effects.ghosting;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import scene.surface.mesh.Mesh;
import scene.Scene;

/**
 *
 * @author cmolikl
 */
public class PartonomicOrganization {

    private Scene<Mesh> scene;
    private int[][] g;
    private boolean loaded = false;
    int lastId;
    public Mesh root;

    public PartonomicOrganization(Scene<Mesh> scene) {
        this.scene = scene;
        lastId = scene.getSize();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int[][] getGraph() {
        return g;
    }

    public boolean isPart(Mesh whole, Mesh part) {
        return g[whole.getId()][part.getId()] == 1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Mesh[] meshes = new Mesh[scene.getSize()];
        meshes = scene.getAllMeshes().toArray(meshes);
        for(Mesh mesh  : meshes) {
            builder.append(mesh.getName());
            builder.append("\n");
            for(Mesh childMesh : mesh.getChildren()) {
                   builder.append("\t");
                   builder.append(childMesh.getName());
                   builder.append("\n");
            }
        }
        return builder.toString();
    }

    public void save() {
        File file = new File(scene.path, scene.fileName + ".po");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            write(writer);
            writer.close();
        }
        catch(IOException e) {
            System.err.println(e);
        }
    }

    private void write(BufferedWriter writer) throws IOException {
        String cg = toString();
        System.out.println(cg);
        writer.write(cg);
    }

    public void load() {
        File file = new File(scene.path, scene.fileName + ".po");
        if(file.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                read(reader);
                loaded = true;
                reader.close();
            }
            catch(IOException e) {
                System.err.println(e);
            }
        }
        else {
            root = new Mesh();
            root.name = scene.fileName;
            root.id = lastId;
            lastId++;
            for(Mesh mesh : scene.getAllMeshes()) {
                root.addChild(mesh);
            }
            scene.addMesh(root);
            calculateGraph();
        }
    }

    private void read(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        Mesh parent = null;
        Mesh child = null;
        boolean first = true;
        while(line != null) {
            if(line.startsWith("\t")) {
                line = line.substring(1);
                if(parent != null) {
                    child = scene.getMesh(line);
                    if(child == null) {
                        child = new Mesh();
                        child.name = line;
                        child.id = lastId;
                        lastId++;
                        scene.addMesh(child);
                    }
                    parent.addChild(child);
                }
            }
            else {
                parent = scene.getMesh(line);
                if(parent == null) {
                    parent = new Mesh();
                    parent.name = line;
                    parent.id = lastId;
                    lastId++;
                    scene.addMesh(parent);
                }
                if(first) {
                    root = parent;
                    first = false;
                }
            }
            line = reader.readLine();
        }
        calculateGraph();
    }

    protected void calculateGraph() {
        int n = scene.getSize();
        g = new int[n][n];
        for(Mesh mesh : scene.getAllMeshes()) {
            for(Mesh childMesh : mesh.getChildren()) {
                g[mesh.getId()][childMesh.getId()] = 1;
            }
        }
    }
}