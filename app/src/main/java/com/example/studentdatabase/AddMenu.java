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

    Spinner spinner;
    Button saveButton, cancelButton;
    EditText studentName, program;
    ImageView personImg;
    String[] yearLevels;
    String yearLevel;
    Uri selectedImageUri = null; // store picked image URI

    boolean isEdit = false;
    int studentId = -1; // used for updates

    // ✅ Open Files app for image selection
    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    resizeAndSetImage(uri); // 🔥 resize before showing

                    // ✅ persist access
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


        yearLevels = getResources().getStringArray(R.array.year_lvl);
        studentName = findViewById(R.id.editTextText);
        program = findViewById(R.id.editTextText2);
        spinner = findViewById(R.id.spinner);
        saveButton = findViewById(R.id.button);
        cancelButton = findViewById(R.id.button2);
        personImg = findViewById(R.id.person);

        // Default placeholder image
        personImg.setImageResource(R.drawable.baseline_person_24);

        // ✅ Tap to open Files app
        personImg.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));

        // 🔹 Check if this is EDIT mode
        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);

        if (isEdit) {
            studentId = intent.getIntExtra("id", -1);
            String name = intent.getStringExtra("name");
            String course = intent.getStringExtra("course");
            String image = intent.getStringExtra("image");

            // Prefill fields
            studentName.setText(name);

            // Extract program and year if formatted as "BSIT - 3"
            if (course != null && course.contains(" - ")) {
                String[] parts = course.split(" - ");
                program.setText(parts[0]);
                String year = parts.length > 1 ? parts[1] : "";
                setSpinnerSelection(year);
            } else {
                program.setText(course);
            }

            // Prefill image
            if (image != null && !image.isEmpty()) {
                selectedImageUri = Uri.parse(image);
                resizeAndSetImage(selectedImageUri);
            }

            // Change button label
            saveButton.setText("Update");

            // ✅ Update existing student
            saveButton.setOnClickListener(view -> updateStudent());

        } else {
            // ✅ Normal add mode
            saveButton.setOnClickListener(view -> addStudent());
        }

        cancelButton.setOnClickListener(view -> finish());
        spinner.setOnItemSelectedListener(this);
    }

    /**
     * ✅ Add new student
     */
    private void addStudent() {
        String nameInput = studentName.getText().toString().trim();
        String programInput = program.getText().toString().trim();
        String yearInput = spinner.getSelectedItem().toString();

        if (nameInput.isEmpty() || programInput.isEmpty()) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return;
        }

        String course = programInput + " - " + yearInput;
        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

        DBHelper db = new DBHelper(this);
        long result = db.addStudent(new Student(nameInput, course, imageUriStr));

        if (result != -1) {
            Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // ✅ no Intent needed
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
        String programInput = program.getText().toString().trim();
        String yearInput = spinner.getSelectedItem().toString();

        if (nameInput.isEmpty() || programInput.isEmpty()) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return;
        }

        String course = programInput + " - " + yearInput;
        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

        DBHelper db = new DBHelper(this);
        Student updated = new Student(studentId, nameInput, course, imageUriStr);
        int result = db.updateStudent(updated);

        if (result > 0) {
            Toast.makeText(this, "Student updated", Toast.LENGTH_SHORT).show();

            // ✅ Now set result and close activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * ✅ Resize and show selected image to avoid large memory usage
     */
    private void resizeAndSetImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if (originalBitmap != null) {
                int maxSize = 500;
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                float ratio = (float) width / (float) height;

                int finalWidth = maxSize;
                int finalHeight = maxSize;

                if (ratio > 1) {
                    finalHeight = (int) (maxSize / ratio);
                } else {
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

    // ✅ Helper to select correct year level
    private void setSpinnerSelection(String year) {
        for (int i = 0; i < yearLevels.length; i++) {
            if (yearLevels[i].equalsIgnoreCase(year)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, android.view.View view, int i, long l) {
        yearLevel = spinner.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}
