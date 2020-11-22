package ptui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

		Uncompresser u = new RITUncompress().new Uncompresser(new File(args[0]));
		RITQTNode root = u.genNodes(1);
		System.out.println(root.toString());
		u.parseNode(root);

		System.out.println();
		System.out.println(u.getScreen());
	}

	private class Uncompresser {

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
		private Uncompresser(File in) {

			try {
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

			} catch (NumberFormatException ex) {

				System.err.println("The input file contains invalid characters.");
				System.exit(-4);

			} catch (IOException ex) {
				// If the input file does not exist or is not readable, display an error message
				// and exit.
				System.err.println("The input file does not exist or is not readable.");
				System.exit(-2);
			}
		}

		/**
		 * Recursively generates the QT Node tree from the list of ints.
		 * 
		 * @param startpoint - the index of ints to start at.
		 * @return an RITQTNode whose children emulate that in the .rit file.
		 */
		private RITQTNode genNodes(int startpoint) {

			int temp = ints.get(startpoint);
			if (temp == -1) {
				return new RITQTNode(temp, genNodes(startpoint + 1), genNodes(startpoint + 2), genNodes(startpoint + 3),
						genNodes(startpoint + 4));
			} else {
				return new RITQTNode(temp);
			}
		}

		/**
		 * Parses the node into the 2-d array of grayscale values.
		 * 
		 * @param node the node to parse.
		 */
		private void parseNode(RITQTNode node) {
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

			int offset = dim / depth;

			System.out.println(node.getVal() + " @ (" + xoff + "," + yoff + ") " + offset + "x" + offset);

			if (node.isLeaf()) {
				for (int i = xoff; i < offset; i++) {
					for (int j = yoff; j < offset; j++) {
						screen[j][i] = node.getVal();

					}
				}
				System.out.println(getScreen());

			} else {
				System.out.println("NOOP\n");
				depth *= 2;

				parseNode(node.getUpperLeft(), xoff, yoff, depth);
				parseNode(node.getUpperRight(), offset / 2 + 1, yoff, depth);
				parseNode(node.getLowerLeft(), xoff, offset / 2 + 1, depth);
				parseNode(node.getLowerRight(), offset / 2 + 1, offset / 2 + 1, depth);
			}
		}

		private String getScreen() {
			String out = "";
			for (int[] current : screen) {
				out += Arrays.toString(current) + "\n";
			}
			return out;
		}
	}
}