/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tiger.effects.ghosting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import mesh.loaders.ObjLoader;
import scene.Scene;
import scene.surface.mesh.Mesh;
import tiger.core.CameraProperties;
import tiger.core.Window;

/**
 *
 * @author cmolikl
 */
public class LabelingUserTest extends Thread {
    
    public static final String DELIMITER = "; ";
    
    private GhostingCmolik ls;
    private Window w;
    private JFrame frame;
    private JButton startButton;
    private JButton dontKnowButton;
    private JButton dontHaveLabel;
    private JButton exitButton;
    private Integer[] sequence;
    private int repeat;
    private long prevFrameTime;
    private boolean run;
    private boolean waitingForClick;
    
    private long highlightTime;
    private long clickTime;
    private int highlightId;
    private int clickId;
    private int error;
    
    private static final String LOG_PATH = "C:/Temp/log_";
    private BufferedWriter log;
    
    public LabelingUserTest(GhostingCmolik ls, Window w, int repeat) {
        this.ls = ls;
        this.w = w;
        this.repeat = repeat;
        this.run = false;
        this.waitingForClick = false;
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        Calendar cal = Calendar.getInstance();
        try {
            this.log = new BufferedWriter(new FileWriter(LOG_PATH + dateFormat.format(cal.getTime()) + ".csv")); 
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setLog(BufferedWriter log) {
        this.log = log;
    }
    
    public void setSequence(Integer[] sequence) {
        this.sequence = sequence;
    }
    
    @Override
    public void run() {
        int i = 0;
        while(true) {
            if(run) {
                if(!waitingForClick) {
                    try {
                        ls.roi.setValue(new int[] {0, 0, 0, 0});
                        //syncFrameRate(3000);
                        Thread.sleep(2000);
                        if(repeat == 0) {
                            try {
                                log.close();
                            }
                            catch(IOException e) {
                                e.printStackTrace();
                            }
                            setRun(false);
                            dontKnowButton.setEnabled(false);
                            dontHaveLabel.setEnabled(false);
                            exitButton.setEnabled(true);
                            return;
                        }
                        highlightTime = System.currentTimeMillis();
                        setRoi(sequence[i]);
                        highlightId = sequence[i];
                        System.out.println("ROI set to " + sequence[i]);
                        waitingForClick = true;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    Thread.yield();
                    continue;
                }
                i++;
                if(i >= sequence.length) {
                    if(repeat > 0) {
                        i = 0;
                        int last = sequence[sequence.length - 1];  
                        repeat--;
                        do {
                            Collections.shuffle(Arrays.asList(sequence));
                        }
                        while(sequence[0] == last);
                    }
                }
            }
            else {
                Thread.yield();
            }
        }
    }
    
    protected void syncFrameRate(long waitTime) {
        long nextFrameTime = prevFrameTime + waitTime;
        long currTime = System.currentTimeMillis();
        while (currTime < nextFrameTime) {
            Thread.yield();
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            currTime = System.currentTimeMillis();
        }
        prevFrameTime = currTime;
    }
    
    protected void setRoi(int roi) {
        int[] roiMask = new int[] {0, 0, 0, 0};
        int index = roi / 32;
        int bit = roi % 32;
        roiMask[index] = 1 << bit;
        ls.roi.setValue(roiMask);
        //ls.saveImage = true;
    }
    
    public void setRun(boolean run) {
        this.run = run;
    }
    
    public void sendClick(int id) {
        clickTime = System.currentTimeMillis();
        clickId = id;
        if(clickId != highlightId) {
            error = 1;
        }
        else {
            error = 0;
        }
        print();
        waitingForClick = false;
    }
    
    private void print() {
        try {
            log.write(highlightTime + DELIMITER + highlightId + DELIMITER + 
                      clickTime + DELIMITER + clickId + DELIMITER + 
                     (clickTime - highlightTime) + DELIMITER + error);
            log.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void createUI() {
        frame = new JFrame("Test");
        Dimension d = new Dimension(200, 50);
        
        startButton = new JButton("Start test");
        startButton.setMinimumSize(d);
        startButton.setMaximumSize(d);
        startButton.setPreferredSize(d);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(sequence == null) {
                    ArrayList<Integer> sequence = new ArrayList<Integer>(ls.scene.getSize());
                    for(int i = 0; i < ls.scene.getSize(); i++) {
                        if(ls.labeling.oq != null && ls.labeling.oq.get() != null && ls.labeling.oq.get()[i] < ls.labeling.QUERY_TRESHOLD) continue;
                        sequence.add(i);
                    }
                    Collections.shuffle(sequence);
                    setSequence(sequence.toArray(new Integer[sequence.size()]));
                }
                setRun(true);
                startButton.setEnabled(false);
                dontKnowButton.setEnabled(true);
                dontHaveLabel.setEnabled(true);
                exitButton.setEnabled(false);
                start();
            }
        });
        frame.getContentPane().add(startButton);
        
        dontKnowButton = new JButton("I cannot decide");
        dontKnowButton.setMinimumSize(d);
        dontKnowButton.setMaximumSize(d);
        dontKnowButton.setPreferredSize(d);
        dontKnowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendClick(-1);
            }
        });
        dontKnowButton.setEnabled(false);
        
