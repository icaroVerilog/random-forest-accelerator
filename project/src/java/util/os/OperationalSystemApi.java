package project.src.java.util.os;

import java.io.IOException;

public class OperationalSystemApi {
	public static void clearTerminal() {
		try {
			String os = System.getProperty("os.name").toLowerCase();

			System.out.println(os);

			if (os.contains("windows")) {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} else {
				new ProcessBuilder("clear").inheritIO().start().waitFor();
			}
		} catch (IOException | InterruptedException ignored) {
		}
	}
}
