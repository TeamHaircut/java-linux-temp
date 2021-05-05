package teamhaircut;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.beans.*;
import java.util.Random;
import WatchDir;

public class Main extends JPanel implements ActionListener, PropertyChangeListener{
	
	public static int prog = 0;
    private JProgressBar progressBar;
    private JButton startButton;
    public static JTextArea taskOutput;
    private Task task;
    //String myLine = "start";
    
    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            Random random = new Random();
            int progress = 0;
            prog = progress;
            
            //Initialize progress property.
            setProgress(prog);
            
            /////////////////////////////////////////////////////////////////////////////////////////////////
//    		ProcessBuilder processBuilder = new ProcessBuilder();
//    		//processBuilder.command("bash", "-c", "sh /root/Desktop/scriptA.sh");
//    		processBuilder.command("java", "-version");
//    		try {
//    			Process process = processBuilder.start();
//    	         InputStream in = process.getInputStream();
//    	         for (int i = 0; i < in.available(); i++) {
//    	            System.out.println("" + in.read());
//    	         }
//    			List<String> results = readOutput(process.getInputStream());
//    			StringBuilder output = new StringBuilder();
//    			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//    			String line;
//    			//line = reader.readLine();
//    			while(	(line=reader.readLine()) != null	) {
//    				output.append(line+"\n");
//    			}
//    			int exitVal = process.waitFor();
//    			if(exitVal == 0) {
//    				System.out.println("SUCCESS");
//    				progress += 1;
//    				//myLine = line;
//    			} else {
//    				System.out.println("FAILED");
//    			}
//    		} catch (Exception e) {
//    			e.printStackTrace();
//    		}
    		///////////////////////////////////////////////////////////////////////////////////////////////////
            

            while (prog < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException ignore) {}
                //Make random progress.

                //progress += random.nextInt(10);
                setProgress(Math.min(prog, 100));
            }
            return null;
        }
 
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            startButton.setEnabled(true);
            setCursor(null); //turn off the wait cursor
            taskOutput.append("Done!\n");
        }
    }
	
    public Main() {
        super(new BorderLayout());
 
        //Create the demo's UI.
        startButton = new JButton("Install");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);
 
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
 
        taskOutput = new JTextArea(10, 30);//5,20
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
 
        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(progressBar);
        
        JScrollPane jScrollPane = new JScrollPane(taskOutput);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
        add(panel, BorderLayout.PAGE_START);
        add(jScrollPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
    }
    
    public void actionPerformed(ActionEvent evt) {
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            taskOutput.append(String.format(
                    "Completed %d%% of task.\n", task.getProgress()));
        } 
    } 
	
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SW Installer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        JComponent newContentPane = new Main();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

	public static void main(String[] args) {
		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
 
        //Path dir = Paths.get("C:/Users/RuthDan/Desktop/test");
        Path dir = Paths.get("/root/Desktop/test");
        boolean recursive = false;
        try {
			new WatchDir(dir, recursive).processEvents();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}//end main

}
