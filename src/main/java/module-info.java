module QuadTree {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.media;
    requires transitive java.desktop;

    opens quadtree to javafx.controls, javafx.media, javafx.graphics;
    opens quadtree.logic to javafx.controls, javafx.media, javafx.graphics;
    opens quadtree.view to javafx.controls, javafx.media, javafx.graphics;

    exports quadtree;
    exports quadtree.view;
    exports quadtree.logic;
}
