package ptui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RITUncompress {
	public static void main(String[] args) {

		if (args.length != 2) {
			// If the command line does not have two arguments, display a usage error and
			// exit.
			System.err.println("Usage: java RITUncompress compressed.rit uncompressed.txt");
			System.exit(-1);
			;
		}
		List<Integer> vals = parseInput(args);

	}

	private static List<Integer> parseInput(String[] args) {
		File in = new File(args[0]);
		List<Integer> ints = new ArrayList<>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(in));

			while (br.ready()) {
				ints.add(Integer.parseInt(br.readLine()));
			}
			br.close();
			System.out.println();
		} catch (NumberFormatException ex) {

			System.err.println("The input file contains invalid characters.");
			System.exit(-4);

		} catch (IOException ex) {
			// If the input file does not exist or is not readable, display an error message
			// and exit.
			System.err.println("The input file does not exist or is not readable.");
			System.exit(-2);
		}

		return ints;
	}

}