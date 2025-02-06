package FacialCapture;

import java.sql.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class DatabaseHelper {

    public static Connection connect() {
        try {
            String url = "jdbc:mysql://localhost:3306/facialcapture";
            String user = "root";
            String password = "";

            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the MySQL database.");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the MySQL database.");
        }
    }

    public static void storeEmbedding(Connection connection, byte[] embedding) {
        String query = "INSERT INTO faces (embedding) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBytes(1, embedding);
            statement.executeUpdate();
            System.out.println("Face embedding stored in the database.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to store face embedding.");
        }
    }

    public static void storeEmbedding(Connection connection, byte[] embedding, String name) {
        String query = "INSERT INTO faces (thing, name, embedding) VALUES (?, ?, ?)";
        byte[] thingy = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBytes(1, embedding);
            statement.setString(2, name);
            statement.setBytes(3, thingy);

            statement.executeUpdate();
            System.out.println("Face embedding and name stored in the database.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to store face embedding and name.");
        }
    }

    public static String isFaceRecognized(Connection connection, byte[] embedding) {
        String query = "SELECT embedding, name FROM faces";
        System.out.println("query: " + query);
        System.out.println("embedding: " + embedding);
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                System.out.println("counter one");
                byte[] dbEmbedding = resultSet.getBytes("embedding");
                System.out.println("counter two");
                String name = resultSet.getString("name");

                double distance = calculateDistance(embedding, dbEmbedding);
                double similarity = calculateCosineSimilarity(embedding, dbEmbedding);

                System.out.println(name);
                System.out.println("Cosine similarity: " + similarity);
                System.out.println("distance: " + distance);
                System.out.println((similarity > 0.4) + " " + (distance < 150));

                if (similarity > 0.4 && distance < 140) { // Recognition threshold
                    System.out.println("Recognized as: " + name);
                    System.out.println("Face recognized with similarity: " + similarity + ", distance: " + distance);
                    return name;
                }
            }

            return "Unable to recognize face";
            // System.out.println("counter: " + counter);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to compare embeddings.");
        }
        return null; // No match found
    }


    public static double calculateDistance(byte[] embedding1, byte[] embedding2) {
        if (embedding1.length != embedding2.length) {
            return Double.MAX_VALUE; // Return a large value if dimensions do not match
        }

        double sum = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            double diff = embedding1[i] - embedding2[i];
            sum += diff * diff;
        }
        return Math.sqrt(Math.sqrt(sum));
    }

    public static double calculateCosineSimilarity(byte[] embedding1, byte[] embedding2) {
        if (embedding1.length != embedding2.length) {
            return -1.0; 
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            double value1 = embedding1[i];
            double value2 = embedding2[i];

            dotProduct += value1 * value2;
            normA += value1 * value1;
            normB += value2 * value2;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }


    public static void saveEmbeddingsAsImages(Connection connection) {
        String query = "SELECT embedding FROM faces";
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
    
            int counter = 0;
            while (resultSet.next()) {
                byte[] imageData = resultSet.getBytes("embedding");
    
                // Debug: Print the size of the image data
                System.out.println("Image data size for entry " + counter + ": " + (imageData != null ? imageData.length : "null"));
    
                // Validate the image data
                if (imageData == null || imageData.length == 0) {
                    System.err.println("Skipping entry " + counter + ": Invalid or empty image data.");
                    continue;
                }
    
                // Convert byte array to BufferedImage
                ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                BufferedImage image = ImageIO.read(bis);
    
                // Check if the image is null
                if (image == null) {
                    System.err.println("Skipping entry " + counter + ": Failed to decode image data.");
                    continue;
                }
    
                // Save the image as a .jpg file
                File outputFile = new File("face_image_" + counter + ".jpg");
                ImageIO.write(image, "jpg", outputFile);
                System.out.println("Saved image: " + outputFile.getAbsolutePath());
    
                counter++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to retrieve embeddings from the database.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to convert byte array to image.");
        }
    }

    public static void main(String[] args) {
        Connection connection = connect();
        saveEmbeddingsAsImages(connection); // Call the new method
    }
}