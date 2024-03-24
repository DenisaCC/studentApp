package com.example.licentaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaapp.Connection.ConnectionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    Button registerbtn, backbtn;
    EditText lastname, firstname, number, email, username, password;
    TextView status;
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    List<String> facultyNames = new ArrayList<>();
    Connection con;
    Statement stmt;
    private Uri selectedImageUri;
    private static final int REQUEST_FILE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerbtn = findViewById(R.id.registerbtn);
        lastname = findViewById(R.id.lastname);
        firstname = findViewById(R.id.firstname);
        number = findViewById(R.id.number);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        ImageButton backButton = findViewById(R.id.backbtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adăugați aici codul pentru redirecționare către activitatea anterioară sau unde doriți să fie redirecționat utilizatorul
                // De exemplu:
                Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });


        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    new RegisterUserTask().execute("");
                }
            }
        });

        TextView signinText = findViewById(R.id.signinText);
        signinText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deschideți o altă activitate pentru înregistrare
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        populateFaculties();
    }

    private boolean validateInputs() {
        String lastnameStr = lastname.getText().toString().trim();
        String firstnameStr = firstname.getText().toString().trim();
        String numberStr = number.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String facultyStr = autoCompleteTextView.getText().toString().trim();

        if (TextUtils.isEmpty(lastnameStr) || TextUtils.isEmpty(firstnameStr) || TextUtils.isEmpty(numberStr) ||
                TextUtils.isEmpty(emailStr) || TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passwordStr) ||
                TextUtils.isEmpty(facultyStr)) {
            Toast.makeText(RegisterActivity.this, "Completați toate câmpurile", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(emailStr)) {
            Toast.makeText(RegisterActivity.this, "Adresa de e-mail invalidă", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidPassword(passwordStr)) {
            Toast.makeText(RegisterActivity.this, "Parola trebuie să conțină cel puțin 8 caractere, o cifră și o majusculă", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && hasUpperCaseAndDigit(password);
    }

    private boolean hasUpperCaseAndDigit(String password) {
        boolean hasDigit = false;
        boolean hasUpperCase = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
            if (hasDigit && hasUpperCase) {
                return true;
            }
        }
        return false;
    }

    public class RegisterUserTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            email.setText("");
            username.setText("");
            password.setText("");
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                con = connectionClass(ConnectionClass.un, ConnectionClass.pass, ConnectionClass.db, ConnectionClass.ip, ConnectionClass.port);
                if (con == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "Verificați conexiunea la internet", Toast.LENGTH_LONG).show();
                        }
                    });
                    return "Fără conexiune la internet";
                } else {
                    String facultyName = autoCompleteTextView.getText().toString();
                    String facultateIDQuery = "SELECT IDFacultate FROM Facultate WHERE Denumire = ?";
                    PreparedStatement pstmtFacultateID = con.prepareStatement(facultateIDQuery);
                    pstmtFacultateID.setString(1, facultyName);
                    ResultSet rsFacultateID = pstmtFacultateID.executeQuery();
                    int facultateID = 0;
                    if (rsFacultateID.next()) {
                        facultateID = rsFacultateID.getInt("IDFacultate");
                    }

                    // Verificare și inserare în tabelul Utilizator
                    String emailStr = email.getText().toString().trim();
                    String usernameStr = username.getText().toString().trim();
                    String passwordStr = password.getText().toString().trim();
                    String nrMatricolStr = number.getText().toString().trim();

                    // Obține calea către imaginea de profil
                    String imagePath = selectedImageUri.toString(); // Converteste URI-ul in string

                    // Verificați dacă datele corespund cu cele din tabelul Student
                    // Pentru aceasta, puteți efectua o interogare suplimentară în baza de date
                    // și comparați datele introduse cu cele din tabelul Student

                    // Dacă datele sunt valide, inserați-le în tabelul Utilizator
                    String sql = "INSERT INTO Utilizator (Email, NumeUtilizator, Parola, NrMatricol, ImagineProfil) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = con.prepareStatement(sql);
                    pstmt.setString(1, emailStr);
                    pstmt.setString(2, usernameStr);
                    pstmt.setString(3, passwordStr);
                    pstmt.setString(4, nrMatricolStr);
                    pstmt.setString(5, imagePath); // Adaugă calea către imaginea de profil
                    pstmt.executeUpdate();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                return e.getMessage();
            }
            return null;
        }

    }

    private void populateFaculties() {
        try {
            con = connectionClass(ConnectionClass.un, ConnectionClass.pass, ConnectionClass.db, ConnectionClass.ip, ConnectionClass.port);
            if (con != null) {
                String query = "SELECT Denumire FROM Facultate";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    String facultyName = rs.getString("Denumire");
                    facultyNames.add(facultyName);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, facultyNames);
                autoCompleteTextView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

    @SuppressLint("NewApi")
    public Connection connectionClass(String user, String password, String db, String ip, String port) {
        StrictMode.ThreadPolicy a = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(a);
        Connection connection = null;
        String ConnectURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + db + ";user=" + user + ";" + "password=" + password + ";";
            connection = DriverManager.getConnection(ConnectURL);
        } catch (Exception e) {
            Log.e("Error is", e.getMessage());
        }
        return connection;
    }

    public void OnClickButtonListener() {
        backbtn = findViewById(R.id.backbtn);
        backbtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }

    public void onChangeProfilePhotoClick(View view) {
        // Creează un Intent pentru a deschide galeria de imagini
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Specifică tipul de fișiere pe care utilizatorul poate să le selecteze
        startActivityForResult(intent, REQUEST_FILE_IMAGE);
    }

    // Overridează metoda onActivityResult pentru a procesa rezultatul selectării imaginii din galerie
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Extrage URI-ul imaginii selectate din galerie
            selectedImageUri = data.getData();

            // Setează imaginea în ImageView-ul destinat fotografiei de profil
            ImageView userImageView = findViewById(R.id.userImage);
            userImageView.setImageURI(selectedImageUri);

            // Aici poți să procesezi imaginea (de exemplu, să o redimensionezi sau să o salvezi într-un format specific)
            // Pentru a redimensiona imaginea sau a o salva, vei avea nevoie să obții un Bitmap din URI
            // și să folosești o bibliotecă precum Glide sau Picasso pentru a manipula imaginea.
        }
    }
}
