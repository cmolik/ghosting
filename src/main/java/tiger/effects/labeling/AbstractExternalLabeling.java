package tiger.effects.labeling;

import java.awt.geom.Rectangle2D;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import java.nio.FloatBuffer;
import java.util.Properties;
import tiger.core.FrameBuffer;
import tiger.core.GlslProgramFloatParameter;
import tiger.core.GlslProgramIntParameter;
import tiger.core.GlslProgramUIntArrayParameter;
import tiger.core.Link;
import tiger.core.MutableLink;
import tiger.core.Pass;
import tiger.core.SwapingLink;
import tiger.core.Texture;
import tiger.core.Texture2D;
import tiger.core.Window;

import tiger.effects.ghosting.Grouping;
import tiger.util.saq.Saq;


public abstract class AbstractExternalLabeling extends AbstractLabeling {
    
    protected Boolean initialized = false;
    protected boolean verbose = false;
    public boolean calculateLayout = true;

    static float scale = 1.1f;

    public static final int HORIZONTAL_LAYOUT = 0;
    public static final int VERTICAL_LAYOUT = 1;
    public static final int RADIAL_LAYOUT = 2;
    public static final int LEFT_LAYOUT = 3;
    public static final int RIGHT_LAYOUT = 4;
    public static final int TOP_LAYOUT = 5;
    public static final int BOTTOM_LAYOUT = 6;
    public static final int MANUAL_LAYOUT = 7;
    public static final int MANUAL_RADIAL_LAYOUT = 8;
    protected int layoutType = 0;

    public static final int QUERY_TRESHOLD = 50;

    //private Scene scene;
    protected Link<Grouping> labels;
    protected int maxNumberOfLabels;
    protected int numberOfLabels;
    protected int size;
    
    float maxLookupDistance;

    //Texture2D idTexture;
    //FrameBuffer idBuffer;

    //Texture2D hullTexture;
    //FrameBuffer hullBuffer;

    public Texture2D outline;
    FrameBuffer outlineBuffer;

    Texture2D texture2;
    FrameBuffer target2;

    public Texture2D distanceTexture1;
    public Texture2D distanceTexture2;
    public Texture2D maxDistanceTexture2;

    FrameBuffer distanceBuffer1;
    FrameBuffer distanceBuffer2;
    FrameBuffer maxDistanceBuffer2;

    public Texture2D equilibriumTexture;
    Texture2D equilibriumTexture2;
    FrameBuffer equilibriumBuffer;

    Texture2D maxTexture1;
    Texture2D maxTexture2;
    SwapingLink<Texture> maxTexture;
    SwapingLink<Texture> invMaxTexture;
    FrameBuffer maxBuffer1;
    FrameBuffer maxBuffer2;
    SwapingLink<FrameBuffer> maxBuffer;

    Texture2D idSumTexture;
    FrameBuffer idSumBuffer;

    Texture2D sumTexture;
    FrameBuffer sumBuffer;

    Texture2D minSumTexture;
    Texture2D minSumIdTexture;
    FrameBuffer minSumBuffer;

    Texture2D mappingTexture;
    FrameBuffer mappingBuffer;

    //Pass idPass;
    //Pass hullPass;
    Pass idSum;
    Saq silhouette1;
    Saq silhouette2;
    AbstractErosion dist1;
    AbstractErosion dist2;
    Pass maxDist2;
    Pass equilibrium;
    Pass max;
    Pass sum;
    Pass minSum;
    Pass substract;
    //Pass draw;
    Pass saq;

    public GlslProgramFloatParameter delta;
    public GlslProgramFloatParameter delta2;
    GlslProgramFloatParameter maxLabels;
    GlslProgramFloatParameter xOffset;
    GlslProgramFloatParameter yOffset;
    public GlslProgramFloatParameter salienceWeight;
    public GlslProgramFloatParameter lengthWeight;
    public GlslProgramFloatParameter overlapWeight;
    GlslProgramUIntArrayParameter doNotLabelParam;
    
