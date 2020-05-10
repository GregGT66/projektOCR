package pl.edu.pwr.s241965.opencvtest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

public class CornersActivity extends AppCompatActivity {

    private ArrayList<PointF> corners;
    private Bitmap selectedImage;
    private ImageView imageView2;
    private Button submitButton;

    @SuppressLint({"SourceLockedOrientationActivity", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corners);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        corners = new ArrayList<>();

        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getIntent().putExtra("corners", corners);

                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        imageView2 = findViewById(R.id.imageView2);

        imageView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Matrix inverse = new Matrix();
                    ((ImageView) v).getImageMatrix().invert(inverse);
                    float[] touchPoint = new float[]{event.getX(), event.getY()};
                    inverse.mapPoints(touchPoint);
                    int xCoord = (int) touchPoint[0];
                    int yCoord = (int) touchPoint[1];

                    if ((xCoord >= 0 && xCoord <= selectedImage.getWidth()) && (yCoord >= 0 && yCoord <= selectedImage.getHeight())) {
                        if (corners.size() >= 4) corners.clear();
                        corners.add(new PointF(xCoord, yCoord));
                        System.out.println(corners);

                        Bitmap bmp = selectedImage.copy(selectedImage.getConfig(), true);
                        Canvas canvas = new Canvas(bmp);
                        Paint paint = new Paint();
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.GREEN);
                        for (PointF p : corners) {
                            canvas.drawCircle(p.x, p.y, 100, paint);
                        }
                        imageView2.setImageBitmap(bmp);
                    }
                }
                return true;
            }
        });

        if (getIntent().hasExtra("image")) {
            selectedImage = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("image"), 0, getIntent().getByteArrayExtra("image").length);
            imageView2.setImageBitmap(selectedImage);
        }
    }
}
