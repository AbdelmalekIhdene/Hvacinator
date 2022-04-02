package com.coen390.hvacinator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AddUnitDialog.AddUnitDialogListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseDatabase rdb;

    private final String EMAIL_ADDRESS = "malekihd0@gmail.com";
    private final String PASSWORD = "hvacinator123";

    private ArrayList<Unit> units;
    private int reloadUnits_getTasksFinished = 0;

    private ListView unitList;
    private UnitListAdapter unitListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        units = new ArrayList<Unit>();
        FloatingActionButton insertProfileDialogButton = findViewById(R.id.AddUnitButton);
        insertProfileDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
        unitList = findViewById(R.id.UnitList);
    }

    private void openDialog() {
        AddUnitDialog dialog = new AddUnitDialog();
        ArrayList<String> IDList = new ArrayList<String>();
        for(Unit unit : units) IDList.add(unit.ID);
        dialog.setIDList(IDList);
        dialog.show(getSupportFragmentManager(), "add unit dialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        reloadUser(mAuth.getCurrentUser().getUid());
    }

    @Override
    public void saveUnit(Unit unit) {
        db.collection("units").document(unit.ID).set(unit).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> updatedUser = new HashMap<>();
                ArrayList<String> unitIDs = new ArrayList<String>();
                for(Unit _unit: units) {
                    unitIDs.add(_unit.ID);
                }
                unitIDs.add(unit.ID);
                updatedUser.put("unitIDs", unitIDs);
                db.collection("users").document(mAuth.getCurrentUser().getUid()).set(updatedUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        reloadUser(mAuth.getCurrentUser().getUid());
                    }
                });
            }
        });
    }

    private void reloadUser(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> unitIDs = (ArrayList<String>) document.get("unitIDs");
                        reloadUnits(unitIDs);
                    } else {
                        Map<String, Object> newUser = new HashMap<>();
                        newUser.put("unitIDs", new ArrayList<String>());
                        userRef.set(newUser);
                    }
                }
            }
        });
    }

    private void reloadUnits(ArrayList<String> unitIDs) {
        System.out.println("reloadUnits");
        reloadUnits_getTasksFinished = unitIDs.size();
        units.clear();
        for(String unitID: unitIDs) {
            DocumentReference unitRef = db.collection("units").document(unitID);
            unitRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            units.add(new Unit(document.getData()));
                            reloadUnits_getTasksFinished -= 1;
                            System.out.println((new Unit(document.getData())).ID);
                            if(reloadUnits_getTasksFinished == 0) {
                                reloadUI();
                            }
                        }
                    }
                }
            });
        }
    }

    private void reloadUI() {
        ArrayList<String> unitIDs = new ArrayList<String>();
        System.out.println(units);
        for(Unit unit : units) {
            unitIDs.add(unit.ID);
        }
        unitListAdapter = new UnitListAdapter(this, units);
        unitList.setAdapter(unitListAdapter);
        unitList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, UnitViewActivity.class);
                intent.putExtra("ID", units.get(i).ID);
                startActivity(intent);
            }
        });
    }

    public void logout(View view) { //Log out function called by onclick of logout button
        mAuth.signOut();
        Intent s = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(s);
    }
}