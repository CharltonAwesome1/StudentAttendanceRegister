package FacialCapture;

// import org.bytedeco.opencv.opencv_core.*;
// import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

// import org.bytedeco.javacpp.annotation.*;
// import org.bytedeco.javacpp.indexer.*;
// import org.bytedeco.javacpp.presets.*;
// import org.bytedeco.javacpp.tools.*;
// import org.bytedeco.javacpp.*;
// import org.bytedeco.opencv.opencv_core.*;
// import org.bytedeco
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
// import org.bytedeco.opencv.opencv_core.MatOfRect;
// import org.bytedeco.opencv.opencv_core.Rect;
// import org.bytedeco.opencv.opencv_core.Scalar;
// import org.bytedeco.opencv.opencv_core.Size;
// // import org.bytedeco.opencv.global.opencv_highgui.HighGui;
// import org.bytedeco.opencv.global.opencv_imgproc;
// import org.bytedeco.opencv.videoio.VideoCapture;

// import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgproc;
// import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

// import org.bytedeco.opencv.opencv_core.*;
// import org.bytedeco.opencv.
// import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

// import javax.swing.*;
// import java.awt.*;
// import java.awt.image.BufferedImage;
import java.io.File;
// import java.sql.Connection;
// import java.util.List;
import org.bytedeco.opencv.global.opencv_imgcodecs;
// import org.bytedeco.javacv.*;
import org.opencv.core.Core;
// import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
// import org.opencv.core.Rect;
// import org.opencv.core.Scalar;
// import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier; // Use this for JavaCV bindings

import org.opencv.videoio.VideoCapture;
// import java.sql.Connection;
// import java.util.List;

// Other imports...

public class FacialCapture {
    private static Connection dbConnection;
    private static CascadeClassifier faceDetector;
    private static OpenCVFrameGrabber grabber;
    private static JLabel imageLabel;

