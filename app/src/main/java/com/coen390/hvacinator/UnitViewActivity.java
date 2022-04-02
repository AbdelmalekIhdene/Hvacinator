package com.coen390.hvacinator;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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