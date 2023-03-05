package quadtree.logic;

import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

public class QuadTreeUtil {
	private QuadTreeUtil() {
	}

	public static QuadTreeNode bufferedImageToQuadTree(BufferedImage image) {
		if (image == null) {
			throw new IllegalArgumentException("Image cannot be null!");
		}
		int maxDimensions = Math.max(image.getHeight(), image.getWidth());
		int dimensions = 1;
		while (dimensions < maxDimensions) {
			dimensions *= 2;
		}
		int[][] imageData = new int[dimensions][dimensions];
		for (int i = image.getMinX(); i < image.getWidth(); i++) {
			for (int j = image.getMinY(); j < image.getHeight(); j++) {
				// Remove alpha channel if it exists
				imageData[j][i] = image.getRGB(i, j) % 0x1000000;
			}
		}
		return QuadTreeNodeImpl.buildFromIntArray(imageData);
	}

	public static Color intToColor(int color) {
		var red = (color >> 16) & 0xFF;
		var green = (color >> 8) & 0xFF;
		var blue = color & 0xFF;
		return Color.color(red / 255.0, green / 255.0, blue / 255.0);
	}

	public static int colorToInt(Color color) {
		return ((((int) (color.getRed() * 255)) & 0xFF) << 16) | ((((int) (color.getGreen() * 255)) & 0xFF) << 8)
				| (((int) (color.getBlue() * 255)) & 0xFF);
	}
}
