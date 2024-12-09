package FacialCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
// import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.global.opencv_core;
// import org.bytedeco.javacv.*;
// import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;
// import org.bytedeco.opencv.global.opencv_core; 
import org.bytedeco.opencv.global.opencv_highgui;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
// import org.opencv.core.Core;
// import org.opencv.highgui.HighGui;
// import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgcodecs.Imgcodecs;

public class FacialCapture {
    private static Connection dbConnection;
    private static CascadeClassifier faceDetector;
    private static OpenCVFrameGrabber cameraGrabber;
    private static JLabel videoDisplayLabel;
    private static JFrame frame;
    private static JPanel buttonPanel;
    private static JLabel nameLabel;
    private static JTextField nameTextField;
    private static JButton captureButton;
    private static JButton recognizeButton;
    private static JButton buttonThree;

    public static void main(String[] args) {
        try {
            System.out.println("Initializing database connection...");
            dbConnection = DatabaseHelper.connect();
            System.out.println("Database connection established.");

            System.out.println("Initializing camera grabber...");
            cameraGrabber = new OpenCVFrameGrabber(0);
            cameraGrabber.start();
            System.out.println("Camera grabber initialized.");
            System.out.println(cameraGrabber.getFrameRate());
            System.out.println(cameraGrabber.getImageWidth());
            System.out.println(cameraGrabber.getImageHeight());
            // System.out.println(cameraGrabber.get

            System.out.println("Loading face detector...");
            faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
            if (faceDetector.empty()) {
                throw new RuntimeException("Failed to load face detector.");
            }
            System.out.println("Face detector loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        frame = new JFrame("Facial Recognition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        videoDisplayLabel = new JLabel();
        frame.add(videoDisplayLabel, BorderLayout.CENTER);

        buttonPanel = new JPanel();
        nameLabel = new JLabel("Enter your name");
        nameLabel = new JLabel("Enter your name");
        nameLabel.setPreferredSize(new Dimension(150, 30)); // Set a fixed size for JLabel
        nameTextField = new JTextField();
        nameTextField.setPreferredSize(new Dimension(200, 30)); // Set a fixed size for JTextField
        // JTextField nameTextField = new JTextField();
        captureButton = new JButton("Capture Face");
        recognizeButton = new JButton("Recognize Face");
        buttonThree = new JButton("buttonThree");
        buttonPanel.add(nameLabel);
        buttonPanel.add(nameTextField);
        buttonPanel.add(captureButton);
        buttonPanel.add(recognizeButton);
        buttonPanel.add(buttonThree);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        captureButton.addActionListener(e -> captureFace());
        recognizeButton.addActionListener(e -> recognizeFaceTwo());
        buttonThree.addActionListener(e -> buttonThreeMethod());

        new Thread(FacialCapture::updateCameraFeed).start();

        frame.setVisible(true);

    }

    private static void buttonThreeMethod() {

        File dir = new File("src/main/resources/happy_faces/");
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        System.out.println("dir: " + dir.getAbsolutePath());
        System.out.println("files.length: " + files.length);

        if (files == null || files.length == 0) {
            System.out.println("No .png images found in the specified folder.");
            return;
        }

        List<Mat> images = new ArrayList<>();

        for (File file : files) {
            System.out.println("Reading file: " + file.getAbsolutePath());
            Mat img = opencv_imgcodecs.imread(file.getAbsolutePath(), 1);
            if (!img.empty()) {
                images.add(img);
            }
        }

        if (images.isEmpty()) {
            System.out.println("No valid images found.");
            return;
        }

        Mat avgImage = images.get(0);

        for (int i = 1; i < images.size(); i++) {
            Mat currentImage = images.get(i);
            double alpha = 1.0 / (i + 1);
            double beta = 1.0 - alpha;

            opencv_core.addWeighted(currentImage, alpha, avgImage, beta, 0.0, avgImage);
        }

        opencv_imgcodecs.imwrite(dir + "avg_happy_face.png", avgImage);

        Mat resultImage = opencv_imgcodecs.imread(dir + "avg_happy_face.png");

        opencv_highgui.imshow("Average Happy Face", resultImage);
        opencv_highgui.waitKey(0);
        opencv_highgui.destroyAllWindows();
    }

    private static void recognizeFaceTwo() {
        try {
            List<Mat> frameList = new ArrayList<>();
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < 1000) {
                org.bytedeco.javacv.Frame grabbedFrame = cameraGrabber.grab();
                if (grabbedFrame != null) {
                    Mat frameMatrix = new OpenCVFrameConverter.ToMat().convert(grabbedFrame);
                    frameList.add(frameMatrix);
                    // OpenCVFrameConverter.ToMat().close();
                    // frameMatrix.release();
                    // frameMatrix.close();
                }

                // Thread.sleep(1); 
            }
            // System.out.println("frameList.length: " + frameList.size());

            List<Rect> frozenFaces = new ArrayList<>();
            for (Mat frameMatrix : frameList) {
                if (frameMatrix.empty()) {
                    System.out.println("Skipped an empty frame.");
                    continue;
                }
                List<Rect> detectedFaces = detectFaces(frameMatrix);

                for (Rect rect : detectedFaces) {
                    frozenFaces.add(new Rect(rect.x(), rect.y(), rect.width(), rect.height()));
                }
            }

            System.out.println("outside");

            List<Rect> newFrozenFaces = validateFaceRectangles(frozenFaces);

            if (newFrozenFaces.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No face detected! Please ensure your face is visible in the camera.");
                return;
            }

            List<Mat> images = new ArrayList<>();

            System.out.println("frameList.size(): " + frameList.size());
            for (int i = 0; i < frameList.size(); i++) {
                images.add(frameList.get(i));
            }

            org.bytedeco.opencv.opencv_core.Mat avgImage = (org.bytedeco.opencv.opencv_core.Mat) images.get(0).clone();
            if (avgImage.empty()) {
                System.out.println("Error: avgImage is empty and cannot be used as a reference.");
                return;
            }

            for (int i = 1; i < images.size(); i++) {
                org.bytedeco.opencv.opencv_core.Mat currentImage = (org.bytedeco.opencv.opencv_core.Mat) images.get(i);

                if (currentImage.empty()) {
                    System.out.println("Skipped image: currentImage is empty.");
                    continue;
                }

                if (currentImage.channels() != avgImage.channels()) {
                    opencv_imgproc.cvtColor(currentImage, currentImage, opencv_imgproc.COLOR_BGR2BGRA);
                }

                if (currentImage.depth() != avgImage.depth()) {
                    currentImage.convertTo(currentImage, avgImage.depth());
                }

                if (!currentImage.isContinuous() || !avgImage.isContinuous()) {
                    System.out.println("One of the images is not continuous!");
                }

                opencv_imgproc.resize(currentImage, currentImage, avgImage.size());
                opencv_imgproc.resize(currentImage, currentImage, new Size(avgImage.cols(), avgImage.rows()));

                double alpha = 1.0 / (i + 1);
                double beta = 1.0 - alpha;
                opencv_core.addWeighted(currentImage, alpha, avgImage, beta, 0.0, avgImage);

            }

            for (Rect rect : newFrozenFaces) {
                opencv_imgproc.rectangle(
                        avgImage,
                        rect,
                        new Scalar(0, 255, 0, 0),
                        2,
                        opencv_imgproc.LINE_8,
                        0);
            }

            BufferedImage image = new Java2DFrameConverter()
                    .convert(new OpenCVFrameConverter.ToMat().convert(avgImage));

            JFrame selectionFrame = new JFrame("Select a Face");
            selectionFrame.setSize(image.getWidth(), image.getHeight());
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            selectionFrame.add(imageLabel);
            selectionFrame.setVisible(true);

            double scaleX = (double) image.getWidth() / frameList.get(0).cols();
            double scaleY = (double) image.getHeight() / frameList.get(0).rows();

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();

                    int adjustedX = (int) (x * scaleX);
                    int adjustedY = (int) (y * scaleY);

                    for (Rect rect : newFrozenFaces) {
                        if (adjustedX >= rect.x() && adjustedX <= (rect.x() + rect.width()) &&
                                adjustedY >= rect.y() && adjustedY <= (rect.y() + rect.height())) {
                            Mat selectedFace = new Mat(avgImage, rect);

                            byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);
                            String isRecognized = DatabaseHelper.isFaceRecognized(dbConnection, embedding);
                            if (isRecognized != null && !isRecognized.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Face recognized as \"" + isRecognized + "\"");
                            } else {
                                JOptionPane.showMessageDialog(null, "Face not recognized.");
                            }

                        }
                    }

                    JOptionPane.showMessageDialog(selectionFrame, "No face selected.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Rect> validateFaceRectangles(List<Rect> rectangles) {
        List<Rect> validatedRectangles = new ArrayList<>();

        for (Rect rect : rectangles) {
            boolean shouldAdd = true;

            for (int i = 0; i < validatedRectangles.size(); i++) {
                Rect existingRect = validatedRectangles.get(i);

                if (isFullyContained(rect, existingRect) || calculateIoU(rect, existingRect) > 0.5) {
                    if (calculateArea(rect) > calculateArea(existingRect)) {
                        validatedRectangles.set(i, rect);
                    }
                    shouldAdd = false;
                    break;
                }
            }

            if (shouldAdd) {
                validatedRectangles.add(rect);
            }
        }

        return validatedRectangles;
    }

    private static void updateCameraFeed() {

        try {

            while (true) {
                org.bytedeco.javacv.Frame grabbedFrame = cameraGrabber.grab();
                if (grabbedFrame != null) {
                    Mat frameMatrix = new OpenCVFrameConverter.ToMat().convert(grabbedFrame);
                    FaceProcessor.highlightFaces(frameMatrix, faceDetector);

                    BufferedImage videoFrame = new Java2DFrameConverter().convert(grabbedFrame);
                    SwingUtilities.invokeLater(() -> videoDisplayLabel.setIcon(new ImageIcon(videoFrame)));
                }
                // Thread.sleep(33); // ~30fps
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void captureFace() {
        try {
            org.bytedeco.javacv.Frame frame = cameraGrabber.grab();
            if (frame != null) {
                Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

                List<Rect> detectedFaces = detectFaces(mat);
                System.out.println("detectedFaces.size(): " + detectedFaces.size());

                if (detectedFaces.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "No face detected! Please ensure your face is visible in the camera.");
                    return;
                }

                for (Rect rect : detectedFaces) {
                    opencv_imgproc.rectangle(
                            mat,
                            rect,
                            new Scalar(0, 255, 0, 0),
                            2,
                            opencv_imgproc.LINE_8,
                            0);
                }

                BufferedImage image = new Java2DFrameConverter().convert(new OpenCVFrameConverter.ToMat().convert(mat));

                JFrame selectionFrame = new JFrame("Select a Face");
                selectionFrame.setSize(800, 600);
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                selectionFrame.add(imageLabel);
                selectionFrame.setVisible(true);

                // double scaleX = (double) image.getWidth() / mat.cols();
                // double scaleY = (double) image.getHeight() / mat.rows();

                System.out.println("image.getWidth(): " + image.getWidth());
                System.out.println("image.getHeight(): " + image.getHeight());
                System.out.println("mat.cols() " + mat.cols());
                System.out.println("mat.rows() " + mat.rows());
                double scaleX = (double) mat.cols() / image.getWidth();
                double scaleY = (double) mat.rows() / image.getHeight();

                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        String name = nameTextField.getText().trim();
                        boolean faceSelected = true;
                        System.out.println("image.getWidth(): " + image.getWidth());
                        System.out.println("image.getHeight(): " + image.getHeight());
                        System.out.println("mat.cols() " + mat.cols());
                        System.out.println("mat.rows() " + mat.rows());
                        // int counter = 0;

                        if (name.isEmpty()) {
                            System.out.println("Name cannot be empty");
                            return;
                        }

                        int x = e.getX();
                        int y = e.getY();
                        System.out.println("e.getX()" + e.getX());
                        System.out.println("e.getY()" + e.getY());

                        // int adjustedX = (int) (x / scaleX);
                        // int adjustedY = (int) (y / scaleY);
                        int adjustedX = (int) (x * scaleX);
                        int adjustedY = (int) (y * scaleY);

                        for (Rect rect : detectedFaces) {
                            // System.out.println("rect: " + detectedFaces);
                            System.out.println("adjustedX: " + adjustedX);
                            System.out.println(rect.x());
                            // System.out.println(rect.size());
                            System.out.println("adjustedY: " + adjustedY);
                            System.out.println(rect.y());
                            System.out.println(rect.width());
                            System.out.println(rect.height());
                            System.out.println("detectedFaces.length: " + detectedFaces.size());
                            // if (adjustedX >= rect.x() &&
                            //         adjustedX <= rect.x() + rect.width() &&
                            //         adjustedY >= rect.y() &&
                            //         adjustedY <= rect.y() + rect.height()) {

                            //     faceSelected = true;
                            //     JOptionPane.showMessageDialog(selectionFrame,
                            //             "Face Selected: " + rect);
                            // Mat selectedFace = new Mat(mat, rect);

                            // byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);
                            // byte[] embedding = FaceProcessor.generateEmbedding(new Mat(mat, rect));

                            Mat face = new Mat(); // Assume this is your captured face

                            // File saving
                            String directory = "src/main/resources/happy_faces/faces_test/";
                            // File dir = new File("src/main/resources/happy_faces/");
                            new File(directory).mkdirs(); // Create directory if not exists
                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String filename = directory + "face_" + timestamp + ".jpg";
                            // System.out.println(embedding.length);
                            // if (opencv_imgcodecs.imwrite(filename, mat)) {
                            //     System.out.println("Image saved to " + filename);
                            //     // saveFilePathToDatabase(filename, 1, "Front view"); // Call DB method
                            //     System.out.println("File saved to : " + filename);
                            // }
                            //  else {
                            //     System.err.println("Failed to save image.");
                            // }

                            Mat selectedFace = new Mat(mat, rect); // Extract the face ROI using the selected Rect
                            // byte[] embedding = FaceProcessor.generateEmbedding(new Mat(mat, rect));
                            byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);

                            if (opencv_imgcodecs.imwrite(filename, selectedFace)) {
                                System.out.println("Face image saved to " + filename);
                            } else {
                                System.err.println("Failed to save face image.");
                            }
                            DatabaseHelper.storeEmbedding(dbConnection, embedding, name);
                            JOptionPane.showMessageDialog(null, "Face stored successfully.");
                            selectionFrame.dispose(); // Close the selection window
                            return;
                            // }
                        }

                        if (!faceSelected) {
                            JOptionPane.showMessageDialog(selectionFrame, "No face selected.");
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Rect> detectFaces(Mat frameMatrix) {
        List<Rect> faceRectangles = new ArrayList<>();
        RectVector detectedFaces = new RectVector();

        try {
            Mat grayFrame = new Mat();

            opencv_imgproc.cvtColor(frameMatrix, grayFrame, opencv_imgproc.COLOR_BGR2GRAY);

            // CHARLTON - SLOWS DOWN DETECTION AND CAUSES FALSE NEGATIVES
            // Enhance contrast 
            // opencv_imgproc.equalizeHist(grayFrame, grayFrame);

            faceDetector.detectMultiScale(grayFrame, detectedFaces);

            for (int i = 0; i < detectedFaces.size(); i++) {
                Rect detectedFace = detectedFaces.get(i);

                if (detectedFace.width() > 0 && detectedFace.height() > 0) {
                    boolean shouldAdd = true;

                    for (int k = 0; k < faceRectangles.size(); k++) {
                        Rect existingRect = faceRectangles.get(k);

                        if (isFullyContained(detectedFace, existingRect)
                                || calculateIoU(existingRect, detectedFace) > 0.5) {
                            if (calculateArea(detectedFace) > calculateArea(existingRect)) {
                                faceRectangles.set(k, detectedFace);
                            }
                            shouldAdd = false;
                            break;
                        }
                    }

                    if (shouldAdd) {
                        faceRectangles.add(detectedFace);
                    }
                } else {
                    System.out.println("Invalid face dimensions detected.");
                }
            }

            grayFrame.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return faceRectangles;
    }

    private static boolean isFullyContained(Rect inner, Rect outer) {
        return inner.x() >= outer.x() &&
                inner.y() >= outer.y() &&
                inner.x() + inner.width() <= outer.x() + outer.width() &&
                inner.y() + inner.height() <= outer.y() + outer.height();
    }

    private static int calculateArea(Rect rect) {
        return rect.width() * rect.height();
    }

    private static double calculateIoU(Rect rect1, Rect rect2) {
        int x1 = Math.max(rect1.x(), rect2.x());
        int y1 = Math.max(rect1.y(), rect2.y());
        int x2 = Math.min(rect1.x() + rect1.width(), rect2.x() + rect2.width());
        int y2 = Math.min(rect1.y() + rect1.height(), rect2.y() + rect2.height());

        int intersectionWidth = Math.max(0, x2 - x1);
        int intersectionHeight = Math.max(0, y2 - y1);
        int intersectionArea = intersectionWidth * intersectionHeight;

        int areaRect1 = rect1.width() * rect1.height();
        int areaRect2 = rect2.width() * rect2.height();
        int unionArea = areaRect1 + areaRect2 - intersectionArea;

        return (double) intersectionArea / unionArea;
    }

}