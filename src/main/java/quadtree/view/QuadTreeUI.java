package quadtree.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import quadtree.QuadTreeApplication;
import quadtree.logic.QuadTreeNode;
import quadtree.logic.QuadTreeNodeFileHandler;
import quadtree.logic.QuadTreeUtil;

public class QuadTreeUI extends Scene {
    // Configuration Constants
    private static final String VBOX_STYLE = "-fx-background-color: rgb(40, 40, 43);";
    private static final String HBOX_STYLE = "-fx-border-color: rgb(115, 147, 179);";
    private static final String BUTTON_STYLE = "-fx-background-color: rgb(54, 69, 79);"
            + HBOX_STYLE
            + "-fx-text-fill: #D3D3D3;"
            + "-fx-font-size: 20;"
            + "-fx-background-radius: 15px;"
            + "-fx-border-radius: 15px;";
    private static final double BORDER_LINE_WIDTH = 3;
    private static final double BOX_SPACING_1 = 10;
    private static final double BOX_SPACING_2 = 15;
    private static final double BUTTON_WIDTH = 240;
    private static final double BUTTON_HEIGHT = 50;
    // Utility
    private final Rectangle2D screen = Screen.getPrimary().getBounds();
    private final QuadTreeApplication application;
    private final FileChooser fileChooser = new FileChooser();
    // Buttons
    private final Button importImage = buttonConfiguration("Import Image");
    private final Button save = buttonConfiguration("Save as QuadTree");
    private final Button fullScreen = buttonConfiguration("Full-Screen Mode");
    private final Button importFile = buttonConfiguration("Import QuadTree");
    private final Button showBorders = buttonConfiguration("Show Borders");
    private final Button removeBorders = buttonConfiguration("Remove Borders");
    private final Button simplePaint = buttonConfiguration("Simple Paint");
    // Containers
    private final HBox buttonsBox = new HBox(importImage, save, fullScreen,
            importFile, showBorders, removeBorders, simplePaint);
    private final HBox imageBox = new HBox();
    private final VBox mainBox = new VBox(buttonsBox, imageBox);
    // Dynamic vars
    private boolean isShowingBorders = false;
    private File currentImageFile;
    private QuadTreeNode currentImageTreeRoot;
    private QuadTreeNode fileTreeRoot;

    public QuadTreeUI(double width, double height, QuadTreeApplication application) {
        super(new HBox(), width, height);
        this.application = application;

        importImage.setOnAction(importImage());
        importFile.setOnAction(importFile());
        save.setOnAction(saveImageAsFile());
        showBorders.setOnAction(displayBordersEvent(true));
        removeBorders.setOnAction(displayBordersEvent(false));
        fullScreen.setOnAction(action -> application.getStage().setFullScreen(true));
        simplePaint.setOnAction(action -> application.showSimplePaintWindow());

        buttonsBox.setSpacing(BOX_SPACING_2);
        buttonsBox.setStyle(VBOX_STYLE);
        buttonsBox.setAlignment(Pos.CENTER);
        imageBox.setStyle(HBOX_STYLE);
        imageBox.setMinSize(width, height - 50);
        imageBox.setMaxSize(width, height - 50);
        mainBox.setSpacing(BOX_SPACING_1);
        mainBox.setStyle(VBOX_STYLE);
        mainBox.setAlignment(Pos.CENTER);
        setRoot(mainBox);
    }

    /**
     * Draws each pixel of the given quad tree image.
     * 
     * @param graphicsContext Graphics of the canvas.
     * @param treeRoot        QuadTree to draw.
     * @throws IOException
     */
    private void drawImage(GraphicsContext graphicsContext, QuadTreeNode treeRoot, double x, double y)
            throws IOException {
        if (treeRoot.isLeaf()) {
            int color = treeRoot.getRelativeColor(0, 0);
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = (color >> 0) & 0xFF;
            int dimension = treeRoot.getDimension();
            if (isShowingBorders) {
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setLineWidth(BORDER_LINE_WIDTH);
                graphicsContext.strokeRect(x + 1, y + 1, dimension + 2.0, dimension + 2.0);
                graphicsContext.setFill(Color.rgb(red, green, blue, 1.0));
                graphicsContext.fillRect(x, y, dimension, dimension);
            } else {
                graphicsContext.setFill(Color.rgb(red, green, blue, 1.0));
                graphicsContext.fillRect(x, y, dimension, dimension);
            }
        } else {
            int midPoint = treeRoot.getDimension() / 2;
            drawImage(graphicsContext, treeRoot.getTopLeft(), x, y);
            drawImage(graphicsContext, treeRoot.getTopRight(), x + midPoint, y);
            drawImage(graphicsContext, treeRoot.getBottomLeft(), x, y + midPoint);
            drawImage(graphicsContext, treeRoot.getBottomRight(), x + midPoint, y + midPoint);
        }
    }

