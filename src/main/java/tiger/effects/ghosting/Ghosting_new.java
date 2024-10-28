/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import com.jogamp.opengl.util.GLBuffers;
import gleem.BSphere;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;
import javax.imageio.ImageIO;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.ComponentEvents;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import scene.surface.mesh.Mesh;
import scene.surface.mesh.MeshUtils;
import scene.Scene;
import mesh.occlusions.OcclusionQuery;
import tiger.core.CameraProperties;
import tiger.core.DefaultParameter;
import tiger.core.FrameBuffer;
import tiger.core.GlslProgramFloatArrayParameter;
import tiger.core.GlslProgramFloatParameter;
import tiger.core.GlslProgramIntParameter;
import tiger.core.GlslProgramUIntArrayParameter;
import tiger.core.MutableLink;
import tiger.core.Pass;
import tiger.core.RenderState;
import tiger.core.SwapingLink;
import tiger.core.Texture;
import tiger.core.Texture2D;
import tiger.core.Window;
import tiger.effects.labeling.AbstractExternalLabeling;
import tiger.ui.BooleanMenuItem;
import tiger.ui.FloatSlider;
import tiger.ui.IntSlider;
import tiger.ui.TigerChangeEvent;
import tiger.ui.TigerChangeListener;
import tiger.util.saq.Saq;
//import com.jogamp.opengl.util.awt.TextRenderer;

/**
 *
 * @author cmolikl
 */
public abstract class Ghosting_new extends Ghosting implements GLEventListener {

    public static final int initialX = 512;
    public static final int initialY = 512;
    public int width = initialX;
    public int height = initialY;
    
    String parametersPath = "C:/Temp/parameters.txt";
    String propertiesPath = "C:/Temp/";

    boolean interaction = false;
    boolean multipleSelection = false;
    boolean interactionAlowed = true;

    public Scene<Mesh> scene;
    Scene<Mesh> convexHull;

    int bitmaskBits = 32;

    MutableLink<Grouping> layerGroups;
    MutableLink<Grouping> labelGroups;

    GlslProgramFloatParameter[] importance;

    long intStart;
    long intLength = 500;

    Texture2D projectionTexture;
    Texture2D accumTexture;
    //Texture2D maxDepth;
    Texture2D color1;
    Texture2D depth1;
    Texture2D id1;
    Texture2D labelingId1;
    Texture2D color2;
    Texture2D depth2;
    Texture2D id2;
    Texture2D labelingId2;
    SwapingLink<Texture> color;
    SwapingLink<Texture> invColor;
    SwapingLink<Texture> depth;
    SwapingLink<Texture> invDepth;
    SwapingLink<Texture> id;
    SwapingLink<Texture> invLabelingId;
    SwapingLink<Texture> labelingId;
    Texture2D finalColor;
    Texture2D finalId;
    Texture2D layerLabelingId;
    Texture2D finalLabelingId;
    Texture2D countTexture;
    Texture2D hullTexture;
    Texture2D silhouetteTexture;
    Texture2D halloTexture;
    Texture2D compositionTexture;

    FrameBuffer projectionBuffer;
    FrameBuffer accumBuffer;
    //FrameBuffer maxDepthBuffer;
    FrameBuffer layer1;
    FrameBuffer layer2;
    SwapingLink<FrameBuffer> layer;
    FrameBuffer finalBuffer;
    FrameBuffer layerBitmaskBuffer;
    FrameBuffer layerLabelBitmaskBuffer;
    //FrameBuffer bbuffer2;
    FrameBuffer bitmaskBuffer;
    FrameBuffer countBuffer;
    FrameBuffer hullBuffer;
    FrameBuffer silhouetteBuffer;
    FrameBuffer halloBuffer;
    FrameBuffer compositionBuffer;

    Pass projectionPass;
    Pass accumPass;
    //Pass maxDepthPass;
    Pass draw;
    Pass firstLayer;
    Pass nextLayer;
    Pass composeLayers;
    Pass blendLayers;
    Pass saq;
    Pass layerBitmask;
    Pass layerLabelBitmask;
    Pass bitmask;
    Pass count;
    Pass hullPass;
    Pass displayHullPass;
    Pass silhouette;
    //Erosion2 hallo;
    Pass halloCompose;
    public AbstractExternalLabeling labeling;
    Pass showLabelingId;
    //TextRenderer textRenderer;

    OcclusionQuery oq;

    GlslProgramIntParameter index;
    GlslProgramIntParameter bit;
    GlslProgramIntParameter labelingIndex;
    GlslProgramIntParameter labelingBit;
    GlslProgramFloatParameter imp;
    //GlslProgramFloatParameter meshId;
    GlslProgramFloatParameter offsetX;
    GlslProgramFloatParameter offsetY;

    GlslProgramFloatParameter importancePower = new GlslProgramFloatParameter("importancePower", 1f);
    GlslProgramFloatParameter importanceDecrease = new GlslProgramFloatParameter("importanceDecrease", 0.0f);
    GlslProgramIntParameter selectiveTransparency = new GlslProgramIntParameter("selectiveTransparency", 1);
    GlslProgramIntParameter distanceOpacity = new GlslProgramIntParameter("distanceOpacity", 1);
    GlslProgramFloatParameter maxD = new GlslProgramFloatParameter("maxD", 0.1f);
    GlslProgramFloatParameter distancePower = new GlslProgramFloatParameter("distancePower", 12f);
    GlslProgramIntParameter shapeOpacity = new GlslProgramIntParameter("shapeOpacity", 1);
    GlslProgramFloatParameter shapePower = new GlslProgramFloatParameter("shapePower", 0.2f);
    GlslProgramIntParameter considerTransparency = new GlslProgramIntParameter("considerTransparency", 0);
    GlslProgramFloatParameter layerTransparencyTreshold = new GlslProgramFloatParameter("layerTransparencyTreshold", 0.75f);
    GlslProgramFloatParameter accumulatedTransparencyTreshold = new GlslProgramFloatParameter("accumulatedTransparencyTreshold", 0.05f);
    GlslProgramIntParameter numberOfLayersToRender = new GlslProgramIntParameter("numberOfLayers", 25);
    
