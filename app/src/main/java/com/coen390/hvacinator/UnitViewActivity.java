package com.coen390.hvacinator;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UnitViewActivity extends AppCompatActivity {
    protected FirebaseFirestore db;
    protected FirebaseAuth auth;
    private Unit unit;
    private TextView IDLabel;
    private EditText nicknameTextbox;
    private EditText targetTemperatureTextbox;
    private Button saveButton;
    private XYPlot plot;
    private SimpleXYSeries temperatureSeries;
    private DatabaseReference logs;
    private long temperatureTimestampStart = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_view);
        Intent intent = getIntent();
        String ID  = intent.getStringExtra("ID");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        IDLabel = (TextView) findViewById(R.id.unitViewIDLabel);
        nicknameTextbox = (EditText) findViewById(R.id.unitViewNicknameTextbox);
        targetTemperatureTextbox = (EditText) findViewById(R.id.unitViewTargetTemperatureTextbox);
        saveButton = (Button) findViewById(R.id.unitViewSaveButton);
        initPlot();
        FirebaseDatabase rdb = FirebaseDatabase.getInstance();
        logs = rdb.getReference("units/" + ID + "/logs");
        logs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                int i = 0;
                for(DataSnapshot node: children) {
                    i++;
                    if(i >= temperatureSeries.size()) {
                        Timestamp timestamp = Timestamp.valueOf(node.getKey());
                        if(temperatureTimestampStart == -1) {
                            temperatureTimestampStart = timestamp.getTime();
                        }
                        RDBNode _node = node.getValue(RDBNode.class);
                        if(_node.temperature == null) continue;
                        temperatureSeries.addLast(timestamp.getTime() - temperatureTimestampStart + 1, _node.temperature);
                    }
                }
                plot.redraw();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value...
                return;
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Yo!");
                String nickname = nicknameTextbox.getText().toString();
                String targetTemperature = targetTemperatureTextbox.getText().toString();
                if(nickname.length() == 0) {
                    Toast.makeText(UnitViewActivity.this, "Nickname field is empty", Toast.LENGTH_SHORT).show();
                    return;
                } else if(targetTemperature.length() == 0) {
                    Toast.makeText(UnitViewActivity.this, "Target temperature field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Long _targetTemperature = Long.parseLong(targetTemperature);
                unit.nickname = nickname;
                unit.targetTemperature = _targetTemperature;
                db.collection("units").document(unit.ID).set(unit).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UnitViewActivity.this, "Unit saved successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initPlot() {
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.unitViewPlot);
        plot.setRangeBoundaries(0, 45, BoundaryMode.FIXED);
        plot.setDomainStep(StepMode.SUBDIVIDE, 12);
        PanZoom.attach(plot, PanZoom.Pan.HORIZONTAL, PanZoom.Zoom.STRETCH_HORIZONTAL, PanZoom.ZoomLimit.MIN_TICKS);
        temperatureSeries = new SimpleXYSeries(SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Temperature");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter);
        format.setPointLabelFormatter(null);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
//        format.setInterpolationParams(
//                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(temperatureSeries, format);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(((Double) obj).longValue() + temperatureTimestampStart - 1);
                return toAppendTo.append(timestamp);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String ID = intent.getStringExtra("ID");
        DocumentReference unitRef = db.collection("units").document(ID);
        unitRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists())
                    {
                        unit = new Unit(document.getData());
                        IDLabel.setText("ID:  " + unit.ID);
                        nicknameTextbox.setText(unit.nickname);
                        targetTemperatureTextbox.setText(Long.toString(unit.targetTemperature));
                    }
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete_unit:
                deleteUnit();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteUnit()
    {
        FirebaseUser user = auth.getCurrentUser();
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> unitIDs = (ArrayList<String>) document.get("unitIDs");
                        unitIDs.remove(unit.ID);
                        Map<String, Object> newUser = new HashMap<>();
                        newUser.put("unitIDs", unitIDs);
                        db.collection("users").document(user.getUid()).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                db.collection("units").document(unit.ID).delete().addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        onBackPressed();
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });

    }
}