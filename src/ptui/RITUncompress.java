package ptui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import exception.FileWriteException;
import model.RITQTNode;

public class RITUncompress {

	public static void main(String[] args) {

		if (args.length != 2) {
			// If the command line does not have two arguments, display a usage error and
			// exit.
			System.err.println("Usage: java RITUncompress compressed.rit uncompressed.txt");
			System.exit(-1);
			;
		}
		try {
			File in = new File(args[0]);
			System.out.println("Uncompressing: " + in.getName());

			Uncompresser u = new RITUncompress().new Uncompresser(in);

			List<Integer> ints = u.getInts();
			System.out.print("QTree: ");
			for (int i = 1; i < ints.size(); i++) {
				System.out.print(ints.get(i) + " ");
			}
			System.out.println();

			System.out.println("Output file: " + new File(args[1]).getPath());

			RITQTNode root = u.getTree(1);
			u.parseNode(root);
			u.writeFile(new File(args[1]));

		} catch (Exception ex) {
			if (ex instanceof IOException) {
				// If the input file does not exist or is not readable, display an error message
				// and exit.
				System.err.println("The input file does not exist or is not readable.");
				System.exit(-2);

			} else if (ex instanceof FileWriteException) {

				System.err.println("The file " + args[1] + " could not be successfully written to.");
				System.exit(-3);

			} else if (ex instanceof NumberFormatException) {

				System.err.println("The input file contains invalid characters.");
				System.exit(-4);
			}
		}

	}

	public class Uncompresser {

		private List<Integer> ints;
		private int[][] screen;
		private int dim;

		/**
		 * Initializes the uncompresser. This object will, as the name implies,
		 * uncompress a .rit file into it's 2-d array of grayscale elements. Many of the
		 * elements of decompression are broken up, either for debugging, vanity, or
		 * execution time evaluation.
		 * 
		 * @param in - The file to parse.
		 */
		public Uncompresser(File in) throws Exception {

			ints = new ArrayList<>();
			BufferedReader br = new BufferedReader(new FileReader(in));

			while (br.ready()) {
				ints.add(Integer.parseInt(br.readLine()));
			}
			br.close();
			dim = (int) Math.sqrt(ints.get(0));

			screen = new int[dim][dim];
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < dim; j++) {
					screen[i][j] = -255;
				}
			}

		}

		public List<Integer> getInts() {
			return Collections.unmodifiableList(ints);
		}

		/**
		 * Recursively generates the QT Node tree from the list of ints.
		 * 
		 * @param startpoint - the index of ints to start at.
		 * @return an RITQTNode whose children emulate that in the .rit file.
		 */
		public RITQTNode getTree(int startpoint) {
			int temp = ints.remove(startpoint);
			if (temp == -1) {
				return new RITQTNode(temp, getTree(startpoint), getTree(startpoint), getTree(startpoint),
						getTree(startpoint));

			} else {
				return new RITQTNode(temp);
			}
		}

		/**
		 * Parses the node into the 2-d array of grayscale values.
		 * 
		 * @param node the node to parse.
		 */
		public void parseNode(RITQTNode node) {
			parseNode(node, 0, 0, 1);
		}

		/**
		 * Parse this node into a 2-d array of grayscale values.
		 * 
		 * @param node  - the node to parse
		 * @param xoff  - the x-offset of the values in the array
		 * @param yoff  - the y-offset of the values in the array
		 * @param depth - the amount to divide the width of the board by in order to get
		 *              the width of this node's block of pixels.
		 * @param board - an array to write these values to.
		 * @return
		 */
		private void parseNode(RITQTNode node, int xoff, int yoff, int depth) {

			int cubeSize = dim / depth;

			if (node.isLeaf()) {
				for (int i = xoff; i < xoff + cubeSize; i++) {
					for (int j = yoff; j < yoff + cubeSize; j++) {
						screen[j][i] = node.getVal();

					}
				}

			} else {
				depth *= 2;

				int offset = dim / depth;

				parseNode(node.getUpperLeft(), xoff, yoff, depth);
				parseNode(node.getUpperRight(), xoff + offset, yoff, depth);
				parseNode(node.getLowerLeft(), xoff, yoff + offset, depth);
				parseNode(node.getLowerRight(), xoff + offset, yoff + offset, depth);

			}
		}

		/*
		 * [0, 0, 0, 85]
		 * [0, 0, 170, 255]
		 * [170, 170, 255, 85]
		 * [170, 170, 170, 0]
		 */

		public void writeFile(File f) throws FileWriteException {

			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				for (int i = 0; i < dim; i++) {
					for (int j = 0; j < dim; j++) {
						bw.write(screen[i][j] + "\n");
					}
				}
				bw.close();
			} catch (IOException ex) {
				throw new FileWriteException();
			}
		}

		@SuppressWarnings("unused")
		private String getScreen() {
			String out = "";
			for (int[] current : screen) {
				out += Arrays.toString(current) + "\n";
			}
			return out;
		}
	}
}