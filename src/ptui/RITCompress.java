package ptui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import exception.FileWriteException;
import model.RITQTNode;

public class RITCompress {
	public static void main(String[] args) {

		try {
			if (args.length != 2) {
				throw new IllegalArgumentException();
			}

			System.out.println("Compressing: " + args[0]);
			Compressor c = new RITCompress().new Compressor(new File(args[0]));
			RITQTNode node = c.compress();

			System.out.println("QTree: " + node.toStringPreorder());
			System.out.println("Output file: " + args[1]);

			int before = (int) Math.pow(c.getDim(), 2);
			int after = node.toStringPreorder().split(" ").length + 1; // yes this is a massive bodge but it works
			double perc = 100 - (((double) after / before) * 100);
			saveFile(node.toStringPreorder().split(" "), new File(args[1]), before);

			System.out.println("Raw image size: " + before);
			System.out.println("Compressed image size: " + after);
			System.out.println("Compression %: " + perc);

		} catch (Exception ex) {

			if (ex instanceof IllegalArgumentException) {
				System.err.println("Usage: java RITCompress uncompressed-file.txt compressed-file.rit");
				System.exit(-1);

			} else if (ex instanceof IOException) {
				// If the input file does not exist or is not readable, display an error message
				// and exit.
				System.err.println("The input file does not exist or is not readable!");
				System.exit(-2);

			} else if (ex instanceof FileWriteException) {
				System.err.println("An error occured while trying to write the file at " + args[1]);
				System.exit(-3);

			} else {
				System.err.println("An uncaught exception occured");
				System.exit(-100);
			}
		}
	}

	public static void saveFile(String[] str, File f, int res) throws FileWriteException {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));

			// first write the resolution
			bw.write(res + "\n");

			// then the rest of the values.
			for (String current : str) {
				bw.write(current + "\n");
			}
			bw.close();

		} catch (Exception ex) {
			// If the output file cannot be created, display an error message and exit.
			throw new FileWriteException();
		}
	}

	public class Compressor {

		private int[][] screen;
		private int dim;

		public Compressor(File f) throws IOException {

			List<Integer> ints = new ArrayList<>();

			// Read in the file
			BufferedReader br = new BufferedReader(new FileReader(f));
			while (br.ready()) {
				ints.add(Integer.parseInt(br.readLine()));
			}
			br.close();

			dim = (int) Math.sqrt(ints.size());
			screen = new int[getDim()][getDim()];

			// Put everything into a 2-d array
			for (int i = 0; i < getDim(); i++) {
				for (int j = 0; j < getDim(); j++) {
					screen[i][j] = ints.get(i * getDim() + j);
				}
			}

		}

		public RITQTNode compress() {
			RITQTNode temp = buildTree(0, 0, getDim());
			int newval = temp.getNumChildren();
			int oldval = newval + 1;
			while (oldval != newval) {
				oldval = newval;
				temp = reduce(temp);
				newval = temp.getNumChildren();
			}

			return temp;

		}

		private RITQTNode buildTree(int xoffset, int yoffset, int blockSize) {

			blockSize /= 2;

			RITQTNode nw, ne, sw, se;

			if (blockSize != 1) {

				nw = buildTree(xoffset, yoffset, blockSize);
				ne = buildTree(xoffset + blockSize, yoffset, blockSize);
				sw = buildTree(xoffset, yoffset + blockSize, blockSize);
				se = buildTree(xoffset + blockSize, yoffset + blockSize, blockSize);

			} else {

				nw = new RITQTNode(screen[yoffset][xoffset]);
				ne = new RITQTNode(screen[yoffset][xoffset + 1]);
				sw = new RITQTNode(screen[yoffset + 1][xoffset]);
				se = new RITQTNode(screen[yoffset + 1][xoffset + 1]);

			}

			return new RITQTNode(-1, nw, ne, sw, se);

		}

		private RITQTNode reduce(RITQTNode in) {
			if (in.isLeaf()) {
				return in;
			}

			RITQTNode nw, ne, sw, se;
			nw = in.getUpperLeft();
			ne = in.getUpperRight();
			sw = in.getLowerLeft();
			se = in.getLowerRight();

			// all the values are equal
			if (nw.getVal() == ne.getVal() && sw.getVal() == se.getVal() && nw.getVal() == se.getVal()) {
				if (nw.getVal() != -1) {
					return new RITQTNode(nw.getVal());
				}
			}

			// all the values are not equal but also not leaves
			return new RITQTNode(-1, reduce(nw), reduce(ne), reduce(sw), reduce(se));
		}

		public int getDim() {
			return dim;
		}
	}
}