    /**
     * Import any image and display it.
     */
    private EventHandler<ActionEvent> importImage() {
        return event -> {
            fileChooser.getExtensionFilters().clear();
            currentImageFile = fileChooser.showOpenDialog(application.getStage());

            if (currentImageFile != null) {
                try {
                    Image image = new Image(new FileInputStream(currentImageFile.getAbsolutePath()));
                    ImageView imageView = imageViewConfiguration(image);
                    imageBox.getChildren().clear();
                    imageBox.getChildren().add(imageView);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Imports quad tree image txt file and draws it.
     */
    private EventHandler<ActionEvent> importFile() {
        return event -> {
            File file = fileChooser.showOpenDialog(application.getStage());

            try {
                if (file != null) {
                    currentImageFile = null;
                    fileTreeRoot = QuadTreeNodeFileHandler.readFromFile(file.getAbsolutePath());
                    Canvas canvas = canvasConfiguration();
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    drawImage(gc, fileTreeRoot, 0, 0);
                    Image image = canvas.snapshot(null, null);
                    ImageView imageView = imageViewConfiguration(image);
                    imageBox.getChildren().clear();
                    imageBox.getChildren().add(imageView);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * Saves quad tree image as txt file in selected directory.
     */
    private EventHandler<ActionEvent> saveImageAsFile() {
        return event -> {
            if (currentImageFile == null) {
                customErrorAlert("Invalid Action!", "No image found!");
            } else {
                try {
                    BufferedImage bufImage = ImageIO.read(currentImageFile);
                    currentImageTreeRoot = QuadTreeUtil.bufferedImageToQuadTree(bufImage);

                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)",
                            "*.txt");
                    fileChooser.getExtensionFilters().add(extFilter);
                    File selectedDirectory = fileChooser.showSaveDialog(application.getStage());

                    if (selectedDirectory != null) {
                        String path = selectedDirectory.getAbsolutePath();
                        QuadTreeNodeFileHandler.writeToFile(currentImageTreeRoot, path);
                        customInformationAlert("Success!", "Image saved!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Shows the border around each quadtree element.
     * 
     * @param state Indicates if the border should be displayed.
     */
    private EventHandler<ActionEvent> displayBordersEvent(boolean state) {
        return event -> {
            if (fileTreeRoot != null) {
                isShowingBorders = state;
                Canvas canvas = canvasConfiguration();
                GraphicsContext gc = canvas.getGraphicsContext2D();
                try {
                    drawImage(gc, fileTreeRoot, 0, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Image image = canvas.snapshot(null, null);
                ImageView imageView = imageViewConfiguration(image);
                imageBox.getChildren().clear();
                imageBox.getChildren().add(imageView);
            }
        };
    }

    /**
     * Resizes the image according to available space on the screen.
     * 
     */
    private ImageView imageViewConfiguration(Image image) {
        double width = screen.getMaxX() - 70;
        double height = screen.getMaxY() - 175;
        ImageView temp = new ImageView(image);
        temp.setPreserveRatio(true);
        if (image.getHeight() > height && image.getWidth() <= width) {
            temp.setFitHeight(height);
        }
        if (image.getWidth() > width && image.getHeight() <= height) {
            temp.setFitWidth(width);
        }
        if (image.getWidth() > width && image.getHeight() > height) {
            temp.setFitWidth(width);
            temp.setFitHeight(height);
        }
        return temp;
    }

    /**
     * Simplifies button's creation process.
     * 
     * @param text Button's text.
     */
    private Button buttonConfiguration(String text) {
        Button temp = new Button();
        temp.setText(text);
        temp.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        temp.setMaxSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        temp.setStyle(BUTTON_STYLE);
        return temp;
    }

    /**
     * Creates canvas with the size of the quad tree image.
     * 
     * @return Canvas with correct height and width.
     */
    private Canvas canvasConfiguration() {
        Canvas canvas = new Canvas();
        int dimension = fileTreeRoot.getDimension();
        canvas.setHeight(dimension);
        canvas.setWidth(dimension);
        return canvas;
    }

    /**
     * Show custom error alert without header.
     * 
     * @param title   Alert title.
     * @param content Alert content.
     */
    private void customErrorAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show custom information alert without header.
     * 
     * @param title   Alert title.
     * @param content Alert content.
     */
    private void customInformationAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(content);
        alert.showAndWait();
    }
}
