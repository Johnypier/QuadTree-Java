import java.time.Duration;
import java.time.Instant;

import javafx.application.Application;

public class Starter {

	public static void main(String[] args) {
		Instant start = Instant.now();
		Application.launch(QuadTreeGUI.class, args);
		Instant end = Instant.now();
		System.out.println("Working time: " + Duration.between(start, end).getSeconds());
	}
}
