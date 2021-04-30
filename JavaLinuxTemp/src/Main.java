import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
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