    GlslProgramIntParameter attention = new GlslProgramIntParameter("attention", 1);
    GlslProgramIntParameter importanceFiltering = new GlslProgramIntParameter("importanceFiltering", 0);
    GlslProgramIntParameter layering = new GlslProgramIntParameter("layering", 1);
    GlslProgramIntParameter edges = new GlslProgramIntParameter("edges", 1);
    GlslProgramIntParameter background = new GlslProgramIntParameter("background", 1);
    GlslProgramIntParameter redBlueVis = new GlslProgramIntParameter("redBlue", 0);
    public GlslProgramIntParameter selection = new GlslProgramIntParameter("selection", 0);
    GlslProgramIntParameter showLabeling = new GlslProgramIntParameter("Show labeling", 1);
    GlslProgramIntParameter fakeLabels = new GlslProgramIntParameter("fakeLabels", 0);
    GlslProgramIntParameter idToShow = new GlslProgramIntParameter("showId", 0);
    GlslProgramIntParameter showId = new GlslProgramIntParameter("Show Id", 0);
    GlslProgramIntParameter roiVis = new GlslProgramIntParameter("roiVis", 0);
    public GlslProgramFloatArrayParameter roiColor = new GlslProgramFloatArrayParameter("roiColor", new float[] {0.5f, 1f, 0.5f});
    GlslProgramIntParameter startTest = new GlslProgramIntParameter("Start test", 0);
    GlslProgramIntParameter showParameters = new GlslProgramIntParameter("Show parameters", 1);
    GlslProgramIntParameter showModelParameters = new GlslProgramIntParameter("Show model parameters", 1);
    GlslProgramIntParameter showInternalArea = new GlslProgramIntParameter("Show internal area", 0);
    GlslProgramIntParameter showComposition = new GlslProgramIntParameter("showComposition", 1);
    
    public GlslProgramUIntArrayParameter roi = new GlslProgramUIntArrayParameter("roi", new int[] {0, 0, 0, 0});
    
    int neededTextures;
    int[] buffers = new int[] {GL.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1, GL2.GL_COLOR_ATTACHMENT2};

    long time = System.currentTimeMillis();
    long counter = 0;
    long layers = 0;

    /*ConnectionGraph cg;
    public int[][] distanceMatrix;
    PartonomicOrganization po;
    public int[][] transitiveClosure;*/

    //ObjectList exploration;
    
    public Window window;
    JFrame objects;
    
    public boolean saveImage = false;

    public Ghosting_new(Scene scene, AbstractExternalLabeling labeling) {
        this(scene, null, null, labeling);
    }

    public Ghosting_new(Scene<Mesh> scene, Grouping layerGroups, Grouping labelGroups, AbstractExternalLabeling labeling) {
        this.scene = scene;
        this.layerGroups = new MutableLink<Grouping>(layerGroups);
        this.labelGroups = new MutableLink<Grouping>(labelGroups);
        this.labeling = labeling;

        BSphere bs = scene.getBoundingSphere();
        bs.setRadius(bs.getRadius() * 1.1f);

        if(this.layerGroups.get() == null) {
            Grouping lg = new Grouping();
            for(Mesh mesh : scene.getAllMeshes()) {
                lg.addToNewGroup(mesh);
            }
            this.layerGroups.set(lg);
        }
        if(this.labelGroups.get() == null) {
            Grouping lg = new Grouping();
            for(Mesh mesh : scene.getAllMeshes()) {
                lg.addToNewGroup(mesh);
            }
            this.labelGroups.set(lg);
        }

        /*System.out.println("\nCONTACT GRAPH");
        cg = new ConnectionGraph(scene);
        cg.load();
        if(!cg.isLoaded()) {
            cg.initTrees(0f);
            cg.calculateCollisions(scene.getBoundingSphere().getRadius() / 1000f);
            cg.save();
        }

        // load partonomic organization
        System.out.println("\nPARTONOMIC ORGANIZATION");
        po = new PartonomicOrganization(scene);
        po.load();
        System.out.println(po.toString());
        // calculate P from DP
        transitiveClosure = GraphUtils.transitiveClosure(po.getGraph());
        for(int i = 0; i < transitiveClosure.length; i++) {
            System.out.println(Arrays.toString(transitiveClosure[i]));
        }

        int[][] g = cg.getGraph();
        Mesh[] meshes = scene.getAllMeshes().toArray(new Mesh[scene.getSize()]);
        //eliminate contacts
        for(int i = 0; i < meshes.length; i++) {
            if(meshes[i].id >= g.length) continue;
            for(int j = 0; j < meshes.length; j++) {
                if(meshes[j].id >= g.length) continue;
                if(meshes[i].parent != meshes[j].parent) {
                    g[meshes[i].id][meshes[j].id] = 0;
                    g[meshes[j].id][meshes[i].id] = 0;
                }
            }
        }

        //calculate shortest distances
        distanceMatrix = GraphUtils.shortestPaths(g);
        //for(int i = 0; i < distanceMatrix.length; i++) {
        //    System.out.println(Arrays.toString(distanceMatrix[i]));
        //}

        System.out.println("Scene size = " + scene.getSize());

        exploration = new Exploration(this);*/

        convexHull = new Scene();
        Mesh convexHullMesh = MeshUtils.calculateConvexHull(scene.getAllMeshes(), true);
        convexHull.addMesh(convexHullMesh);

        String[] meshNames = new String[scene.getSize()];
        for(Mesh mesh : scene.getAllMeshes()) {
            meshNames[mesh.getId()] = "" + mesh.getId();
        }

        //this.oldImportance = new float[scene.getSize()];
        //this.simportance = new float[scene.getSize()];
        this.importance = new GlslProgramFloatParameter[scene.getSize()];
        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            //simportance[i] = 1.0f;
            importance[i] = new GlslProgramFloatParameter("imp", 1.0f);
            //intStart = System.currentTimeMillis();
            i++;
        }
        //eimportance[4] = 1.0f;
        //eimportance[8] = 1.0f;

        oq = new OcclusionQuery();

