// // // package FacialCapture;

// // // import org.bytedeco.javacv.*;
// // // import org.bytedeco.opencv.opencv_core.*;
// // // import org.bytedeco.opencv.global.opencv_imgproc;
// // // import org.bytedeco.opencv.global.opencv_videoio;
// // // import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
// // // import org.bytedeco.opencv.opencv_videoio.VideoCapture;

// // // public class App {
// // //     public static void main(String[] args) {

// // //         System.out.println("This is a test");
// // //         // if (true){
// // //         // System.exit(0);
// // //         // }
// // //         // VideoCapture camera = new VideoCapture(0); // Open camera
// // //         // if (!camera.isOpened()) {
// // //         //     System.out.println("Error: Camera not accessible");
// // //         // } else {
// // //         //     System.out.println("Camera opened successfully");
// // //         //     System.exit(0);
// // //         //     // Add your processing logic here
// // //         // }
// // //         try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
// // //             System.out.println("inside");
// // //             // grabber.setVideoOption("backend", "DSHOW");
// // //             grabber.setVideoOption("backend", "FFMPEG");
// // //             grabber.start();
// // //             System.out.println("one");
// // //             // Load a pre-trained face detection model
// // //             CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
// // //             System.out.println("second one");

// // //             CanvasFrame canvas = new CanvasFrame("Facial Capture", CanvasFrame.getDefaultGamma() / grabber.getGamma());
// // //             System.out.println("third one");

// // //             while (canvas.isVisible()) {
// // //                 Frame frame = grabber.grab();
// // //                 System.out.println("frame == null: " + (frame == null));
// // //                 if (frame != null) {
// // //                     Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
// // //                     System.out.println("two");

// // //                     // Convert to grayscale for face detection
// // //                     Mat gray = new Mat();
// // //                     opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
// // //                     System.out.println("three");

// // //                     System.out.println(gray);
// // //                     // Detect faces
// // //                     System.out.println("three one");
// // //                     RectVector faces = new RectVector();
// // //                     System.out.println("three two");
// // //                     System.out.println(faces);
// // //                     System.out.println("three three");
// // //                     faceDetector.detectMultiScale(gray, faces);
// // //                     System.out.println("four");
// // //                     System.out.println("faces.size(): " + faces.size());

// // //                     // Draw rectangles around detected faces
// // //                     for (int i = 0; i < faces.size(); i++) {
// // //                         Rect face = faces.get(i);
// // //                         opencv_imgproc.rectangle(mat, face, Scalar.RED);
// // //                     }

// // //                     // Show the frame with detected faces
// // //                     canvas.showImage(new OpenCVFrameConverter.ToMat().convert(mat));
// // //                 }
// // //             }
// // //             // canvas.

// // //             grabber.stop();
// // //             canvas.dispose();
// // //         } catch (Exception e) {
// // //             e.printStackTrace();
// // //         }
// // //     }
// // // }

// // // package FacialCapture;

// // // import org.bytedeco.javacv.*;
// // // import org.bytedeco.opencv.opencv_core.*;
// // // import org.bytedeco.opencv.global.opencv_imgproc;
// // // import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
// // // import java.sql.Connection;
// // // import java.util.List;

// // // public class App {
// // //     public static void main(String[] args) {
// // //         System.out.println("Starting FacialCapture application...");

// // //         try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
// // //             grabber.start();
// // //             System.out.println("Camera started");

// // //             CascadeClassifier faceDetector = new CascadeClassifier(
// // //                     App.class.getResource("/haarcascade_frontalface_alt.xml").getPath());

// // //             if (faceDetector.empty()) {
// // //                 faceDetector.close();
// // //                 throw new RuntimeException("Failed to load face detector.");
// // //             }

// // //             CanvasFrame canvas = new CanvasFrame("Facial Capture");
// // //             Connection dbConnection = DatabaseHelper.connect();

// // //             while (canvas.isVisible()) {
// // //                 Frame frame = grabber.grab();
// // //                 if (frame != null) {
// // //                     Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
// // //                     List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);

// // //                     // Save faces to the database
// // //                     for (Mat face : faces) {
// // //                         byte[] embedding = FaceProcessor.generateEmbedding(face);
// // //                         DatabaseHelper.storeEmbedding(dbConnection, embedding);
// // //                     }

// // //                     canvas.showImage(new OpenCVFrameConverter.ToMat().convert(mat));
// // //                 }
// // //             }

// // //             grabber.stop();
// // //             canvas.dispose();
// // //             dbConnection.close();
// // //         } catch (RuntimeException e) {
// // //             e.printStackTrace();

// // //         } catch (Exception e) {
// // //             e.printStackTrace();
// // //         }
// // //     }
// // // }
// // package FacialCapture;

