package com.fluidity.program.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * @author Yalesan Thayalan {@literal <yalesan2006@outlook.com>}
 */
public class File {
	private String path;

	public File(final String path) {
		this.path = path;
	}

	public static void createFile(String path) {

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
		try (PrintWriter printWriter = new PrintWriter(path);) {
			printWriter.write(line);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
