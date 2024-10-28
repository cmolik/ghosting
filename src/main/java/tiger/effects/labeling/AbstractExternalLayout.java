/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.labeling;

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.geom.Rectangle2D;
import com.jogamp.opengl.GLAutoDrawable;
import javax.vecmath.Point2f;
import tiger.core.GlslProgramFloatParameter;
import tiger.core.GlslProgramIntParameter;
import tiger.core.Link;
import tiger.core.Pass;
import tiger.effects.ghosting.Grouping;

/**
 *
 * @author cmolikl
 */
public abstract class AbstractExternalLayout extends Pass {
    
    public static final float ALIGN_LEFT = 0f;
    public static final float ALIGN_MIDDLE = 0.5f;
    public static final float ALIGN_RIGHT = 1f;
    public static final float ALIGN_BOTTOM = 0f;
    public static final float ALIGN_TOP = 1f;

    public static final int STRAIGHT_LINE = 0;
    public static final int ORTHOGONAL_LINE = 1;

    public float[] lineColor = {0f, 0f, 0f};
    public float[] selectionColor = {0.8f, 0.4f, 0.0f};

    public int lineType = 1;
    
    protected Point2f[] anchors;
    protected Point2f[] labels;
    protected boolean[] valid;
    protected float[] vAlign;
    protected float[] hAlign;
    public Link<Grouping> names;
    public Rectangle2D[] labelBounds;
    public GlslProgramFloatParameter[] importance;
    
    protected Point2f[] prevAnchors;
    protected Point2f[] prevLabels;
    
    protected Link<float[]> oq;
    protected int treshold = 0;
    
    protected TextRenderer renderer;
    protected float labelHeight;
    public boolean renderLabels = true;
    
    public GlslProgramIntParameter fakeLabels = new GlslProgramIntParameter("fakeLabels", 0);
    public GlslProgramIntParameter colorLines = new GlslProgramIntParameter("ColorLines", 0);
    public GlslProgramIntParameter colorLabel = new GlslProgramIntParameter("ColorLabels", 0);
    
    public static final int BLACK_COLOR = 0;
    public static final int COLOR_BY_IMPORTANCE = 1;
    public static final int COLOR_BY_ERROR = 2;
    
    public abstract void setOcclusionQuery(Link<float[]> oq, int treshold);
    
    public abstract void calculateLayout(GLAutoDrawable drawable, float[] points);
}
