package ptui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.RITQTNode;

public class RITCompress {
	public static void main(String[] args) {
		if (args.length != 2) {
			// If the command line does not have two arguments, display a usage error and
			// exit.
			System.err.println("Usage: java RITCompress uncompressed-file.txt compressed-file.rit");
			System.exit(-1);
			return;
		}
		System.out.println("Compressing: " + args[0]);
		Compressor c = new RITCompress().new Compressor(new File(args[0]));
		RITQTNode node = c.compress();

		System.out.println("QTree: " + node.toStringPreorder());

	}

	private class Compressor {

		private int[][] screen;
		private int dim;

		protected Compressor(File f) {

			List<Integer> ints = new ArrayList<>();

			try {
				// Read in the file
				BufferedReader br = new BufferedReader(new FileReader(f));
				while (br.ready()) {
					ints.add(Integer.parseInt(br.readLine()));
				}
				br.close();

				dim = (int) Math.sqrt(ints.size());
				screen = new int[dim][dim];

				// Put everything into a 2-d array
				for (int i = 0; i < dim; i++) {
					for (int j = 0; j < dim; j++) {
						screen[i][j] = ints.get(i * dim + j);
					}
				}

			} catch (IOException ex) {
				// If the input file does not exist or is not readable, display an error message
				// and exit.
				System.err.println("The input file does not exist or is not readable!");
			}

		}

		protected RITQTNode compress() {
			RITQTNode temp = buildTree(0, 0, 1);

			// temp = reduce(temp);

			return temp;

		}

		private RITQTNode buildTree(int xoffset, int yoffset, int depth) {

			int squareSize = dim / depth;

			if (squareSize == 2) {

				RITQTNode nw = new RITQTNode(screen[yoffset][xoffset]);
				RITQTNode ne = new RITQTNode(screen[yoffset][xoffset + 1]);
				RITQTNode sw = new RITQTNode(screen[yoffset + 1][xoffset]);
				RITQTNode se = new RITQTNode(screen[yoffset + 1][xoffset + 1]);

				return new RITQTNode(-1, nw, ne, sw, se);

			} else {

				depth *= 2;

				RITQTNode nw = buildTree(xoffset, yoffset, depth);
				RITQTNode ne = buildTree(xoffset + squareSize, yoffset, depth);
				RITQTNode sw = buildTree(xoffset, yoffset + squareSize, depth);
				RITQTNode se = buildTree(xoffset + squareSize, yoffset + squareSize, depth);

				return new RITQTNode(-1, nw, ne, sw, se);
			}
		}

		private RITQTNode reduce(RITQTNode node) {
			// this shouldn't happen but in case it does
			if (node.isLeaf()) {
				return node;
			}

			int ul = node.getUpperLeft().getVal();
			int ur = node.getUpperRight().getVal();
			int ll = node.getLowerLeft().getVal();
			int lr = node.getLowerRight().getVal();

			if (ul == ur && ll == lr && ul == ll) {
				// if all of the children equal each other...
				if (ul != -1) {
					// and they aren't branches themselves, return the reduced node.
					return new RITQTNode(ul);
				} else {
					// else keep going.
					return new RITQTNode(-1, reduce(node.getUpperLeft()), reduce(node.getUpperRight()),
							reduce(node.getLowerLeft()), reduce(node.getLowerRight()));
				}

			} else {
				return node;
			}
		}
	}
}
