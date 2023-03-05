package quadtree.logic;

import java.io.Serializable;

public interface QuadTreeNode extends Serializable {
	long serialVersionUID = 3L;
    QuadTreeNode getTopLeft();
    QuadTreeNode getTopRight();
    QuadTreeNode getBottomLeft();
    QuadTreeNode getBottomRight();

    int getRelativeColor(int x, int y);
    void setRelativeColor(int x, int y, int color);

    int getDimension();
    int getSize();
    boolean isLeaf();
    int[][] toArray();

    default double getCompressionRatio() {
        double nodes = this.getSize();
        double pixels = Math.pow(this.getDimension(), 2);
        return nodes / pixels;
    }
}
