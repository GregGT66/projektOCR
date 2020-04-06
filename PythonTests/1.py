import imutils
import cv2

image = cv2.imread("img.jpg")

(h, w, d) = image.shape
print("width={}, height={}, depth={}".format(w, h, d))

(B, G, R) = image[100, 50]
print("R={}, G={}, B={}".format(R, G, B))

cv2.imshow("Image", image)

center = (w // 2, h // 2)
M = cv2.getRotationMatrix2D(center, -45, 1.0)
rotated = cv2.warpAffine(image, M, (w, h))
cv2.imshow("OpenCV Rotation", rotated)

rotated = imutils.rotate(image, 45)
cv2.imshow("Imutils Rotation", rotated)

output = image.copy()
cv2.rectangle(output, (320, 60), (420, 160), (0, 0, 255), 2)
cv2.imshow("Rectangle", output)

gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
cv2.imshow("Gray", gray)

edged = cv2.Canny(gray, 50, 150)
cv2.imshow("Edged", edged)

thresh = cv2.threshold(gray, 100, 255, cv2.THRESH_BINARY_INV)[1]
cv2.imshow("Thresh", thresh)

mask = thresh.copy()
mask = cv2.erode(mask, None, iterations=5)
cv2.imshow("Eroded", mask)

mask = thresh.copy()
mask = cv2.dilate(mask, None, iterations=5)
cv2.imshow("Dilated", mask)

cv2.waitKey(0)
