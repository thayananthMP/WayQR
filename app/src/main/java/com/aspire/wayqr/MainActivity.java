package com.aspire.wayqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int hitTime = 3000;

    private ArFragment arFragment;

    private ModelRenderable arrowRenderable, rightRenderable, leftRenderable,
            sphereRenderable, marsRenderable, locationRenderable;
    private Session session;
    private Spinner mspinner;
    int countGo = 1, countRight = 1, countLeft = 1, countSL = 1, countSR = 1;
    int pathGo = 0, pathRight = 0, pathLeft = 0, pathSL = 0, pathSR = 0;
    boolean startValue = true, endValue = true, stepsValue = true;
    private boolean sessionConfigured = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String selectedValue = null;
    private boolean isRunning = false;
    private boolean isThreadRunning = false;
    private DatabaseHelper mDatabaseHelper;
    private Thread thread = null;
    private String qrValue;

    @UiThread
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isRunning = false;
        qrValue = "";
        isThreadRunning = false;
        //Adding Spinner
        String[] arraySpinner = new String[]{"Carina"};
        mspinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinner.setAdapter(adapter);
        //Getting value form Spinner
        selectedValue = mspinner.getSelectedItem().toString();
        //Getting AR Fragment
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        FloatingActionButton fabOne = (FloatingActionButton) findViewById(R.id.fabOne);
        fabOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRunning = false;
                Toast.makeText(getApplicationContext(), "Before QR :" + getQrValue(),
                        Toast.LENGTH_SHORT).show();
                arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
            }

            @WorkerThread
            private void onUpdateFrame(FrameTime frameTime) {
                String str = null;
//                final String str[] = new String[1];
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        str[0] = MainActivity.this.onUpdateFrame(frameTime);
//
//                    }
//                });
//                if (qrValue == null) {
//                    qrValue = MainActivity.this.onUpdateFrame(frameTime);
//                    Toast.makeText(getApplicationContext(), "After QR :" + qrValue,
//                            Toast.LENGTH_SHORT).show();
//                }
                if (!isRunning) {
                    if (qrValue.length() == 0) {
                        str = MainActivity.this.onUpdateFrame(frameTime);

                        if (str != null && qrValue.length() > 0) {
                            qrValue = str;
                            arFragment.getArSceneView().getScene().removeOnUpdateListener(this::onUpdateFrame);
                            isRunning = true;
                            setQrValue(qrValue);
                            Toast.makeText(getApplicationContext(), "After QR :" + getQrValue(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else if (!qrValue.equals(str)) {
                        if (str != null && qrValue.length() > 0) {
                            qrValue = str;
                            arFragment.getArSceneView().getScene().removeOnUpdateListener(this::onUpdateFrame);
                            isRunning = true;
                            setQrValue(qrValue);
                            Toast.makeText(getApplicationContext(), "After QR :" + getQrValue(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
//                if (!isRunning) {
//                    setQrValue(MainActivity.this.onUpdateFrame(frameTime));
//                    while (getQrValue() != null) {
//                        if (getQrValue().equals("START")) {
//                            Toast.makeText(getApplicationContext(), "QR :" + textQR,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        if (getQrValue().equals("STEPS")) {
//                            Toast.makeText(getApplicationContext(), "QR :" + textQR,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        if (getQrValue().equals("END")) {
//                            Toast.makeText(getApplicationContext(), "QR :" + textQR,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        createARMap(selectedValue, getQrValue());
//                    }
//                }
            }
        });
        FloatingActionButton fabTwo = (FloatingActionButton) findViewById(R.id.fabTwo);
        fabTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mDatabaseHelper.deleteDemo();
//                restartProcess();
                createARMap(selectedValue, getQrValue());
            }
        });
        FloatingActionButton fabThree = (FloatingActionButton) findViewById(R.id.fabThree);
        fabThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isThreadRunning) {
                    isThreadRunning = true;
                    addLocationPoints(getQrValue());
                    thread.start();
                } else {
                    isThreadRunning = false;

                    thread.interrupt();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                            "Points Fetched", Toast.LENGTH_SHORT).show());
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        FloatingActionButton fabFour = (FloatingActionButton) findViewById(R.id.fabFour);
        fabFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mDatabaseHelper.deleteALL();
                restartProcess();
//                List<Node> nodesList = arSceneView.getScene().getChildren();
//                for (Node node : nodesList) {
//                    if (node.getScene() == arSceneView.getScene()) {
//                        arSceneView.getScene().removeChild(node);
//                        node.setRenderable(null);
//                        try {
//                            session.update();
//                        } catch (CameraNotAvailableException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
            }
        });
        //Hiding Plane Detection
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
//        ModelRenderable.builder()
//                .setSource(this, R.raw.modeltriangle)
//                .build()
//                .thenAccept(renderable -> arrowRenderable = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Log.e(TAG, "Rendering Failed", throwable);
//                            return null;
//                        });
//        ModelRenderable.builder()
//                .setSource(this, R.raw.directionalarrow)
//                .build()
//                .thenAccept(renderable -> rightRenderable = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Log.e(TAG, "Rendering Failed", throwable);
//                            return null;
//                        });
//        ModelRenderable.builder()
//                .setSource(this, R.raw.mars)
//                .build()
//                .thenAccept(renderable -> marsRenderable = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Log.e(TAG, "Rendering Failed", throwable);
//                            return null;
//                        });
//        ModelRenderable.builder()
//                .setSource(this, R.raw.leftmodel)
//                .build()
//                .thenAccept(renderable -> leftRenderable = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Log.e(TAG, "Rendering Failed", throwable);
//                            return null;
//                        });
        ModelRenderable.builder()
                .setSource(this, R.raw.model)
                .build()
                .thenAccept(renderable -> sphereRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Rendering Failed", throwable);
                            return null;
                        });
