package com.example.sms13033;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// Έχουμε κάνει implement τους listeners των κλάσεων AddDialog,DeleteDialog,UpdateDialog έτσι ώστε να μπορύμε να έχουμε πρόσβαση σε αυτές
// και για να μπορύμε να εκτέλεσουμε τις μεθόδους τους.
public class MainActivity2 extends AppCompatActivity implements AddDialog.AddDialogListener,DeleteDialog.DeleteDialogListener,UpdateDialog.UpdateDialogListener {

    RadioGroup radioGroup;
    TextView textView;
    Button addButton,updateButton,deleteButton,backButton;
    RadioButton radioButton;
    SQLiteDatabase db;
    StringBuilder stringBuilder;
    List<String> arrayList = new ArrayList<>();

    private static final int REC_RESULT = 653;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        radioGroup = findViewById(R.id.radioGroup);
        addButton=findViewById(R.id.button6);
        updateButton=findViewById(R.id.button4);
        deleteButton=findViewById(R.id.button8);
        backButton=findViewById(R.id.button7);
        textView=findViewById(R.id.textView);
        String s= "Παρακάτω εμφανίζονται οι δοσμένοι κωδικοί μετακίνησης με τα αντίστοιχα μηνύματα περιγραφής:";
        textView.setText(s);

        // Δημιουργούμε μια βάση αν δεν υπάρχει ήδη αλλίως αν υπάρχει αποκτάμε πρόσβαση σε αυτήν.
        db = openOrCreateDatabase("13033_Message_Database", Context.MODE_PRIVATE,null);

        retrieveDataFromDatabase();

    }

    // Μέθοδος που τραβάμε τα δεδομένα απο την SQL βάση που δημιουργήσαμε και τα εμφανίζουμε στον χρήστη.
    public void retrieveDataFromDatabase(){
        Cursor cursor = db.rawQuery("SELECT * FROM Message_Codes",new String[]{});
        if(cursor.getCount()>0) {
            //Δημιουργούμε έναν string builder.
            stringBuilder = new StringBuilder();
            // Όσο έχουμε δεδομένα στην βάση μας θα δημιουρείτε αυτόματα ένα radio button γία κάθε γραμμή στην βάση μας
            while (cursor.moveToNext()) {
                radioButton = new RadioButton(this);
                // Τραβάμε κάθε γραμμή της βάσης μας και την βάζουμε στον sting builder.
                stringBuilder.append(cursor.getInt(0)).append(". ").append(cursor.getString(1));
                arrayList.add(stringBuilder.toString());
                radioButton.setText(stringBuilder.toString());
                // Προσέτουμε το radio button για την κάθε γραμμή.
                radioGroup.addView(radioButton);
                // Καθαρίζουμε την λίστα μας και τον string builder.
                arrayList.clear();
                stringBuilder.delete(0,stringBuilder.length());
            }
        }
    }

    // Καλείτε με το πάτημα του κουμπιού με το βελάκι(back) και μας πάει στην προηγούμενη φόρμα.
    public void back(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Καλείτε με το πάτημα του κουμπιού "Προσθήκη".
    public void add(View view){
        openDialogAdd();
    }


    // Φτίαχνουμε ένα αντικείμενο για την κλάση AddDialog και στην συνέχεια εμφανίζουμε το γραφικό περιβάλλον που έχουμε φτιάξει.
    public void openDialogAdd(){
        AddDialog addDialog = new AddDialog();
        addDialog.show(getSupportFragmentManager(),"add dialog");
    }

    // Καλείτε με το πάτημα του κουμπιού "Διαγραφή".
    public void delete(View view){
        openDialogDelete();
    }

    // Φτίαχνουμε ένα αντικείμενο για την κλάση DeleteDialog και στην συνέχεια εμφανίζουμε το γραφικό περιβάλλον που έχουμε φτιάξει.
    public void openDialogDelete(){
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.show(getSupportFragmentManager(),"delete dialog");
    }

    // Καλείτε με το πάτημα του κουμπιού "Επεξεργασία".
    public void update(View view){
        openDialogUpdate();
    }

    // Φτίαχνουμε ένα αντικείμενο για την κλάση UpdateDialog και στην συνέχεια εμφανίζουμε το γραφικό περιβάλλον που έχουμε φτιάξει.
    public void openDialogUpdate(){
        UpdateDialog updateDialog = new UpdateDialog();
        updateDialog.show(getSupportFragmentManager(),"update dialog");
    }

    // Μέθοδος της UpdateDialog
    @Override
    public void applyChanges(int code, String message) {
        ContentValues contentValues = new ContentValues();
        String codeStr = String.valueOf(code);
        contentValues.put("code",code);
        contentValues.put("message",message);
        // Αννανεώνουμε τα δεδομένα της βάσης με τον κωδικό μηνύματος που όρισε ο χρήστης.
        db.update("Message_Codes",contentValues,"code=?",new String[]{codeStr});
        // Ξαναφορώνουμε τα δεδομένα από την βάση.
        radioGroup.removeAllViews();
        retrieveDataFromDatabase();
    }

    // Μέθοδος της DeleteDialog
    @Override
    public void Delete(String code) {
        // Σβήνουμε απο την βάση το μήνυμα με τον κωδικό που όρισε ο χρήστης.
        db.delete("Message_Codes","code=?",new String[]{code});
        // Ξαναφορώνουμε τα δεδομένα από την βάση.
        radioGroup.removeAllViews();
        retrieveDataFromDatabase();
    }

    // Μέθοδος της AddDialog
    @Override
    public void insertChanges(int code, String message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("code",code);
        contentValues.put("message", message);
        // Βάζουμε στην βάση που έχουμε φτιάξει τα καινούρια δεδομένα για τα μηνύματα μετακίνσης.
        db.insert("Message_Codes",null, contentValues);
        // Ξαναφορώνουμε τα δεδομένα από την βάση.
        radioGroup.removeAllViews();
        retrieveDataFromDatabase();
    }

    // Μέθοδος αναγώρισης φωνής που ενεργοιείται με το πάτημα του κουμπιού με το μικρόφωνο.
    public void recognizeVoice(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ δώστε μια εντολή.");
        startActivityForResult(intent, REC_RESULT);
    }

    // Όταν ο χρήστης πεί την κατάλληλη φωνητική εντολή πραγματοποιούντε οι παρακάτω ενέργειες.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REC_RESULT && resultCode == RESULT_OK) {

            assert data != null;
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.contains("προσθήκη")) {
                addButton.performClick();
            }
            if (matches.contains("επεξεργασία")){
                updateButton.performClick();
            }
            if (matches.contains("διαγραφή")){
                deleteButton.performClick();
            }
            if (matches.contains("πίσω")){
                backButton.performClick();
            }
        }
    }
}