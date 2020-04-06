import imutils
import cv2
import numpy as np
import matplotlib.pyplot as plt

# https://theailearner.com/tag/cv2-warpperspective/
# https://docs.opencv.org/3.0-beta/doc/py_tutorials/py_imgproc/py_geometric_transformations/py_geometric_transformations.html#geometric-transformations

img = cv2.imread('img.JPG')
rows, cols, ch = img.shape

pts1 = np.float32([[408, 136], [796, 730], [808, 141], [388, 733]])
np.random.shuffle(pts1)

# 304, 811, 1019, 1658 - suma x, y
# TL, TR, BL, BR, warp funkcja

pts1 = pts1[np.argsort(np.sum(pts1, axis=1))]

topwidth = pts1[1, 0] - pts1[0, 0]
bottomwidth = pts1[3, 0] - pts1[2, 0]

leftheight = pts1[2, 1] - pts1[0, 1]
bottomheight = pts1[3, 1] - pts1[1, 1]

width = max(topwidth, bottomwidth)
height = max(leftheight, bottomheight)

pts2 = np.float32([[0, 0], [width, 0], [0, height], [width, height]])

M = cv2.getPerspectiveTransform(pts1, pts2)
dst = cv2.warpPerspective(img, M, (width, height))

plt.subplot(121), plt.imshow(img), plt.title('Input')
plt.subplot(122), plt.imshow(dst), plt.title('Output')
plt.show()