// // import org.bytedeco.opencv.opencv_core.*;
// // import org.bytedeco.opencv.opencv_dnn.Net;
// // import org.opencv.core.CvType;
// // import org.bytedeco.opencv.global.opencv_dnn;
// // import org.bytedeco.javacv.*;

// // import javax.swing.*;
// // import java.awt.*;
// // import java.awt.image.BufferedImage;
// // import java.io.File;
// // import java.sql.Connection;
// // import java.util.List;

// // public class App {
// //     private static Connection dbConnection;
// //     private static Net faceRecognitionModel;
// //     private static OpenCVFrameGrabber grabber;
// //     private static JLabel imageLabel;
// //     private static JTextField nameField;
// //     private static Net faceNet;

// //     public static void main(String[] args) {
// //         // Initialize resources
// //         String outputDir = "captured_faces/";
// //         new File(outputDir).mkdirs();

// //         try {

// //             // dbConnection = DatabaseHelper.connect();
// //             grabber = new OpenCVFrameGrabber(0);
// //             grabber.start();

// //             System.out.println("one");
// //             String modelPath;
// //             File modelFile = new File("models\\facenet.pb");
// //             if (!modelFile.exists()) {
// //                 System.out.println("Model file not found!");
// //             }
// //             modelPath = "models\\facenet.pb";
// //             // modelPath = "/src/models/20180402-114759.pb";
// //             // modelPath = "/models/20180402-114759.pb";
// //             // Load the pre-trained deep learning model (e.g., FaceNet or OpenFace)
// //             // String modelPath = "models/20180402-114759.pb"; // Adjust with actual model
// //             // path
// //             // String modelPath = "models/20180402-114759.pb"; // Adjust with actual model
// //             // path
// //             System.out.println("two");
// //             faceRecognitionModel = opencv_dnn.readNetFromTensorflow(
// //                 modelPath, 
// //             "models\\opencv_face_detector.pbtxt");
// //             System.out.println("three");

// //             for (int i = 0; i < faceRecognitionModel.getLayerNames().size(); i++) {
// //                 System.out.println(faceRecognitionModel.getLayerNames().get(i));
// //             }

// //             if (faceRecognitionModel.empty()) {
// //                 throw new RuntimeException("Failed to load face recognition model.");
// //             }
// //         } catch (Exception e) {
// //             e.printStackTrace();
// //             return;
// //         }

// //         // Build GUI
// //         JFrame frame = new JFrame("Facial Recognition");
// //         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// //         frame.setSize(800, 600);

// //         imageLabel = new JLabel();
// //         frame.add(imageLabel, BorderLayout.CENTER);

// //         JPanel buttonPanel = new JPanel();
// //         nameField = new JTextField(20);
// //         JButton captureButton = new JButton("Capture Face");
// //         JButton recognizeButton = new JButton("Recognize Face");

// //         buttonPanel.add(new JLabel("Name:"));
// //         buttonPanel.add(nameField);
// //         buttonPanel.add(captureButton);
// //         buttonPanel.add(recognizeButton);
// //         frame.add(buttonPanel, BorderLayout.SOUTH);

// //         // Add button actions
// //         captureButton.addActionListener(e -> {
// //             String name = nameField.getText().trim(); // Get input value
// //             if (name.isEmpty()) {
// //                 JOptionPane.showMessageDialog(null, "Please enter a name before capturing a face.");
// //             } else {
// //                 captureFace(name);
// //             }
// //         });
// //         recognizeButton.addActionListener(e -> recognizeFace());

// //         // Start updating the camera feed
// //         new Thread(App::updateCameraFeed).start();

// //         frame.setVisible(true);
// //     }

// //     private static void updateCameraFeed() {
// //         try {
// //             while (true) {
// //                 org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this class
// //                 if (frame != null) {
// //                     BufferedImage image = new Java2DFrameConverter().convert(frame);
// //                     SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
// //                 }
// //             }
// //         } catch (Exception e) {
// //             e.printStackTrace();
// //         }
// //     }

// //     private static void captureFace(String name) {
// //         try {
// //             org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this class
// //             Mat image = new OpenCVFrameConverter.ToMat().convert(frame);
// //             Mat blob = new Mat();
// //             File modelFile = new File("models\\facenet.pb");
// //             if (!modelFile.exists()) {
// //                 System.out.println("Model file not found!");
// //             }
// //             String modelPath = "/models/facenet.pb";
// //             faceRecognitionModel = opencv_dnn.readNetFromTensorflow("models\\facenet.pb");
// //             System.out.println(faceRecognitionModel == null);
// //             opencv_dnn.blobFromImage(image, blob, 1.0, new Size(224, 224), new Scalar(104, 177, 123, 0), true, false,
// //                     CvType.CV_32F);
// //             // Specify the correct input layer name
// //             faceRecognitionModel.setInput(blob, "image_batch", 1.0, new Scalar(104.0, 177.0, 123.0, 0.0));

