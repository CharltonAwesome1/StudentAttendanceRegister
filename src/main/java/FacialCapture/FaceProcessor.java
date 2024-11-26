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
        Mat gray = new Mat();

        // Convert frame to grayscale
        opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

        // Detect faces
        RectVector detectedFaces = new RectVector();
        faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
        faceDetector.detectMultiScale(gray, detectedFaces);

        // System.out.println("Number of faces detected: " + detectedFaces.size());

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
        face.data().get(embedding);
        return embedding;
    }
}
