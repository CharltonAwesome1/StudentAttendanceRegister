module FacialCapture {
    // Requires JUnit for testing

    // Requires OpenCV for face recognition functionality
    requires transitive org.bytedeco.opencv; 
    // requires org.bytedeco.opencv.platform;
    requires org.bytedeco.javacpp;
    // requires org.bytedeco.opencv.presets;
    // requires org.bytedeco.javacpp
    // requires org.bytedeco.javacpp.Loader;
    // requires org.bytedeco.javacpp.chrono;
    // requires org.byte
    requires java.desktop; // For Swing, AWT, and image handling (BufferedImage)
    requires transitive java.sql; // For JDBC (java.sql.Connection)
    // requires org.bytedeco.opencv.*;

    // Requires MySQL Connector for database interaction
    // requires mysql.connector.java;

    // requires com.mysql;
    requires org.bytedeco.javacv;
    // requires org.bytedeco.opencv.presets;
    // requires FacialCapture
    // exports
    // requires 

    exports FacialCapture;
    // exports org.bytedeco.opencv;
    // exports org
    // exports org.bytedeco.*;
    // exports org.bytedeco.opencv;
    // Exports your main package if you want to expose it to other modules
    opens FacialCapture to org.bytedeco.javacpp, org.bytedeco.opencv.presets, org.bytedeco.opencv,org.bytedeco.opencv.presets.opencv_core ;
    // opens org.bytedeco.javacpp to org.bytedeco.opencv;  // This gives opencv access to javacpp internals
    // opens org.bytedeco.opencv to org.bytedeco.javacpp;  // This allows javacpp to access opencv presets
    // opens org.bytedeco.opencv to org.bytedeco.javacpp;
    //       org.bytedeco.opencv
    // opens org.bytedeco.opencv_core to org.bytedeco.javacpp;

    // exports com.example.facialcapture;
    // exports com.example;
    // exports com;
    // exports com.example;
    // exports app;
    // exports
}
