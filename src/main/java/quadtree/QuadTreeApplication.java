package quadtree;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import quadtree.view.QuadTreeUI;
import quadtree.view.SimplePaint;

public class QuadTreeApplication extends Application {
	// Utility
	private final Rectangle2D screen = Screen.getPrimary().getBounds();
	private Stage stage = new Stage();

	// Used to launch the application.
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		stage.setTitle("QuadTree Compression");
		stage.setScene(new QuadTreeUI(screen.getMaxX() - 70, screen.getMaxY() - 120, this));
		stage.setMaximized(true);
		stage.show();
	}

	public void showSimplePaintWindow() {
		Stage paintStage = new Stage();
		paintStage.setTitle("Simple Paint");
		paintStage.setScene(new SimplePaint(screen.getMaxX() - 120, screen.getMaxY() - 120, paintStage));
		paintStage.setMaximized(true);
		paintStage.show();
	}

	public Stage getStage() {
		return this.stage;
	}
}
