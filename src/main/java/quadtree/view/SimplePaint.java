package quadtree.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SimplePaint extends Scene {
    // Configuration Constants
    private static final double CANVAS_SIZE_LIMIT = Screen.getPrimary().getBounds().getMaxY() - 70;
    private static final String BOX_STYLE = "-fx-background-color: rgb(40, 40, 43);";
    private static final double DEF_SIZE_1 = 50;
    private static final double DEF_SIZE_2 = 100;
    private static final double DEF_SPACING = 15;
    private static final String GENERAL_STYLE = "-fx-background-color: rgb(54, 69, 79);"
            + "-fx-border-color: rgb(115, 147, 179);"
            + "-fx-text-fill: #D3D3D3;"
            + "-fx-font-size: 18;"
            + "-fx-font-alignment: center";
    // Canvas and its graphics
    private final Canvas canvas = new Canvas(CANVAS_SIZE_LIMIT - 200.0, CANVAS_SIZE_LIMIT - 200.0);
    private final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
    // Labels
    private final Label resizeCanvasLabel = new Label();
    private final Label colorsLabel = new Label("Select Color");
    // Fields
    private final TextField resizeCanvasField = new TextField();
    // Buttons
    private final Button reset = paintButtonConfiguration("Reset");
    private final Button save = paintButtonConfiguration("Save");
    private final Button resize = paintButtonConfiguration("Change");
    private final Button fullScreen = paintButtonConfiguration("Full-Screen");
    private final Button red = colorButtonConfiguration("rgb(255,0,0)");
    private final Button green = colorButtonConfiguration("rgb(0,128,0)");
    private final Button blue = colorButtonConfiguration("rgb(0,0,255)");
    private final Button yellow = colorButtonConfiguration("rgb(255, 255, 0)");
    private final Button black = colorButtonConfiguration("rgb(0, 0, 0)");
    // Containers
    private final HBox topBox = new HBox(resizeCanvasLabel, resizeCanvasField, resize, reset,
            save, fullScreen, colorsLabel, red, green, blue, yellow, black);
    private final VBox bodyBox = new VBox(topBox, canvas);
    // Dynamic vars
    private Color currentColor = Color.BLACK;

    public SimplePaint(double width, double height, Stage stage) {
        super(new VBox(), width, height);

        resizeCanvasLabel.setText("Insert new size of the canvas: between 0 and " + CANVAS_SIZE_LIMIT);
        resizeCanvasLabel.setMinHeight(DEF_SIZE_1);
        resizeCanvasLabel.setStyle(GENERAL_STYLE);
        colorsLabel.setMinSize(DEF_SIZE_2, DEF_SIZE_1);
        colorsLabel.setStyle(GENERAL_STYLE);

        resizeCanvasField.setMaxSize(DEF_SIZE_2, DEF_SIZE_1);
        resizeCanvasField.setStyle(GENERAL_STYLE);

        red.setOnAction(getColor(red));
        green.setOnAction(getColor(green));
        blue.setOnAction(getColor(blue));
        yellow.setOnAction(getColor(yellow));
        black.setOnAction(getColor(black));

        fullScreen.setOnAction(action -> stage.setFullScreen(true));
        reset.setOnAction(action -> resetCanvas());
        resize.setOnAction(resize());
        save.setOnAction(saveAsImage(stage));

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            graphicsContext.setFill(currentColor);
            graphicsContext.fillRect(event.getX() - 2, event.getY() - 2, 8, 8);
        });

        topBox.setSpacing(DEF_SPACING);
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle(BOX_STYLE);
        bodyBox.setSpacing(DEF_SPACING);
        bodyBox.setAlignment(Pos.CENTER);
        bodyBox.setStyle(BOX_STYLE);
        resetCanvas();
        setRoot(bodyBox);
    }

    /**
     * Opens the dialog to select the destination where the image will be stored.
     * 
     * @param primaryStage Required to display folder selection window.
     * @return Event which stores user's image at selected location.
     */
    private EventHandler<ActionEvent> saveAsImage(Stage primaryStage) {
        return event -> {
            System.out.println("uwu");
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
            customInformationAlert("Success!", "Saved!");
        };
    }

    /**
     * Simply changes the size of the canvas according to user input.
     */
    private EventHandler<ActionEvent> resize() {
        return event -> {
            System.out.println("uwu");
            if (resizeCanvasField.getText() != null && !resizeCanvasField.getText().isEmpty()) {
                double newSize = Double.parseDouble(resizeCanvasField.getText());
                if (newSize <= CANVAS_SIZE_LIMIT) {
                    canvas.setWidth(newSize);
                    canvas.setHeight(newSize);
                    resetCanvas();
                } else {
                    customErrorAlert("Error!", "New canvas size is out of bound!");
                }
            }
        };
    }

    /**
     * Simply changes current paint color to the one taken from the button (e.g. red
     * button)
     * 
     * @param button Button to get the color from.
     */
    private EventHandler<ActionEvent> getColor(Button button) {
        return event -> currentColor = (Color) button.getBackground().getFills().get(0).getFill();
    }

    /**
     * Fills canvas with white color.
     */
    private void resetCanvas() {
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Simplifies the process of button's configuration.
     * 
     * @param text Buttons's text.
     * @return Configured button.
     */
    private Button paintButtonConfiguration(String text) {
        Button temp = new Button();
        temp.setText(text);
        temp.setMinSize(DEF_SIZE_2, DEF_SIZE_1);
        temp.setStyle(GENERAL_STYLE);
        return temp;
    }

    /**
     * Creates color selection buttons.
     * 
     * @param colorRGB Button's background color.
     * @return Configured button.
     */
    private Button colorButtonConfiguration(String colorRGB) {
        Button temp = new Button();
        temp.setMinSize(DEF_SIZE_1, DEF_SIZE_1);
        temp.setMaxSize(DEF_SIZE_1, DEF_SIZE_1);
        temp.setStyle(String.format("-fx-background-color: %s;", colorRGB));
        return temp;
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
