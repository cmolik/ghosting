/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Properties;
import tiger.effects.labeling.AbstractExternalLabeling;
import tiger.effects.labeling.AbstractExternalLabeling;
import tiger.effects.labeling.AbstractExternalLabeling;

/**
 *
 * @author cmolikl
 */
public class LabelingParameters {
    public static void saveParameters(AbstractExternalLabeling l, BufferedWriter w) throws IOException {
        w.write("" + l.delta.getValue());
        w.newLine();
        w.write("" + l.delta2.getValue());
        w.newLine();
        w.write("" + l.salienceWeight.getValue());
        w.newLine();
        w.write("" + l.lengthWeight.getValue());
        w.newLine();
        w.write("" + l.overlapWeight.getValue());
        w.newLine();
    }

    public static void loadParameters(AbstractExternalLabeling l, BufferedReader r) throws IOException {
        l.delta.setValue(Float.parseFloat(r.readLine()));
        l.delta2.setValue(Float.parseFloat(r.readLine()));
        l.salienceWeight.setValue(Float.parseFloat(r.readLine()));
        l.lengthWeight.setValue(Float.parseFloat(r.readLine()));
        l.overlapWeight.setValue(Float.parseFloat(r.readLine()));
    }
    
    public static void saveProperties(AbstractExternalLabeling l, Properties properties) {
        properties.setProperty("layout.type" , "" + l.getLayoutType());
        properties.setProperty("layout.line", "" + l.getLineType());
        
        properties.setProperty(l.delta.name, l.delta.toString());
        properties.setProperty(l.delta2.name, l.delta2.toString());
        properties.setProperty(l.salienceWeight.name, l.salienceWeight.toString());
        properties.setProperty(l.lengthWeight.name, l.lengthWeight.toString());
        properties.setProperty(l.overlapWeight.name, l.overlapWeight.toString());
    }
    
    public static void loadProperties(AbstractExternalLabeling l, Properties properties) {
        String delta = properties.getProperty(l.delta.name);
        if(delta != null) l.delta.parseValue(delta);
        String p = properties.getProperty(l.delta2.name);
        if(p != null) l.delta2.parseValue(p);
        p = properties.getProperty(l.salienceWeight.name);
        if(p != null) l.salienceWeight.parseValue(p);
        p = properties.getProperty(l.lengthWeight.name);
        if(p != null) l.lengthWeight.parseValue(p);
        p = properties.getProperty(l.overlapWeight.name);
        if(p != null) l.overlapWeight.parseValue(p);
        
        p = properties.getProperty("layout.type");
        if(p != null) {
            if(delta != null) l.setLayoutType(Integer.parseInt(p), l.delta.getValue());
            else l.setLayoutType(Integer.parseInt(p));
        }
        p = properties.getProperty("layout.line");
        if(p != null) l.setLineType(Integer.parseInt(p));
    }
}
