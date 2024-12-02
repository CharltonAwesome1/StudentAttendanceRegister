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
            //     System.out.println("oopsie");
            //     faceDetector.close();
            //     // throw new RuntimeException("Failed to load face detector.");
            // }
            while (thing) {
                org.bytedeco.javacv.Frame frame = grabber.grab();
                System.out.println("frame != null: " + frame != null);
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

    private static List<Rect> detectFaces(Mat mat) {
        List<Rect> faceRectangles = new ArrayList<>();
        int counter = 0;
        long size = 0;

        try {

            while (counter < 5 && size == 0) {

                Mat gray = new Mat();

                // Convert the image to grayscale
                opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

                // Enhance image contrast (optional but recommended for better detection)
                opencv_imgproc.equalizeHist(gray, gray);

                // Detect faces
                RectVector detectedFaces = new RectVector();
                faceDetector.detectMultiScale(gray, detectedFaces);
                if (gray == null) {
                    return null;
                }
                // size = detectedFaces.size();
                // Collect face rectangles
                for (int i = 0; i < detectedFaces.size(); i++) {
                    size++;
                    faceRectangles.add(detectedFaces.get(i));
                    System.out.println("detectedFaces.get(i) " + detectedFaces.get(i));
                }
                counter++;
                Thread.sleep(100);
                // You can close the gray Mat here only if no longer needed
                gray.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("counter: " + counter + "\n" + "size: " + size);
        return faceRectangles;
    }

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