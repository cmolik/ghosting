/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tiger.effects.labeling;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import tiger.core.Window;

/**
 *
 * @author cmolikl
 */
public abstract class AbstractLabeling implements GLEventListener {
    
    public abstract void createUI(Window w);
    
    public abstract void calculate(GLAutoDrawable drawable);
    
}
