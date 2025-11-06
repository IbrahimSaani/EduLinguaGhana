package com.edulinguaghana;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnEnglish, btnFrench, btnTwi, btnEwe, btnGa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link buttons to the layout
        btnEnglish = findViewById(R.id.btnEnglish);
        btnFrench  = findViewById(R.id.btnFrench);
        btnTwi     = findViewById(R.id.btnTwi);
        btnEwe     = findViewById(R.id.btnEwe);
        btnGa      = findViewById(R.id.btnGa);

        // Click listeners
        btnEnglish.setOnClickListener(v -> showLanguageSelected("English"));
        btnFrench.setOnClickListener(v -> showLanguageSelected("French"));
        btnTwi.setOnClickListener(v -> showLanguageSelected("Twi"));
        btnEwe.setOnClickListener(v -> showLanguageSelected("Ewe"));
        btnGa.setOnClickListener(v -> showLanguageSelected("Ga"));
    }

    private void showLanguageSelected(String language) {
        Toast.makeText(this, "Selected: " + language, Toast.LENGTH_SHORT).show();
        // Later: open alphabet/number screen for this language
    }
}