// //             // Set the input to the network
// //             // faceNet.setInput(blob, "image_batch");
// //             faceNet.setInput(blob);
// //             // faceRecognitionModel.setInput(blob, "image_batch", 1.0, new Scalar(104.0,
// //             // 177.0, 123.0, 0.0));
// //             for (int i = 0; i < faceRecognitionModel.getLayerNames().size(); i++) {
// //                 System.out.println(faceRecognitionModel.getLayerNames().get(i));
// //             }
// //             // Run forward pass to get the embeddings
// //             Mat result = faceNet.forward();
// //             System.out.println(result);

// //             if (frame != null) {
// //                 Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
// //                 List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, null); // Detect faces (Haar can still be used)

// //                 // Process each detected face using the deep learning model for embedding
// //                 for (Mat face : faces) {
// //                     byte[] embedding = generateEmbeddingUsingModel(face); // Use pre-trained model to generate embedding
// //                     DatabaseHelper.storeEmbedding(dbConnection, embedding, name); // Pass name for storage
// //                     System.out.println("Face captured and stored for: " + name);
// //                 }

// //                 JOptionPane.showMessageDialog(null, "Face captured and stored successfully.");
// //             }
// //         } catch (Exception e) {
// //             e.printStackTrace();
// //         }
// //     }

// //     private static byte[] generateEmbeddingUsingModel(Mat face) {
// //         try (// Convert Mat to blob for input into the deep learning model
// //              // Mat inputBlob = opencv_dnn.blobFromImage(face, 1.0, new Size(224, 224), new
// //              // Scalar(104, 177, 123, 0), true, false);
// //              // Mat inputBlob = opencv_dnn.blobFromImage(face, 1.0, new Size(224, 224), new
// //              // Scalar(104, 177, 123, 0), true, false);
// //                 org.bytedeco.javacv.Frame frame = grabber.grab()) {
// //             Mat image = new OpenCVFrameConverter.ToMat().convert(frame); // Assuming frame is the current video frame
// //             Mat blob = new Mat();
// //             opencv_dnn.blobFromImage(image, blob, 1.0, new Size(224, 224), new Scalar(104.0, 177.0, 123.0, 0.0), true,
// //                     false,
// //                     CvType.CV_32F);

// //             // Set input blob to the network
// //             faceRecognitionModel.setInput(image);
// //         } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
// //             e.printStackTrace();
// //         }
// //         // Forward pass to get the embedding
// //         Mat output = faceRecognitionModel.forward();

// //         // Convert output to byte[] (or any desired format)
// //         byte[] embedding = new byte[(int) output.total() * output.channels()];
// //         output.data().get(embedding);
// //         return embedding;
// //     }

// //     private static void recognizeFace() {
// //         try {
// //             org.bytedeco.javacv.Frame frame = grabber.grab();
// //             if (frame != null) {
// //                 Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
// //                 List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, null); // Detect faces (Haar can still be used)

// //                 for (Mat face : faces) {
// //                     byte[] embedding = generateEmbeddingUsingModel(face);
// //                     String recognizedName = DatabaseHelper.isFaceRecognized(dbConnection, embedding);

// //                     if (recognizedName != null) {
// //                         System.out.println("Recognized as: " + recognizedName);
// //                         JOptionPane.showMessageDialog(null, "Face recognized as: " + recognizedName);
// //                         return;
// //                     }
// //                 }

// //                 JOptionPane.showMessageDialog(null, "Face not recognized.");
// //             }
// //         } catch (Exception e) {
// //             e.printStackTrace();
// //         }
// //     }
// // }

// // //CHARLTON - WORKS COMPLETELY:
// package FacialCapture;

// import org.bytedeco.opencv.opencv_core.*;
// import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

// import javax.swing.*;
// import java.awt.*;
// import java.awt.image.BufferedImage;
// import java.io.File;
// import java.sql.Connection;
// import java.util.List;
// import org.bytedeco.opencv.global.opencv_imgcodecs;
// import org.bytedeco.javacv.*;
// // import org.bytedeco.opencv.opencv_core.*;
// // import org.bytedeco.opencv.global.opencv_imgproc;
// // import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
// // import java.sql.Connection;
// // import java.util.List;

// // Other imports...

// public class App {
//     private static Connection dbConnection;
//     private static CascadeClassifier faceDetector;
//     private static OpenCVFrameGrabber grabber;
//     private static JLabel imageLabel;
//     private static JTextField nameField;

// public static void main(String[] args) {
// // Initialize resources
// String outputDir = "captured_faces/";
// new File(outputDir).mkdirs();

