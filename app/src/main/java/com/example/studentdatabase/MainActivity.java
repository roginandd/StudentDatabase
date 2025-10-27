package com.example.studentdatabase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    StudentAdapter adapter;
    List<Student> studentList;
    DBHelper dbHelper;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchView);
        // Initialize DB
        dbHelper = new DBHelper(this);
        studentList = dbHelper.getAllStudents();  // load all from DB

        // Set up adapter
        adapter = new StudentAdapter(this, studentList);
        recyclerView.setAdapter(adapter);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterList(newText);
                return true;
            }
        });
        // Swipe and move handler
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback(studentList, adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    static ItemTouchHelper.SimpleCallback callback(List<Student> persons, StudentAdapter adapter) {
        return new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(persons, from, to);
                adapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Student s = persons.get(position);

                // Delete from DB
                DBHelper db = new DBHelper(viewHolder.itemView.getContext());
                db.deleteStudent(s.getId());

                persons.remove(position);
                adapter.notifyItemRemoved(position);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            Intent intent = new Intent(this, AddMenu.class);
            startActivityForResult(intent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 0 && data != null) {
            String studentName = data.getStringExtra("studentName");
            String program = data.getStringExtra("program");
            String yearLevel = data.getStringExtra("yearLevel");
            String imageUri = data.getStringExtra("imageUri");

            String course = program + " - " + yearLevel;

            // ✅ Create and save student
            Student s = new Student(studentName, course, imageUri != null ? imageUri : "");
            dbHelper.addStudent(s);

            // ✅ Refresh list
        }

        if (resultCode == RESULT_OK)
            refreshStudentList();

    }
    private void refreshStudentList() {
        studentList.clear();
        studentList.addAll(dbHelper.getAllStudents());
        adapter.notifyDataSetChanged();
    }


}