//        ModelRenderable.builder()
//                .setSource(this, R.raw.locationmodel)
//                .build()
//                .thenAccept(renderable -> locationRenderable = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Log.e(TAG, "Rendering Failed", throwable);
//                            return null;
//                        });
    }

    public String getQrValue() {
        return qrValue;
    }

    public void setQrValue(String qrValue) {
        this.qrValue = qrValue;
    }

    @WorkerThread
    private void addLocationPoints(String valueQR) {
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
            float y = Float.parseFloat(axisModel.get_yAxis()) - 2f;
            float z = Float.parseFloat(axisModel.get_zAxis()) - 1f;
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
            node1.setRenderable(locationRenderable);
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

    private String onUpdateFrame(FrameTime frameTime) {
        session.getConfig().setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
//        session = arFragment.getArSceneView().getSession();
        Frame frame = arFragment.getArSceneView().getArFrame();
        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return null;
        }
        Collection<AugmentedImage> updateAugmentedImage = frame.getUpdatedTrackables
                (AugmentedImage.class);
        for (AugmentedImage image : updateAugmentedImage) {
            if (image.getTrackingState() == TrackingState.TRACKING) {
                Log.d(">>>>>>>", "$$$$$" + image.getName());
                if (image.getName().equals("START")) {
                    return image.getName();
//                    AnchorNode arNodeStart = new AnchorNode();
//                    float[] pos = {0, 2, -2};
//                    float[] rotation = {0, 0, 0, 0};
//                    arNodeStart.setAnchor(session.createAnchor(new Pose(pos, rotation)));
//                    arNodeStart.setRenderable(arrowRenderable);
//                    arNodeStart.setParent(arSceneView.getScene());
//                    createARMap(image.getName());
//                        if (countGo == 1) {
//                            renderingPath(arNodeStart.getAnchor(), arNodeStart);
//                        }
                }
            }
            if (image.getTrackingState() == TrackingState.TRACKING) {
                if (image.getName().equals("STEPS")) {
                    return image.getName();
//                    AnchorNode arNodeSteps = new AnchorNode();
//                    float[] pos = {0, 2, -2};
//                    float[] rotation = {0, 0, 0, 0};
//                    arNodeSteps.setAnchor(session.createAnchor(new Pose(pos, rotation)));
//                    arNodeSteps.setRenderable(rightRenderable);
//                    arNodeSteps.setParent(arSceneView.getScene());
//                    createARMap(image.getName());
//                        if (countRight == 1) {
//                            renderingPath(arNodeSteps.getAnchor(), arNodeSteps);
//                          }
                }
            }
            if (image.getTrackingState() == TrackingState.TRACKING) {
                if (image.getName().equals("END")) {
                    return image.getName();
//                    AnchorNode arNodeEnd = new AnchorNode();
//                    float[] pos = {0, 2, -2};
//                    float[] rotation = {0, 0, 0, 0};
//                    arNodeEnd.setAnchor(session.createAnchor(new Pose(pos, rotation)));
//                    arNodeEnd.setRenderable(leftRenderable);
//                    arNodeEnd.setParent(arSceneView.getScene());
//                    createARMap(image.getName());
//                        if (countLeft == 1) {
//                            renderingPath(arNodeEnd.getAnchor(), arNodeEnd);
//                        }
                }
            }
//            if (image.getTrackingState() == TrackingState.TRACKING) {
//                if (image.getName().equals("SL") && slValue) {
//                    AnchorNode arNodeSL = new AnchorNode();
//                    if (arNodeSL.getRenderable() != arrowRenderable) {
//                        arNodeSL.setAnchor(image.createAnchor(image.getCenterPose()));
//                        arNodeSL.getAnchor().getPose();
//                        Pose.makeRotation(0, 0, 0, -2);
//                        arNodeSL.setRenderable(arrowRenderable);
//                        arNodeSL.setName("Sl");
//                        arNodeSL.setParent(arSceneView.getScene());
//                        if (countSL == 1) {
//                            renderingPath(arNodeSL.getAnchor(), arNodeSL);
//                        }
//                    }
//                }
//            }
//            if (image.getTrackingState() == TrackingState.TRACKING) {
//                if (image.getName().equals("SR") && srValue) {
//                    AnchorNode arNodeSR = new AnchorNode();
//                    if (arNodeSR.getRenderable() != arrowRenderable) {
//                        arNodeSR.setAnchor(image.createAnchor(image.getCenterPose()));
//                        arNodeSR.getAnchor().getPose();
//                        Pose.makeRotation(0, 0, 0, -2);
//                        arNodeSR.setRenderable(arrowRenderable);
//                        arNodeSR.setName("Sr");
//                        arNodeSR.setParent(arSceneView.getScene());
//                        if (countSR == 1) {
//                            renderingPath(arNodeSR.getAnchor(), arNodeSR);
//                        }
//                    }
//                }
//            }
        }
        return null;
    }
