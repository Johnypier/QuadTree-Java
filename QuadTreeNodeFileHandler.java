import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class QuadTreeNodeFileHandler implements Serializable {
	private static final long serialVersionUID = 4L;
	private static transient QuadTreeNode quadTree;

	public static void writeToFile(QuadTreeNode quadTreeNode, String path) throws IOException {
		try (FileOutputStream fileOutputStream = new FileOutputStream(path);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
			objectOutputStream.writeObject(quadTreeNode);
			objectOutputStream.flush();
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeObject(quadTree.toArray());
	}

	@SuppressWarnings("static-access")
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		int[][] array = (int[][]) ois.readObject();
		QuadTreeNode a = QuadTreeNodeImpl.buildFromIntArray(array);
		this.setQuadTreeNode(a);
	}

	private static void setQuadTreeNode(QuadTreeNode value) {
		quadTree = value;
	}

	public static QuadTreeNode readFromFile(String path) throws IOException, ClassNotFoundException {
		try (FileInputStream fileInputStream = new FileInputStream(path);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
			return (QuadTreeNode) objectInputStream.readObject();
		}
	}
}