        dontHaveLabel = new JButton("There is no label ");
        dontHaveLabel.setMinimumSize(d);
        dontHaveLabel.setMaximumSize(d);
        dontHaveLabel.setPreferredSize(d);
        dontHaveLabel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendClick(-2);
            }
        });
        dontHaveLabel.setEnabled(false);
        
        
        exitButton = new JButton("Exit");
        exitButton.setMinimumSize(d);
        exitButton.setMaximumSize(d);
        exitButton.setPreferredSize(d);
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.getContentPane().add(startButton);
        frame.getContentPane().add(dontKnowButton);
        frame.getContentPane().add(dontHaveLabel);
        frame.getContentPane().add(exitButton);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String... args) throws IOException, InterruptedException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("The atempt to set system Look&Feel failed. Continuing with default.");
        }
        
        if(args.length < 3) {
            System.out.println("Input parameters are missing.");
            System.out.println("Input parameters: path_to_model path_to_state number_of_iterations [sequence] [roi_color]");
            System.exit(1);
        }
        
        ArrayList<Integer> sequence = new ArrayList<Integer>();
        if(args.length >= 4) {
            StringTokenizer tokenizer = new StringTokenizer(args[3], ",");
            while(tokenizer.hasMoreTokens()) {
                sequence.add(Integer.parseInt(tokenizer.nextToken()));
            }
        } 
        
        float[] roiColor = new float[] {0.5f, 1f, 0.5f};
        if(args.length >= 5) {
            StringTokenizer tokenizer = new StringTokenizer(args[4], ",");
            for(int i = 0; i < roiColor.length; i++) {
                roiColor[i] = Integer.parseInt(tokenizer.nextToken()) / 255f;
            }
        }

        ObjLoader loader = new ObjLoader();
        final Scene<Mesh> scene = loader.loadFile(args[0]);
        Properties properties = new Properties();
        properties.load(new BufferedReader(new FileReader(args[1])));

        int i = 0;
        for(Mesh mesh : scene.getAllMeshes()) {
            mesh.setId(i);
            mesh.renderMethod = Mesh.VERTEX_BUFFER;
            System.out.println(i + " " + mesh.getName());
            i++;
        }

        final GhostingCmolik effect = new GhostingCmolik(scene);

        final Window w = new Window(scene, 512, 512);
        effect.createUI(w);
        w.setEffect(effect);
        w.runFastAsPosible = true;
        w.printFps = false;
        w.start();
        
        effect.loadProperties(properties);
        effect.labeling.loadProperties(properties);
        ModelParameters.loadProperties(effect, properties);
        CameraProperties.loadProperties(w.getControler(), properties);
        
        Thread.sleep(2000);
        
        //ls.setInteractionAlowed(false);
        //w.setInteractionAlowed(false);
        
        final LabelingUserTest test = new LabelingUserTest(effect, w, Integer.parseInt(args[2]));
        if(sequence.size() > 0) {
            Collections.shuffle(sequence);
            test.setSequence(sequence.toArray(new Integer[sequence.size()]));
        }
        effect.roiColor.setValue(roiColor);

        w.canvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                if(effect.labeling.layout.colorLabel.getValue() == 2 && effect.selection.getValue() == 1) {
                   int x = e.getX();
                   int y = effect.height - e.getY();
                   Rectangle2D[] bounds = effect.labeling.getLabelBounds();
                   Mesh[] meshes = scene.getAllMeshes().toArray(new Mesh[scene.getSize()]);
                   boolean labelClicked = false;
                   for(int i = 0; i < bounds.length; i++) {
                       System.out.println("<div style=\"position:absolute; top:" + (effect.height - bounds[i].getMinY() - bounds[i].getHeight()) + "px; left:" + bounds[i].getMinX() + "px; width:" + bounds[i].getWidth() + "px; height:" + (bounds[i].getHeight() + 5.0) + "px;\" onclick=\"sendClick(" + i + ")\"></div>");
                       if(bounds[i].contains(x, y)) {
                            System.out.println("Label " + i + " clicked.");
                            System.out.println("Should corespond to object " + meshes[i].getName());
                            labelClicked = true;
                            test.sendClick(i);
                       }
                   }
                   if(!labelClicked) {
                       System.out.println("Background clicked.");
                   }
                }
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        
        test.createUI();
    }
      
}
