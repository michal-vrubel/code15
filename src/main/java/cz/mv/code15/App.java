package cz.mv.code15;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class App {
	public static void main(String[] args) throws IOException, URISyntaxException {
		String sceneFilePath = "./scene.txt";
		String commandsFilePath = "./commands.txt";
		
		for (String arg : args) {
		    if (arg.startsWith("--scene")) {
		        String[] parts = arg.substring(2).split("=", 2);
		        sceneFilePath = parts[1];
		    }
		    if (arg.startsWith("--commands")) {
		        String[] parts = arg.substring(2).split("=", 2);
		        commandsFilePath =parts[1];
		    }
		}
		
		File sceneFile = new File(sceneFilePath);
		File commandsFile = new File(commandsFilePath);

		if (!sceneFile.exists()) {
			System.err.println("Scene file does not exist: " + sceneFilePath);
			System.exit(1);
		}

		if (!commandsFile.exists()) {
			System.err.println("Commands file does not exist: " + commandsFilePath);
			System.exit(1);
		}
		Warehouse warehouse = Warehouse.loadWarehouse(sceneFile);
		
		warehouse.loadAndRunCommands(commandsFile);
		
		long sum = warehouse.getSumOfCoordinates();
		System.out.println("SUM: " + sum);
		
		warehouse.printWarehouse();
	}
}
