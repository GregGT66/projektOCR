import imutils
import cv2
import numpy as np
import matplotlib.pyplot as plt

import pytesseract

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

# https://www.pyimagesearch.com/2018/09/17/opencv-ocr-and-text-recognition-with-tesseract/
# https://www.pyimagesearch.com/2017/07/17/credit-card-ocr-with-opencv-and-python/
# https://www.pyimagesearch.com/2017/07/10/using-tesseract-ocr-python/

gray = cv2.cvtColor(dst, cv2.COLOR_BGR2GRAY)

t_val = 180
# thresh = cv2.threshold(gray, t_val, 255, cv2.THRESH_BINARY)[1]
thresh = cv2.threshold(gray, t_val, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]

# blur = cv2.medianBlur(thresh, 3)
blur = thresh

blur_rgb = cv2.cvtColor(blur, cv2.COLOR_GRAY2RGB)
cv2.imwrite("temp.png", blur_rgb)

# config = "-l eng --oem 1 --psm 6"
# pytesseract.pytesseract.tesseract_cmd = r'C:\Users\pewojda\AppData\Local\Tesseract-OCR'
# text = pytesseract.image_to_string(blur_rgb, config=config)
# print(text)

plt.subplot(241), plt.imshow(img), plt.title('Input')
plt.subplot(242), plt.imshow(gray), plt.title('Gray')
plt.subplot(243), plt.imshow(thresh), plt.title('Threshold')
plt.subplot(244), plt.imshow(blur), plt.title('Blur')
plt.subplot(245), plt.imshow(blur_rgb), plt.title('Saved')
plt.show()