    public static void main(String[] args) {
        // Initialize resources
        try {
            // OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
            System.out.println("one");
            dbConnection = DatabaseHelper.connect();
            System.out.println("two");
            // OpenCVFrameGrabber.getDeviceDescriptions
            // System.out.println(OpenCVFrameGrabber.list.size());
            // for (int i = 0; i < OpenCVFrameGrabber.list.size(); i++) { // Check first 5
            // devices
            // try {
            // OpenCVFrameGrabber testGrabber = new OpenCVFrameGrabber(i);
            // System.out.println(i + " start: " + LocalDateTime.now());
            // testGrabber.start();
            // System.out.println("Camera found at index: " + i);
            // testGrabber.stop();
            // testGrabber.close();
            // break;
            // } catch (Exception e) {
            // System.out.println("No camera at index: " + i);
            // }
            // }
            // String[] devices = VideoInputFrameGrabber.getDeviceDescriptions();
            // System.out.println("length: " + devices.length);
            // for (int i = 0; i < devices.length; i++) {
            //     System.out.println("Device " + i + ": " + devices[i]);
            // }

            // String[] devices = FFmpegFrameGrabber.getDeviceDescriptions();
            // for (String device : devices) {
            //     System.out.println(device);
            // }

            //
            // VideoCapture thing = new VideoCapture(0);
            // System.out.println(thing.open(0));
            // System.out.println(thing.getBackendName());
            // System.out.println(thing.ap);
            // String fileName = thing.
            grabber = new OpenCVFrameGrabber(0);
            System.out.println("Using grabber with fileName: " + grabber.getFormat());

            System.out.println(LocalDateTime.now());
            System.out.println(OpenCVFrameGrabber.list);
            System.out.println(grabber);
            // System.out.println(grabber.);
            // System.out.println(grabber.);

            System.out.println("three");
            // grabber.setImageWidth(640);
            // grabber.setImageHeight(480);
            // grabber.setFormat(fileName);
            grabber.start();
            System.out.println(OpenCVFrameGrabber.list);
            System.out.println(LocalDateTime.now());

            // for (int i = 0; i < 5; i++) {
            // grabber.grab();
            // System.out.println(i + ": " + LocalDateTime.now());
            // }
            System.out.println("four");
            System.out.println(LocalDateTime.now());
            System.out.println("five");
            faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");

            // faceDetector = new CascadeClassifier(
            // App.class.getResource("/haarcascade_frontalface_alt.xml").getPath());
            // CascadeClassifier faceDetector = new
            // CascadeClassifier("haarcascade_frontalface_alt.xml");

            // faceDetector = new CascadeClassifier(
            // FacialCapture.class.getResource("haarcascade_frontalface_alt.xml").getPath()
            // );
            // // System.out.println("faceDetector");
            // // System.out.println(faceDetector);
            System.out.println(LocalDateTime.now());
            if (faceDetector.empty()) {
                System.out.println("oopsie");
                faceDetector.close();
                // throw new RuntimeException("Failed to load face detector.");
            }
        } catch (Exception e) {
            // faceDetector.close();
            e.printStackTrace();
            return;
        }

        // Build GUI
        JFrame frame = new JFrame("Facial Recognition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        imageLabel = new JLabel();
        frame.add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton captureButton = new JButton("Capture Face");
        JButton recognizeButton = new JButton("Recognize Face");

        buttonPanel.add(captureButton);
        buttonPanel.add(recognizeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add button actions
        captureButton.addActionListener(e -> captureFace());
        recognizeButton.addActionListener(e -> recognizeFace());

        // Start updating the camera feed
        new Thread(FacialCapture::updateCameraFeed).start();

        frame.setVisible(true);

    }

    // private static void updateCameraFeed() {
    // try {
    // while (true) {
    // org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this class
    // if (frame != null) {
    // BufferedImage image = new Java2DFrameConverter().convert(frame);
    // SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    private static void updateCameraFeed() {
        try {
            while (true) {
                org.bytedeco.javacv.Frame frame = grabber.grab();
                if (frame != null) {
                    Thread.sleep(33);
                    Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

                    // Detect faces and draw rectangles
                    FaceProcessor.detectAndSaveFaces(mat, faceDetector);

                    // Convert Mat back to BufferedImage for display
                    BufferedImage image = new Java2DFrameConverter()
                            .convert(new OpenCVFrameConverter.ToMat().convert(mat));

                    // Update the JLabel with the processed image
                    SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void captureFace() {
        try {
            System.out.println("captureFace captureFace captureFace");
            faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
            org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this class
            if (frame != null) {
                // new OpenCVFrameConverter.ToMat().convertToMat
                Mat mat = new OpenCVFrameConverter.ToMat().convertToMat(frame);
                List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);

                // Save detected faces to the database
                for (Mat face : faces) {
                    byte[] embedding = FaceProcessor.generateEmbedding(face);
                    DatabaseHelper.storeEmbedding(dbConnection, embedding);
                    System.out.println("Face captured and stored.");
                }

                JOptionPane.showMessageDialog(null, "Face captured and stored successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void recognizeFace() {
        try {
            System.out.println("recognizeFace recognizeFace recognizeFace");
            org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this class
            // System.out.println(frame == null);
            if (frame != null) {
                // System.out.println("recognizeFace one");
                Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
                // System.out.println("recognizeFace two");
                List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);
                // System.out.println("recognizeFace three");

                // Compare each detected face with the database
                for (Mat face : faces) {
                    // System.out.println("recognizeFace four");
                    byte[] embedding = FaceProcessor.generateEmbedding(face);
                    // System.out.println("recognizeFace five");
                    // boolean isRecognized = DatabaseHelper.isFaceRecognized(dbConnection,
                    // embedding);
                    String isRecognized = DatabaseHelper.isFaceRecognized(dbConnection, embedding);
                    // mat.close();
                    // System.out.println("isRecognized: " + isRecognized);
                    if (isRecognized != null && !isRecognized.isEmpty()) {
                        String formattedString = String.format("Face recognized as \"%s\"", isRecognized);
                        JOptionPane.showMessageDialog(null, formattedString);
                        return;
                    } else {
                        JOptionPane.showMessageDialog(null, "Face not recognized.");

                    }
                }

                // OpenCVFrameConverter.c
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}