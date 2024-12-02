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
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;

import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.global.opencv_imgproc;

import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier; // Use this for JavaCV bindings

import org.opencv.videoio.VideoCapture;

public class FacialCapture {
    private static Connection dbConnection;
    private static CascadeClassifier faceDetector;
    private static OpenCVFrameGrabber grabber;
    private static JLabel imageLabel;
    // private static boolean thing = true;
    private static volatile boolean thing = true; // Mark as volatile
    private static Thread cameraFeedThread;
    private static boolean isRecognitionInProgress = false;

    // private static void startCameraFeed() {
    // // Start the camera feed in a new thread
    // cameraFeedThread = new Thread(FacialCapture::updateCameraFeed);
    // cameraFeedThread.start();
    // }
    private static void startCameraFeed() {
        // System.out.println("cameraFeedThread");
        // System.out.println(cameraFeedThread == null);
        // System.out.println(cameraFeedThread.isAlive());
        if (cameraFeedThread == null || !cameraFeedThread.isAlive()) {
            thing = true;
            isRecognitionInProgress = false;
            System.out.println("new Thread(FacialCapture::updateCameraFeed);");
            cameraFeedThread = new Thread(FacialCapture::updateCameraFeed);
            cameraFeedThread.start();
        }
    }
    // private static void stopCameraFeed() {
    // // Stop the camera feed by setting the flag to false
    // thing = false;

    // // Wait for the thread to finish its execution before proceeding
    // try {
    // if (cameraFeedThread != null && cameraFeedThread.isAlive()) {
    // cameraFeedThread.join(); // This will wait until the thread finishes
    // }
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
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
        JButton extraButton = new JButton("extraButton extraButton");

        buttonPanel.add(captureButton);
        buttonPanel.add(recognizeButton);
        buttonPanel.add(extraButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add button actions
        captureButton.addActionListener(e -> captureFace());
        recognizeButton.addActionListener(e -> recognizeFace());
        extraButton.addActionListener(e -> captureFaceTwo());

        // Start updating the camera feed
        new Thread(FacialCapture::updateCameraFeed).start();

        frame.setVisible(true);

    }

