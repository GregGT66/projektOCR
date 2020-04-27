package com.pwr.edu.imageproc.rocr.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.CameraView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pwr.edu.imageproc.rocr.R;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CameraFragment extends Fragment {

    private CameraView cameraView;
    private FloatingActionButton takePictureButton;

    // Executor impl.
    private Executor executor;
    private final static int CORE_POOL_SIZE = 2;
    private final static int MAX_CORE_POOL_SIZE = 8;
    private final static long KEEP_ALIVE_TIME = 1;
    private final static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();

    // Request codes
    private final int CAMERA_REQUEST_CODE = 10;

    private class TakePictureButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Date date = Calendar.getInstance().getTime();
            String stamp = date.toString();
            String photoName = stamp.replace(" ", "_") + ".png";
            File file = getImageUri(photoName);
            Log.v("IMG_PATH", file.getPath());
            cameraView.takePicture(file, executor, new ImageSavedListener());
        }
    }

    private class ImageSavedListener implements ImageCapture.OnImageSavedCallback {

        @Override
        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

        }

        @Override
        public void onError(@NonNull ImageCaptureException exception) {

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAX_CORE_POOL_SIZE,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                blockingQueue);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraView = view.findViewById(R.id.viewFinder);
        takePictureButton = view.findViewById(R.id.fab_takePicture);
        takePictureButton.setOnClickListener(new TakePictureButtonListener());

        // Check for permissions
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            // Permission already granted
            startCamera();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "App needs permissions to work", Toast.LENGTH_LONG).show();
                    requireActivity().finish();
                }
                return;
        }
    }

    private void startCamera() {
        cameraView.setCaptureMode(CameraView.CaptureMode.IMAGE);
        cameraView.bindToLifecycle(getViewLifecycleOwner());
    }

    private File getImageUri(String fileName) {
        File dir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.d("DIR_E", "failed to create directory");
        }
        return new File(dir.getPath() + File.separator + fileName);
    }
}
