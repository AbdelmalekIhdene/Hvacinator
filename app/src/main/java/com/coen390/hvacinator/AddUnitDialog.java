package com.coen390.hvacinator;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

public class AddUnitDialog extends AppCompatDialogFragment {
    protected EditText nicknameTextbox;
    protected EditText IDTextbox;
    protected EditText targetTemperatureTextbox;
    protected AddUnitDialogListener listener;
    protected ArrayList<String> IDList;

    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_unit_dialog, null);
        builder.setView(view);
        builder.setTitle("New Unit");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = nicknameTextbox.getText().toString();
                String ID = IDTextbox.getText().toString();
                String targetTemperature = targetTemperatureTextbox.getText().toString();
                if(nickname.length() == 0 || ID.length() == 0 || targetTemperature.length() == 0) {
                    Toast.makeText(getActivity(), "Some fields are empty", Toast.LENGTH_SHORT).show();
                    return;
                }
//                if(ID.length() != 16) {
//                    Toast.makeText(getActivity(), "ID must be 16 digits long", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                Long _targetTemperature = Long.parseLong(targetTemperature);
                if((_targetTemperature >= 0 && _targetTemperature <= 99) == false) {
                    Toast.makeText(getActivity(), "Target temperature must be between 0 and 99", Toast.LENGTH_SHORT).show();
                    return;
                }
                for(String existingID: IDList) {
                    if(existingID.equals(ID)) {
                        Toast.makeText(getActivity(), "ID already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                listener.saveUnit(new Unit(ID, nickname, _targetTemperature));
                dialog.dismiss();
            }
        });

        nicknameTextbox = view.findViewById(R.id.AddUnitDialogNickname);
        IDTextbox = view.findViewById(R.id.AddUnitDialogID);
        targetTemperatureTextbox = view.findViewById(R.id.AddUnitDialogTargetTemperature);

        return dialog;
    }

    public void setIDList(ArrayList<String> IDList) {
        this.IDList = IDList;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddUnitDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CreateProfileDialogListener");
        }
    }

    public interface AddUnitDialogListener {
        void saveUnit(Unit unit);
    }
}
