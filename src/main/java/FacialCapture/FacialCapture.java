package FacialCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
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
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_highgui;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

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
    private static JButton buttonFour;
    private static JButton compareFacesButton;

    public static void main(String[] args) {
        try {
            System.out.println("Initializing database connection...");
            dbConnection = DatabaseHelper.connect();
            System.out.println("Database connection established.");

            System.out.println("Initializing camera grabber...");
            System.out.println(OpenCVFrameGrabber.list);
            cameraGrabber = new OpenCVFrameGrabber(1);
            System.out.println(OpenCVFrameGrabber.getDefault());
            cameraGrabber.start();
            System.out.println("Camera grabber initialized.");
            System.out.println(cameraGrabber.getFrameRate());
            System.out.println(cameraGrabber.getImageWidth());
            System.out.println(cameraGrabber.getImageHeight());

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
        nameLabel.setPreferredSize(new Dimension(150, 30));
        nameTextField = new JTextField();
        nameTextField.setPreferredSize(new Dimension(200, 30));
        captureButton = new JButton("Capture Face");
        recognizeButton = new JButton("Recognize Face");
        buttonThree = new JButton("buttonThree");
        buttonFour = new JButton("buttonFour");
        compareFacesButton = new JButton("Find Face");
        buttonPanel.add(nameTextField);
        buttonPanel.add(captureButton);
        buttonPanel.add(recognizeButton);
        buttonPanel.add(compareFacesButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        captureButton.addActionListener(e -> captureFace());
        recognizeButton.addActionListener(e -> recognizeFaceTwo());
        buttonThree.addActionListener(e -> buttonThreeMethod());
        buttonFour.addActionListener(e -> buttonFourMethod());
        compareFacesButton.addActionListener(e -> compareCaptureFaceToSavedImages());

        new Thread(FacialCapture::updateCameraFeed).start();

        frame.setVisible(true);

    }

    private static void compareCaptureFaceToSavedImages() {

        try {
            org.bytedeco.javacv.Frame frame = cameraGrabber.grab();
            if (frame != null) {
                Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

                Mat originalMat = mat.clone(); // Preserve the original mat
                List<Rect> detectedFaces = detectFaces(originalMat); // Pass a clone if detectFaces modifies the input

                if (detectedFaces.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "No face detected! Please ensure your face is visible in the camera.");
                    return;
                }

                for (Rect rect : detectedFaces) {
                    opencv_imgproc.rectangle(mat, rect, new Scalar(0, 255, 0, 0), 2, opencv_imgproc.LINE_8, 0);
                }

                BufferedImage image = new Java2DFrameConverter().convert(new OpenCVFrameConverter.ToMat().convert(mat));

                JFrame selectionFrame = new JFrame("Select a Face");
                selectionFrame.setSize(image.getWidth(), image.getHeight());
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                selectionFrame.add(imageLabel);
                selectionFrame.setVisible(true);

                double scaleX = (double) mat.cols() / image.getWidth();
                double scaleY = (double) mat.rows() / image.getHeight();

                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                        int entriesFoundCounter = 0;
                        int facesLoopCounter = 0;
                        int x, y, adjustedX, adjustedY;
                        List<Mat> images = new ArrayList<>();
                        List<RetrievedData> retrievedDatas = new ArrayList<>();

                        File dir = new File("src/main/resources/happy_faces/faces_test/");
                        File[] files = dir.listFiles((d, fileName) -> fileName.toLowerCase().endsWith(".jpg"));
                        System.out.println("dir: " + dir.getAbsolutePath());
                        System.out.println("files.length: " + files.length);

                        if (files == null || files.length == 0) {
                            System.out.println("No .jpg images found in the specified folder.");
                            return;
                        }

                        for (File file : files) {
                            System.out.println("Reading file: " + file.getAbsolutePath());
                            Mat img = opencv_imgcodecs.imread(file.getAbsolutePath(), 1);
                            if (!img.empty()) {
                                images.add(img);
                                retrievedDatas.add(new RetrievedData(img, file.getName()));
                            }
                        }

                        if (images.isEmpty()) {
                            System.out.println("No valid images found.");
                            return;
                        }

                        x = e.getX();
                        y = e.getY();
                        adjustedX = (int) (x * scaleX);
                        adjustedY = (int) (y * scaleY);

                        System.out.println("Mouse clicked at: (" + e.getX() + ", " + e.getY() + ")");
                        System.out.println("Scaling factors: scaleX=" + scaleX + ", scaleY=" + scaleY);
                        System.out.println("Adjusted coordinates: adjustedX=" + adjustedX + ", adjustedY=" + adjustedY);

                        for (Rect rect : detectedFaces) {
                            System.out.println("Detected face: x=" + rect.x() + ", y=" + rect.y() +
                                    ", width=" + rect.width() + ", height=" + rect.height());
                            System.out.println("Face region: x=" + rect.x() + "-" + (rect.x() + rect.width()) +
                                    ", y=" + rect.y() + "-" + (rect.y() + rect.height()));
                        }

                        for (Rect rect : detectedFaces) {

                            if ((adjustedX >= rect.x() &&
                                    adjustedX <= rect.x() + rect.width() &&
                                    adjustedY >= rect.y() &&
                                    adjustedY <= rect.y() + rect.height()) == false) {
                                facesLoopCounter++;
                                continue;
                            }

                            // opencv_imgproc.rectangle(mat, rect, new Scalar(255, 0, 0, 0), 3, opencv_imgproc.LINE_8, 0);
                            Mat selectedFace = new Mat(originalMat, rect);
                            // Mat selectedFace = new Mat(mat, rect);
                            // System.out.println(mat.);
                            Mat resizedFace = new Mat();
                            opencv_imgproc.resize(selectedFace, resizedFace, new Size(128, 128));
                            byte[] capturedEmbedding = FaceProcessor.generateEmbedding(resizedFace);

                            // for (int i = 0; i < images.size(); i++) {
                            for (int i = 0; i < retrievedDatas.size(); i++) {

                                String fileCapturedFaceName = retrievedDatas.get(i).fileName.contains("-")
                                        ? retrievedDatas.get(i).fileName
                                                .substring(retrievedDatas.get(i).fileName
                                                        .lastIndexOf("-") + 1)
                                        : retrievedDatas.get(i).fileName;
                                fileCapturedFaceName = fileCapturedFaceName.substring(0,
                                        fileCapturedFaceName.length() - 4);
                                Mat currentImage = retrievedDatas.get(i).image;

                                byte[] embedding = FaceProcessor.generateEmbedding(currentImage);

                                double distance = DatabaseHelper.calculateDistance(embedding, capturedEmbedding);
                                double similarity = DatabaseHelper.calculateCosineSimilarity(embedding,
                                        capturedEmbedding);

                                if (distance < 150 && similarity > 0.5) {
                                    entriesFoundCounter++;
                                    String recognizedName = String.format("Face recognized as %s",
                                            fileCapturedFaceName);
                                    System.out.println("Name: " + fileCapturedFaceName
                                            + ",Cosine similarity: " + similarity
                                            + ",distance: " + distance);
                                    JOptionPane.showMessageDialog(null, recognizedName);
                                }
                            }

                        }

                        if (facesLoopCounter == detectedFaces.size()) {
                            JOptionPane.showMessageDialog(null, "Click inside a rectangle to scan face!", null, 2);
                        } else if (entriesFoundCounter > 1) {
                            JOptionPane.showMessageDialog(null, "Multiple entries found!", null, 0);
                        } else if (entriesFoundCounter == 0) {
                            JOptionPane.showMessageDialog(null, "No entries found!", null, 2);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void buttonFourMethod() {

        File dir = new File("src/main/resources/happy_faces/faces_test/");
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg"));
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

        // Mat avgImage = images.get(0);

        // for (int i = 1; i < images.size(); i++) {
        //     Mat currentImage = images.get(i);
        //     double alpha = 1.0 / (i + 1);
        //     double beta = 1.0 - alpha;

        //     opencv_core.addWeighted(currentImage, alpha, avgImage, beta, 0.0, avgImage);
        // }

        // opencv_imgcodecs.imwrite(dir + "avg_happy_face.png", avgImage);

        // Mat resultImage = opencv_imgcodecs.imread(dir + "avg_happy_face.png");

        // opencv_highgui.imshow("Average Happy Face", resultImage);
        // opencv_highgui.waitKey(0);
        // opencv_highgui.destroyAllWindows();

        Mat baseImageForCompareOne = images.get(1);
        byte[] embeddingTwo = FaceProcessor.generateEmbedding(baseImageForCompareOne);

        for (int i = 0; i < images.size(); i++) {
            Mat currentImage = images.get(i);
            // double alpha = 1.0 / (i + 1);
            // double beta = 1.0 - alpha;
            // BufferedImage image = new Java2DFrameConverter()
            //         .convert(new OpenCVFrameConverter.ToMat().convert(currentImage));

            // JFrame selectionFrame = new JFrame("Select a Face");
            // selectionFrame.setSize(image.getWidth(), image.getHeight());
            // selectionFrame.setLocationRelativeTo(null);
            // JLabel imageLabel = new JLabel(new ImageIcon(image));
            // selectionFrame.add(imageLabel);
            // selectionFrame.setVisible(true);
            // JOptionPane.showMessageDialog(null, image, null, i);
            // Mat selectedFace = new Mat(image);

            byte[] embedding = FaceProcessor.generateEmbedding(currentImage);

            double distance = DatabaseHelper.calculateDistance(embedding, embeddingTwo);
            double similarity = DatabaseHelper.calculateCosineSimilarity(embedding, embeddingTwo);

            // System.out.println(name);
            System.out.println("Cosine similarity: " + similarity);
            System.out.println("distance: " + distance);
            // System.out.println((similarity > 0.4) + " " + (distance < 150));
            System.out.println();

            // System.out.println(embedding.length);
            // System.out.println(embedding.);
            // opencv_core.addWeighted(currentImage, alpha, avgImage, beta, 0.0, avgImage);
        }

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
            // System.out.println("Before loop: " + System.currentTimeMillis());

            while (System.currentTimeMillis() - startTime < 1000) {
            // while (System.currentTimeMillis() - startTime < 500) {
                org.bytedeco.javacv.Frame grabbedFrame = cameraGrabber.grab();
                if (grabbedFrame != null) {
                    Mat frameMatrix = new OpenCVFrameConverter.ToMat().convert(grabbedFrame);
                    frameList.add(frameMatrix);
                }
                // Thread.sleep(1); 
            }
            // System.out.println("After loop: " + System.currentTimeMillis());

            List<Rect> frozenFaces = new ArrayList<>();
            for (Mat frameMatrix : frameList) {
                if (frameMatrix.empty()) {
                    System.out.println("Skipped an empty frame.");
                    continue;
                }
                List<Rect> detectedFaces = detectFacesNoValidation(frameMatrix);

                for (Rect rect : detectedFaces) {
                    frozenFaces.add(new Rect(rect.x(), rect.y(), rect.width(), rect.height()));
                }
            }

            // System.out.println("After frozenFaces: " + System.currentTimeMillis());
            // System.out.println("frozenFaces.size(): " + frozenFaces.size());

            // System.out.println("outside");

            List<Rect> newFrozenFaces = validateFaceRectangles(frozenFaces);
            // System.out.println("newFrozenFaces.size(): " + newFrozenFaces.size());

            if (newFrozenFaces.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No face detected! Please ensure your face is visible in the camera.");
                return;
            }

            List<Mat> images = new ArrayList<>();

            // System.out.println("frameList.size(): " + frameList.size());
            for (int i = 0; i < frameList.size(); i++) {
                images.add(frameList.get(i));
            }

            // System.out.println("After frameList.size: " + System.currentTimeMillis());

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

            // System.out.println("After images.size: " + System.currentTimeMillis());

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

            // long endTime = System.currentTimeMillis();
            // System.out.println("Duration:" + (endTime - startTime));
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
                Mat originalMat = mat.clone(); // Preserve the original mat
                List<Rect> detectedFaces = detectFaces(originalMat); // Pass a clone if detectFaces modifies the input
                System.out.println("detectedFaces.size(): " + detectedFaces.size());

                if (detectedFaces.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "No face detected! Please ensure your face is visible in the camera.");
                    return;
                }

                for (Rect rect : detectedFaces) {
                    opencv_imgproc.rectangle(mat, rect, new Scalar(0, 255, 0, 0), 2, opencv_imgproc.LINE_8, 0);
                }

                BufferedImage image = new Java2DFrameConverter().convert(new OpenCVFrameConverter.ToMat().convert(mat));

                JFrame selectionFrame = new JFrame("Select a Face");
                // selectionFrame.setSize(800, 600);
                selectionFrame.setSize(image.getWidth(), image.getHeight());
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                selectionFrame.add(imageLabel);
                selectionFrame.setVisible(true);

                double scaleX = (double) mat.cols() / image.getWidth();
                double scaleY = (double) mat.rows() / image.getHeight();

                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        String name = nameTextField.getText().trim();
                        boolean faceSelected = true;

                        if (name.isEmpty()) {
                            System.out.println("Name cannot be empty");
                            return;
                        }

                        int x = e.getX();
                        int y = e.getY();

                        int adjustedX = (int) (x * scaleX);
                        int adjustedY = (int) (y * scaleY);
                        System.out.println("Mouse clicked at: (" + e.getX() + ", " + e.getY() + ")");
                        System.out.println("Scaling factors: scaleX=" + scaleX + ", scaleY=" + scaleY);
                        System.out.println("Adjusted coordinates: adjustedX=" + adjustedX + ", adjustedY=" + adjustedY);

                        for (Rect rect : detectedFaces) {
                            System.out.println("Detected face: x=" + rect.x() + ", y=" + rect.y() +
                                    ", width=" + rect.width() + ", height=" + rect.height());
                            System.out.println("Face region: x=" + rect.x() + "-" + (rect.x() + rect.width()) +
                                    ", y=" + rect.y() + "-" + (rect.y() + rect.height()));
                        }

                        for (Rect rect : detectedFaces) {

                            if ((adjustedX >= rect.x() &&
                                    adjustedX <= rect.x() + rect.width() &&
                                    adjustedY >= rect.y() &&
                                    adjustedY <= rect.y() + rect.height()) == false) {
                                continue;
                            }

                            // File saving
                            String directory = "src/main/resources/happy_faces/faces_test/";
                            new File(directory).mkdirs();
                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String filename = directory + "face_" + timestamp + "-" + name + ".jpg";
                            // System.out.println(embedding.length);
                            // if (opencv_imgcodecs.imwrite(filename, mat)) {
                            //     System.out.println("Image saved to " + filename);
                            //     // saveFilePathToDatabase(filename, 1, "Front view"); // Call DB method
                            //     System.out.println("File saved to : " + filename);
                            // }
                            //  else {
                            //     System.err.println("Failed to save image.");
                            // }

                            Mat selectedFace = new Mat(originalMat, rect); // Extract the face ROI using the selected Rect
                            // byte[] embedding = FaceProcessor.generateEmbedding(new Mat(mat, rect));
                            // byte[] embedding = FaceProcessor.generateEmbedding(selectedFace);
                            Mat resizedFace = new Mat();
                            opencv_imgproc.resize(selectedFace, resizedFace, new Size(128, 128));
                            // if (opencv_imgcodecs.imwrite(filename, selectedFace)) {
                            if (opencv_imgcodecs.imwrite(filename, resizedFace)) {
                                System.out.println("Face image saved to " + filename);
                            } else {
                                System.err.println("Failed to save face image.");
                            }
                            // DatabaseHelper.storeEmbedding(dbConnection, embedding, name);
                            JOptionPane.showMessageDialog(null, "Face stored successfully.");
                            nameTextField.setText("");
                            selectionFrame.dispose(); // Close the selection window
                            // return;
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

    private static List<Rect> detectFacesNoValidation(Mat frameMatrix) {
        List<Rect> faceRectangles = new ArrayList<>();
        RectVector detectedFaces = new RectVector();

        try {
            Mat grayFrame = new Mat();
            // System.out.println("faceDetector.detectMultiScale 1 : " + System.currentTimeMillis());
            opencv_imgproc.cvtColor(frameMatrix, grayFrame, opencv_imgproc.COLOR_BGR2GRAY);
            // System.out.println("faceDetector.detectMultiScale 2 : " + System.currentTimeMillis());
            faceDetector.detectMultiScale(grayFrame, detectedFaces);
            // System.out.println("faceDetector.detectMultiScale 3 : " + System.currentTimeMillis());

            for (int i = 0; i < detectedFaces.size(); i++) {
                Rect detectedFace = detectedFaces.get(i);

                if (detectedFace.width() > 0 && detectedFace.height() > 0) {
                    boolean shouldAdd = true;

                    if (shouldAdd) {
                        faceRectangles.add(new Rect(detectedFace));
                    }
                } else {
                    System.out.println("Invalid face dimensions detected.");
                }
            }
            // System.out.println("faceDetector.detectMultiScale 4 : " + System.currentTimeMillis());
            

            grayFrame.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return faceRectangles;
    }

    private static List<Rect> detectFaces(Mat frameMatrix) {
        List<Rect> faceRectangles = new ArrayList<>();
        RectVector detectedFaces = new RectVector();

        try {
            Mat grayFrame = new Mat();

            opencv_imgproc.cvtColor(frameMatrix, grayFrame, opencv_imgproc.COLOR_BGR2GRAY);

            // CHARLTON - SLOWS DOWN DETECTION AND CAUSES FALSE NEGATIVES
            // Enhance contrast 
            opencv_imgproc.equalizeHist(grayFrame, grayFrame);

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
                        // System.out.println("detectedFace.width() : " + detectedFace.width());
                        // System.out.println("detectedFace.height(): " + detectedFace.height());
                        // faceRectangles.add(detectedFace);
                        faceRectangles.add(new Rect(detectedFace));
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

    public static class RetrievedData {
        Mat image;
        String fileName;

        public RetrievedData() {

        }

        public RetrievedData(Mat image, String fileName) {
            this.image = image;
            this.fileName = fileName;
        }
    }

}
