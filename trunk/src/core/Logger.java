package core;

import java.io.PrintStream;

public class Logger {
	
	public static void write(PrintStream stream, String message) {
		stream.print(message);
	}
}
