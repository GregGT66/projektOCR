package pl.edu.pwr.s241965.opencvtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

@SuppressLint("SourceLockedOrientationActivity")
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MainActivity extends AppCompatActivity {
    private ImageView imageView;

    private Bitmap selectedImage;

    private ArrayList<PointF> corners;

    private String decodedText;
    private String fileName;

    private boolean preProcessed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        verifyStoragePermissions(this);

        corners = new ArrayList<>();

        imageView = findViewById(R.id.imageView);

        Button selectButton = findViewById(R.id.selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Button rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null) {
                    rotateImage();
                } else {
                    Toast.makeText(getApplicationContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button cornerButton = findViewById(R.id.cornerButton);
        cornerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null) {
                    launchCornerSelect();
                } else {
                    Toast.makeText(getApplicationContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button processImage = findViewById(R.id.processImage);
        processImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (corners.size() == 4) {
                    if (!preProcessed) {
                        imageProcess();
                    } else {
                        Toast.makeText(getApplicationContext(), "Image already processed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No corners selected!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button readText = findViewById(R.id.readText);
        readText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preProcessed) {
                    //ocr
                    //add to string
                    //show results - toast
                } else {
                    Toast.makeText(getApplicationContext(), "Process image first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!decodedText.isEmpty()) {
                    //ask for name
                    //save photo
                    //save text
                    //internal memory own directory
                }
                else {
                    Toast.makeText(getApplicationContext(), "OCR image first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void imageProcess() {
        Mat img = new Mat();
        Utils.bitmapToMat(selectedImage, img);

        Collections.sort(corners, new Comparator<PointF>() {
            @Override
            public int compare(PointF o1, PointF o2) {
                return (int) ((o1.x + o1.y) - (o2.x + o2.y));
            }
        });

        //Size bug... (maybe rotation)
        int topWidth = (int) Math.abs(corners.get(1).x - corners.get(0).x);
        int bottomWidth = (int) Math.abs(corners.get(3).x - corners.get(2).x);
        int leftHeight = (int) Math.abs(corners.get(2).y - corners.get(0).y);
        int rightHeight = (int) Math.abs(corners.get(3).y - corners.get(1).y);

        int width = Math.max(topWidth, bottomWidth);
        int height = Math.max(leftHeight, rightHeight);

        MatOfPoint2f src = new MatOfPoint2f(
                new Point(corners.get(0).x, corners.get(0).y),
                new Point(corners.get(1).x, corners.get(1).y),
                new Point(corners.get(2).x, corners.get(2).y),
                new Point(corners.get(3).x, corners.get(3).y)
        );

        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(width, 0),
                new Point(0, height),
                new Point(width, height)
        );

        Mat M = Imgproc.getPerspectiveTransform(src, dst);

        Mat newImg = new Mat();
        Imgproc.warpPerspective(img, newImg, M, new Size(width, height));

        Imgproc.cvtColor(newImg, newImg, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.threshold(newImg, newImg, 180, 255, Imgproc.THRESH_OTSU);

        Imgproc.medianBlur(newImg, newImg, 3);

        selectedImage = Bitmap.createBitmap(newImg.cols(), newImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(newImg, selectedImage);
        imageView.setImageBitmap(selectedImage);

        preProcessed = true;
    }

    private void launchCornerSelect() {
        Intent cornerSelect = new Intent(getApplicationContext(), CornersActivity.class);

        Bitmap image;
        image = selectedImage.copy(selectedImage.getConfig(), true);

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 5, bs);

        cornerSelect.putExtra("image", bs.toByteArray());

        startActivityForResult(cornerSelect, 2);
    }

    public void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Take Photo")) {
                    //Low quality...
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[which].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);

                } else if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        selectedImage = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                        imageView.setImageBitmap(selectedImage);

                        preProcessed = false;
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImageUri = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        if (selectedImageUri != null) {
                            Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);

                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);

                                selectedImage = BitmapFactory.decodeFile(picturePath);
                                imageView.setImageBitmap(selectedImage);

                                preProcessed = false;

                                cursor.close();
                            }
                        }
                    }
                    break;
                case 2:
                    if (resultCode == RESULT_OK && data != null) {
                        corners = (ArrayList<PointF>) Objects.requireNonNull(data.getExtras()).get("corners");
                        preProcessed = false;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        final String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully");
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void rotateImage() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        selectedImage = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, true);
        imageView.setImageBitmap(selectedImage);

        //Cropped after rotation...

        //Mat img = new Mat();
        //Utils.bitmapToMat(selectedImage, img);

        //int centerX = Math.round(img.width()/2.0F);
        //int centerY = Math.round(img.height()/2.0F);

        //Mat rot = Imgproc.getRotationMatrix2D(new Point(centerX, centerY), -90, 1);
        //Imgproc.warpAffine(img, img, rot, img.size());

        //Utils.matToBitmap(img, selectedImage);
        //imageView.setImageBitmap(selectedImage);
    }
}
