package FacialCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;

import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.global.opencv_imgproc;

import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier; // Use this for JavaCV bindings

// import org.opencv.videoio.VideoCapture;

public class FacialCapture {
    private static Connection dbConnection;
    private static CascadeClassifier faceDetector;
    private static OpenCVFrameGrabber grabber;
    private static JLabel imageLabel;
    private static volatile boolean thing = true; // Mark as volatile
    private static Thread cameraFeedThread;
    private static boolean isRecognitionInProgress = false;

    private static void startCameraFeed() {
        if (cameraFeedThread == null || !cameraFeedThread.isAlive()) {
            thing = true;
            isRecognitionInProgress = false;
            System.out.println("new Thread(FacialCapture::updateCameraFeed);");
            cameraFeedThread = new Thread(FacialCapture::updateCameraFeed);
            cameraFeedThread.start();
        }
    }

    private static void stopCameraFeed() {
        if (cameraFeedThread != null && cameraFeedThread.isAlive()) {
            thing = false; // Stop the camera feed
            try {
                cameraFeedThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Initialize resources
        try {
            System.out.println("one");
            dbConnection = DatabaseHelper.connect();
            System.out.println("two");

            grabber = new OpenCVFrameGrabber(0);

            System.out.println(LocalDateTime.now());

            System.out.println("three");

            grabber.start();
            System.out.println(OpenCVFrameGrabber.list);
            System.out.println(LocalDateTime.now());

            System.out.println("four");
            System.out.println(LocalDateTime.now());
            System.out.println("five");
            faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");

            System.out.println(LocalDateTime.now());
            if (faceDetector.empty()) {
                System.out.println("oopsie");
                faceDetector.close();
            }
        } catch (Exception e) {
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
        // JButton recognizeButton = new JButton("Recognize Face");
        JButton extraButton = new JButton("extraButton extraButton");

        buttonPanel.add(captureButton);
        // buttonPanel.add(recognizeButton);
        buttonPanel.add(extraButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add button actions
        captureButton.addActionListener(e -> captureFace());
        // recognizeButton.addActionListener(e -> recognizeFace());
        extraButton.addActionListener(e -> recognizeFaceTwo());

        // Start updating the camera feed
        new Thread(FacialCapture::updateCameraFeed).start();

        frame.setVisible(true);

    }

    private static void recognizeFaceTwo() {
        try {
            org.bytedeco.javacv.Frame frame = grabber.grab();
            if (frame != null) {
                Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

                // Detect faces and get bounding rectangles
                List<Rect> detectedFaces = detectFaces(mat);

                // Clone detected face rectangles to freeze the state
                List<Rect> frozenFaces = new ArrayList<>();
                for (Rect rect : detectedFaces) {
                    System.out.println("Cloning Rect: x=" + rect.x() + " y=" + rect.y() +
                            " width=" + rect.width() + " height=" + rect.height());
                    // frozenFaces.add(new Rect(rect)); // Explicitly clone each Rect
                    frozenFaces.add(new Rect(rect.x(), rect.y(), rect.width(), rect.height()));

                }

                if (frozenFaces.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "No face detected! Please ensure your face is visible in the camera.");
                    return; // Exit if no faces are detected
                }

                // Draw rectangles on the detected faces for user reference

                for (Rect rect : frozenFaces) {
                    opencv_imgproc.rectangle(
                            mat,
                            rect,
                            new Scalar(0, 255, 0, 0)); // Green color for highlighting
                }

                // Convert Mat back to BufferedImage for display
                BufferedImage image = new Java2DFrameConverter().convert(new OpenCVFrameConverter.ToMat().convert(mat));

                // Show the image with detected faces in a selection window
                JFrame selectionFrame = new JFrame("Select a Face");
                // selectionFrame.setSize(800, 600);
                selectionFrame.setSize(image.getWidth(), image.getHeight());
                selectionFrame.setResizable(false);
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                selectionFrame.add(imageLabel);
                selectionFrame.setVisible(true);

                // Calculate the scaling factor
                double scaleX = (double) image.getWidth() / mat.cols();
                double scaleY = (double) image.getHeight() / mat.rows();
                // image.rele

                // Add mouse listener for face selection using frozenFaces
                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int x = e.getX();
                        int y = e.getY();

                        System.out.println("Mouse click detected: x=" + x + ", y=" + y);

                        // Adjust mouse coordinates to match original image size
                        int adjustedX = (int) (x * scaleX);
                        int adjustedY = (int) (y * scaleY);

                        System.out.println("Adjusted click: x=" + adjustedX + ", y=" + adjustedY);
                        System.out.println("frozenFaces length: " + frozenFaces.size());

                        // Determine if click is inside any frozen face rectangle
                        for (Rect rect : frozenFaces) {
                            // Print the current rect details and valid range
                            System.out.println("Checking Rect: x=" + rect.x() + ", y=" + rect.y() +
                                    ", width=" + rect.width() + ", height=" + rect.height());
                            System.out.println("Valid X range: " + rect.x() + " to " + (rect.x() + rect.width()));
                            System.out.println("Valid Y range: " + rect.y() + " to " + (rect.y() + rect.height()));

                            // Individual boundary checks
                            boolean withinXBounds = adjustedX >= rect.x() && adjustedX <= (rect.x() + rect.width());
                            boolean withinYBounds = adjustedY >= rect.y() && adjustedY <= (rect.y() + rect.height());

                            // Print the results of boundary checks
                            System.out.println("Boundary check results: X=" + withinXBounds + ", Y=" + withinYBounds);

                            // Final condition to determine if the click is inside the rectangle
                            if (withinXBounds && withinYBounds) {
                                System.out.println("Face Selected: Rect[x=" + rect.x() +
                                        ", y=" + rect.y() + ", width=" + rect.width() +
                                        ", height=" + rect.height() + "]");
                                // System.out.println("Face Selected: " + rect);
                                Mat selectedFace = new Mat(mat, rect);

                                // Pass the selected Mat to generateEmbedding()
                                byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);
                                String isRecognized = DatabaseHelper.isFaceRecognized(dbConnection, embedding);
                                if (isRecognized != null && !isRecognized.isEmpty()) {
                                    String formattedString = String.format("Face recognized as \"%s\"", isRecognized);
                                    JOptionPane.showMessageDialog(null, formattedString);
                                    return;
                                } else {
                                    JOptionPane.showMessageDialog(null, "Face not recognized.");
                                }

                                // Optionally store or use the embedding
                                System.out.println("Generated embedding for selected face.");
                                // JOptionPane.showMessageDialog(selectionFrame, "Face Selected: " + rect);

                                // Optional: Dispose of the selection window
                                selectionFrame.dispose();
                                // return;
                                return;
                            }
                        }

                        System.out.println("No face selected.");
                        JOptionPane.showMessageDialog(selectionFrame, "No face selected.");
                    }
                });
                // mat.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateCameraFeed() {

        try {

            while (thing) {
                org.bytedeco.javacv.Frame frame = grabber.grab();
                if (frame != null) {
                    Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
                    FaceProcessor.highlightFaces(mat, faceDetector);

                    BufferedImage image = new Java2DFrameConverter().convert(frame);
                    SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
                }
                Thread.sleep(33); // ~30fps
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void captureFace() {
        try {
            org.bytedeco.javacv.Frame frame = grabber.grab();
            if (frame != null) {
                Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

                // Detect faces and get bounding rectangles
                List<Rect> detectedFaces = detectFaces(mat);
                System.out.println("detectedFaces.size(): " + detectedFaces.size());

                if (detectedFaces.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "No face detected! Please ensure your face is visible in the camera.");
                    return; // Exit if no faces are detected
                }

                // Draw rectangles on the detected faces for user reference
                for (Rect rect : detectedFaces) {
                    opencv_imgproc.rectangle(
                            mat,
                            rect,
                            new Scalar(0, 255, 0, 0)); // Green color for highlighting
                }

                // Convert Mat back to BufferedImage for display
                BufferedImage image = new Java2DFrameConverter().convert(new OpenCVFrameConverter.ToMat().convert(mat));

                // Show the image with detected faces in a selection window
                JFrame selectionFrame = new JFrame("Select a Face");
                selectionFrame.setSize(800, 600);
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                selectionFrame.add(imageLabel);
                selectionFrame.setVisible(true);

                // Calculate the scaling factor
                double scaleX = (double) image.getWidth() / mat.cols();
                double scaleY = (double) image.getHeight() / mat.rows();
                System.out.println(image.getWidth());
                System.out.println(image.getHeight());
                System.out.println(mat.cols());
                System.out.println(mat.rows());

                // Add mouse listener for face selection
                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int x = e.getX();
                        int y = e.getY();
                        System.out.println("e.getX()" + e.getX());
                        System.out.println("e.getY()" + e.getY());
                        // System.out.println(e);

                        // Adjust mouse coordinates to match original image size
                        int adjustedX = (int) (x / scaleX);
                        int adjustedY = (int) (y / scaleY);

                        // Determine if click is inside any detected face
                        for (Rect rect : detectedFaces) {
                            System.out.println("rect: " + rect);
                            System.out.println("adjustedX: " + adjustedX);
                            System.out.println("adjustedY: " + adjustedY);
                            if (adjustedX >= rect.x() && adjustedX <= rect.x() + rect.width() &&
                                    adjustedY >= rect.y() && adjustedY <= rect.y() + rect.height()) {

                                JOptionPane.showMessageDialog(selectionFrame,
                                        "Face Selected: " + rect);
                                Mat selectedFace = new Mat(mat, rect);

                                // Store or recognize the selected face
                                byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);
                                DatabaseHelper.storeEmbedding(dbConnection, embedding);
                                JOptionPane.showMessageDialog(null, "Face stored successfully.");
                                selectionFrame.dispose(); // Close the selection window
                                return;
                            }
                        }
                        JOptionPane.showMessageDialog(selectionFrame, "No face selected.");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Rect> detectFaces(Mat mat) {
        List<Rect> faceRectangles = new ArrayList<>();
        RectVector detectedFaces = new RectVector();

        try {
            for (int j = 0; j < 33; j++) {
                Mat gray = new Mat();

                // Convert to grayscale
                opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

                // Enhance contrast (optional)
                opencv_imgproc.equalizeHist(gray, gray);

                faceDetector.detectMultiScale(gray, detectedFaces);

                for (int i = 0; i < detectedFaces.size(); i++) {
                    Rect rect = detectedFaces.get(i);

                    // Validate rect dimensions
                    if (rect.width() > 0 && rect.height() > 0) {
                        boolean shouldAdd = true; // Assume the rectangle is valid for now

                        // Check overlap with existing rectangles
                        for (int k = 0; k < faceRectangles.size(); k++) {
                            Rect existingRect = faceRectangles.get(k);

                            // If fully contained or significantly overlapping, keep the larger rectangle
                            if (isFullyContained(rect, existingRect) || calculateIoU(existingRect, rect) > 0.5) {
                                if (calculateArea(rect) > calculateArea(existingRect)) {
                                    // Replace the smaller rectangle with the larger one
                                    faceRectangles.set(k, rect);
                                }
                                shouldAdd = false; // Do not add this rectangle again
                                break;
                            }
                        }

                        if (shouldAdd) {
                            faceRectangles.add(rect);
                        }
                    } else {
                        System.out.println("Invalid Rect dimensions detected.");
                    }
                }

                Thread.sleep(1);
                gray.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return faceRectangles;
    }

    private static boolean isFullyContained(Rect inner, Rect outer) {
        // Check if the inner rectangle is fully inside the outer rectangle
        return inner.x() >= outer.x() &&
                inner.y() >= outer.y() &&
                inner.x() + inner.width() <= outer.x() + outer.width() &&
                inner.y() + inner.height() <= outer.y() + outer.height();
    }

    private static int calculateArea(Rect rect) {
        return rect.width() * rect.height();
    }

    private static double calculateIoU(Rect rect1, Rect rect2) {
        // Calculate the coordinates of the intersection rectangle
        int x1 = Math.max(rect1.x(), rect2.x());
        int y1 = Math.max(rect1.y(), rect2.y());
        int x2 = Math.min(rect1.x() + rect1.width(), rect2.x() + rect2.width());
        int y2 = Math.min(rect1.y() + rect1.height(), rect2.y() + rect2.height());

        // Calculate the area of the intersection
        int intersectionWidth = Math.max(0, x2 - x1);
        int intersectionHeight = Math.max(0, y2 - y1);
        int intersectionArea = intersectionWidth * intersectionHeight;

        // Calculate the area of the union
        int areaRect1 = rect1.width() * rect1.height();
        int areaRect2 = rect2.width() * rect2.height();
        int unionArea = areaRect1 + areaRect2 - intersectionArea;

        // Calculate and return the IoU (Intersection over Union)
        return (double) intersectionArea / unionArea;
    }

    private static void recognizeFace() {
        if (isRecognitionInProgress) {
            return; // Prevent recognition if it's already in progress
        }

        isRecognitionInProgress = true; // Mark recognition as in progress

        stopCameraFeed(); // Stop the camera feed before recognizing face

        try {
            System.out.println("recognizeFace: Starting recognition process");
            org.bytedeco.javacv.Frame frame = grabber.grab();
            if (frame != null) {
                Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

                List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);
                if (faces != null) {
                    for (Mat face : faces) {
                        byte[] embedding = FaceProcessor.generateEmbedding(face);
                        String isRecognized = DatabaseHelper.isFaceRecognized(dbConnection, embedding);
                        if (isRecognized != null && !isRecognized.isEmpty()) {
                            String formattedString = String.format("Face recognized as \"%s\"", isRecognized);
                            JOptionPane.showMessageDialog(null, formattedString);
                            return;
                        } else {
                            JOptionPane.showMessageDialog(null, "Face not recognized.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRecognitionInProgress = false; // Reset recognition state
            startCameraFeed(); // Restart the camera feed after recognition attempt
        }
    }

}