    public GlslProgramIntParameter noLabel = new GlslProgramIntParameter("noLabel", 0);
    public GlslProgramIntParameter labelInvisibleGeometry = new GlslProgramIntParameter("LabelInvisible", 1);
    public GlslProgramIntParameter considerTransparency = new GlslProgramIntParameter("considerTransparency", 1);
    public GlslProgramIntParameter considerSalience = new GlslProgramIntParameter("considerSalience", 1);
    public GlslProgramIntParameter considerLength = new GlslProgramIntParameter("considerLength", 1);
    public GlslProgramIntParameter considerCount = new GlslProgramIntParameter("considerCount", 1);
    public GlslProgramIntParameter considerCoherence = new GlslProgramIntParameter("considerCoherence", 1);

    public MutableLink<float[]> oq = new MutableLink(null);

    FloatBuffer pbuffer;
    FloatBuffer sbuffer;
    float[] placedArray;
    int[] doNotLabel = new int[4];

    public AbstractExternalLayout hLayout;
    public AbstractExternalLayout vLayout;
    public AbstractExternalLayout rLayout;
    public AbstractExternalLayout mLayout;
    public AbstractExternalLayout mrLayout;
    public String mLayoutPath;
    public String mrLayoutPath;
    
    public AbstractExternalLayout layout;
    
    public abstract void register(Texture2D idTexture, Texture2D countTexture, Texture2D hullTexture, Link<Grouping> labelGroups, int maxNumberOfLabels, int size, int layoutType, GlslProgramFloatParameter[] importance);

    public abstract float[] getLeaderLines(); 

    public abstract Rectangle2D[] getLabelBounds();
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public void setLayoutType(int layoutType) {
        float delta = 0f;
        switch(layoutType) {
            case LEFT_LAYOUT:
            case RIGHT_LAYOUT:
                delta = 0.05f / scale;
                break;
            case HORIZONTAL_LAYOUT:
            case VERTICAL_LAYOUT:
            case RADIAL_LAYOUT:
            case TOP_LAYOUT:
            case BOTTOM_LAYOUT:
                delta = 0.2f / scale;
                break;
        }
        setLayoutType(layoutType, delta);
    }
    
    public int getLayoutType() {
        return layoutType;
    }
    
    public void setLayoutType(int layoutType, float delta) {
        if(layoutType > 6 && mLayoutPath != null) {
            dist2.setLayoutType(layoutType);
            this.layoutType = layoutType;
            this.delta.setValue(delta);

            switch(layoutType) {
                case LEFT_LAYOUT:
                case RIGHT_LAYOUT:
                case HORIZONTAL_LAYOUT:
                    layout = hLayout;
                    break;
                case VERTICAL_LAYOUT:
                case TOP_LAYOUT:
                case BOTTOM_LAYOUT:
                    layout = vLayout;
                    break;
                case RADIAL_LAYOUT:
                    layout = rLayout;
                    break;
                case MANUAL_LAYOUT:
                    layout = mLayout;
                    break;
                case MANUAL_RADIAL_LAYOUT:
                    layout = mrLayout;
                    break;
            }
        }
    }
    
    public void setLineType(int lineType) {
        vLayout.lineType = lineType;
        hLayout.lineType = lineType;
    }
    
    public int getLineType() {
        return hLayout.lineType;
    }
    
    public void saveProperties(Properties properties) {
        properties.setProperty("layout.type" , "" + getLayoutType());
        properties.setProperty("layout.line", "" + getLineType());
        
        properties.setProperty(delta.name, delta.toString());
        properties.setProperty(delta2.name, delta2.toString());
        properties.setProperty(salienceWeight.name, salienceWeight.toString());
        properties.setProperty(lengthWeight.name, lengthWeight.toString());
        properties.setProperty(overlapWeight.name, overlapWeight.toString());
    }
    
    public void loadProperties(Properties properties) {
        String p = properties.getProperty(delta.name);
        if(delta != null) delta.parseValue(p);
        p = properties.getProperty(delta2.name);
        if(p != null) delta2.parseValue(p);
        p = properties.getProperty(salienceWeight.name);
        if(p != null) salienceWeight.parseValue(p);
        p = properties.getProperty(lengthWeight.name);
        if(p != null) lengthWeight.parseValue(p);
        p = properties.getProperty(overlapWeight.name);
        if(p != null) overlapWeight.parseValue(p);
        
        p = properties.getProperty("layout.type");
        if(p != null) {
            if(delta != null) setLayoutType(Integer.parseInt(p), delta.getValue());
            else setLayoutType(Integer.parseInt(p));
        }
        p = properties.getProperty("layout.line");
        if(p != null) setLineType(Integer.parseInt(p));
    }
}