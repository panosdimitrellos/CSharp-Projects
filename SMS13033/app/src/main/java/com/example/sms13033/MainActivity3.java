package com.example.sms13033;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity3 extends AppCompatActivity {

    TextView textViewUsername,textViewEmail,textViewPass;
    EditText editTextUsername,editTextEmail,editTextPass;
    ImageView imageView;
    Button buttonSignin,buttonSignup;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    SharedPreferences preferencesForUser;
    private static final int REC_RESULT = 653;

    String sUsername;
    String sEmail;
    String sPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        //Ορίζουμε τα στοιχεία που εμφανίζονται στο activity μας.
        textViewUsername = findViewById(R.id.textView4);
        textViewUsername.setText(R.string.textview_username);
        textViewEmail = findViewById(R.id.textView2);
        textViewEmail.setText("Email");
        textViewPass = findViewById(R.id.textView3);
        textViewPass.setText("Κωδικός");
        editTextUsername=findViewById(R.id.editTextTextPersonName5);
        editTextEmail=findViewById(R.id.editTextTextPersonName3);
        editTextPass=findViewById(R.id.editTextTextPersonName4);
        imageView =findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.smsicon);
        buttonSignin = findViewById(R.id.button9);
        buttonSignup = findViewById(R.id.button11);


        mAuth = FirebaseAuth.getInstance();

        // Δημιουργούμε shared preferences για να αποθηκεύονται τα στοιχεία του χρήστη , έτσι ώστε να μην χρειάζεται να τα βάζει ο ίδιος κάθε φορά.
        preferencesForUser = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sUsername = preferencesForUser.getString("username","");
        editTextUsername.setText(sUsername);
        sEmail = preferencesForUser.getString("email","");
        editTextEmail.setText(sEmail);
        sPassword = preferencesForUser.getString("password","");
        editTextPass.setText(sPassword);

        //Παίρνουμε τον RESULT_CODE από το MainActivity.
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("resultcode", REC_RESULT);

    }

    // Μέθοδος σύνδεσης χρήστη.
    public void signin(View view){
        //Παίρνουμε τα δεδομένα που έβαλε ο χρήστης.
        sUsername= editTextUsername.getText().toString();
        sEmail = editTextEmail.getText().toString();
        sPassword = editTextPass.getText().toString();
        //Ελέγχουμε αν είναι όλα τα στοιχεία συμπληρωμένα.
        if(sUsername.isEmpty()||sEmail.isEmpty()||sPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Παρακαλώ συμπληρώστε όλα τα πεδία", Toast.LENGTH_LONG).show();
        }else {
            //Συνδέουμε τον υπάρχον χρήστη με την μέθοδο ταυτοποποίησης της Firebase με email και κωδίκο.
            mAuth.signInWithEmailAndPassword(sEmail, sPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        //Ελέγχουμε αν η ταυτοποίηση πέτυχε και αν ναι ορίζουμε τον current user της εφαρμογής. Στην συνέχεια τον ειδοποιούμε ότι συνδέθηκε με ένα μήνυμα Toast.
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = mAuth.getCurrentUser();
                                assert currentUser != null;
                                if(!sUsername.equals(currentUser.getDisplayName())){
                                    addNewUsernameToUser(sUsername,currentUser);
                                }
                                Toast.makeText(getApplicationContext(), "Καλώς ήρθες "+currentUser.getDisplayName()+" !", Toast.LENGTH_LONG).show();


                                // Βάζουμε στα shared preferences τα τελευταία δεδομένα που έβαλε ο χρήστης με την βοήθεια ενός editor.
                                SharedPreferences.Editor editor2 = preferencesForUser.edit();
                                editor2.putString("username",editTextUsername.getText().toString());
                                editor2.putString("email",editTextEmail.getText().toString());
                                editor2.putString("password",editTextPass.getText().toString());
                                editor2.apply();

                                //Μεταφέρουμε το ID του user στο επόμενο activity.
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("userID", currentUser.getUid());
                                startActivity(intent);
                            } else {
                                // Εμφανίζονται τα κατάλληλα μηνύματα στις περιπτώσεις όπου δεν υπάρχει ο χρήστης ή κάποιο απο τα στοχεία που έβαλε ο χρήστης είναι λανθασμένα.
                                Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();

                            }

                        }
                    });
        }
    }

    // Μέθοδος εγγραφής χρήστη.
    public void signup(View view){
        //Παίρνουμε τα δεδομένα που έβαλε ο χρήστης.
        sUsername= editTextUsername.getText().toString();
        sEmail = editTextEmail.getText().toString();
        sPassword = editTextPass.getText().toString();
        //Ελέγχουμε αν είναι όλα τα στοιχεία συμπληρωμένα.
        if(sUsername.isEmpty()||sEmail.isEmpty()||sPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Παρακαλώ συμπληρώστε όλα τα πεδία", Toast.LENGTH_LONG).show();
        }else {
            // Δημιουργούμε έναν χρήστη με την μέθοδο της Firebase με email και κωδικό.
            mAuth.createUserWithEmailAndPassword(sEmail, sPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        //Ελέγχουμε αν η εγγραφή πέτυχε και αν ναι ορίζουμε τον current user της εφαρμογής. Στην συνέχεια τον ειδοποιούμε ότι ο χρήστης εγγράφηκε με ένα μήνυμα Toast.
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = mAuth.getCurrentUser();
                                //Προσθέτουμε καινούργιο username στον χρήστη εφόσον αυτός υπάρχει.
                                assert currentUser != null;
                                addUsernameToUser(sUsername, currentUser);
                                Toast.makeText(getApplicationContext(), "'Εγινε επιτυχής εγγραφή !", Toast.LENGTH_LONG).show();

                                // Βάζουμε στα shared preferences τα τελευταία δεδομένα που έβαλε ο χρήστης με την βοήθεια ενός editor.
                                SharedPreferences.Editor editor2 = preferencesForUser.edit();
                                editor2.putString("username",editTextUsername.getText().toString());
                                editor2.putString("email",editTextEmail.getText().toString());
                                editor2.putString("password",editTextPass.getText().toString());
                                editor2.apply();

                                //Μεταφέρουμε το ID του user στο επόμενο activity.
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("userID", currentUser.getUid());
                                startActivity(intent);
                            } else {
                                // Εμφανίζονται τα κατάλληλα μηνύματα στις περιπτώσεις όπου υπάρχει ήδη χρήστης με το συγκεκριμένο email ή κάποιο απο τα στοχεία που έβαλε ο χρήστης είναι λανθασμένα.
                                Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    // Μέθοδος προσθήκης username στον καινούργιο χρήστη.
    public void addUsernameToUser(String username, FirebaseUser user){
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        // Κάνουμε update το προφίλ του χρήστη.
        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "Το username σας αποθηκεύτηκε.",Toast.LENGTH_LONG).show();
            }
        });
    }

    // Μέθοδος προσθήκης αλλαγής username στον υπάρχον χρήστη.
    public void addNewUsernameToUser(String username, FirebaseUser user){
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        // Κάνουμε update το προφίλ του χρήστη.
        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "Το username σας αποθηκεύτηκε.",Toast.LENGTH_LONG).show();
            }
        });
    }

    // Μεθόδοι αναγώρισης φωνής
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
            if (matches.contains("σύνδεση")) {
                buttonSignin.performClick();
            }
            if (matches.contains("εγγραφή")) {
                buttonSignup.performClick();
            }
        }
    }

    // Δείχνει μήνυμα στον χρήστη.
    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    // Ενεργοποείτε με το πάτημα του κουμπιού i και δείχνει οδηγίες χρήσρης των φωνητικών εντολών.
    public void info(View view){
        showMessage("Πληροφορίες χρήσης φωνητικών εντολών",
                "Πατήστε το κουμπί με την ένδειξη του μικροφώνου.\n"+
                        "---------------------------------------------------------\n"+
                        "Για να συνδεθείτε πείτε την λέξη 'σύνδεση'.\n"+
                        "---------------------------------------------------------\n"+
                        "Για να εγγραφείτε πείτε την λέξη 'εγγραφή'.");
    }
}