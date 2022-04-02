package com.coen390.hvacinator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UnitListAdapter extends ArrayAdapter<Unit> {
    private Context mContext;
    private ArrayList<Unit> units = new ArrayList<>();
    private ArrayList<DatabaseReference> refs = new ArrayList<>();

    public UnitListAdapter(@NonNull Context context, ArrayList<Unit> units) {
        super(context, 0, units);
        mContext = context;
        this.units = units;
        FirebaseDatabase rdb = FirebaseDatabase.getInstance();
        for(Unit unit: units) {
            refs.add(rdb.getReference("units/" + unit.ID + "/temperature"));
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.unit_list_item, parent, false);
        }

        Unit unit = units.get(position);
        TextView unitIDText = listItem.findViewById(R.id.ItemUnitIDText);
        TextView unitNicknameText = listItem.findViewById(R.id.ItemUnitNicknameText);
        TextView targetTemperatureText = listItem.findViewById(R.id.ItemTargetTemperatureText);
        TextView currentTemperatureText = listItem.findViewById(R.id.ItemCurrentTemperatureText);
        refs.get(position).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Float value = dataSnapshot.getValue(Float.class);
                if(value != null) {
                    currentTemperatureText.setText(String.valueOf(Math.round(value)) + " °C");
                } else {
                    currentTemperatureText.setText("-- °C");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value...
                return;
            }
        });

        unitIDText.setText(unit.ID);
        unitNicknameText.setText(unit.nickname);
        targetTemperatureText.setText(unit.targetTemperature.toString() + " °C");
        return listItem;
    }
}
