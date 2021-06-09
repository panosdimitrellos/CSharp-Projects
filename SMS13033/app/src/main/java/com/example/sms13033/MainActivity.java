package com.example.sms13033;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    EditText name ,address;
    RadioGroup radioGroup;
    Button editButton,sendButton;
    RadioButton radioButton;

    SharedPreferences preferences;
    int code;
    String codeMessages;
    String message;

    int i = 0;
    StringBuilder stringBuilder = new StringBuilder();
    List<String> arrayList = new ArrayList<>();

    SQLiteDatabase db;

    String sUserID;
    FirebaseDatabase database;
    DatabaseReference myRef;

    String strCurrentLocation;
    String strDateTime;
    LocationManager locationManager;

    int REC_RESULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.editTextTextPersonName);
        address = findViewById(R.id.editTextTextPersonName2);
        radioGroup = findViewById(R.id.radioGroup);
        editButton = findViewById(R.id.button2);
        sendButton = findViewById(R.id.button);

        // Δημιουργούμε shared preferences για να αποθηκεύονται το ονοματεπώνυμο και η διεύθυνση του χρήστη , έτσι ώστε να μην χρειάζεται να τα βάζει κάθε φορά εκείνος.
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sName = preferences.getString("name","");
        name.setText(sName);
        String sAddress = preferences.getString("address","");
        address.setText(sAddress);

        REC_RESULT = getIntent().getIntExtra("resultcode",653);

        // Δημιουργούμε μια βάση αν δεν υπάρχει ήδη στην οποία θα αποθηκεύουμε τον κωδικό και την περιγραφή των μηνυμάτων μετακίνησης.
        db = openOrCreateDatabase("13033_Message_Database", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Message_Codes(code INTEGER PRIMARY KEY,message TEXT)");

        fillDatabase();
        retrieveDataFromDatabase();

        // Ζητάμε για permissions για αποστολή SMS.
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},5434);
        }


        // Απο το MainAcivity3 τραβάμε το ID του user.
        sUserID = getIntent().getStringExtra("userID");

        //Φτιάχνουμε σην Firebase το path που θα αποθηκεύει τα δεδομένα του κάθε χρήστη .
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users/"+sUserID);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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


    // Μέθοδος που γεμίζουμε με sql queries τήν αρχική βάση μας με τον δωσμένο κωδικό και την περιγραφή του.
    public void fillDatabase(){
        code=1;
        codeMessages="Μετάβαση σε φαρμακείο ή επίσκεψη στον γιατρό.";
        db.execSQL("INSERT OR REPLACE INTO Message_Codes VALUES('" + code + "','" + codeMessages + "')");

        code = 2;
        codeMessages="Μετάβαση σε εν λειτουργία κατάστημα προμηθειών αγαθών πρώτης ανάγκης.";
        db.execSQL("INSERT OR REPLACE INTO Message_Codes VALUES('" + code + "','" + codeMessages + "')");

        code = 3;
        codeMessages = "Μετάβαση στην τράπεζα.";
        db.execSQL("INSERT OR REPLACE INTO Message_Codes VALUES('" + code + "','" + codeMessages + "')");

        code=4;
        codeMessages="Παροχή βοήθειας σε ανθρώπους που βρίσκονται σε ανάγκη ή συνοδεία ανηλίκων μαθητών από/προς το σχολείο.";
        db.execSQL("INSERT OR REPLACE INTO Message_Codes VALUES('" + code + "','" + codeMessages + "')");

        code=5;
        codeMessages="Μετάβαση σε τελετή κηδείας ή μετάβαση διαζευγμένων γονέων σε τέκνα.";
        db.execSQL("INSERT OR REPLACE INTO Message_Codes VALUES('" + code + "','" + codeMessages + "')");


        code=6;
        codeMessages="Σωματική άσκηση σε εξωτερικό χώρο ή κίνηση με κατοικίδιο ζώο.";
        db.execSQL("INSERT OR REPLACE INTO Message_Codes VALUES('" + code + "','" + codeMessages + "')");
    }

    // Με αυτήν την μέθοδο στένουμε SMS στο 13033.
    public  void sendSMS(View view){
        String telephone = "13033";

        // Ορίζουμε ως κωδικό το ID απο το τσεκαρισμένο radio button .
        if((radioGroup.getCheckedRadioButtonId() != -1)){
            code= radioGroup.getCheckedRadioButtonId();
        }

        // Φτιάχνουμε το μήνυμα που θα στείλουμε.
        message = code+" "+name.getText().toString()+" "+address.getText().toString();
        SmsManager manager = SmsManager.getDefault();
        // Στέλνουμε το μήνυμα.
        manager.sendTextMessage(telephone, null, message, null, null);
        Toast.makeText(this,"Το μήνυμα στάλθηκε επιτυχώς!", Toast.LENGTH_LONG).show();

        // Βάζουμε στα shared preferences τα τελευταία δεδομένα που έβαλε ο χρήστης με την βοήθεια ενός editor.
        SharedPreferences.Editor editor1 = preferences.edit();
        editor1.putString("name",name.getText().toString());
        editor1.putString("address",address.getText().toString());
        editor1.apply();

        gps();

    }


    // Καλείτε με το πάτημα του κουμπιού με το μολυβάκι(edit) και μας πάει στην επόμενη φόρμα.
    public void edit(View view){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    // Καλείτε με το πάτημα του κουμπιού με το βελάκι(back) και μας πάει στην προηγούμενη φόρμα.
    public void back(View view){
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
    }

    // Μέθοδος που εμφανίζει ένα μήνυμα στον χρήστη.
    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .show();
    }


    // Καλείτε με το πάτημα του κουμπιού με το i(info) και μας μήνυμα με τη πληροφορίες για την χρήση φωνητικών εντολών .
    public void info(View view){
        showMessage("Πληροφορίες χρήσης φωνητικών εντολών",
                "Πατήστε το κουμπί με την ένδειξη του μικροφώνου.\n"+
                        "---------------------------------------------------------\n"+
                        "Για να γράψετε το ονοματεπώνυμο πείτε την λέξη 'όνομα' και στην συνέχεια το όνοματεπώνυμο σας.\n" +
                        "---------------------------------------------------------\n"+
                        "Για να γράψετε την διεύθυνση πείτε την λέξη 'διεύθυνση' και στην συνέχεια την διεύθυνση σας.\n"+
                        "---------------------------------------------------------\n"+
                        "Για να επιλέξετε κωδικό μετακίνησης πείτε την λέξη 'μετακίνηση' και στην συνέχεια τον αριθμό κωδικού που θέλετε.\n"+
                        "---------------------------------------------------------\n"+
                        "Για να επεξεργαστείτε τα μηνύματα μετακίνησης πείτε 'επεξεργασία'.\n"+
                        "---------------------------------------------------------\n"+
                        "Για να στείλετε το μήνυμα πείτε 'αποστολή'.");
    }


    // Μέθοδοι αναγώρισης φωνής που ενεργοιείται με το πάτημα του κουμπιού με το μικρόφωνο.
    public void recognizeVoice(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ δώστε μια εντολή.");
        startActivityForResult(intent, REC_RESULT);
    }

    public void recognizeVoiceForName(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ πείτε το όνομα σας.");
        startActivityForResult(intent, REC_RESULT);
    }

    public void recognizeVoiceForAddress(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ πείτε την διεύθυνση σας.");
        startActivityForResult(intent, REC_RESULT);
    }

    public void recognizeVoiceForCode(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ επιλέξτε τον κωδικό μετακίνησης σας.");
        startActivityForResult(intent, REC_RESULT);
    }

    // Όταν ο χρήστης πεί την κατάλληλη φωνητική εντολή πραγματοποιούντε οι παρακάτω ενέργειες.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(i == 1){
            if (requestCode == REC_RESULT && resultCode == RESULT_OK) {
                assert data != null;
                ArrayList<String> matches2 = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                name.setText(matches2.get(0));
            }
            i=0;
        }
        else if(i == 2){
            if (requestCode == REC_RESULT && resultCode == RESULT_OK) {
                assert data != null;
                ArrayList<String> matches3 = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                address.setText(matches3.get(0));

            }
            i=0;
        }
        else if(i == 3){
            if (requestCode == REC_RESULT && resultCode == RESULT_OK) {

                int radioButtonID;
                assert data != null;
                ArrayList<String> matches4 = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                // Όταν ο χρήστης πει τον κατάλληλο αριθμό κωδικού τότε γίνεται τσεκ το radio button με τον κωδικό που επέλεξε.
                if(matches4.contains("1")){
                    radioButtonID=1;
                    radioGroup.check(radioButtonID);
                }
                else if(matches4.contains("δύο")){
                    radioButtonID=2;
                    radioGroup.check(radioButtonID);
                }
                else if(matches4.contains("3")){
                    radioButtonID=3;
                    radioGroup.check(radioButtonID);
                }
                else if(matches4.contains("4")){
                    radioButtonID=4;
                    radioGroup.check(radioButtonID);
                }
                else if(matches4.contains("5")){
                    radioButtonID=5;
                    radioGroup.check(radioButtonID);
                }
                else if(matches4.contains("6")){
                    radioButtonID=6;
                    radioGroup.check(radioButtonID);
                }
                else if(matches4.contains("7")){
                    radioButtonID=7;
                    radioGroup.check(radioButtonID);
                }
                i=0;
            }
        }

        // Καλείται αρχίκα και ο χρήστης δίνει την κατάλλη φωνητική εντολή που θέλει να κάνει.
        if(i==0) {
            if (requestCode == REC_RESULT && resultCode == RESULT_OK) {
                assert data != null;
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                // Το i αλλάζει όταν δώσει μια φωνητική εντολή ο χρήστης ετσι ώστε να του εμφανιστεί το κατάλληλο
                // μήνυμα και να το κατατοπίσει η εφαρμογή στην εντολή που περιμένει να ακούσει.
                if (matches.contains("όνομα")) {
                    i = 1;
                    recognizeVoiceForName();
                }
                if (matches.contains("διεύθυνση")) {
                    i = 2;
                    recognizeVoiceForAddress();
                }
                if (matches.contains("μετακίνηση")){
                    i=3;
                    recognizeVoiceForCode();
                }
                if(matches.contains("επεξεργασία")){
                    editButton.performClick();
                }
                if(matches.contains("αποστολή")){
                    sendButton.performClick();
                }
            }
        }
    }

    // Μέθδος που καλλείτε για να παρθούν τα δικαιώματα για την χρήση της τοποθεσίας του χρήστη.
    public void gps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        // Κάθε φορά που καλείται η μέθοδος , παίρνετε και το timestamp που έγινε η ενέργεια.
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyy hh:mm:ss a");
        strDateTime = simpleDateFormat.format(calendar.getTime());

    }

    // Με την μέθοδο αυτή παίρνουμε την τοποθεσία του χρήστη.
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Ορίζουμε το γεωγραφικό μήκος και πλάτος.
        double x = location.getLatitude();
        double y = location.getLongitude();
        strCurrentLocation = String.valueOf(x)+" , "+String.valueOf(y);

        writeData();
    }


    // Γράφουμε στο path που έχουμε ορίσει στην Firebase την τοποθεσία και το timestamp του χρήστη και καλούμε αυτην την μέθοδο όταν σταλθεί το μήνυμα.
    public void writeData(){
        myRef.setValue("Location: "+strCurrentLocation+" "+"Timestamp: "+strDateTime);
    }
}