    private static void captureFaceTwo() {
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
                                System.out.println( "Face Selected: " + rect);
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

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private static void captureFaceTwo() {
    // try {
    // org.bytedeco.javacv.Frame frame = grabber.grab();
    // if (frame != null) {
    // Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

    // // Detect faces and get bounding rectangles
    // List<Rect> detectedFaces = detectFaces(mat);
    // // System.out.println("detectedFaces.size(): " + detectedFaces.size());

    // if (detectedFaces.isEmpty()) {
    // JOptionPane.showMessageDialog(null,
    // "No face detected! Please ensure your face is visible in the camera.");
    // return; // Exit if no faces are detected
    // }

    // // Draw rectangles on the detected faces for user reference
    // for (Rect rect : detectedFaces) {
    // // System.out.println("rect: + " + rect);
    // System.out.println("Face detected at coordinates: ");
    // System.out.println("x: " + rect.x() + ", y: " + rect.y());
    // System.out.println("Width: " + rect.width() + ", Height: " + rect.height());

    // opencv_imgproc.rectangle(
    // mat,
    // rect,
    // new Scalar(0, 255, 0, 0)); // Green color for highlighting
    // }

    // // Convert Mat back to BufferedImage for display
    // BufferedImage image = new Java2DFrameConverter().convert(new
    // OpenCVFrameConverter.ToMat().convert(mat));

    // // Show the image with detected faces in a selection window
    // JFrame selectionFrame = new JFrame("Select a Face");
    // selectionFrame.setSize(800, 600);
    // JLabel imageLabel = new JLabel(new ImageIcon(image));
    // selectionFrame.add(imageLabel);
    // selectionFrame.setVisible(true);

    // // Calculate the scaling factor
    // // double scaleX = (double) image.getWidth() / mat.cols();
    // // double scaleY = (double) image.getHeight() / mat.rows();

    // // Calculate the scaling factor
    // double scaleX = (double) mat.cols() / image.getWidth();
    // double scaleY = (double) mat.rows() / image.getHeight();

    // // System.out.println(image.getWidth());
    // // System.out.println(image.getHeight());

    // // Add mouse listener for face selection
    // imageLabel.addMouseListener(new MouseAdapter() {
    // @Override
    // public void mouseClicked(MouseEvent e) {
    // int x = e.getX();
    // int y = e.getY();
    // System.out.println("e.getX()" + e.getX());
    // System.out.println("e.getY()" + e.getY());
    // // System.out.println(e);

    // // Adjust mouse coordinates to match original image size
    // // int adjustedX = (int) (x / scaleX);
    // // int adjustedY = (int) (y / scaleY);
    // int adjustedX = (int) (x * scaleX);
    // int adjustedY = (int) (y * scaleY);

    // // Determine if click is inside any detected face
    // for (Rect rect : detectedFaces) {
    // // System.out.println("rect: " + rect);
    // System.out.println("adjustedX: " + adjustedX);
    // System.out.println("adjustedY: " + adjustedY);
    // System.out.println("rect.width(): " + rect.width());
    // System.out.println("rect.height(): " + rect.height());
    // System.out.println(adjustedX >= rect.x());
    // System.out.println(adjustedX <= rect.x() + rect.width());
    // System.out.println(adjustedY >= rect.y());
    // System.out.println(adjustedY <= rect.y() + rect.height());
    // if (adjustedX >= rect.x() && adjustedX <= rect.x() + rect.width() &&
    // adjustedY >= rect.y() && adjustedY <= rect.y() + rect.height()) {

    // JOptionPane.showMessageDialog(selectionFrame, "Face Selected: " + rect);
    // // Mat selectedFace = new Mat(mat, rect);

    // // Store or recognize the selected face
    // // byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);
    // // DatabaseHelper.storeEmbedding(dbConnection, embedding);
    // // JOptionPane.showMessageDialog(null, "Face stored successfully.");
    // // selectionFrame.dispose(); // Close the selection window
    // return;
    // }
    // }
    // JOptionPane.showMessageDialog(selectionFrame, "No face selected.");
    // }
    // });
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    private static void updateCameraFeed() {

        try {
            // grabber = new OpenCVFrameGrabber(0);

            // System.out.println(LocalDateTime.now());

            // System.out.println("three");

            // grabber.start();
            // System.out.println(OpenCVFrameGrabber.list);
            // System.out.println(LocalDateTime.now());

            // System.out.println("four");
            // System.out.println(LocalDateTime.now());
            // System.out.println("five");
            // faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");

            // System.out.println(LocalDateTime.now());
            // if (faceDetector.empty()) {
            // System.out.println("oopsie");
            // faceDetector.close();
            // // throw new RuntimeException("Failed to load face detector.");
            // }
            while (thing) {
                org.bytedeco.javacv.Frame frame = grabber.grab();
                // System.out.println("frame != null: " + frame != null);
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
    // private static void updateCameraFeed() {
    // try {
    // while (thing) {
    // org.bytedeco.javacv.Frame frame = grabber.grab();
    // if (frame != null) {
    // Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

    // // Detect faces and draw rectangles (live preview)
    // FaceProcessor.highlightFaces(mat, faceDetector);

    // // Convert Mat to BufferedImage for display
    // BufferedImage image = new Java2DFrameConverter().convert(frame);

    // // Update the JLabel with the processed image
    // SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
    // }
    // Thread.sleep(33); // ~30fps
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    // private static void updateCameraFeed() {
    // try {
    // while (thing) {
    // System.out.println("thing");
    // org.bytedeco.javacv.Frame frame = grabber.grab();
    // if (frame != null) {
    // Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

    // // Detect faces and draw rectangles (live preview)
    // FaceProcessor.highlightFaces(mat, faceDetector);

    // // Convert Mat to BufferedImage for display
    // BufferedImage image = new Java2DFrameConverter().convert(frame);

    // // Update the JLabel with the processed image
    // SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
    // // SwingUtilities.invokeLater(() -> imageLabel.setIcon(new
    // ImageIcon(image)));

    // }
    // Thread.sleep(33); // ~30fps
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    // private static void captureFace() {
    // try {
    // org.bytedeco.javacv.Frame frame = grabber.grab();
    // if (frame != null) {
    // Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

    // // Detect faces and get bounding rectangles
    // List<Rect> detectedFaces = detectFaces(mat);
    // System.out.println("detectedFaces.size(): " + detectedFaces.size());

    // if (detectedFaces.isEmpty()) {
    // JOptionPane.showMessageDialog(null,
    // "No face detected! Please ensure your face is visible in the camera.");
    // return; // Exit if no faces are detected
    // }

    // // Draw rectangles on the detected faces for user reference
    // for (Rect rect : detectedFaces) {
    // opencv_imgproc.rectangle(
    // mat,
    // rect,
    // new Scalar(0, 255, 0, 0));
    // }

    // // Convert Mat back to BufferedImage for display
    // BufferedImage image = new Java2DFrameConverter()
    // .convert(new OpenCVFrameConverter.ToMat().convert(mat));

    // // Show the image with detected faces in a selection window
    // JFrame selectionFrame = new JFrame("Select a Face");
    // selectionFrame.setSize(800, 600);
    // JLabel imageLabel = new JLabel(new ImageIcon(image));
    // selectionFrame.add(imageLabel);
    // selectionFrame.setVisible(true);

    // // Add mouse listener for face selection
    // imageLabel.addMouseListener(new MouseAdapter() {
    // @Override
    // public void mouseClicked(MouseEvent e) {
    // int x = e.getX();
    // int y = e.getY();

    // // Determine if click is inside any detected face
    // for (Rect rect : detectedFaces) {
    // System.out.println("rect: " + rect);
    // System.out.println("x: " + x );
    // System.out.println("y: " + y);
    // if (x >= rect.x() && x <= rect.x() + rect.width() &&
    // y >= rect.y() && y <= rect.y() + rect.height()) {

    // JOptionPane.showMessageDialog(selectionFrame,
    // "Face Selected: " + rect);
    // Mat selectedFace = new Mat(mat, rect);

    // // Store or recognize the selected face
    // byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);
    // DatabaseHelper.storeEmbedding(dbConnection, embedding);
    // JOptionPane.showMessageDialog(null, "Face stored successfully.");
    // selectionFrame.dispose(); // Close the selection window
    // return;
    // }
    // }
    // JOptionPane.showMessageDialog(selectionFrame, "No face selected.");
    // }
    // });
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

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

    // private static List<Rect> detectFaces(Mat mat) {
    // List<Rect> faceRectangles = new ArrayList<>();
    // RectVector detectedFaces = new RectVector();

    // try {

    // for (int j = 0; j < 33; j++) {
    // Mat gray = new Mat();

    // // Convert to grayscale
    // opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

    // // Enhance contrast (optional)
    // opencv_imgproc.equalizeHist(gray, gray);

    // faceDetector.detectMultiScale(gray, detectedFaces);

    // for (int i = 0; i < detectedFaces.size(); i++) {
    // Rect rect = detectedFaces.get(i);
    // System.out.println("Detected Rect: x=" + rect.x() + " y=" + rect.y() +
    // " width=" + rect.width() + " height=" + rect.height());

    // // Validate rect dimensions
    // if (rect.width() > 0 && rect.height() > 0) {
    // faceRectangles.add(rect);
    // } else {
    // System.out.println("Invalid Rect dimensions detected.");
    // }
    // }
    // Thread.sleep(1); // ~30fps
    // gray.close();

    // }
    // // Detect faces
    // // faceDetector.detectMultiScale(gray, detectedFaces);

    // // for (int i = 0; i < detectedFaces.size(); i++) {
    // // Rect rect = detectedFaces.get(i);
    // // System.out.println("Detected Rect: x=" + rect.x() + " y=" + rect.y() +
    // // " width=" + rect.width() + " height=" + rect.height());

    // // // Validate rect dimensions
    // // if (rect.width() > 0 && rect.height() > 0) {
    // // faceRectangles.add(rect);
    // // } else {
    // // System.out.println("Invalid Rect dimensions detected.");
    // // }
    // // }

    // // gray.close();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }

    // return faceRectangles;
    // }
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
                    // System.out.println("Detected Rect: x=" + rect.x() + " y=" + rect.y() +
                    // " width=" + rect.width() + " height=" + rect.height());

                    // Validate rect dimensions
                    if (rect.width() > 0 && rect.height() > 0) {
                        // Check if the current rectangle overlaps significantly with any existing one
                        boolean isDuplicate = false;
                        for (Rect existingRect : faceRectangles) {
                            if (calculateIoU(existingRect, rect) > 0.5) { // Threshold IoU to 0.5 (50%)
                                isDuplicate = true;
                                break; // Exit the loop if a duplicate is found
                            }
                        }
                        // Only add non-duplicate rectangles
                        if (!isDuplicate) {
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

    private static double calculateIoU(Rect rect1, Rect rect2) {
        // Calculate the coordinates of the intersection rectangle
        int x1 = Math.max(rect1.x(), rect2.x()); // The leftmost x-coordinate
        int y1 = Math.max(rect1.y(), rect2.y()); // The topmost y-coordinate
        int x2 = Math.min(rect1.x() + rect1.width(), rect2.x() + rect2.width()); // The rightmost x-coordinate
        int y2 = Math.min(rect1.y() + rect1.height(), rect2.y() + rect2.height()); // The bottommost y-coordinate

        // Calculate the area of the intersection
        int intersectionWidth = Math.max(0, x2 - x1); // Ensure non-negative intersection width
        int intersectionHeight = Math.max(0, y2 - y1); // Ensure non-negative intersection height
        int intersectionArea = intersectionWidth * intersectionHeight;

        // Calculate the area of the union
        int areaRect1 = rect1.width() * rect1.height();
        int areaRect2 = rect2.width() * rect2.height();
        int unionArea = areaRect1 + areaRect2 - intersectionArea;

        // Calculate and return the IoU (Intersection over Union)
        return (double) intersectionArea / unionArea;
    }

    // private static List<Rect> detectFaces(Mat mat) {
    // List<Rect> faceRectangles = new ArrayList<>();
    // int counter = 0;
    // long size = 0;

    // try {
    // while (counter < 5 && size == 0) {
    // Mat gray = new Mat();
    // opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
    // opencv_imgproc.equalizeHist(gray, gray);

    // RectVector detectedFaces = new RectVector();
    // faceDetector.detectMultiScale(gray, detectedFaces);

    // if (detectedFaces.size() > 0) {
    // size = detectedFaces.size();
    // for (int i = 0; i < size; i++) {
    // faceRectangles.add(detectedFaces.get(i));
    // }
    // }

    // counter++;
    // Thread.sleep(100); // Ensure synchronization between detection attempts
    // gray.close(); // Release resources
    // }

    // // while (counter < 5 && size == 0) {

    // // Mat gray = new Mat();

    // // // Convert the image to grayscale
    // // opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

    // // // Enhance image contrast (optional but recommended for better detection)
    // // opencv_imgproc.equalizeHist(gray, gray);

    // // // Detect faces
    // // RectVector detectedFaces = new RectVector();
    // // faceDetector.detectMultiScale(gray, detectedFaces);
    // // if (gray == null) {
    // // return null;
    // // }
    // // // size = detectedFaces.size();
    // // // Collect face rectangles
    // // for (int i = 0; i < detectedFaces.size(); i++) {
    // // size++;
    // // faceRectangles.add(detectedFaces.get(i));
    // // System.out.println("detectedFaces.get(i) " + detectedFaces.get(i));
    // // }
    // // counter++;
    // // Thread.sleep(100);
    // // // You can close the gray Mat here only if no longer needed
    // // gray.close();
    // // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }

    // System.out.println("counter: " + counter + "\n" + "size: " + size);
    // return faceRectangles;
    // }

    // private static void recognizeFace() {

    // thing = false;
    // try {
    // System.out.println("recognizeFace recognizeFace recognizeFace");
    // org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this class
    // System.out.println(frame == null);
    // System.out.println(frame);
    // if (frame != null) {
    // System.out.println("recognizeFace one");
    // Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
    // System.out.println("recognizeFace two");

    // List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);
    // // System.out.println("recognizeFace three: " + faces.size());

    // if (faces == null) {
    // return;
    // }

    // // Compare each detected face with the database
    // for (Mat face : faces) {
    // byte[] embedding = FaceProcessor.generateEmbedding(face);
    // String isRecognized = DatabaseHelper.isFaceRecognized(dbConnection,
    // embedding);
    // thing = true;
    // if (isRecognized != null && !isRecognized.isEmpty()) {
    // String formattedString = String.format("Face recognized as \"%s\"",
    // isRecognized);
    // JOptionPane.showMessageDialog(null, formattedString);
    // thing = true;
    // return;
    // } else {
    // thing = true;
    // JOptionPane.showMessageDialog(null, "Face not recognized.");
    // }
    // }
    // thing = true;

    // }
    // } catch (Exception e) {
    // thing = true;
    // e.printStackTrace();
    // } finally {
    // // Thread.
    // // new Thread(FacialCapture::updateCameraFeed).start();
    // thing = true; // Ensure feed resumes after recognition attempt
    // }
    // thing = true;
    // }

    // private static void recognizeFace() {
    // stopCameraFeed(); // Stop the camera feed temporarily

    // try {
    // System.out.println("recognizeFace recognizeFace recognizeFace");
    // org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this class
    // System.out.println(frame == null);
    // System.out.println(frame);
    // if (frame != null) {
    // System.out.println("recognizeFace one");
    // Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
    // System.out.println("recognizeFace two");

    // List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);

    // if (faces == null) {
    // return;
    // }

    // // Compare each detected face with the database
    // for (Mat face : faces) {
    // byte[] embedding = FaceProcessor.generateEmbedding(face);
    // String isRecognized = DatabaseHelper.isFaceRecognized(dbConnection,
    // embedding);
    // if (isRecognized != null && !isRecognized.isEmpty()) {
    // String formattedString = String.format("Face recognized as \"%s\"",
    // isRecognized);
    // JOptionPane.showMessageDialog(null, formattedString);
    // return;
    // } else {
    // JOptionPane.showMessageDialog(null, "Face not recognized.");
    // }
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // } finally {
    // thing = true; // Ensure feed resumes after recognition attempt
    // startCameraFeed(); // Restart the camera feed thread
    // }
    // }
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
        // startCameraFeed();
    }

    // thing = true;

}