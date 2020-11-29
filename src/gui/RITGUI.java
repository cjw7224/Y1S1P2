package gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import exception.FileWriteException;
import exception.InsufficientArgumentsException;
import exception.InvalidRangeException;
import exception.InvalidResolutionException;
import gui.RITViewer.UncompressedImageRenderer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import model.RITQTNode;
import ptui.RITCompress;
import ptui.RITCompress.Compressor;
import ptui.RITUncompress;
import ptui.RITUncompress.Uncompresser;

public class RITGUI extends Application {

	private static final PrintStream err = System.err;

	private static Button inButton = new Button("Input File");
	private static Button outButton = new Button("Output File");
	private static FileChooser inChooser = new FileChooser();
	private static FileChooser outChooser = new FileChooser();
	private static File inFile;
	private static File outFile;
	private static RadioButton dec = new RadioButton("Decompress");
	private static RadioButton comp = new RadioButton("Compress");
	private static RadioButton view = new RadioButton("View");
	private static TextArea console = new TextArea();
	private static Canvas c = new Canvas();
	private static Group canvGroup = new Group();

	@Override
	public void start(Stage stage) throws Exception {

		ExtensionFilter[] filters = { new ExtensionFilter("Text Files", "*.txt"),
				new ExtensionFilter("RIT Quadtree Files", "*.rit"), };

		inChooser.getExtensionFilters().addAll(filters);
		inChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		outChooser.getExtensionFilters().addAll(filters);
		outChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

		// FILE SELECTOR BUTTONS

		inButton.setPrefWidth(400);
		inButton.setOnAction(act -> {
			inFile = inChooser.showOpenDialog(stage);
			if (inFile == null) {
				inButton.setText("Input file: No File Selected");
			} else {
				inButton.setText("Input file: " + inFile.getAbsolutePath());
			}
		});

		outButton.setPrefWidth(400);
		outButton.setOnAction(act -> {
			outFile = outChooser.showOpenDialog(stage);
			if (outFile == null) {
				outButton.setText("Output file: No File Selected");
			} else {
				outButton.setText("Output file: " + outFile.getAbsolutePath());
			}
		});

		// RADIO BUTTONS

		VBox v = new VBox();
		v.setSpacing(10);
		v.setPadding(new Insets(10, 20, 10, 20));
		v.getChildren().addAll(inButton, outButton);
		v.setAlignment(Pos.TOP_CENTER);

		ToggleGroup g = new ToggleGroup();
		dec.setToggleGroup(g);
		dec.setSelected(true);
		comp.setToggleGroup(g);
		view.setToggleGroup(g);

		VBox b = new VBox();

		b.setAlignment(Pos.CENTER_LEFT);
		b.getChildren().addAll(dec, comp, view);
		v.getChildren().add(b);

		console.setMaxWidth(420);
		console.setMinWidth(420);
		console.setMaxHeight(100);
		console.setMinHeight(100);
		console.setEditable(false);
		v.getChildren().add(console);

		// GO BUTTON
		Button goButton = new Button("GO");
		goButton.setStyle("-fx-background-color: #99ff99");
		goButton.setPrefWidth(300);
		goButton.setOnAction(act -> {
			goButton(inFile, outFile);
		});

		// RESET BUTTON
		Button resetButton = new Button("RESET");
		resetButton.setStyle("-fx-background-color: #ff9999");
		resetButton.setPrefWidth(300);
		resetButton.setOnAction(act -> {
			reset();
		});

		v.getChildren().add(goButton);
		v.getChildren().add(resetButton);
		v.getChildren().add(canvGroup);
		reset();

		stage.setHeight(900);
		stage.setWidth(550);
		stage.setResizable(false);
		stage.setScene(new Scene(v));
		stage.setTitle("RIT Quadtree Image Viewer");
		stage.show();

	}

	private static void reset() {
		c = new Canvas();
		c.setWidth(512);
		c.setHeight(512);
		canvGroup.getChildren().clear();
		canvGroup.getChildren().add(c);
		canvGroup.autosize();
		inButton.setText("Input File");
		outButton.setText("Output File");
		console.clear();
		inFile = null;
		outFile = null;
	}

	private static void goButton(File in1, File in2) {

//		Thread t = new Thread() {
//			public void run() {

		// System.setErr(null);

		if (dec.isSelected()) {
			decompress(in1, in2);
		} else if (comp.isSelected()) {
			compress(in1, in2);
		} else if (view.isSelected()) {
			view(in1);
		}
		System.setErr(err);

	}

//		};
//		t.start();
//	}

