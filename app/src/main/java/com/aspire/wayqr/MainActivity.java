package com.aspire.wayqr;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int hitTime = 3000;
    private ArFragment arFragment;
    private ModelRenderable sphereRenderable;
    private static final String INSUFFICIENT_FEATURES_MESSAGE =
            "Can't find anything. Aim device at a surface with more texture or color.";
    private static final String EXCESSIVE_MOTION_MESSAGE = "Moving too fast. Slow down.";
    private static final String INSUFFICIENT_LIGHT_MESSAGE =
            "Too dark. Try moving to a well-lit area.";
    private static final String BAD_STATE_MESSAGE =
            "Tracking lost due to bad internal state. Please try restarting the AR experience.";

    private Frame arFrame;
    private Session session;
    private Spinner mspinner;

    private Button createBtn;
    private Button exploreBtn;
    private boolean sessionConfigured = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String selectedValue = null;
    private boolean isThreadRunning = false;
    private DatabaseHelper mDatabaseHelper;
    private Thread thread = null;
    private String qrValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Resources res = getResources();
        String[] bayNames = res.getStringArray(R.array.bay_array);
        //Adding Spinner
        mspinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this
                 ,android.R.layout.simple_spinner_item,bayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinner.setAdapter(adapter);
        //Getting value form Spinner
        selectedValue = mspinner.getSelectedItem().toString();
        //Button
        createBtn = findViewById(R.id.createMap);
        exploreBtn = findViewById(R.id.exploreMap);
        //Getting AR Fragment
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
            }

            private void onUpdateFrame(FrameTime frameTime) {
                Config config = session.getConfig();
                config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
                try {
                    arFrame = session.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }
                arFrame = arFragment.getArSceneView().getArFrame();
                Camera camera = arFrame.getCamera();
                if (camera.getTrackingState() != TrackingState.TRACKING) {
                    return;
                }
                Collection<AugmentedImage> updateAugmentedImage = arFrame.getUpdatedTrackables
                        (AugmentedImage.class);
                for (AugmentedImage image : updateAugmentedImage) {
                    if (image.getTrackingState() == TrackingState.TRACKING) {
                        switch (image.getIndex()) {
                            case 0:
                                Toast.makeText(getApplicationContext(),
                                        "" + image.getName(), Toast.LENGTH_SHORT).show();
                                createMap(selectedValue);
                                break;
                            case 1:
//                                imageView.setVisibility(View.INVISIBLE);
//                                createAnchorNode(image);
                                Toast.makeText(getApplicationContext(),
                                        "" + image.getName(), Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
//                                imageView.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(),
                                        "" + image.getName(), Toast.LENGTH_SHORT).show();
//                                createAnchorNode(image);
                                break;
                            case 3:
//                                imageView.setVisibility(View.INVISIBLE);
//                                createAnchorNode(image);
                                break;
                        }
                    } else if (camera.getTrackingState() == TrackingState.PAUSED) {
                        Toast.makeText(getApplicationContext(),
                                "" + getTrackingFailureReasonString(camera), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

        });

//        //Hiding Plane Detection
//        arFragment.getPlaneDiscoveryController().hide();
//        arFragment.getPlaneDiscoveryController().setInstructionView(null);
//        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);

        ModelRenderable.builder()
                .setSource(this, R.raw.model)
                .build()
                .thenAccept(renderable -> sphereRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Rendering Failed", throwable);
                            return null;
                        });
    }

    public String getQrValue() {
        return qrValue;
    }

    public void setQrValue(String qrValue) {
        this.qrValue = qrValue;
    }

    public static String getTrackingFailureReasonString(Camera camera) {
        TrackingFailureReason reason = camera.getTrackingFailureReason();
        switch (reason) {
            case NONE:
                return "";
            case BAD_STATE:
                return BAD_STATE_MESSAGE;
            case INSUFFICIENT_LIGHT:
                return INSUFFICIENT_LIGHT_MESSAGE;
            case EXCESSIVE_MOTION:
                return EXCESSIVE_MOTION_MESSAGE;
            case INSUFFICIENT_FEATURES:
                return INSUFFICIENT_FEATURES_MESSAGE;
        }
        return "Unknown tracking failure reason: " + reason;
    }

    @WorkerThread
    private void createMap(String valueQR) {
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionevent) -> {
                    if (sphereRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(sphereRenderable);
                    andy.select();
                }
        );

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Frame frame = null;
                while (isThreadRunning) {
                    try {
                        frame = arFragment.getArSceneView().getArFrame();
                        Camera camera = frame.getCamera();
                        if (camera.getTrackingState() == TrackingState.TRACKING) {
                            Pose CameraPose = camera.getDisplayOrientedPose();
                            float x = CameraPose.tx();
                            float y = CameraPose.ty();
                            float z = CameraPose.tz();
                            long valueAdded = mDatabaseHelper.addCoordinates(x, y, z, selectedValue,
                                    valueQR);
                            if (valueAdded > 0) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                        "HitPoints...", Toast.LENGTH_SHORT).show());
//                                runOnUiThread(() -> createARMap());
                            }
                        }
                        Thread.sleep(hitTime);
                    } catch (InterruptedException e) {
                        isThreadRunning = false;
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @WorkerThread
    public void createARMap(String locationValue, String partValue) {
        List<AxisModel> axisModelList = mDatabaseHelper.getCoordinates(locationValue, partValue);
        for (AxisModel axisModel : axisModelList) {
            float x = Float.parseFloat(axisModel.get_xAxis());
            float y = Float.parseFloat(axisModel.get_yAxis());
            float z = Float.parseFloat(axisModel.get_zAxis());
            Node node = new Node();
            node.setLocalPosition(new Vector3(x, y, z));
            node.setRenderable(sphereRenderable);
            node.setParent(arFragment.getArSceneView().getScene());
//                pointMarker(x);
        }
    }

    public void pointMarker(float xValue) {
        if (xValue == mDatabaseHelper.finalPoint().x) {
            Node node1 = new Node();
            node1.setLocalPosition(mDatabaseHelper.finalPoint());
            node1.setRenderable(sphereRenderable);
            node1.setParent(arFragment.getArSceneView().getScene());
        }
    }

    @WorkerThread
    private void restartProcess() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @WorkerThread
    public boolean buildAugmentedImageDatabase(Config config) {
        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap[] augmentedImageBitmap = loadAugmentedImage();
        config.setFocusMode(Config.FocusMode.AUTO);
        if (augmentedImageBitmap == null) {
            return false;
        }
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("START", augmentedImageBitmap[0]);
        augmentedImageDatabase.addImage("STEPS", augmentedImageBitmap[1]);
        augmentedImageDatabase.addImage("END", augmentedImageBitmap[2]);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        session.configure(config);
        return true;
    }

    @WorkerThread
    private Bitmap[] loadAugmentedImage() {
        Bitmap[] value = new Bitmap[3];
        try {
            InputStream inputStreamOne = getAssets().open("startQR.jpeg");
            value[0] = BitmapFactory.decodeStream(inputStreamOne);
        } catch (IOException e) {
            Log.d(TAG, "Exception loading Augmented Image : START ", e);
        }
        try {
            InputStream inputStreamTwo = getAssets().open("stepsQR.jpeg");
            value[1] = BitmapFactory.decodeStream(inputStreamTwo);
        } catch (IOException e) {
            Log.d(TAG, "Exception loading Augmented Image : STEPS", e);
        }
        try {
            InputStream inputStreamSL = getAssets().open("endQR.png");
            value[2] = BitmapFactory.decodeStream(inputStreamSL);
        } catch (IOException e) {
            Log.d(TAG, "Exception : END ");
        }
        return value;
    }

    private void configureSession() {
        Config config = new Config(session);
        if (!buildAugmentedImageDatabase(config)) {
            Toast.makeText(this, "Unable to setup augmented", Toast.LENGTH_SHORT)
                    .show();
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        session.getConfig().setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            arFragment.getArSceneView().pause();
            session.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session == null) {
            String message = null;
            Exception exception = null;
            try {
                session = new Session(this);
            } catch (UnavailableArcoreNotInstalledException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update android";
                exception = e;
            } catch (Exception e) {
                message = "AR is not supported";
                exception = e;
            }

            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
            sessionConfigured = true;
        }
        if (sessionConfigured) {
            configureSession();
            sessionConfigured = false;
            arFragment.getArSceneView().setupSession(session);
        }
    }
}
