import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.beans.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main extends JPanel implements ActionListener, PropertyChangeListener{
	
	public static int prog = 0;
    private JProgressBar progressBar;
    private JButton startButton;
    public static JTextArea taskOutput;
    private Task task;
    
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

            while (prog < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException ignore) {}
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
            //taskOutput.append("Done!\n");
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
 
        taskOutput = new JTextArea(10, 30);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        DefaultCaret caret = (DefaultCaret)taskOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 
        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(progressBar);
        
        JScrollPane jScrollPane = new JScrollPane(taskOutput);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
        add(panel, BorderLayout.PAGE_START);
        add(jScrollPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
    }
    
    public void actionPerformed(ActionEvent evt) {
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        /////////////////////////////////////////////////////////////////////////////////////////////////
		ProcessBuilder processBuilder = new ProcessBuilder();
		//processBuilder.command("/bin/bash", "-c", "sh /root/Desktop/createFile.sh &");
		processBuilder.command("/bin/bash", "-c", "sh /root/Desktop/install.sh &");
		//processBuilder.command("/bin/bash", "-c", "sh /media/CDROM/install.sh &");
		try {
			Process process = processBuilder.start();
			System.out.println(process.getInputStream());
			int exitVal = process.waitFor();
			if(exitVal == 0) {
				System.out.println("SUCCESS");
			} else {
				System.out.println("FAILED");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////
        //Instances of javax.swing.SwingWorker are not reusable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
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
 
        Path dir = Paths.get("/root/Desktop/test");
        boolean recursive = false;
        try {
			new WatchDir1(dir, recursive).processEvents();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}//end main

}

class WatchDir1 {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDir1(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                //System.out.println(child);

                // print out event
                //Main.taskOutput.append(child.toString()+"\n");
                //Main.prog++;
/////////////////////////////////EDIT HERE/////////////////////////////////
                try {
                	if(child.toString().equals("/root/Desktop/test/output.log")) {
                		Main.prog = (int)((float) ((Files.size(child)/864f)*100));
                	}
                	if(child.toString().equals("/root/Desktop/test/helper.log") && event.kind().name().equals(ENTRY_MODIFY.toString())) {
                		String msg = "";
                		switch((int)Files.size(child)) {
                		  case 2:
                			  msg = "Installation log can be found at /var/log/update\nInitializing update";
                		    break;
                		  case 4:
                			  msg = "Backing up old data";
                  		    break;
                  		  case 6:
                  			msg = "Copying new data to system";
                  		    break;
                		  case 8:
                			  msg = "Software update is complete";
                  		    break;
                  		  case 10:
                  			msg = "Please restart your computer";
                  		    break;
                		  default:
                		    // code block
                		}
//                		Main.taskOutput.append(String.format(
//    							"%s: %s\n", event.kind().name(), Files.size(child))
//    							);
                		Main.taskOutput.append(String.format(
                				"%s\n", msg
    							));
                	}
    					
                	

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
///////////////////////////////////////////////////////////////////////////

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }

//    public static void main(String[] args) throws IOException {
//        // parse arguments
//        if (args.length == 0 || args.length > 2)
//            usage();
//        boolean recursive = false;
//        int dirArg = 0;
//        if (args[0].equals("-r")) {
//            if (args.length < 2)
//                usage();
//            recursive = true;
//            dirArg++;
//        }
//
//        // register directory and process its events
//        Path dir = Paths.get(args[dirArg]);
//        new WatchDir(dir, recursive).processEvents();
//    }
}
