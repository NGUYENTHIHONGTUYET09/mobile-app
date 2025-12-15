package com.example.mobileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Step2PersonalInfoActivity extends AppCompatActivity {
    private Spinner spinnerGender;
    private EditText etAge, etHeight, etWeight;
    private Button btnNext;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2_personal_info);

        prefs = getSharedPreferences("FashionAssistant", MODE_PRIVATE);

        spinnerGender = findViewById(R.id.spinnerGender);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnNext = findViewById(R.id.btnNext);

        // Setup gender spinner
        String[] genders = {"Chọn giới tính", "Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        btnNext.setOnClickListener(v -> saveAndContinue());
    }

    private void saveAndContinue() {
        String gender = spinnerGender.getSelectedItem().toString();
        String age = etAge.getText().toString();
        String height = etHeight.getText().toString();
        String weight = etWeight.getText().toString();

        if (gender.equals("Chọn giới tính") || age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("gender", gender);
        editor.putString("age", age);
        editor.putString("height", height);
        editor.putString("weight", weight);
        editor.apply();

        Intent intent = new Intent(this, Step3PreferencesActivity.class);
        startActivity(intent);
        finish();
    }
}

