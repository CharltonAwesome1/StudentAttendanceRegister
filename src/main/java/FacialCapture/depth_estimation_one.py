import sys
import cv2
import numpy as np

def compute_disparity(img1_path, img2_path):
    imgL = cv2.imread(img1_path, cv2.IMREAD_GRAYSCALE)
    imgR = cv2.imread(img2_path, cv2.IMREAD_GRAYSCALE)

    stereo = cv2.StereoBM_create(numDisparities=16, blockSize=15)
    disparity = stereo.compute(imgL, imgR)

    disparity = cv2.normalize(disparity, None, 0, 255, cv2.NORM_MINMAX)
    output_path = "depth_embedding.png"
    cv2.imwrite(output_path, disparity.astype(np.uint8))
    return output_path

if __name__ == "__main__":
    img1_path = sys.argv[1]
    img2_path = sys.argv[2]
    depth_output = compute_disparity(img1_path, img2_path)
    print(f"Depth map saved at {depth_output}")