	private static void decompress(File in1, File in2) {
		try {
			if (in1 == null || in2 == null || in1.getName().toLowerCase().endsWith(".txt")
					|| in2.getName().toLowerCase().endsWith(".rit")) {
				throw new InsufficientArgumentsException();
			}

			// TODO Decompress codeFile in = new File(args[0]);
			console.appendText("Uncompressing: " + in1.getName() + ". This may take a while, please wait...\n");

			// This is here because otherwise the uncompresser will hang without warning.
			Thread.sleep(1000);

			Uncompresser u = new RITUncompress().new Uncompresser(in1);

			List<Integer> ints = u.getInts();
			console.appendText("QTree: ");
			for (int i = 1; i < ints.size(); i++) {
				console.appendText(ints.get(i) + " ");
			}
			console.appendText("\n");

			console.appendText("Output file: " + in2.getPath() + "\n");

			RITQTNode root = u.getTree(1);
			u.parseNode(root);
			u.writeFile(in2);

		} catch (Exception ex) {

			int exitcode = 0;

			if (ex instanceof InsufficientArgumentsException) {
				// If the command line does not have two arguments, display a usage error and
				// exit.
				console.appendText("Input and output files are either not defined or not .rit/.txt files respectively.\n");
				exitcode = -1;

			} else if (ex instanceof IOException) {
				// If the input file does not exist or is not readable, display an error message
				// and exit.
				console.appendText("The input file does not exist or is not readable.\n");
				exitcode = -2;

			} else if (ex instanceof FileWriteException) {
				// If the output file cannot be created, display an error message and exit.
				console.appendText("The file " + in2.getName() + " could not be successfully written to.\n");
				exitcode = -3;

			} else if (ex instanceof NumberFormatException) {

				console.appendText("The input file contains invalid characters.\n");
				exitcode = -4;

			} else {
				console.appendText("An uncaught exception occured...\n");
				exitcode = -100;
			}

			console.appendText("Exit code: " + exitcode + "\n\n");
		}
	}

	private static void compress(File in1, File in2) {
		try {
			if (in1 == null || in2 == null) {
				throw new IllegalArgumentException();
			} else if (in1.getName().toLowerCase().endsWith(".rit") || in2.getName().toLowerCase().endsWith(".txt")) {
				throw new IllegalArgumentException();
			}

			console.appendText("Compressing: " + in1.getName() + "\n");
			Compressor c = new RITCompress().new Compressor(in1);
			RITQTNode node = c.compress();

			console.appendText("QTree: " + node.toStringPreorder() + "\n");
			console.appendText("Output file: " + in2.getPath() + "\n");

			int before = (int) Math.pow(c.getDim(), 2);
			int after = node.toStringPreorder().split(" ").length + 1; // yes this is a massive bodge but it works
			double perc = 100 - (((double) after / before) * 100);
			RITCompress.saveFile(node.toStringPreorder().split(" "), in2, before);

			console.appendText("Raw image size: " + before + "\n");
			console.appendText("Compressed image size: " + after + "\n");
			console.appendText("Compression %: " + perc + "\n\n");

		} catch (Exception ex) {
			int exitcode = 0;
			if (ex instanceof IllegalArgumentException) {
				console.appendText("Input File: *.txt, Output file: *.rit\n");
				exitcode = -1;

			} else if (ex instanceof IOException) {
				// If the input file does not exist or is not readable, display an error message
				// and exit.
				console.appendText("The input file does not exist or is not readable.\n");
				exitcode = -2;

			} else if (ex instanceof FileWriteException) {
				console.appendText("An error occured while trying to write the file at " + in2.getPath() + "\n");
				exitcode = -3;

			} else {
				console.appendText("An uncaught exception occured\n");
				exitcode = -100;
			}
			console.appendText("Exit code: " + exitcode + "\n\n");
		}
	}

	private static void view(File in1) {

		try {

			if (in1 == null) {
				throw new IllegalArgumentException();
			} else if (in1.getName().toLowerCase().endsWith(".rit")) {
				throw new IllegalArgumentException();
			}
			console.appendText("Opening " + in1.getName() + "...\n");
			UncompressedImageRenderer r = new RITViewer().new UncompressedImageRenderer(in1);
			c = r.draw();
			canvGroup.getChildren().clear();
			canvGroup.getChildren().add(c);
			console.appendText("Image " + in1.getName() + " loaded!\n");
		} catch (Exception ex) {
			int exitcode = 0;

			if (ex instanceof IllegalArgumentException) {
				// If the command line argument is not present, display a usage error and exit.
				console.appendText("Input file not selected or not a .txt file.\n");
				exitcode = -1;

			} else if (ex instanceof IOException) {
				// If the file does not exist or is not readable, display an error message and
				// exit.
				console.appendText("File was not found or is not readable.\n");
				exitcode = -2;

			} else if (ex instanceof InvalidResolutionException) {
				// If the image size is not square (a power of two), display an error message
				// and exit.
				console.appendText("Image size is not a valid power of two.\n");
				exitcode = -3;

			} else if (ex instanceof InvalidRangeException) {
				// If an integer value is encountered that is outside the range 0-255, display
				// an error message and exit.
				console.appendText("An invalid value outside of the range 0-255 was read from the input file.\n");
				exitcode = -4;

			} else if (ex instanceof NumberFormatException) {
				// If a non-integer value is encountered for a pixel value, display an error
				// message and exit.
				console.appendText("An invalid character was read from the input file.\n");
				exitcode = -5;

			} else {
				console.appendText("An uncaught error occured\n.");
				ex.printStackTrace();
				exitcode = -100;
			}
			console.appendText("Exit code: " + exitcode + "\n\n");
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
