package FacialCapture;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

public class FaceProcessor {
    public static List<Mat> detectAndSaveFaces(Mat mat, CascadeClassifier faceDetector) {
        List<Mat> faces = new ArrayList<>();
        if (faceDetector == null) {
            return null;
        }

        Mat gray = new Mat();
        // Convert frame to grayscale
        opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

        // Detect faces
        RectVector detectedFaces = new RectVector();
        // faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
        try {
            faceDetector.detectMultiScale(gray, detectedFaces);
        } catch (Exception e) {
            return null;
        }


        // Draw rectangles around detected faces and extract face regions
        for (int i = 0; i < detectedFaces.size(); i++) {
            Rect rect = detectedFaces.get(i);

            // Draw rectangle on the original frame
            opencv_imgproc.rectangle(
                    mat, // Input image
                    rect, // Rectangle to draw
                    new Scalar(0, 255, 0, 0), // Color (Green)
                    2, // Thickness
                    opencv_imgproc.LINE_8, // Line type
                    0 // Shift
            );

            // Extract face region and resize
            Mat face = new Mat(mat, rect);
            Mat resizedFace = new Mat();
            opencv_imgproc.resize(face, resizedFace, new Size(128, 128));
            faces.add(resizedFace);
        }

        faceDetector.close();
        return faces;
    }

    public static byte[] generateEmbedding(Mat face) {
        // Flatten and normalize face matrix (placeholder)
        byte[] embedding = new byte[(int) (face.total() * face.elemSize())];
        System.out.println(embedding.length);
        face.data().get(embedding);
        return embedding;
    }

    public static void highlightFaces(Mat mat, CascadeClassifier faceDetector) {
        // System.out.println("highlightFaces - Start");

        // Check if the input image or faceDetector is null
        if (mat == null || faceDetector == null) {
            System.out.println("mat or faceDetector is null");
            return;
        }

        Mat gray = new Mat();
        try {
            // Convert frame to grayscale
            opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
            // System.out.println("Grayscale conversion successful");
        } catch (Exception ex) {
            System.out.println("Error in grayscale conversion: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        // Check if the CascadeClassifier is loaded properly
        faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");

        if (faceDetector.empty()) {
            System.out.println("CascadeClassifier failed to load.");
            return;
        }

        // Initialize the RectVector to store detected faces
        RectVector detectedFaces = new RectVector();
        try {
            // System.out.println("Before detectMultiScale");
            faceDetector.detectMultiScale(gray, detectedFaces);
            // System.out.println("After detectMultiScale");

        } catch (Exception e) {
            System.out.println("Error in detectMultiScale: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        // Draw rectangles around detected faces
        for (int i = 0; i < detectedFaces.size(); i++) {
            Rect rect = detectedFaces.get(i);
            // System.out.println("Detected face at: " + rect.toString());
            opencv_imgproc.rectangle(
                    mat, // Input image
                    rect, // Rectangle to draw
                    new Scalar(0, 255, 0, 0), // Color (Green)
                    2, // Thickness
                    opencv_imgproc.LINE_8, // Line type
                    0 // Shift
            );
        }
    }

}
