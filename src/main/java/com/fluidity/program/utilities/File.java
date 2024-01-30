package com.fluidity.program.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Scanner;

public class File {
	private String path;

	public File(final String path) {
		this.path = path;
	}

	public static void createFile(String name) {
		try {
			java.io.File fileToCreate = new java.io.File(name);
			fileToCreate.createNewFile();
		} catch (IOException e) {
			System.out.println("Error: File Could Not be Created: " + e.getMessage());
		}
	}

	public String readFile() {
		String output = null;
		try (Scanner scanner = new Scanner(Paths.get(path))) {
			while (scanner.hasNextLine()) {
				output = scanner.nextLine() + "\n";
			}
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return output;
	}

	public String writeLine(String line) {
		try (PrintWriter printWriter = new PrintWriter(path)) {
			printWriter.write(line);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