        projectionTexture = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);
        accumTexture = new Texture2D(GL2.GL_LUMINANCE32F, GL.GL_LUMINANCE, GL.GL_FLOAT);
        color1 = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        depth1 = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        id1 = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);
        labelingId1 = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);
        color2 = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        depth2 = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);
        id2 = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);
        labelingId2 = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);

        color = new SwapingLink<Texture>(color1, color2);
        invColor = new SwapingLink<Texture>(color2, color1);
        depth = new SwapingLink<Texture>(depth1, depth2);
        invDepth = new SwapingLink<Texture>(depth2, depth1);
        id = new SwapingLink<Texture>(id1, id2);
        labelingId = new SwapingLink<Texture>(labelingId1, labelingId2);
        invLabelingId = new SwapingLink<Texture>(labelingId2, labelingId1);

        finalColor = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);

        layerLabelingId = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);

        finalId = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);

        finalLabelingId = new Texture2D(GL2.GL_RGBA32UI, GL2.GL_RGBA_INTEGER, GL.GL_UNSIGNED_INT);

        countTexture = new Texture2D(GL2.GL_LUMINANCE32F, GL.GL_LUMINANCE, GL.GL_FLOAT);

        hullTexture = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);

        compositionTexture = new Texture2D(GL.GL_RGBA32F, GL.GL_RGBA, GL.GL_FLOAT);

        neededTextures = scene.getSize() / bitmaskBits;
        int n = scene.getSize() % bitmaskBits;
        if(n > 0) {
            neededTextures++;
        }
        System.out.println("Needed Layers: " + neededTextures);
        /*bitmaskTexture = new Texture2DArray(glad.getWidth(), glad.getHeight(), neededTextures,
            GL.GL_UNSIGNED_INT, GL.GL_RGBA32UI_EXT, GL.GL_RGBA_INTEGER_EXT);
        bitmaskTexture.init(gl);*/
        
        if(labeling != null) { 
            labeling.register(finalLabelingId, countTexture, hullTexture, this.labelGroups, scene.getSize(), 512, AbstractExternalLabeling.HORIZONTAL_LAYOUT, importance);
            
            considerTransparency = labeling.considerTransparency;
        
            labeling.hLayout.fakeLabels = fakeLabels;
            labeling.vLayout.fakeLabels = fakeLabels;
            labeling.rLayout.fakeLabels = fakeLabels;
            //labeling.mLayout.fakeLabels = fakeLabels;
        }

        projectionBuffer = new FrameBuffer(false, projectionTexture);
        accumBuffer = new FrameBuffer(false, accumTexture);

        layer1 = new FrameBuffer(color1, depth1, id1, labelingId1);
        layer2 = new FrameBuffer(color2, depth2, id2, labelingId2);
        layer = new SwapingLink<FrameBuffer>(layer1, layer2);

        finalBuffer = new FrameBuffer(false, finalColor);
        
        
        layerBitmaskBuffer = new FrameBuffer(false, finalId);
        layerLabelBitmaskBuffer = new FrameBuffer(false, layerLabelingId);
        //if(labeling == null || labeling.labelInvisibleGeometry.getValue() == 0) {
        //bbuffer2 = new FrameBuffer(false, finalId);
        //}
        bitmaskBuffer = new FrameBuffer(false, finalLabelingId);
        
        countBuffer = new FrameBuffer(false, countTexture, accumTexture);

        hullBuffer = new FrameBuffer(false, hullTexture);

        compositionBuffer = new FrameBuffer(false, compositionTexture);

        //imp = new GlslProgramFloatParameter("imp", 0f);
        
        RenderState rs = new RenderState();
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);
        rs.enable(GL.GL_COLOR_LOGIC_OP);
        rs.setLogicOp(GL.GL_OR);

        InputStream vertex = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/projection.vert");
        InputStream fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/projection.frag");
        projectionPass = new Pass(vertex, fragment);
        projectionPass.scene = scene;
        projectionPass.renderState = rs;
        projectionPass.setTarget(projectionBuffer);

        rs = new RenderState();
        rs.clearBuffers(true);
        rs.setBuffersToClear(GL.GL_COLOR_BUFFER_BIT);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_COLOR_LOGIC_OP);
        rs.enable(GL.GL_BLEND);
        rs.setBlendFunc(GL.GL_ONE, GL.GL_ONE);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/accum.frag");
        accumPass = new Saq(fragment, projectionTexture);
        accumPass.scene = scene;
        accumPass.renderState = rs;
        accumPass.setTarget(accumBuffer);

        rs = new RenderState();
        rs.clearBuffers(true);
        //rs.setBuffersToClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        rs.setClearColor(1f, 1f, 1f, 1f);
        //rs.setClearDepth(0f);
        rs.enable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        vertex = ClassLoader.getSystemResourceAsStream("tiger/example/Phong.vert");
        fragment = ClassLoader.getSystemResourceAsStream("tiger/example/Phong.frag");
        draw = new Pass(vertex, fragment);
        draw.scene = scene;
        draw.renderState = rs;

        rs = new RenderState();
        rs.clearBuffers(false);
        rs.setClearColor(0f, 0f, 0f, 0f);
        rs.enable(GL.GL_DEPTH_TEST);
        rs.setDepthFunc(GL.GL_LEQUAL);
        rs.enable(GL.GL_CULL_FACE);
        rs.setCullFaces(GL.GL_BACK);
        rs.disable(GL.GL_BLEND);

        index = new GlslProgramIntParameter("index", 0);
        bit = new GlslProgramIntParameter("bit", 0);
        labelingIndex = new GlslProgramIntParameter("labelingIndex", 0);
        labelingBit = new GlslProgramIntParameter("labelingBit", 0);

        vertex = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/firstLayer.vert");
        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/firstLayer.frag");
        firstLayer = new Pass(vertex, fragment);
        firstLayer.scene = scene;
        firstLayer.setTarget(layer);
        firstLayer.glslVaryingParameters.add(attention);
        firstLayer.glslVaryingParameters.add(redBlueVis);
        firstLayer.glslVaryingParameters.add(roi);
        firstLayer.glslVaryingParameters.add(roiVis);
        firstLayer.glslVaryingParameters.add(roiColor);
        firstLayer.renderState = rs;

        vertex = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/nextLayer.vert");
        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/nextLayer.frag");
        nextLayer = new Pass(vertex, fragment);
        nextLayer.scene = scene;
        nextLayer.addTexture(depth, "depth");
        nextLayer.addTexture(finalId, "id");
        nextLayer.glslVaryingParameters.add(attention);
        nextLayer.glslVaryingParameters.add(importanceFiltering);
        nextLayer.glslVaryingParameters.add(layering);
        nextLayer.glslVaryingParameters.add(distanceOpacity);
        //nextLayer.glslVaryingParameters.add(maxD);
        nextLayer.glslVaryingParameters.add(importancePower);
        nextLayer.glslVaryingParameters.add(redBlueVis);
        nextLayer.glslVaryingParameters.add(roi);
        nextLayer.glslVaryingParameters.add(roiVis);
        nextLayer.glslVaryingParameters.add(roiColor);
        nextLayer.setTarget(layer);
        nextLayer.renderState = rs;

        /*rs = new RenderState();
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_COLOR_LOGIC_OP);
        rs.setLogicOp(GL.GL_OR);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/selection/intsaq.frag");
        layerBitmask = new Saq(fragment, id);
        layerBitmask.setTarget(layerBitmaskBuffer);
        layerBitmask.renderState = rs;
        layerBitmask.init(glad);*/

        createCompositionPass();

        createBlendPass();

        rs = new RenderState();
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_COLOR_LOGIC_OP);
        rs.setLogicOp(GL.GL_OR);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/layerBitmask.frag");
        layerBitmask = new Saq(fragment);
        layerBitmask.addTexture(id, "id");
        layerBitmask.setTarget(layerBitmaskBuffer);
        layerBitmask.renderState = rs;
        
        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/intsaq2.frag");
        layerLabelBitmask = new Saq(fragment);
        layerLabelBitmask.addTexture(labelingId, "labelingId");
        layerLabelBitmask.addTexture(compositionTexture, "composition");
        layerLabelBitmask.addTexture(finalColor, "finalColor");
        layerLabelBitmask.setTarget(layerLabelBitmaskBuffer);
        layerLabelBitmask.renderState = rs;
        layerLabelBitmask.glslVaryingParameters.add(considerTransparency);
        layerLabelBitmask.glslVaryingParameters.add(layerTransparencyTreshold);
        layerLabelBitmask.glslVaryingParameters.add(accumulatedTransparencyTreshold);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/intsaq.frag");
        bitmask = new Saq(fragment);
        bitmask.addTexture(id, "id");
        bitmask.addTexture(labelingId, "labelingId");
        bitmask.setTarget(bitmaskBuffer);
        bitmask.renderState = rs;

        rs = new RenderState();
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_BLEND);
        rs.setBlendFunc(GL.GL_ONE, GL.GL_ONE);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/count.frag");
        count = new Saq(fragment, labelingId);
        count.addTexture(compositionTexture, "composition");
        count.addTexture(finalColor, "finalColor");
        count.setTarget(countBuffer);
        count.renderState = rs;
        count.glslVaryingParameters.add(layerTransparencyTreshold);
        count.glslVaryingParameters.add(accumulatedTransparencyTreshold);

        silhouetteTexture = new Texture2D();
        silhouetteBuffer = new FrameBuffer(false, silhouetteTexture);

        halloTexture = new Texture2D();
        halloBuffer = new FrameBuffer(false, halloTexture);

        /*rs = new RenderState();
        rs.clearBuffers(true);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/hotspot/silhouette.frag");
        silhouette = new Saq(fragment, finalId);
        silhouette.init(glad);
        silhouette.setTarget(silhouetteBuffer);
        silhouette.glslVaryingParameters.add(offsetX);
        silhouette.glslVaryingParameters.add(offsetY);
        silhouette.renderState = rs;*/

        rs = new RenderState();
        rs.clearBuffers(false);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/background.frag");
        saq = new Saq(fragment, finalColor);
        saq.addTexture(compositionTexture, "color");
        saq.addTexture(layerLabelingId, "layerId");
        saq.addTexture(finalId, "id");
        //saq.addTexture(finalLabelingId, "labelingId");
        saq.glslVaryingParameters.add(background);
        saq.glslVaryingParameters.add(showComposition);
        //fragment = ClassLoader.getSystemResourceAsStream("tiger/selection/falsecolors.frag");
        //saq = new Saq(fragment, accumTexture);
        saq.renderState = rs;

        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/inttofloat.frag");
        showLabelingId = new Saq(fragment, finalLabelingId);
        showLabelingId.renderState = rs;
        showLabelingId.glslVaryingParameters.add(idToShow);



        /*float step = 4f/512f;
        hallo = new Erosion2(silhouetteTexture, halloBuffer, step, 2);
        hallo.init(glad);

        rs = new RenderState();
        rs.clearBuffers(false);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_BLEND);
        rs.setBlendFunc(GL.GL_ONE, GL.GL_SRC_ALPHA);

        fragment = ClassLoader.getSystemResourceAsStream("tiger/hotspot/falsecolors.frag");
        halloCompose = new Saq(fragment, halloTexture);
        halloCompose.addTexture(finalId, "id");
        halloCompose.renderState = rs;
        halloCompose.init(glad);*/

        rs = new RenderState();
        rs.clearBuffers(true);
        rs.enable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        vertex = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/extrude.vert");
        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/id.frag");
        hullPass = new Pass(vertex, fragment);
        hullPass.glslDefaultParameters.add(DefaultParameter.MESH_ID);
        hullPass.setTarget(hullBuffer);
        hullPass.scene = convexHull;
        hullPass.renderState = rs;
        
        rs = new RenderState();
        rs.clearBuffers(false);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.disable(GL.GL_BLEND);

        //vertex = ClassLoader.getSystemResourceAsStream("tiger/effects/labeling/extrude.vert");
        fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/Convolution.frag");
        displayHullPass = new Saq(fragment, hullTexture);
        displayHullPass.renderState = rs;
        
        //labeling = new ExternalLabeling(finalLabelingId, countTexture, hullTexture, meshNames, 512, ExternalLabeling.HORIZONTAL_LAYOUT, eimportance);

    }

    public void setImportances(float[] imp) {
        //simportance = importance;
        for(int i = 0; i < imp.length; i++) {
            importance[i].setValue(imp[i]);
        }
        //intStart = System.currentTimeMillis();
    }

    public void init(GLAutoDrawable glad) {
        GL gl = glad.getGL();
        gl.setSwapInterval(0);

        System.out.println("Window size on init is: " + glad.getSurfaceWidth() + ", " + glad.getSurfaceHeight());

        oq.init(glad);

        projectionTexture.init(glad);
        accumTexture.init(glad);
        color1.init(glad);
        depth1.init(glad);
        id1.init(glad);
        labelingId1.init(glad);
        color2.init(glad);
        depth2.init(glad);
        id2.init(glad);
        labelingId2.init(glad);

        finalColor.init(glad);
        
        layerLabelingId.init(glad);
        
        finalId.init(glad);

        finalLabelingId.init(glad);

        countTexture.init(glad);

        hullTexture.init(glad);

        compositionTexture.init(glad);

        projectionBuffer.init(glad);
        accumBuffer.init(glad);

        layer1.init(glad);
        layer2.init(glad);

        finalBuffer.init(glad);

        layerBitmaskBuffer.init(glad);
        layerLabelBitmaskBuffer.init(glad);
        bitmaskBuffer.init(glad);
        //if(labeling.labelInvisibleGeometry.getValue() == 0) {
        //bbuffer2.init(glad);
        //}

        countBuffer.init(glad);

        hullBuffer.init(glad);

        compositionBuffer.init(glad);

        draw.init(glad);

        firstLayer.init(glad);

        nextLayer.init(glad);

        offsetX.setValue(1f/glad.getSurfaceWidth());
        offsetY.setValue(1f/glad.getSurfaceHeight());

        composeLayers.init(glad);

        blendLayers.init(glad);

        layerBitmask.init(glad);
        layerLabelBitmask.init(glad);
        bitmask.init(glad);

        count.init(glad);

        silhouetteTexture.init(glad);
        silhouetteTexture.bind(gl);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        silhouetteBuffer.init(glad);

        halloTexture.init(glad);
        halloTexture.bind(gl);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        halloBuffer.init(glad);

        saq.init(glad);
        showLabelingId.init(glad);

        projectionPass.init(glad);
        accumPass.init(glad);

        hullPass.init(glad);
        displayHullPass.init(glad);

        if(labeling != null) {
            labeling.init(glad);
        }

        if(glad instanceof ComponentEvents) {
        ComponentEvents ce = (ComponentEvents) glad;
        ce.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                /*if((labeling.layout.colorLabel.getValue() == 1 || labeling.layout.colorLines.getValue() == 1) && selection.getValue() == 1) {
                   int x = e.getX();
                   int y = height - e.getY();
                   Rectangle2D[] bounds = labeling.getLabelBounds();
                   //Mesh[] meshes = scene.getAllMeshes().toArray(new Mesh[scene.getSize()]);
                   //boolean labelClicked = false;
                   for(int i = 0; i < bounds.length; i++) {
                       if(bounds[i].contains(x, y)) {
                            if(!multipleSelection) {
                                for(int j = 0; j < scene.getSize(); j++) {
                                    if(j == i) continue;
                                    importance[j].setValue(0.1f);
                                }
                            }
                            if(importance[i].getValue() == 0.1f) {
                                importance[i].setValue(1f);
                            }
                            else {
                                importance[i].setValue(0.1f);
                            }
                       }
                   }
                   //if(!labelClicked) {
                   //    System.out.println("Background clicked.");
                   //}
                }*/
            }
            public void mouseReleased(MouseEvent e) {
                //importanceDecrease.setValue(0f);
                if(interaction && labeling != null) {
                   labeling.calculateLayout = true;
                }
                interaction = false;
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        ce.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                interaction = true;
            }
            public void mouseMoved(MouseEvent e) {}
        });

        ce.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    multipleSelection = true;
                }
            }

            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    multipleSelection = false;
                }
                if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    if(selection.getValue() == 0) {
                        selection.setValue(1);
                    }
                    else {
                        selection.setValue(0);
                    }
                }
            }
        });
        }
    }

    public void display(GLAutoDrawable glad) {
        GL2 gl = glad.getGL().getGL2();
        if(selection.getValue() == 0) {
            draw.display(glad);
        }
        else {
            boolean isInteraction = interaction;

            // interpolate importance
            //importance = eimportance;

            countBuffer.get().bind(gl);
            gl.glClearColor(0f, 0f, 0f, 0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            projectionBuffer.bind(gl);
            gl.glClearColorIui(0, 0, 0, 0);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            projectionPass.prepare(glad);
            for(Mesh mesh : scene.getAllMeshes()) {
                if(importance[mesh.getId()].getValue() == 0f) continue;
                bit.setValue(layerGroups.get().getGroup(mesh) % bitmaskBits);
                index.setValue(layerGroups.get().getGroup(mesh) / bitmaskBits);
                bit.init(projectionPass.glslProgram);
                index.init(projectionPass.glslProgram);

                mesh.getRenderer().render(glad, mesh);
            }
            accumPass.display(glad);

            //vyprazdnit prvni a druhy color buffer
            layer.get().bind(gl);
            gl.glClearColor(0f, 0f, 0f, 0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            firstLayer.prepare(glad);
            for(Mesh mesh : scene.getAllMeshes()) {
                bit.setValue(layerGroups.get().getGroup(mesh) % bitmaskBits);
                index.setValue(layerGroups.get().getGroup(mesh) / bitmaskBits);
                int labelGroup = labelGroups.get().getGroup(mesh);
                labelingBit.setValue(labelGroup % bitmaskBits);
                labelingIndex.setValue(labelGroup / bitmaskBits);

                bit.init(firstLayer.glslProgram);
                index.init(firstLayer.glslProgram);
                importance[mesh.getId()].init(firstLayer.glslProgram);
                labelingBit.init(firstLayer.glslProgram);
                labelingIndex.init(firstLayer.glslProgram);
                
                mesh.getRenderer().render(glad, mesh);              
            }

            layerBitmaskBuffer.get().bind(gl);
            gl.glClearColorIui(0, 0, 0, 0);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            layerBitmask.display(glad);
            gl.glDisable(GL.GL_COLOR_LOGIC_OP);

            layer.swap();

            finalBuffer.bind(gl);
            gl.glClearColor(0f, 0f, 0f, 1f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            int maxLayers = 0;
            // while there is geometry to render
            //for(int i = 0; i < 1; i++) {
            do {
                maxLayers++;
                layers++;

                // render next layer
                // - input is color1, depth1
                // - to layer2 (color2, depth2)
                layer.get().bind(gl);
                gl.glClearColor(0f, 0f, 0f, 0f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                nextLayer.prepare(glad);
                oq.beginQuery(glad);
                for(Mesh mesh : scene.getAllMeshes()) {
                    if(importance[mesh.getId()].getValue() == 0f) continue;

                    bit.setValue(layerGroups.get().getGroup(mesh) % bitmaskBits);
                    index.setValue(layerGroups.get().getGroup(mesh) / bitmaskBits);
                    int labelGroup = labelGroups.get().getGroup(mesh);
                    labelingBit.setValue(labelGroup % bitmaskBits);
                    labelingIndex.setValue(labelGroup / bitmaskBits);

                    bit.init(nextLayer.glslProgram);
                    index.init(nextLayer.glslProgram);
                    importance[mesh.getId()].init(nextLayer.glslProgram);
                    labelingBit.init(nextLayer.glslProgram);
                    labelingIndex.init(nextLayer.glslProgram);
                    
                    mesh.getRenderer().render(glad, mesh);
                }
                oq.endQuery(glad);
                
                // compose layers
                // Note that here is not composited the layer generated with last call to nextLayer,
                // but the one created in previous pass (either previous nextLayer or firstLayer)
                // - input is color1, depth1, color2, depth2
                // - to layer1 = layer12
                composeLayers.display(glad);

                id.swap();
                
                layerLabelBitmaskBuffer.get().bind(gl);
                gl.glClearColorIui(0, 0, 0, 0);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT);
                if(labeling != null && (labeling.labelInvisibleGeometry.getValue() == 1 || maxLayers <= 1)) {
                    layerLabelBitmask.display(glad);
                }
                bitmask.display(glad);
                gl.glDisable(GL.GL_COLOR_LOGIC_OP);
                
                if(labeling != null && (labeling.labelInvisibleGeometry.getValue() == 1 || maxLayers <= 1)) {
                    count.display(glad);
                }
                
                blendLayers.display(glad);

                labelingId.swap();
                invLabelingId.swap();
                color.swap();
                invColor.swap();
                depth.swap();
                invDepth.swap();
                layer.swap();
                
            } while(oq.getResult(glad) > 0 && maxLayers < numberOfLayersToRender.getValue());

            counter++;
            long now = System.currentTimeMillis();
            if(now - time >= 5000) {
                System.out.println("Average number of layers is " + layers/counter);
                time = now;
                layers = 0;
                counter = 0;
            }

            //silhouette.display(glad);
            //hallo.display(glad);
            saq.display(glad);
            if(showId.getValue() == 1) {
                showLabelingId.display(glad);
            }
            //halloCompose.display(glad);

            // if needed then calculate and display labeling
            if(labeling != null) {
                if(selection.getValue() == 1 && !isInteraction && interactionAlowed) {
                    if(!labeling.isInitialized()) {
                        labeling.init(glad);
                    }
                    //if(labeling.calculateLayout) {
                        hullPass.display(glad);
                        if(showInternalArea.getValue() == 1) {
                            displayHullPass.display(glad);
                        }
                        //System.out.print("Recalculating layout ... ");
                        labeling.calculate(glad);
                        labeling.calculateLayout = false;
                        //System.out.println("done");
                    //}
                    if(showLabeling.getValue() == 1) {
                        labeling.display(glad);
                    }
                }
                else if(showLabeling.getValue() == 1 && !interactionAlowed) {
                        labeling.display(glad);
                }
            }
            
            if(saveImage) {
                try {
                    gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
                    int size = width * height * 3;
                    //int size = GLBuffers.sizeof(gl, new int[1], GL2.GL_BGR, GL.GL_BYTE, width, height, 1, true);
                    ByteBuffer buffer = GLBuffers.newDirectByteBuffer(size);
                    FrameBuffer.bindScreen(gl);
                    gl.glReadBuffer(GL.GL_FRONT);
                    gl.glReadPixels(0, 0, width, height, GL2.GL_BGR, GL.GL_BYTE, buffer);
                    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    int[] bd = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int b = 2 * buffer.get();
                            int g = 2 * buffer.get();
                            int r = 2 * buffer.get();

                            bd[(height - y - 1) * width + x] = (r << 16) | (g << 8) | b | 0xFF000000;
                        }
                    }
                    if(roi.getValue()[0] == 0) {
                        ImageIO.write(bi, "png", new File("C:/Temp/image.png"));
                    }
                    else {
                        int id = binlog(roi.getValue()[0]);
                        System.out.println("Saving image for roi " + id);
                        ImageIO.write(bi, "png", new File("C:/Temp/image" + id + ".png"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                saveImage = false;
            }
            
            /*FrameBuffer.bindScreen(gl);
            gl.glActiveTexture(GL.GL_TEXTURE0);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDisable(GL.GL_BLEND);
            textRenderer.beginRendering(width, height);
            textRenderer.setColor(0f, 0f, 0f, 1f);
            textRenderer.draw("FPS: ", 10, 10);
            textRenderer.endRendering();*/

            color.restart();
            invColor.restart();
            depth.restart();
            invDepth.restart();
            id.restart();
            labelingId.restart();
            invLabelingId.restart();
            layer.restart();
        }
    }
    
    public int binlog( int bits ) {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }
    
    public int bits2id(int bits) {
        for(int id = 0; id < 32; id++) {
            if(((1 << id) & bits) != 0) return id;
        }
        return -1;
    }


    public void reshape(GLAutoDrawable glad, int x, int y, int width, int height) {
        System.out.println("Width: " + width + ", height: " + height);
        this.width = width;
        this.height = height;
        
        // reshape textures
        projectionTexture.reshape(glad, x, y, width, height);
        accumTexture.reshape(glad, x, y, width, height);
        color1.reshape(glad, x, y, width, height);
        depth1.reshape(glad, x, y, width, height);
        id1.reshape(glad, x, y, width, height);
        labelingId1.reshape(glad, x, y, width, height);
        color2.reshape(glad, x, y, width, height);
        depth2.reshape(glad, x, y, width, height);
        id2.reshape(glad, x, y, width, height);
        labelingId2.reshape(glad, x, y, width, height);

        layerLabelingId.reshape(glad, x, y, width, height);
        finalId.reshape(glad, x, y, width, height);
        finalLabelingId.reshape(glad, x, y, width, height);
        compositionTexture.reshape(glad, x, y, width, height);
        finalColor.reshape(glad, x, y, width, height);
        countTexture.reshape(glad, x, y, width, height);

        hullTexture.reshape(glad, x, y, width, height);

        silhouetteTexture.reshape(glad, x, y, width, height);
        halloTexture.reshape(glad, x, y, width, height);

        // reshape framebuffers
        projectionBuffer.reshape(glad, x, y, width, height);
        accumBuffer.reshape(glad, x, y, width, height);
        layer1.reshape(glad, x, y, width, height);
        layer2.reshape(glad, x, y, width, height);
        layerBitmaskBuffer.reshape(glad, x, y, width, height);
        layerLabelBitmaskBuffer.reshape(glad, x, y, width, height);
        bitmaskBuffer.reshape(glad, x, y, width, height);
        //bbuffer2.reshape(glad, x, y, width, height);

        countBuffer.reshape(glad, x, y, width, height);
        hullBuffer.reshape(glad, x, y, width, height);

        silhouetteBuffer.reshape(glad, x, y, width, height);
        halloBuffer.reshape(glad, x, y, width, height);
        compositionBuffer.reshape(glad, x, y, width, height);

        // reshape parameters of passes
        projectionPass.reshape(glad, x, y, width, height);
        accumPass.reshape(glad, x, y, width, height);
        draw.reshape(glad, x, y, width, height);
        firstLayer.reshape(glad, x, y, width, height);
        nextLayer.reshape(glad, x, y, width, height);
        layerBitmask.reshape(glad, x, y, width, height);
        layerLabelBitmask.reshape(glad, x, y, width, height);
        bitmask.reshape(glad, x, y, width, height);
        count.reshape(glad, x, y, width, height);
        composeLayers.reshape(glad, x, y, width, height);
        blendLayers.reshape(glad, x, y, width, height);
        saq.reshape(glad, x, y, width, height);
        showLabelingId.reshape(glad, x, y, width, height);
        hullPass.reshape(glad, x, y, width, height);
        displayHullPass.reshape(glad, x, y, width, height);
        if(labeling != null) {
            labeling.reshape(glad, x, y, width, height);
            labeling.calculateLayout = true;
        }

        //silhouette.reshape(glad, x, y, width, height);
        //hallo.reshape(glad, x, y, width, height);
        //halloCompose.reshape(glad, x, y, width, height);

        // update shader parameters depending on size of window
        offsetX.setValue(1f/glad.getSurfaceWidth());
        offsetY.setValue(1f/glad.getSurfaceHeight());
        
        System.out.println("Reshape done");
    }

    public void displayChanged(GLAutoDrawable glad, boolean bln, boolean bln1) {
    }

    public void incParam(GlslProgramFloatParameter param, float incValue, float max) {
        System.out.println("Incrementing parameter " + param.name);
        if(param.getValue() + incValue <= max) {
            param.setValue(param.getValue() + incValue);
        }
        else {
            param.setValue(max);
        }
        System.out.println("New value set to: " + param.getValue());
    }

    public void decParam(GlslProgramFloatParameter param, float decValue, float min) {
        System.out.println("Decrementing parameter " + param.name);
        if(param.getValue() - decValue >= min) {
            param.setValue(param.getValue() - decValue);
        }
        else {
            param.setValue(min);
        }
        System.out.println("New value set to: " + param.getValue());
    }

    public void createUI(final Window w) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (Exception e) {
            System.err.println("The atempt to set system Look&Feel failed. Continuing with default.");
        }

        window = w;
        
        objects = new JFrame("3D Objects");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane scrolling = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        objects.getContentPane().add(scrolling);
        for(Mesh mesh : scene.getAllMeshes()) {
            JLabel label = new JLabel(mesh.getName());
            panel.add(label);
            FloatSlider slider = new FloatSlider(importance[mesh.getId()], false, JSlider.HORIZONTAL, 0f, 1f);
            panel.add(slider);
        }
        objects.pack();
        objects.setVisible(true);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);


        JMenu modelParamsMenu = new JMenu("Parameters");
        
        JMenuItem saveParamsItem = new JMenuItem("Save parameters");
        final Ghosting_new ls = this;
        saveParamsItem.addActionListener(new ActionListener() {
            BufferedWriter writer = null;
            public void actionPerformed(ActionEvent e) {
                try {
                    writer = new BufferedWriter(new FileWriter(parametersPath));
                    GhostingParameters.saveParameters(ls, writer);
                    if(labeling != null) LabelingParameters.saveParameters(labeling, writer);
                    ModelParameters.saveParameters(scene, layerGroups.get(), labelGroups.get(), importance, writer);
                    writer.close();
                } catch (IOException ex) {
                    System.out.println("Error, Cannot save parameters.\n" + ex.toString());
                }
            }
        });
        modelParamsMenu.add(saveParamsItem);

        JMenuItem loadParamsItem = new JMenuItem("Load parameters");
        loadParamsItem.addActionListener(new ActionListener() {
            BufferedReader reader = null;
            public void actionPerformed(ActionEvent e) {
                try {
                    reader = new BufferedReader(new FileReader(parametersPath));
                    GhostingParameters.loadParameters(ls, reader);
                    if(labeling != null) LabelingParameters.loadParameters(labeling, reader);
                    Grouping newLayerGroups = new Grouping();
                    Grouping newLabelGroups = new Grouping();
                    ModelParameters.loadParameters(scene, newLayerGroups, newLabelGroups, importance, reader);
                    reader.close();
                    layerGroups.set(newLayerGroups);
                    labelGroups.set(newLabelGroups);
                } catch (IOException ex) {
                    System.out.println("Error, Cannot load parameters.\n" + ex.toString());
                }
            }
        });
        modelParamsMenu.add(loadParamsItem);
        JMenuItem saveStateItem = new JMenuItem("Save state ...");
        saveStateItem.addActionListener(new ActionListener() {
            BufferedWriter writer = null;
            File file = null;
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showSaveDialog(w.frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    try {
                        Properties properties = new Properties();
                        GhostingParameters.saveProperties(ls, properties);
                        if(labeling != null) LabelingParameters.saveProperties(labeling, properties);
                        CameraProperties.saveProperties(w.getControler(), properties);
                        ModelParameters.saveProperties(ls, properties);
                        
                        writer = new BufferedWriter(new FileWriter(file));
                        properties.store(writer, null);
                        writer.close();
                    } catch (IOException ex) {
                        System.out.println("Error, Cannot save state.\n" + ex.toString());
                    }
                }
            }
        });
        modelParamsMenu.add(saveStateItem);

        JMenuItem loadStateItem = new JMenuItem("Load state ...");
        loadStateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(w.frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    loadState(file);
                }
            }
        });
        modelParamsMenu.add(loadStateItem);
        w.addMenu(modelParamsMenu);

        JMenu layoutMenu = new JMenu("View");
        BooleanMenuItem leftMenuItem = new BooleanMenuItem("Selective transparency", selectiveTransparency);
        layoutMenu.add(leftMenuItem);

        BooleanMenuItem rightMenuItem = new BooleanMenuItem("Layering", layering);
        layoutMenu.add(rightMenuItem);

        BooleanMenuItem leftRightMenuItem = new BooleanMenuItem("Attention", attention);
        layoutMenu.add(leftRightMenuItem);

        BooleanMenuItem topMenuItem = new BooleanMenuItem("Importance filtering", importanceFiltering);
        layoutMenu.add(topMenuItem);

        BooleanMenuItem distanceMenuItem = new BooleanMenuItem("Distance opacity", distanceOpacity);
        layoutMenu.add(distanceMenuItem);

        BooleanMenuItem shapeMenuItem = new BooleanMenuItem("Shape opacity", shapeOpacity);
        layoutMenu.add(shapeMenuItem);

        BooleanMenuItem edgesMenuItem = new BooleanMenuItem("Edges", edges);
        layoutMenu.add(edgesMenuItem);

        BooleanMenuItem backgroundMenuItem = new BooleanMenuItem("Background", background);
        layoutMenu.add(backgroundMenuItem);
        
        if(labeling != null) {
            BooleanMenuItem redblueMenuItem = new BooleanMenuItem("Red/Blue visualization", redBlueVis);
            layoutMenu.add(redblueMenuItem);

            BooleanMenuItem selectionMenuItem = new BooleanMenuItem("Selection", selection);
            layoutMenu.add(selectionMenuItem);

            BooleanMenuItem labelingMenuItem = new BooleanMenuItem("Show labeling", showLabeling);
            layoutMenu.add(labelingMenuItem);

            BooleanMenuItem fakeLabelsMenuItem = new BooleanMenuItem("Show fake labels", fakeLabels);
            layoutMenu.add(fakeLabelsMenuItem);

            BooleanMenuItem transparencyMenuItem = new BooleanMenuItem("Labeling considers transparency", labeling.considerTransparency);
            layoutMenu.add(transparencyMenuItem);

            BooleanMenuItem invisibleMenuItem = new BooleanMenuItem("Label invisible", labeling.labelInvisibleGeometry);
            layoutMenu.add(invisibleMenuItem);

            BooleanMenuItem showIdMenuItem = new BooleanMenuItem("Show id", showId);
            layoutMenu.add(showIdMenuItem);

            BooleanMenuItem showRoiMenuItem = new BooleanMenuItem("Show roi", roiVis);
            layoutMenu.add(showRoiMenuItem);

            BooleanMenuItem showInternalAreaMenuItem = new BooleanMenuItem("Show internal area", showInternalArea);
            layoutMenu.add(showInternalAreaMenuItem);
            
            BooleanMenuItem showCompositionMenuItem = new BooleanMenuItem("Show composition", showComposition);
            layoutMenu.add(showCompositionMenuItem);
        }
        
        BooleanMenuItem showParametersMenuItem = new BooleanMenuItem("Show parameters", showParameters);
        showParameters.addChangeListener(new TigerChangeListener() {
            @Override
            public void stateChanged(TigerChangeEvent e) {
                GlslProgramIntParameter item = (GlslProgramIntParameter) e.getSource();
                if(item.getValue() == 1) {
                    w.showParameters();
                    //item.setSelected(false);
                }
                else {
                    w.hideParameters();
                    //item.setSelected(true);
                }
            } 
        });
        layoutMenu.add(showParametersMenuItem);
        
        BooleanMenuItem showModelParametersMenuItem = new BooleanMenuItem("Show model parameters", showModelParameters);
        showModelParameters.addChangeListener(new TigerChangeListener() {
            @Override
            public void stateChanged(TigerChangeEvent e) {
                GlslProgramIntParameter item = (GlslProgramIntParameter) e.getSource();
                if(item.getValue() == 1) {
                    objects.setVisible(true);
                    //item.setSelected(false);
                }
                else {
                    objects.setVisible(false);
                    //item.setSelected(true);
                }
            } 
        });
        layoutMenu.add(showModelParametersMenuItem);
        
        

        FloatSlider opacitySlider = new FloatSlider(importanceDecrease, false, JSlider.HORIZONTAL, 0f, 1f);
        FloatSlider shapeSlider = new FloatSlider(shapePower, false, JSlider.HORIZONTAL, 0f, 2.5f);
        FloatSlider distanceSlider = new FloatSlider(distancePower, false, JSlider.HORIZONTAL, 0f, 100f);
        FloatSlider importanceSlider = new FloatSlider(importancePower, false, JSlider.HORIZONTAL, 0f, 2.5f);
        //FloatSlider sizeSlider = new FloatSlider(exploration.reduction, false, JSlider.HORIZONTAL, 0f, 10f);
        IntSlider showIdSlider = new IntSlider(idToShow, JSlider.HORIZONTAL, 0, labelGroups.get().getSize());
        IntSlider layersSlider = new IntSlider(numberOfLayersToRender, JSlider.HORIZONTAL, 1, 25);
        FloatSlider layerTransparencySlider = new FloatSlider(layerTransparencyTreshold, false, JSlider.HORIZONTAL, 0f, 1f);
        FloatSlider accumulatedTransparencySlider = new FloatSlider(accumulatedTransparencyTreshold, false, JSlider.HORIZONTAL, 0f, 1f);

        JPanel paramsFrame = new JPanel();
        paramsFrame.setBorder(BorderFactory.createTitledBorder("Ghosting"));
        LayoutManager layout = new BoxLayout(paramsFrame, BoxLayout.Y_AXIS);
        paramsFrame.setLayout(layout);

        paramsFrame.add(new JLabel("Gamma"));
        paramsFrame.add(opacitySlider);

        paramsFrame.add(new JLabel("Shape"));
        paramsFrame.add(shapeSlider);

        paramsFrame.add(new JLabel("Distance"));
        paramsFrame.add(distanceSlider);

        paramsFrame.add(new JLabel("Importance"));
        paramsFrame.add(importanceSlider);

        //paramsFrame.add(new JLabel("Size"));
        //paramsFrame.add(sizeSlider);

        paramsFrame.add(new JLabel("ShowId"));
        paramsFrame.add(showIdSlider);
        
        paramsFrame.add(new JLabel("Number of rendered layers"));
        paramsFrame.add(layersSlider);

        paramsFrame.add(new JLabel("Layer Transparency Treshold"));
        paramsFrame.add(layerTransparencySlider);

        paramsFrame.add(new JLabel("Accumulated Transparency Treshold"));
        paramsFrame.add(accumulatedTransparencySlider);

        w.params.add(paramsFrame);

        w.addMenu(layoutMenu);
        if(labeling != null) {
            labeling.createUI(w);
        }
    }
    
    public void loadState(File stateFile) {
        try {
            Properties properties = new Properties();
            BufferedReader reader = new BufferedReader(new FileReader(stateFile));
            properties.load(reader);
            reader.close();

            ModelParameters.loadProperties(this, properties);
            loadProperties(properties);
            if(labeling != null) labeling.loadProperties(properties);
            CameraProperties.loadProperties(window.getControler(), properties);
        } catch (IOException ex) {
            System.out.println("Error, Cannot load state.\n" + ex.toString());
        }
    }

    public void dispose(GLAutoDrawable arg0) {
    }

    protected abstract void createCompositionPass(); 
    
    protected void createBlendPass() {
        RenderState rs = new RenderState();
        rs.clearBuffers(false);
        rs.disable(GL.GL_DEPTH_TEST);
        rs.enable(GL.GL_BLEND);
        rs.setBlendFuncSeparate(GL.GL_DST_ALPHA, GL.GL_ONE, GL.GL_DST_ALPHA, GL.GL_ZERO);
        //rs.setBlendFunc(GL.GL_ONE_MINUS_DST_ALPHA, GL.GL_ONE);

        InputStream fragment = ClassLoader.getSystemResourceAsStream("tiger/effects/ghosting/blend.frag");
        blendLayers = new Saq(fragment);
        blendLayers.addTexture(compositionTexture, "composition");
        blendLayers.addTexture(finalLabelingId, "labelingBitmask");
        blendLayers.addTexture(labelingId, "labelingId");
        blendLayers.setTarget(finalBuffer);
        blendLayers.renderState = rs;
    }
    
    public void setParametersPath(String path) {
        parametersPath = path;
    }
    
    public void setInteractionAlowed(boolean interactionAlowed) {
        this.interactionAlowed = interactionAlowed;
    }
}
