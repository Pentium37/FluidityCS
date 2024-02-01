package com.fluidity.program.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
	private static Logger logger = LogManager.getLogger(FileHandler.class);

	// Prevents the object from being created with the default constructor
	private FileHandler() {
		throw new UnsupportedOperationException("Utility Class");
	}

	public static boolean createFile(String name) {
		try {
			Path newFilePath = Paths.get(name);
			Files.createFile(newFilePath);
		} catch (IOException e) {
			logger.error("Creating new file failed.", e);
			return false;
		}
		return true;
	}

	public static List<String> readFile(String fileName) {
		Path path = Path.of(fileName);
		List<String> file = new ArrayList<>();
		try {
			file = Files.readAllLines(path);
		} catch (IOException e) {
			logger.error("Reading file failed.", e);
		}
		return file;
	}

	public static boolean writeLine(String fileName, String data) {
		Path path = Path.of(fileName);
		try {
			Files.writeString(path, data);
		} catch (IOException e) {
			logger.error("Writing to file failed.", e);
			return false;
		}
		return true;
	}

	public static boolean clearFile(String fileName) {
		Path path = Path.of(fileName);

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write("");
			writer.flush();
		} catch (IOException e) {
			logger.error("Writing to file failed.", e);
			return false;
		}
		return true;
	}
}
