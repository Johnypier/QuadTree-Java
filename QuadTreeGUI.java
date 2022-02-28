import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class QuadTreeGUI extends Application {
	private static final String STYLE = "-fx-background-color: rgb(40, 40, 43);";
	private static final String SPECIAL = "-fx-border-color: rgb(115, 147, 179);";
	private static final String BBSTYLE = "-fx-background-color: rgb(54, 69, 79);" + SPECIAL + "-fx-text-fill: #D3D3D3;"
			+ "-fx-font-size: 20;" + "-fx-background-radius: 15px;" + "-fx-border-radius: 15px;";

	private static final String BUTTON = "-fx-background-color: rgb(54, 69, 79);" + SPECIAL + "-fx-text-fill: #D3D3D3;"
			+ "-fx-font-size: 15;" + "-fx-background-radius: 15px;" + "-fx-border-radius: 15px;";

	private File tempFile;
	private QuadTreeNode root;
	private QuadTreeNode readRoot;
	private boolean showBorders;
	private Color tempColor = Color.BLACK;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("QuadTree Compression");
		primaryStage.setFullScreen(true);

		Rectangle2D screen = Screen.getPrimary().getBounds();
		double height = screen.getMaxY();
		double width = screen.getMaxX() - 250;

		HBox utilityHub = constructUtilityHub(width, height, primaryStage);
		utilityHub.setSpacing(10);

		VBox mainBox = new VBox(utilityHub);
		mainBox.setStyle(STYLE);

		Scene myScene = new Scene(mainBox, width + 180, height - 120);
		primaryStage.setScene(myScene);
		primaryStage.show();
	}

	private HBox constructUtilityHub(double width, double height, Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();

		Button importImage = buttonConfiguration();
		importImage.setText("Import Image");
		Button save = buttonConfiguration();
		save.setText("Save as QuadTree");
		Button fullScreen = buttonConfiguration();
		fullScreen.setText("Full-Screen Mode");
		Button open = buttonConfiguration();
		open.setText("Import QuadTree");
		Button onBorders = buttonConfiguration();
		onBorders.setText("Show Borders");
		Button offBorders = buttonConfiguration();
		offBorders.setText("Remove Borders");
		Button paint = buttonConfiguration();
		paint.setText("Simple Paint");

		VBox buttonsBox = new VBox();
		buttonsBox.setStyle(STYLE);
		buttonsBox.getChildren().addAll(importImage, save, fullScreen, open, onBorders, offBorders, paint);
		buttonsBox.setSpacing(15);

		HBox imageBox = new HBox();
		imageBox.setMaxSize(width, height);
		imageBox.setMinSize(width, height);
		imageBox.setStyle(SPECIAL);

		importImage.setOnAction(importImage(primaryStage, imageBox, width, height));

		open.setOnAction(event -> {
			File file = fileChooser.showOpenDialog(primaryStage);

			try {
				if (file != null) {
					tempFile = null;
					readRoot = QuadTreeNodeFileHandler.readFromFile(file.getAbsolutePath());
					Canvas canvas = canvasConfiguration();
					GraphicsContext gc = canvas.getGraphicsContext2D();
					drawImage(gc, readRoot, 0, 0);
					imageBox.getChildren().clear();
					imageBox.getChildren().add(canvas);
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		});

		onBorders.setOnAction(borderHandler(true, imageBox));

		offBorders.setOnAction(borderHandler(false, imageBox));

		fullScreen.setOnAction(event -> primaryStage.setFullScreen(true));

		save.setOnAction(saveEvent(primaryStage));

		paint.setOnAction(simplePaintEvent());

		HBox temp = new HBox();
		temp.setStyle(STYLE);
		temp.getChildren().addAll(buttonsBox, imageBox);
		return temp;
	}

	private void drawImage(GraphicsContext gc, QuadTreeNode readroot, double x, double y) throws IOException {
		if (readroot.isLeaf()) {
			int color = readroot.getRelativeColor(0, 0);
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = (color >> 0) & 0xFF;
			int dimension = readroot.getDimension();
			if (showBorders) {
				gc.setStroke(Color.BLACK);
				gc.setLineWidth(3);
				gc.strokeRect(x + 1, y + 1, dimension + 2.0, dimension + 2.0);
				gc.setFill(Color.rgb(r, g, b, 1.0));
				gc.fillRect(x, y, dimension, dimension);
			} else {
				gc.setFill(Color.rgb(r, g, b, 1.0));
				gc.fillRect(x, y, dimension, dimension);
			}
		} else {
			int midPoint = readroot.getDimension() / 2;
			drawImage(gc, readroot.getTopLeft(), x, y);
			drawImage(gc, readroot.getTopRight(), x + midPoint, y);
			drawImage(gc, readroot.getBottomLeft(), x, y + midPoint);
			drawImage(gc, readroot.getBottomRight(), x + midPoint, y + midPoint);
		}
	}

	private EventHandler<ActionEvent> simplePaintEvent() {
		return event -> {
			Rectangle2D screen = Screen.getPrimary().getBounds();
			double height = screen.getMaxY();
			double width = screen.getMaxX();

			Image garold = new Image(QuadTreeGUI.class.getResourceAsStream("3x.png"));
			Stage paintStage = new Stage();
			paintStage.setTitle("Simple Paint");
			paintStage.getIcons().add(garold);

			VBox functionalityHub = setupFunctionalityHub(paintStage);
			functionalityHub.setStyle(STYLE);

			Scene paintScene = new Scene(functionalityHub, width - 120, height - 120);
			paintStage.setScene(paintScene);
			paintStage.setFullScreen(true);
			paintStage.show();
		};
	}

	private VBox setupFunctionalityHub(Stage stage) {
		VBox tempHub = new VBox();
		Rectangle2D screen = Screen.getPrimary().getBounds();
		double limit = screen.getMaxY() - 70;

		Label label = new Label("Insert desired size of the canvas " + "(between 0-" + limit + ")");
		label.setMinHeight(50);
		label.setStyle(BUTTON);
		TextField changeSize = new TextField();
		changeSize.setPromptText("Size value");
		changeSize.setMaxWidth(100);
		changeSize.setMaxHeight(50);
		changeSize.setStyle(BUTTON);

		Button reset = paintButtonConfiguration();
		reset.setText("Reset");
		Button colors = paintButtonConfiguration();
		colors.setText("Colors:");
		Button save = paintButtonConfiguration();
		save.setText("Save");
		Button size = paintButtonConfiguration();
		size.setText("Change");
		Button fullScreen = paintButtonConfiguration();
		fullScreen.setText("Full-Screen");

		fullScreen.setOnAction(event -> stage.setFullScreen(true));

		Button red = colorButton();
		red.setStyle("-fx-background-color: rgb(255,0,0);");
		red.setOnAction(getColor(red));
		Button green = colorButton();
		green.setStyle("-fx-background-color: rgb(0,128,0);");
		green.setOnAction(getColor(green));
		Button blue = colorButton();
		blue.setStyle("-fx-background-color: rgb(0,0,255);");
		blue.setOnAction(getColor(blue));
		Button yellow = colorButton();
		yellow.setStyle("-fx-background-color: rgb(255, 255, 0);");
		yellow.setOnAction(getColor(yellow));
		Button black = colorButton();
		black.setStyle("-fx-background-color: rgb(0, 0, 0);");
		black.setOnAction(getColor(black));

		HBox buttonsBox = new HBox(label, changeSize, size, reset, save, colors, red, green, blue, yellow, black,
				fullScreen);
		buttonsBox.setSpacing(10.0);

		Canvas paintCanvas = new Canvas(limit - 200.0, limit - 200.0);
		GraphicsContext gc = paintCanvas.getGraphicsContext2D();
		reset(paintCanvas, Color.WHITE);

		paintCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			gc.setFill(tempColor);
			gc.fillRect(event.getX() - 2, event.getY() - 2, 8, 8);
		});

		reset.setOnAction(event -> reset(paintCanvas, Color.WHITE));

		size.setOnAction(event -> {
			if (changeSize.getText() != null && !changeSize.getText().isEmpty()) {
				double square = Double.parseDouble(changeSize.getText());
				if (square >= 0 && square <= limit) {
					paintCanvas.setWidth(square);
					paintCanvas.setHeight(square);
					reset(paintCanvas, Color.WHITE);
				} else {
					limitSquare();
				}
			} else {
				limitSquare();
			}
		});

		save.setOnAction(saveAsImage(stage, paintCanvas));

		HBox canvasBox = new HBox(paintCanvas);
		canvasBox.alignmentProperty().set(Pos.CENTER);

		tempHub.getChildren().addAll(buttonsBox, canvasBox);
		tempHub.setSpacing(10);
		return tempHub;
	}

	private void reset(Canvas canvas, Color color) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(color);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	private Button paintButtonConfiguration() {
		Button temp = new Button();
		temp.setMinSize(100, 50);
		temp.setMaxSize(100, 50);
		temp.setStyle(BUTTON);
		return temp;
	}

	private Button colorButton() {
		Button temp = new Button();
		temp.setMinSize(50, 50);
		temp.setMaxSize(50, 50);
		return temp;
	}

	private EventHandler<ActionEvent> getColor(Button button) {
		return event -> tempColor = (Color) button.getBackground().getFills().get(0).getFill();
	}

	private EventHandler<ActionEvent> saveEvent(Stage primaryStage) {
		return event -> {
			if (tempFile == null) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Invalid Action");
				alert.setHeaderText(null);
				alert.setContentText("Import new image first!");
				alert.showAndWait();
			} else {
				try {
					BufferedImage bufImage = ImageIO.read(tempFile);
					root = QuadTreeUtil.bufferedImageToQuadTree(bufImage);

					FileChooser fileChooser = new FileChooser();
					FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)",
							"*.txt");
					fileChooser.getExtensionFilters().add(extFilter);
					File selectedDirectory = fileChooser.showSaveDialog(primaryStage);

					if (selectedDirectory != null) {
						String path = selectedDirectory.getAbsolutePath();
						QuadTreeNodeFileHandler.writeToFile(root, path);
						saved();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private EventHandler<ActionEvent> importImage(Stage primaryStage, HBox imageBox, double width, double height) {
		return event -> {
			FileChooser fileChooser = new FileChooser();
			File file = fileChooser.showOpenDialog(primaryStage);
			tempFile = file;

			if (file != null) {
				try {
					Image image = new Image(new FileInputStream(file.getAbsolutePath()));
					ImageView imageView = imageConfiguration(new ImageView(image), image, width, height);
					imageBox.getChildren().clear();
					imageBox.getChildren().add(imageView);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private EventHandler<ActionEvent> saveAsImage(Stage primaryStage, Canvas canvas) {
		return event -> {
			if (canvas != null) {
				FileChooser fileChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
				fileChooser.getExtensionFilters().add(extFilter);

				File selectedDirectory = fileChooser.showSaveDialog(primaryStage);

				Image image = canvas.snapshot(null, null);
				BufferedImage buffImg = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				PixelReader px = image.getPixelReader();
				for (int x = 0; x < image.getWidth(); x++) {
					for (int y = 0; y < image.getHeight(); y++) {
						buffImg.setRGB(x, y, px.getArgb(x, y));
					}
				}
				try {
					ImageIO.write(buffImg, "png", new File(selectedDirectory.getAbsolutePath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				saved();
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Invalid Action");
				alert.setHeaderText(null);
				alert.setContentText("Canvas is empty!");
				alert.showAndWait();
			}
		};
	}

	private void saved() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Success!");
		alert.setHeaderText(null);
		alert.setContentText("Saved!");
		alert.showAndWait();
	}

	private void limitSquare() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error!");
		alert.setHeaderText(null);
		alert.setContentText("Argument out of bound!");
		alert.showAndWait();
	}

	private EventHandler<ActionEvent> borderHandler(boolean state, HBox imageBox) {
		return event -> {
			if (readRoot != null) {
				showBorders = state;
				Canvas canvas = canvasConfiguration();
				GraphicsContext gc = canvas.getGraphicsContext2D();
				try {
					drawImage(gc, readRoot, 0, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
				imageBox.getChildren().clear();
				imageBox.getChildren().add(canvas);
			}
		};
	}

	private ImageView imageConfiguration(ImageView img, Image image, double width, double height) {
		ImageView config = img;
		if (image.getHeight() > height && image.getWidth() <= width) {
			config.setFitHeight(height);
			config.setFitWidth(image.getWidth());
		} else {
			if (image.getWidth() > width && image.getHeight() <= height) {
				config.setFitHeight(image.getHeight());
				config.setFitWidth(width);
			} else {
				if (image.getWidth() > width && image.getHeight() > height) {
					config.setFitHeight(height);
					config.setFitWidth(width);
				} else {
					config.setFitHeight(image.getHeight());
					config.setFitWidth(image.getWidth());
				}
			}
		}
		return config;
	}

	private Button buttonConfiguration() {
		Button temp = new Button();
		temp.setMinSize(240, 50);
		temp.setMaxSize(240, 50);
		temp.setStyle(BBSTYLE);
		return temp;
	}

	private Canvas canvasConfiguration() {
		Canvas canvas = new Canvas();
		int dimension = readRoot.getDimension();
		canvas.setHeight(dimension);
		canvas.setWidth(dimension);
		return canvas;
	}

}
