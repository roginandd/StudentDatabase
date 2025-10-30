package com.example.studentdatabase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class AddMenu extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner programSpinner;
    Button saveButton, cancelButton;
    EditText studentName;
    ImageView personImg;

    String selectedProgram = "";
    Uri selectedImageUri = null;

    boolean isEdit = false;
    int studentId = -1;

    // ✅ Image picker
    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    resizeAndSetImage(uri);
                    final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_student_layout);
        EdgeToEdge.enable(this);

        // 🔹 Initialize UI
        studentName = findViewById(R.id.editTextText);
        programSpinner = findViewById(R.id.spinner);
        saveButton = findViewById(R.id.button);
        cancelButton = findViewById(R.id.button2);
        personImg = findViewById(R.id.person);

        // 🔹 Default image
        personImg.setImageResource(R.drawable.baseline_person_24);

        // Spinner listener
        programSpinner.setOnItemSelectedListener(this);

        // Image picker
        personImg.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));

        // Check if edit mode
        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);

        if (isEdit) {
            setupEditMode(intent);
        } else {
            saveButton.setOnClickListener(v -> addStudent());
        }

        cancelButton.setOnClickListener(v -> finish());
    }

    /**
     * ✅ Setup edit mode
     */
    private void setupEditMode(Intent intent) {
        studentId = intent.getIntExtra("id", -1);
        String name = intent.getStringExtra("name");
        String course = intent.getStringExtra("course");
        String image = intent.getStringExtra("image");

        studentName.setText(name);

        // Set spinner based on existing course
        if (course != null) setSpinnerSelection(programSpinner, course);

        // Prefill image if exists
        if (image != null && !image.isEmpty()) {
            selectedImageUri = Uri.parse(image);
            resizeAndSetImage(selectedImageUri);
        }

        saveButton.setText("Update");
        saveButton.setOnClickListener(v -> updateStudent());
    }

    /**
     * ✅ Add new student
     */
    private void addStudent() {
        String nameInput = studentName.getText().toString().trim();

        if (nameInput.isEmpty()) {
            Toast.makeText(this, "Name field cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProgram.isEmpty()) {
            Toast.makeText(this, "Please select a program", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriStr = selectedImageUri.toString();

        DBHelper db = new DBHelper(this);
        long result = db.addStudent(new Student(nameInput, selectedProgram, imageUriStr));

        if (result != -1) {
            Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "Insert failed", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /**
     * ✅ Update existing student
     */
    private void updateStudent() {
        String nameInput = studentName.getText().toString().trim();

        if (nameInput.isEmpty() || selectedProgram.isEmpty()) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

        DBHelper db = new DBHelper(this);
        Student updated = new Student(studentId, nameInput, selectedProgram, imageUriStr);
        int result = db.updateStudent(updated);

        if (result > 0) {
            Toast.makeText(this, "Student updated", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /**
     * ✅ Resize and show selected image
     */
    private void resizeAndSetImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if (originalBitmap != null) {
                int maxSize = 500;
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                float ratio = (float) width / height;

                int finalWidth, finalHeight;
                if (ratio > 1) {
                    finalWidth = maxSize;
                    finalHeight = (int) (maxSize / ratio);
                } else {
                    finalHeight = maxSize;
                    finalWidth = (int) (maxSize * ratio);
                }

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true);
                personImg.setImageBitmap(resizedBitmap);
            }

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ Set spinner value
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
        selectedProgram = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