// try {
// dbConnection = DatabaseHelper.connect();
// grabber = new OpenCVFrameGrabber(0);
// grabber.start();
// // CascadeClassifier faceDetector = new
// // CascadeClassifier("haarcascade_frontalface_alt.xml");
// // faceDetector = new CascadeClassifier(
// // App.class.getResource("/haarcascade_frontalface_alt.xml").getPath());
// CascadeClassifier faceDetector = new
// CascadeClassifier("haarcascade_frontalface_alt.xml");
// // faceDetector.
// // faceDetector = new CascadeClassifier(
// // App.class.getResource("/haarcascade_frontalface_alt.xml").getPath()
// // );
// System.out.println("faceDetector");
// System.out.println(faceDetector);
// if (faceDetector.empty()) {
// faceDetector.close();
// throw new RuntimeException("Failed to load face detector.");
// }
// } catch (Exception e) {
// faceDetector.close();
// e.printStackTrace();
// return;
// }

// // Build GUI
// JFrame frame = new JFrame("Facial Recognition");
// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// frame.setSize(800, 600);

// imageLabel = new JLabel();
// frame.add(imageLabel, BorderLayout.CENTER);

// JPanel buttonPanel = new JPanel();
// nameField = new JTextField(20);
// JButton captureButton = new JButton("Capture Face");
// JButton recognizeButton = new JButton("Recognize Face");

// buttonPanel.add(new JLabel("Name:"));
// buttonPanel.add(nameField);
// buttonPanel.add(captureButton);
// buttonPanel.add(recognizeButton);
// frame.add(buttonPanel, BorderLayout.SOUTH);

// captureButton.addActionListener(e -> {
// String name = nameField.getText().trim();
// if (name.isEmpty()) {
// JOptionPane.showMessageDialog(null, "Please enter a name before capturing a face.");
// } else {
// captureFace(name);
// }
// });
// recognizeButton.addActionListener(e -> recognizeFace());

// new Thread(App::updateCameraFeed).start();

// frame.setVisible(true);

// }

//     private static void updateCameraFeed() {
//         try {
//             while (true) {
//                 org.bytedeco.javacv.Frame frame = grabber.grab();
//                 if (frame != null) {
//                     BufferedImage image = new Java2DFrameConverter().convert(frame);
//                     SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     private static void captureFace(String name) {
//         try {
//             System.out.println("captureFace captureFace captureFace");
//             faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
//             org.bytedeco.javacv.Frame frame = grabber.grab();

//             if (frame != null) {
//                 Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
//                 List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);

//                 for (Mat face : faces) {
//                     byte[] embedding = FaceProcessor.generateEmbedding(face);
//                     DatabaseHelper.storeEmbedding(dbConnection, embedding, name);
//                     System.out.println("Face captured and stored for: " + name);
//                     // DatabaseHelper.storeEmbedding(dbConnection, embedding);
//                     // System.out.println("Face captured and stored.");
//                 }

//                 JOptionPane.showMessageDialog(null, "Face captured and stored successfully.");
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     private static void recognizeFace() {
//         try {
//             org.bytedeco.javacv.Frame frame = grabber.grab();
//             if (frame != null) {
//                 Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
//                 List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);

//                 for (Mat face : faces) {
//                     byte[] embedding = FaceProcessor.generateEmbedding(face);
//                     String recognizedName = DatabaseHelper.isFaceRecognized(dbConnection,
//                             embedding);

//                     if (recognizedName != null) {
//                         System.out.println("Recognized as: " + recognizedName);
//                         JOptionPane.showMessageDialog(null, "Face recognized as: " + recognizedName);
//                         return;
//                     }
//                 }

//                 JOptionPane.showMessageDialog(null, "Face not recognized.");
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

// }

// // private static void recognizeFace() {
// // try {
// // System.out.println("recognizeFace recognizeFace recognizeFace");
// // faceDetector = new
// // CascadeClassifier("haarcascade_frontalface_alt.xml");
// // org.bytedeco.javacv.Frame frame = grabber.grab(); // Fully qualify this
// // class
// // if (frame != null) {
// // Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
// // List<Mat> faces = FaceProcessor.detectAndSaveFaces(mat, faceDetector);
// // System.out.println("faces");
// // System.out.println(faces.size());

// // // Compare each detected face with the database
// // for (Mat face : faces) {
// // byte[] embedding = FaceProcessor.generateEmbedding(face);
// // boolean isRecognized = DatabaseHelper.isFaceRecognized(dbConnection, embedding);

// // if (isRecognized) {
// // JOptionPane.showMessageDialog(null, "Face recognized!");
// // return;
// // }
// // }

// // JOptionPane.showMessageDialog(null, "Face not recognized.");
// // }
// // } catch (Exception e) {
// // e.printStackTrace();
// // }
// // }
