import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {
	
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Hello World");
        frame.getContentPane().add(label);
 
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
		//temp
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("bash", "-c", "sh /root/Desktop/scriptA.sh");
		try {
			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while(	(line=reader.readLine()) != null	) {
				output.append(line+"\n");
			}
			int exitVal = process.waitFor();
			if(exitVal == 0) {
				System.out.println("SUCCESS");
			} else {
				System.out.println("FAILED");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
