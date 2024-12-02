// package FacialCapture;

// // import org.bytedeco.opencv.opencv_core.*;
// import org.bytedeco.opencv.global.opencv_imgproc;
// import org.bytedeco.opencv.opencv_core.Rect;
// import org.bytedeco.opencv.opencv_core.RectVector;
// import org.bytedeco.opencv.opencv_core.Size;
// // import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
// // import org.opencv.objdetect.CascadeClassifier;
// import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;  // Use this for JavaCV bindings

// import org.bytedeco.opencv.opencv_core.Mat;

// import java.util.ArrayList;
// import java.util.List;

// public class FaceProcessor {
//     public static List<Mat> detectAndSaveFaces(Mat mat, CascadeClassifier faceDetector) {
//         List<Mat> faces = new ArrayList<>();
//         Mat gray = new Mat();
//         System.out.println("detectAndSaveFaces one");

//         // Convert frame to grayscale
//         opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

//         // Detect faces
//         RectVector detectedFaces = new RectVector();
//         faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
//         System.out.println("detectAndSaveFaces two");
//         faceDetector.detectMultiScale(gray, detectedFaces);
//         System.out.println(" detectAndSaveFaces three");
//         System.out.println(detectedFaces.size());

//         // Extract and resize face regions
//         for (int i = 0; i < detectedFaces.size(); i++) {
//             Rect rect = detectedFaces.get(i);
//             Mat face = new Mat(mat, rect);
//             Mat resizedFace = new Mat();
//             opencv_imgproc.resize(face, resizedFace, new Size(128, 128));
//             faces.add(resizedFace);
//         }
//         faceDetector.close();
//         return faces;
//     }

//     public static byte[] generateEmbedding(Mat face) {
//         // Flatten and normalize face matrix (placeholder)
//         byte[] embedding = new byte[(int) (face.total() * face.elemSize())];
//         face.data().get(embedding);
//         return embedding;
//     }
// }

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

        // System.out.println("Number of faces detected: " + detectedFaces.size());

        // Draw rectangles around detected faces and extract face regions
        for (int i = 0; i < detectedFaces.size(); i++) {
            Rect rect = detectedFaces.get(i);
            // System.out.println("mat and rect");
            // System.out.println(mat);
            // System.out.println(rect);

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

        // if (gray.empty()) {
        //     System.out.println("Gray image is empty after conversion.");
        //     return;
        // }

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

        // System.out.println("Number of faces detected: " + detectedFaces.size());
        // if (detectedFaces.size() == 0) {
        //     System.out.println("No faces detected");
        // }

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

    // public static void highlightFaces(Mat mat, CascadeClassifier faceDetector) {
    // System.out.println("highlightFaces highlightFaceshighlightFaces");
    // System.out.println("mat==null: " + mat == null);
    // System.out.println("faceDetector==null: " + faceDetector == null);
    // if (mat == null && faceDetector == null) {
    // return;
    // }
    // Mat gray = new Mat();
    // // if (gray == null){
    // // return;
    // // }
    // System.out.println("after mat");

    // // Convert frame to grayscale
    // try {

    // opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
    // System.out.println("after opencv_imgproc.cvtColor");
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }
    // if (mat == null) {
    // System.out.println("matt is null");
    // return;
    // }

    // // Detect faces
    // RectVector detectedFaces = new RectVector();
    // // if (faceDetector == null) {
    // // return;
    // // }
    // try {
    // System.out.println("before detect multiscale");

    // faceDetector.detectMultiScale(gray, detectedFaces);
    // System.out.println("after detect multiscale");

    // } catch (Exception e) {
    // e.printStackTrace();
    // return;
    // }
    // // faceDetector.detectMultiScale(gray, detectedFaces);
    // if (faceDetector.isNull()) {
    // System.out.println("face detector is null");
    // }
    // System.out.println("After face detector is null");

    // System.out.println("detectedFaces == null: " + (detectedFaces == null));
    // // Draw rectangles around detected faces
    // for (int i = 0; i < detectedFaces.size(); i++) {
    // Rect rect = detectedFaces.get(i);
    // // System.out.println("detectedFaces.get(i) 2: " +
    // // detectedFaces.get(i).getPointer().);
    // // Draw green rectangle on the original frame
    // opencv_imgproc.rectangle(
    // mat, // Input image
    // rect, // Rectangle to draw
    // new Scalar(0, 255, 0, 0), // Color (Green)
    // 2, // Thickness
    // opencv_imgproc.LINE_8, // Line type
    // 0 // Shift
    // );
    // }
    // }

}
