package com.aspire.wayqr;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    private Spinner mSpinner;
    public Button createBtn;
    private Button exploreBtn;

    private String selectedValue;
    private static final String TAG = MainActivity.class.getSimpleName();
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Resources res = getResources();
        String[] bayNames = res.getStringArray(R.array.bay_array);
        //Adding Spinner
        mSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, bayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setVisibility(View.GONE);

        //Button
        createBtn = findViewById(R.id.createMap);
        exploreBtn = findViewById(R.id.exploreMap);
        mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpinner.setVisibility(View.VISIBLE);
                mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedValue = mSpinner.getSelectedItem().toString();
                        Log.d(TAG, " String = " + selectedValue);
                        Intent intent = new Intent(MainActivity.this, ActivityOne.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("location", selectedValue);

                        intent.putExtras(bundle);

                        startActivity(intent);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });

//        //Hiding Plane Detection
//        arFragment.getPlaneDiscoveryController().hide();
//        arFragment.getPlaneDiscoveryController().setInstructionView(null);
//        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
    }

//    @WorkerThread
//    public void createARMap(String locationValue, String partValue) {
//        List<AxisModel> axisModelList = mDatabaseHelper.getCoordinates(locationValue, partValue);
//        for (AxisModel axisModel : axisModelList) {
//            float x = Float.parseFloat(axisModel.get_xAxis());
//            float y = Float.parseFloat(axisModel.get_yAxis());
//            float z = Float.parseFloat(axisModel.get_zAxis());
//            Node node = new Node();
//            node.setLocalPosition(new Vector3(x, y, z));
//            node.setRenderable(sphereRenderable);
//            node.setParent(arFragment.getArSceneView().getScene());
////          pointMarker(x);
//        }
//    }

//    public void pointMarker(float xValue) {
//        if (xValue == mDatabaseHelper.finalPoint().x) {
//            Node node1 = new Node();
//            node1.setLocalPosition(mDatabaseHelper.finalPoint());
//            node1.setRenderable(sphereRenderable);
//            node1.setParent(arFragment.getArSceneView().getScene());
//        }
//    }

    @WorkerThread
    private void restartProcess() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
