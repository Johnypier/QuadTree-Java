package quadtree.logic;

public class QuadTreeNodeImpl implements QuadTreeNode {
	private static final long serialVersionUID = 1L;
	private QuadTreeNodeImpl tl;
	private QuadTreeNodeImpl tr;
	private QuadTreeNodeImpl bl;
	private QuadTreeNodeImpl br;
	private int dimension;
	private int colored;

	public static QuadTreeNode buildFromIntArray(int[][] image) {
		if (imageCheck(image)) {
			throw new IllegalArgumentException("Invalid Image!");
		}
		int dimension = image.length;
		return buildingBranches(dimension, 0, 0, image);
	}

	public QuadTreeNodeImpl(int dimension, QuadTreeNodeImpl tl, QuadTreeNodeImpl tr, QuadTreeNodeImpl bl,
			QuadTreeNodeImpl br, int color) {
		this.dimension = dimension;
		this.colored = color;
		this.tl = tl;
		this.tr = tr;
		this.bl = bl;
		this.br = br;
	}

	public QuadTreeNodeImpl(int dimension, int color) {
		this.dimension = dimension;
		this.colored = color;
		this.tl = null;
		this.tr = null;
		this.bl = null;
		this.br = null;
	}

	private static QuadTreeNodeImpl buildingBranches(int dimension, int row, int col, int[][] image) {
		int halfDimension = dimension / 2;
		QuadTreeNodeImpl tempRoot;
		if (halfDimension >= 1) {
			QuadTreeNodeImpl tl = buildingBranches(halfDimension, row, col, image);
			QuadTreeNodeImpl tr = buildingBranches(halfDimension, row, col + halfDimension, image);
			QuadTreeNodeImpl bl = buildingBranches(halfDimension, row + halfDimension, col, image);
			QuadTreeNodeImpl br = buildingBranches(halfDimension, row + halfDimension, col + halfDimension, image);
			if (areLeaves(tl, tr, bl, br) && image[row][col] == image[row][col + halfDimension]
					&& image[row + halfDimension][col] == image[row][col + halfDimension]
					&& image[row + halfDimension][col] == image[row + halfDimension][col + halfDimension]) {
				tempRoot = new QuadTreeNodeImpl(dimension, image[row][col]);
			} else {
				tempRoot = new QuadTreeNodeImpl(dimension, tl, tr, bl, br, image[row][col]);
			}
		} else {
			tempRoot = new QuadTreeNodeImpl(dimension, image[row][col]);
		}
		return tempRoot;
	}

	@Override
	public QuadTreeNode getTopLeft() {
		return this.tl;
	}

	@Override
	public QuadTreeNode getTopRight() {
		return this.tr;
	}

	@Override
	public QuadTreeNode getBottomLeft() {
		return this.bl;
	}

	@Override
	public QuadTreeNode getBottomRight() {
		return this.br;
	}

	@Override
	public int getRelativeColor(int x, int y) {
		if (Math.min(x, y) < 0 || Math.max(x, y) > this.getDimension()) {
			throw new IllegalArgumentException();
		}
		return getColorMain(this, x, y);
	}

	@Override
	public void setRelativeColor(int x, int y, int color) {
		if (this.getDimension() == 1) {
			this.setColor(color);
		} else {
			int halfDimension = this.getDimension() / 2;
			if (this.isLeaf()) {
				int tempCol = this.getColor();
				this.tl = new QuadTreeNodeImpl(halfDimension, tempCol);
				this.tr = new QuadTreeNodeImpl(halfDimension, tempCol);
				this.bl = new QuadTreeNodeImpl(halfDimension, tempCol);
				this.br = new QuadTreeNodeImpl(halfDimension, tempCol);
			}
			this.colored = -1;
			if (x >= halfDimension) {
				if (y >= halfDimension) {
					this.br.setRelativeColor(x - halfDimension, y - halfDimension, color);
				} else {
					this.tr.setRelativeColor(x - halfDimension, y, color);
				}
			} else if (y >= halfDimension) {
				this.bl.setRelativeColor(x, y - halfDimension, color);
			} else {
				this.tl.setRelativeColor(x, y, color);
			}
			if (areLeaves(this.tl, this.tr, this.bl, this.br) && this.tl.colored == this.br.colored
					&& this.tr.colored == this.br.colored && this.br.colored == this.bl.colored) {
				this.colored = this.tl.colored;
				this.tl = null;
				this.tr = null;
				this.bl = null;
				this.br = null;
			}
		}
	}

	private int getColorMain(QuadTreeNodeImpl node, int x, int y) {
		if (node.isLeaf()) {
			return node.getColor();
		}
		int halfDimenson = node.getDimension() / 2;
		if (x >= halfDimenson) {
			if (y >= halfDimenson) {
				return getColorMain(node.br, x - halfDimenson, y - halfDimenson);
			}
			return getColorMain(node.tr, x - halfDimenson, y);
		}
		if (y >= halfDimenson) {
			return getColorMain(node.bl, x, y - halfDimenson);
		}
		return getColorMain(node.tl, x, y);
	}

	@Override
	public int getDimension() {
		return this.dimension;
	}

	private int getColor() {
		return this.colored;
	}

	private void setColor(int color) {
		this.colored = color;
	}

	@Override
	public int getSize() {
		boolean nullLeaves = this.tl == null && this.tr == null && this.bl == null && this.br == null;
		if (nullLeaves || this.isLeaf()) {
			return 1;
		}
		return 1 + tl.getSize() + tr.getSize() + bl.getSize() + br.getSize();
	}

	@Override
	public boolean isLeaf() {
		return tl == null && tr == null && bl == null && br == null;
	}

	private static boolean areLeaves(QuadTreeNodeImpl tl, QuadTreeNodeImpl tr, QuadTreeNodeImpl bl,
			QuadTreeNodeImpl br) {
		return tl.isLeaf() && tr.isLeaf() && bl.isLeaf() && br.isLeaf();
	}

	private static boolean imageCheck(int[][] image) {
		return image == null || image.length == 0 || image.length != image[0].length
				|| Math.ceil(Math.log(image.length) / Math.log(2)) != Math.ceil(Math.log(image.length) / Math.log(2));
	}

	@Override
	public int[][] toArray() {
		int dimensions = this.getDimension();
		int[][] image = new int[dimensions][dimensions];
		decompress(this, 0, 0, image);
		return image;
	}

	private static void decompress(QuadTreeNodeImpl node, int row, int col, int[][] image) {
		int halfDimension = node.getDimension() / 2;
		if (node.isLeaf()) {
			for (int i = row; i < row + node.getDimension(); i++) {
				for (int j = col; j < col + node.getDimension(); j++) {
					image[i][j] = node.getColor();
				}
			}
		} else {
			decompress((QuadTreeNodeImpl) node.getTopLeft(), row, col, image);
			decompress((QuadTreeNodeImpl) node.getTopRight(), row, col + halfDimension, image);
			decompress((QuadTreeNodeImpl) node.getBottomLeft(), row + halfDimension, col, image);
			decompress((QuadTreeNodeImpl) node.getBottomRight(), row + halfDimension, col + halfDimension, image);
		}
	}
}
