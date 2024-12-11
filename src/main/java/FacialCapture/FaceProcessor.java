package FacialCapture;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
// import org.opencv.imgcodecs.Imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
// import org.bytedeco.opencv.global.opencv_imgcodecs;

// import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FaceProcessor {
    public static List<Mat> detectAndSaveFaces(Mat mat, CascadeClassifier faceDetector) {
        List<Mat> faces = new ArrayList<>();
        if (faceDetector == null) {
            return new ArrayList<>();
        }

        Mat gray = new Mat();
        opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

        RectVector detectedFaces = new RectVector();
        try {
            faceDetector.detectMultiScale(gray, detectedFaces);
        } catch (Exception e) {
            return new ArrayList<>();
        }

        for (int i = 0; i < detectedFaces.size(); i++) {
            Rect rect = detectedFaces.get(i);

            opencv_imgproc.rectangle(mat, rect, new Scalar(0, 255, 0, 0), 2, opencv_imgproc.LINE_8, 0);

            Mat face = new Mat(mat, rect);
            Mat resizedFace = new Mat();
            opencv_imgproc.resize(face, resizedFace, new Size(128, 128));
            faces.add(resizedFace);
        }

        faceDetector.close();
        return faces;
    }

    public static byte[] generateEmbedding(Mat face) {
        byte[] embedding = new byte[(int) (face.total() * face.elemSize())];
        face.data().get(embedding);
        return embedding;
    }

    public static void highlightFaces(Mat mat, CascadeClassifier faceDetector) {

        if (mat == null || faceDetector == null) {
            System.out.println("mat or faceDetector is null");
            return;
        }

        Mat gray = new Mat();
        try {
            opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
        } catch (Exception ex) {
            System.out.println("Error in grayscale conversion: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");

        if (faceDetector.empty()) {
            faceDetector.close();
            System.out.println("CascadeClassifier failed to load.");
            return;
        }

        RectVector detectedFaces = new RectVector();
        try {
            faceDetector.detectMultiScale(gray, detectedFaces);
            faceDetector.close();
        } catch (Exception e) {
            faceDetector.close();
            System.out.println("Error in detectMultiScale: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < detectedFaces.size(); i++) {
            Rect rect = detectedFaces.get(i);
            opencv_imgproc.rectangle(mat, rect, new Scalar(0, 255, 0, 0), 2, opencv_imgproc.LINE_8, 0);
        }
    }

}
