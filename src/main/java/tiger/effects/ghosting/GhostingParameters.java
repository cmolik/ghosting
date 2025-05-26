/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author cmolikl
 */
public class GhostingParameters {
    public static void saveParameters(Ghosting ls, BufferedWriter w) throws IOException {
        w.write("" + ls.importanceDecrease.getValue());
        w.newLine();
        w.write("" + ls.shapePower.getValue());
        w.newLine();
        w.write("" + ls.distancePower.getValue());
        w.newLine();
        w.write("" + ls.importancePower.getValue());
        w.newLine();
    }

    public static void loadParameters(Ghosting ls, BufferedReader r) throws IOException {
        ls.importanceDecrease.setValue(Float.parseFloat(r.readLine()));
        ls.shapePower.setValue(Float.parseFloat(r.readLine()));
        ls.distancePower.setValue(Float.parseFloat(r.readLine()));
        ls.importancePower.setValue(Float.parseFloat(r.readLine()));
    }
    
    public static void saveProperties(Ghosting ls, Properties properties) {
        properties.setProperty("width", "" + ls.width);
        properties.setProperty("height", "" + ls.height);
        properties.setProperty(ls.selectiveTransparency.name, ls.selectiveTransparency.toString());
        properties.setProperty(ls.layering.name, ls.layering.toString());
        properties.setProperty(ls.attention.name, ls.attention.toString()); 
        properties.setProperty(ls.importanceFiltering.name, ls.importanceFiltering.toString());
        properties.setProperty(ls.distanceOpacity.name, ls.distanceOpacity.toString());
        properties.setProperty(ls.shapeOpacity.name, ls.shapeOpacity.toString()); 
        properties.setProperty(ls.edges.name, ls.edges.toString()); 
        properties.setProperty(ls.background.name, ls.background.toString()); 
        properties.setProperty(ls.redBlueVis.name, ls.redBlueVis.toString());  
        // properties.setProperty(ls.selection.name, ls.selection.toString()); 
        properties.setProperty(ls.showLabeling.name, ls.showLabeling.toString()); 
        properties.setProperty(ls.labeling.considerTransparency.name, ls.labeling.considerTransparency.toString()); 
        properties.setProperty(ls.labeling.labelInvisibleGeometry.name, ls.labeling.labelInvisibleGeometry.toString()); 
        properties.setProperty(ls.showId.name, ls.showId.toString()); 
        properties.setProperty(ls.roiVis.name, ls.roiVis.toString()); 
        properties.setProperty(ls.startTest.name, ls.startTest.toString()); 
        properties.setProperty(ls.showParameters.name, ls.showParameters.toString());
        properties.setProperty(ls.showModelParameters.name, ls.showModelParameters.toString()); 
        properties.setProperty(ls.labeling.hLayout.fakeLabels.name, ls.labeling.hLayout.fakeLabels.toString());
        properties.setProperty(ls.accumulatedTransparencyTreshold.name, ls.accumulatedTransparencyTreshold.toString());
        properties.setProperty(ls.layerTransparencyTreshold.name, ls.layerTransparencyTreshold.toString());
         
        properties.setProperty(ls.importanceDecrease.name, ls.importanceDecrease.toString());
        properties.setProperty(ls.shapePower.name, ls.shapePower.toString());
        properties.setProperty(ls.distancePower.name, ls.distancePower.toString());
        properties.setProperty(ls.importancePower.name, ls.importancePower.toString());
    }
    
    public static void loadProperties(Ghosting ls, Properties properties) {
        
        String p = properties.getProperty(ls.selectiveTransparency.name);
        if(p != null) ls.selectiveTransparency.parseValue(p);
        p = properties.getProperty(ls.layering.name);
        if(p != null) ls.layering.parseValue(p);
        p = properties.getProperty(ls.attention.name);
        if(p != null) ls.attention.parseValue(p);
        p = properties.getProperty(ls.importanceFiltering.name);
        if(p != null) ls.importanceFiltering.parseValue(p);
        p = properties.getProperty(ls.distanceOpacity.name);
        if(p != null) ls.distanceOpacity.parseValue(p);
        p = properties.getProperty(ls.shapeOpacity.name);
        if(p != null) ls.shapeOpacity.parseValue(p);
        p = properties.getProperty(ls.edges.name);
        if(p != null) ls.edges.parseValue(p);
        p = properties.getProperty(ls.background.name);
        if(p != null) ls.background.parseValue(p);
        p = properties.getProperty(ls.redBlueVis.name);
        if(p != null) ls.redBlueVis.parseValue(p);
        // p = properties.getProperty(ls.selection.name);
        // if(p != null) ls.selection.parseValue(p);
        p = properties.getProperty(ls.showLabeling.name);
        if(p != null) ls.showLabeling.parseValue(p);
        if(ls.labeling != null) {
            p = properties.getProperty(ls.labeling.considerTransparency.name);
            if(p != null) ls.labeling.considerTransparency.parseValue(p);
            p = properties.getProperty(ls.labeling.labelInvisibleGeometry.name);
            if(p != null) ls.labeling.labelInvisibleGeometry.parseValue(p);
        }
        p = properties.getProperty(ls.showId.name);
        if(p != null) ls.showId.parseValue(p);
        p = properties.getProperty(ls.roiVis.name);
        if(p != null) ls.roiVis.parseValue(p);
        p = properties.getProperty(ls.startTest.name);
        if(p != null) ls.startTest.parseValue(p);
        p = properties.getProperty(ls.showParameters.name);
        if(p != null) ls.showParameters.parseValue(p);
        p = properties.getProperty(ls.showModelParameters.name);
        if(p != null) ls.showModelParameters.parseValue(p);
        if(ls.labeling != null) {
            p = properties.getProperty(ls.labeling.hLayout.fakeLabels.name);
            if(p != null) {
                ls.labeling.hLayout.fakeLabels.parseValue(p);
                ls.labeling.hLayout.fakeLabels.setValue(ls.labeling.hLayout.fakeLabels.getValue());
                ls.labeling.hLayout.fakeLabels.setValue(ls.labeling.hLayout.fakeLabels.getValue());
            }
        }
        p = properties.getProperty(ls.accumulatedTransparencyTreshold.name);
        if(p != null) ls.accumulatedTransparencyTreshold.parseValue(p);
        p = properties.getProperty(ls.layerTransparencyTreshold.name);
        if(p != null) ls.layerTransparencyTreshold.parseValue(p);
             
        p = properties.getProperty(ls.importanceDecrease.name);
        if(p != null) ls.importanceDecrease.parseValue(p);
        p = properties.getProperty(ls.shapePower.name);
        if(p != null) ls.shapePower.parseValue(p);
        p = properties.getProperty(ls.distancePower.name);
        if(p != null) ls.distancePower.parseValue(p);
        p = properties.getProperty(ls.importancePower.name);
        if(p != null) ls.importancePower.parseValue(p);
        
        p = properties.getProperty("width");
        if(p != null) ls.width = Integer.parseInt(p);
        p = properties.getProperty("height");
        if(p != null) ls.height = Integer.parseInt(p);
        
        Dimension d = new Dimension(ls.width, ls.height);
        ls.window.canvas.setMinimumSize(d);
        ls.window.canvas.setMaximumSize(d);
        ls.window.canvas.setPreferredSize(d);
        ls.window.canvas.setSize(d);
        ls.window.frame.pack();
        
        System.out.println("Ghosting parameters changed");
    }
}
