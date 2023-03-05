module QuadTree {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.media;
    requires transitive java.desktop;

    opens quadtree to javafx.media, javafx.graphics;

    exports quadtree;
}
