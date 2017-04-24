package vn.edu.vnu.uet.nlp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuanphong94
 *
 */
public class FileUtils {
	public static final Charset UNICODE = Charset.forName("utf-8");

	public static void appendFile(List<String> lines, String pathname) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathname, true)))) {
			for (String line : lines) {
				if (line.isEmpty() || line == null)
					continue;
				out.println(line);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void truncateFile(String filename) throws IOException {
		Path file = Paths.get(filename);
		BufferedWriter bf = Files.newBufferedWriter(file, UNICODE, StandardOpenOption.CREATE);
		bf = Files.newBufferedWriter(file, UNICODE, StandardOpenOption.WRITE);
		bf = Files.newBufferedWriter(file, UNICODE, StandardOpenOption.TRUNCATE_EXISTING);
		bf.close();
		return;
	}

	/**
	 * Create a UTF-8 BufferedWriter from the provided file name. If the file
	 * has not existed, create it. If the file has had content yet, truncate it.
	 * 
	 * @param filename
	 *            The path to the file
	 * @return A UTF-8 BufferedWriter of the truncated file
	 * @throws IOException
	 */
	public static BufferedWriter newUTF8BufferedWriterFromNewFile(String filename) throws IOException {
		BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE);
		bw = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8, StandardOpenOption.WRITE);
		bw = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

		return bw;
	}

	/**
	 * Create a UTF-8 BufferedReader from the provided file name.
	 * 
	 * @param filename
	 *            The path to the file
	 * @return A UTF-8 BufferedReader of the file
	 * @throws IOException
	 */
	public static BufferedReader newUTF8BufferedReaderFromFile(String filename) throws IOException {
		return Files.newBufferedReader(Paths.get(filename), Constants.cs);
	}

	public static List<String> readFile(String filename) throws IOException {
		List<String> list = new ArrayList<String>();

		BufferedReader br = Files.newBufferedReader(Paths.get(filename), Constants.cs);
		for (String line; (line = br.readLine()) != null;) {
			if (line.trim().isEmpty()) {
				continue;
			}

			list.add(line.trim());
		}

		return list;
	}
}