//    @WorkerThread
//    private void renderingPath(Anchor anchor, AnchorNode anchorNode) {
//        if (anchorNode.getName().equals("Go")) {
//            countGo = 0;
//            startValue = false;
//            Pose poseOne = anchor.getPose();
//            float x = poseOne.tx();
//            float y = poseOne.ty();
//            float z = poseOne.tz() - .5f;
//            while (pathGo < 15) {
//                if (TrackingState.TRACKING == anchor.getTrackingState()) {
//                    AnchorNode anchorNodeGo = new AnchorNode();
//                    anchorNodeGo.setLocalPosition(new Vector3(x, y, z));
//                    anchorNodeGo.setRenderable(arrowRenderable);
//                    anchorNodeGo.setLocalRotation(new Quaternion(0, -2, 0, 2));
//                    anchorNodeGo.setParent(arSceneView.getScene());
//                }
//                ++pathGo;
//                --z;
//            }
//        }
//        if (anchorNode.getName().equals("Right")) {
//            countRight = 0;
//            stepsValue = false;
//            Pose poseTwo = anchor.getPose();
//            float x = poseTwo.tx() + .5f;
//            float y = poseTwo.ty();
//            float z = poseTwo.tz();
//            while (pathRight < 2) {
//                if (TrackingState.TRACKING == anchor.getTrackingState()) {
//                    AnchorNode anchorNodeRight = new AnchorNode();
//                    anchorNodeRight.setLocalPosition(new Vector3(x, y, z));
//                    anchorNodeRight.setRenderable(arrowRenderable);
//                    anchorNodeRight.setLocalRotation(new Quaternion(0, -2, 0, 0));
//                    anchorNodeRight.setParent(arSceneView.getScene());
//                }
//                ++pathRight;
//                ++x;
//            }
//        }
//        if (anchorNode.getName().equals("Left")) {
//            countRight = 0;
//            endValue = false;
//            Pose poseL = anchor.getPose();
//            float x = poseL.tx() - .5f;
//            float y = poseL.ty();
//            float z = poseL.tz();
//            while (pathLeft < 2) {
//                if (TrackingState.TRACKING == anchor.getTrackingState()) {
//                    AnchorNode anchorNodeLeft = new AnchorNode();
//                    anchorNodeLeft.setLocalPosition(new Vector3(x, y, z));
//                    anchorNodeLeft.setRenderable(arrowRenderable);
//                    anchorNodeLeft.setLocalRotation(new Quaternion(0, -2, 0, -8));
//                    anchorNodeLeft.setParent(arSceneView.getScene());
//                }
//                ++pathLeft;
//                --x;
//            }
//        }
//        if (anchorNode.getName().equals("Sl")) {
//            countSL = 0;
//            slValue = false;
//            Pose poseSL = anchor.getPose();
//            float x = poseSL.tx() - .5f;
//            float y = poseSL.ty();
//            float z = poseSL.tz();
//            float x1 = poseSL.tx();
//            float y1 = poseSL.ty();
//            float z1 = poseSL.tz() - .5f;
//            while (pathSR < 2) {
//                if (TrackingState.TRACKING == anchor.getTrackingState()) {
//                    if (selectedValue.equals("Board")) {
//                        AnchorNode anchorNodeSL = new AnchorNode();
//                        anchorNodeSL.setLocalPosition(new Vector3(x, y, z));
//                        anchorNodeSL.setRenderable(arrowRenderable);
//                        anchorNodeSL.setLocalRotation(new Quaternion(0, -2, 0, -8));
//                        anchorNodeSL.setParent(arSceneView.getScene());
//                    } else if (selectedValue.equals("Printer")) {
//                        AnchorNode anchorNodeSLGo = new AnchorNode();
//                        anchorNodeSLGo.setLocalPosition(new Vector3(x1, y1, z1));
//                        anchorNodeSLGo.setRenderable(arrowRenderable);
//                        anchorNodeSLGo.setLocalRotation(new Quaternion(0, -2, 0, 2));
//                        anchorNodeSLGo.setParent(arSceneView.getScene());
//                    }
//                }
//                ++pathSR;
//                --x;
//                --z1;
//            }
//        }
//        if (anchorNode.getName().equals("Sr")) {
//            countSR = 0;
//            srValue = false;
//            Pose poseSR = anchor.getPose();
//            float x = poseSR.tx() + .5f;
//            float y = poseSR.ty();
//            float z = poseSR.tz();
//            float x1 = poseSR.tx();
//            float y1 = poseSR.ty();
//            float z1 = poseSR.tz() - .5f;
//            while (pathSL < 2) {
//                if (TrackingState.TRACKING == anchor.getTrackingState()) {
//                    if (selectedValue.equals("Board")) {
//                        AnchorNode anchorNodeSR = new AnchorNode();
//                        anchorNodeSR.setLocalPosition(new Vector3(x, y, z));
//                        anchorNodeSR.setRenderable(arrowRenderable);
//                        anchorNodeSR.setLocalRotation(new Quaternion(0, -2, 0, 0));
//                        anchorNodeSR.setParent(arSceneView.getScene());
//                    } else if (selectedValue.equals("Printer")) {
//                        AnchorNode anchorNodeSRGo = new AnchorNode();
//                        anchorNodeSRGo.setLocalPosition(new Vector3(x1, y1, z1));
//                        anchorNodeSRGo.setRenderable(arrowRenderable);
//                        anchorNodeSRGo.setLocalRotation(new Quaternion(0, -2, 0, 2));
//                        anchorNodeSRGo.setParent(arSceneView.getScene());
//                    }
//                }
//                ++pathSL;
//                ++x;
//                --z1;
//            }
//        }
//    }

    private void configureSession() {
        Config config = new Config(session);
        if (!buildAugmentedImageDatabase(config)) {
            Toast.makeText(this, "Unable to setup augmented", Toast.LENGTH_SHORT)
                    .show();
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
//        session.getConfig().setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
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
