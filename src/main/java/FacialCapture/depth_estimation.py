import sys
import cv2
import numpy as np
import os

def compute_disparity(img_paths):
    print("Python script started.")
    dir_path = os.path.dirname(os.path.realpath(__file__))
    
    # Read all images
    images = [cv2.imread(os.path.join(dir_path + "/test_images", path).replace("\\", "/"), cv2.IMREAD_GRAYSCALE) for path in img_paths]
    
    if any(img is None for img in images):
        print("Error: One or more images could not be loaded.")
        return None
    
    # Initialize disparity map accumulation
    height, width = images[0].shape
    combined_disparity = np.zeros((height, width), dtype=np.float32)
    
    # Compute disparity for consecutive image pairs
    stereo = cv2.StereoSGBM_create(
        minDisparity=0,
        numDisparities=128,  # Increase this
        blockSize=15,
        uniquenessRatio=10,
        speckleWindowSize=100,
        speckleRange=32,
        disp12MaxDiff=1,
        P1=8 * 3 * 15**2,
        P2=32 * 3 * 15**2,
    )
    
    for i in range(len(images) - 1):
        imgL, imgR = images[i], images[i + 1]
        disparity = stereo.compute(imgL, imgR).astype(np.float32)
        combined_disparity += disparity
    
    # Normalize the combined disparity map
    depth_map = cv2.normalize(combined_disparity, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)
    depth_map = cv2.GaussianBlur(depth_map, (5,5), 0)

    # Apply colormap for better depth visualization
    depth_colored = cv2.applyColorMap(depth_map, cv2.COLORMAP_JET)
    
    # Save the depth map
    depth_map_path = "depth_map.png"
    cv2.imwrite(depth_map_path, depth_colored)
    print(f"Depth map saved at {depth_map_path}")

    ### STEP 1: Generate the checkerboard pattern ###
    checkerboard = np.zeros_like(depth_map, dtype=np.uint8)
    square_size = 20  # Adjust square size

    for y in range(0, checkerboard.shape[0], square_size):
        for x in range(0, checkerboard.shape[1], square_size):
            if (x // square_size + y // square_size) % 2 == 0:
                checkerboard[y:y+square_size, x:x+square_size] = 255  # White squares

    # Convert checkerboard to color
    checkerboard_colored = cv2.applyColorMap(checkerboard, cv2.COLORMAP_JET)

    ### STEP 2: Create a displacement map based on depth ###
    displacement_scale = 10  # Adjust for stronger warping effect

    # Create meshgrid for pixel coordinates
    x_map, y_map = np.meshgrid(np.arange(width), np.arange(height))

    # Scale depth values to create X and Y displacement
    x_displacement = (depth_map / 255.0) * displacement_scale
    y_displacement = (depth_map / 255.0) * displacement_scale

    # Apply displacement to the coordinates
    x_map = np.clip(x_map + x_displacement, 0, width - 1).astype(np.float32)
    y_map = np.clip(y_map + y_displacement, 0, height - 1).astype(np.float32)

    ### STEP 3: Warp the checkerboard ###
    warped_checkerboard = cv2.remap(checkerboard_colored, x_map, y_map, interpolation=cv2.INTER_LINEAR)

    ### STEP 4: Blend the warped checkerboard with the depth map ###
    overlay = cv2.addWeighted(depth_colored, 0.7, warped_checkerboard, 0.3, 0)

    # Save the warped depth-checkerboard visualization
    checkerboard_path = "depth_checkerboard.png"
    cv2.imwrite(checkerboard_path, overlay)
    print(f"Checkerboard depth visualization saved at {checkerboard_path}")
    
    return depth_map_path, checkerboard_path

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py <directory_path>")
        sys.exit(1)
    
    directory_path = sys.argv[1]
    img_paths = [f"frame_{i+1}.jpg" for i in range(100)]  # Adjust if needed
    
    depth_output, checkerboard_output = compute_disparity(img_paths)
    if depth_output and checkerboard_output:
        print("Python script completed successfully.")
