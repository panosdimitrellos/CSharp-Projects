package com.example.sms13033;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

// Κλάση που χρησιμοποείται όταν ο χρήστης πατήσει το κουμπί "Προσθήκη" και εμφανίζεται το κατάλληλο παράθυρο.
public class AddDialog extends AppCompatDialogFragment {

    private EditText editTextCode;
    private  EditText editTextMessage;
    private  AddDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Ορίζουμε το layout που έχουμε φτιάξει το οποίο είναι το layout_dialog.
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog,null);

        // Αφού έχουμε δημιουργήσει τον builder μας του προσθέτουμε τα ακόλυοθα δεδομένα.
        builder.setView(view)
                .setTitle("Προσθέστε ένα νέο μήνυμα μετακίνησης:")
                .setNegativeButton("Ακύρωση", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                // Όταν ο χρήστης πατήσει ΟΚ αποθηκεύονται τα δεδομένα που έβαλε.
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strCode = editTextCode.getText().toString();
                        int code = Integer.parseInt(strCode);
                        String message = editTextMessage.getText().toString();
                        listener.insertChanges(code,message);

                    }
                });

        editTextCode = view.findViewById(R.id.add_code);
        editTextMessage = view.findViewById(R.id.add_message);

        return  builder.create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Ορίζουμε τον listener για να μπορέσουμε με αυτόν να καλέσουμε την insertChanges.
        try {
            listener = (AddDialogListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString()+"must implement AddDialogListener");
        }
    }


    public interface AddDialogListener{
        void insertChanges(int code,String message);
    }
}
