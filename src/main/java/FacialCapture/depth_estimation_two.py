import sys
import cv2
import numpy as np

def compute_disparity(img1_path, img2_path):
    imgL = cv2.imread(img1_path, cv2.IMREAD_GRAYSCALE)
    imgR = cv2.imread(img2_path, cv2.IMREAD_GRAYSCALE)

    stereo = cv2.StereoBM_create(numDisparities=16, blockSize=15)
    disparity = stereo.compute(imgL, imgR)

    # Normalize to 0-255 for visualization
    depth_map = cv2.normalize(disparity, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)

    # Create checkerboard overlay
    checkerboard = np.zeros_like(depth_map, dtype=np.uint8)
    square_size = 20  # Adjust square size

    for y in range(0, checkerboard.shape[0], square_size):
        for x in range(0, checkerboard.shape[1], square_size):
            if (x // square_size + y // square_size) % 2 == 0:
                checkerboard[y:y+square_size, x:x+square_size] = 255  # White square

    # Combine depth and checkerboard
    overlay = cv2.addWeighted(depth_map, 0.7, checkerboard, 0.3, 0)

    # Save depth visualization
    output_path = "depth_checkerboard.png"
    cv2.imwrite(output_path, overlay)
    return output_path

if __name__ == "__main__":
    img1_path = sys.argv[1]
    img2_path = sys.argv[2]
    depth_output = compute_disparity(img1_path, img2_path)
    print(f"Depth map saved at {depth_output}")
