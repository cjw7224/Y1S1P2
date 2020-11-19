package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import exception.InvalidRangeException;
import exception.InvalidResolutionException;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RITViewer extends Application {

	@Override
	public void start(Stage stage) throws Exception {

		try {
			if (args.length != 1) {
				throw new IllegalArgumentException();
			}
			UncompressedImageRenderer r = new UncompressedImageRenderer(new File("images/uncompressed/" + args[0]));

			// System.out.println(r.toString());

			Canvas c = r.draw();

			Group g = new Group();
			g.getChildren().add(c);

			stage.setScene(new Scene(g));
			stage.show();
		} catch (Exception ex) {

			int exitcode = 0;

			if (ex instanceof IllegalArgumentException) {
				// If the command line argument is not present, display a usage error and exit.
				System.err.println("Usage: RITViewer File.txt");
				exitcode = -1;

			} else if (ex instanceof IOException) {
				// If the file does not exist or is not readable, display an error message and
				// exit.
				System.err.println("File was not found or is not readable.");
				exitcode = -2;

			} else if (ex instanceof InvalidResolutionException) {
				// If the image size is not square (a power of two), display an error message
				// and exit.
				System.err.println("Image size is not a valid power of two.");
				exitcode = -3;

			} else if (ex instanceof InvalidRangeException) {
				// If an integer value is encountered that is outside the range 0-255, display
				// an error message and exit.
				System.err.println("An invalid value outside of the range 0-255 was read from the input file.");
				exitcode = -4;

			} else if (ex instanceof NumberFormatException) {
				// If a non-integer value is encountered for a pixel value, display an error
				// message and exit.
				System.err.println("An invalid character was read from the input file.");
				exitcode = -5;

			} else {
				System.err.println("An uncaught error occured");
				ex.printStackTrace();
				exitcode = -100;
			}

			System.exit(exitcode);
		}

	}

	private static String[] args;

	public static void main(String[] args) {
		RITViewer.args = args;

		Application.launch(args);
	}

	private class UncompressedImageRenderer {

		private int dimension;
		private File f;
		private int[][] image;

		public UncompressedImageRenderer(File f) throws Exception {
			this.f = f;
			if (!f.exists() || f.isDirectory()) {
				throw new FileNotFoundException();
			}

			String name = f.getName();
			System.out.println(name);

			// parse all the values in the file

			dimension = Integer
					.parseInt(name.substring(name.replace(".txt", "").lastIndexOf("x") + 1, name.lastIndexOf(".")));

			// https://www.geeksforgeeks.org/java-program-to-find-whether-a-no-is-power-of-two/
			if ((Math.ceil((Math.log(dimension) / Math.log(2)))) != (int) (Math
					.floor(((Math.log(dimension) / Math.log(2)))))) {
				throw new InvalidResolutionException();
			}

			image = new int[dimension][dimension];

			try {
				BufferedReader br = new BufferedReader(new FileReader(f));

				for (int i = 0; i < dimension; i++) {
					for (int j = 0; j < dimension; j++) {
						image[i][j] = Integer.parseInt(br.readLine());
						if (image[i][j] > 255 || image[i][j] < 0) {
							br.close();
							throw new InvalidRangeException();
						}
					}
				}
				br.close();

			} catch (Exception ex) {
				ex.printStackTrace();
				// this shouldn't happen in theory, because we already checked that the file
				// exists.
			}
		}

		public Canvas draw() {
			Canvas out = new Canvas();
			out.setWidth(dimension);
			out.setHeight(dimension);

			GraphicsContext gc = out.getGraphicsContext2D();
			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < dimension; j++) {
					int at = image[i][j];
					double divby = 255;
					Color c = new Color(at / divby, at / divby, at / divby, 1);

					gc.setFill(c);
					gc.setStroke(c);
					gc.fillRect(j, i, j + 1, i + 1);
				}
			}

			return out;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("File: " + f.getName() + "\n");
			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < dimension; j++) {
					sb.append(image[i][j] + " ");
				}
				sb.append("\n");
			}
			return sb.toString().trim();
		}
	}
}
