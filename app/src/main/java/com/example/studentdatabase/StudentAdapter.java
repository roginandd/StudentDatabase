package com.example.studentdatabase;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentHolder> {

    private Context context;
    private List<Student> studentList;
    private List<Student> allStudents; // For search filtering

    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
        this.allStudents = new ArrayList<>(studentList);
    }


    // --- Inflate row layout ---
    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_layout, parent, false);
        return new StudentHolder(view);
    }

    // --- Bind data to holder ---
    @Override
    public void onBindViewHolder(@NonNull StudentHolder holder, int position) {
        Student student = studentList.get(position);

        holder.txtName.setText(student.getName());
        holder.txtCourse.setText(student.getCourse());

        // Display image (if available)
        if (student.getImage() != null && !student.getImage().isEmpty()) {
            try {
                Uri uri = Uri.parse(student.getImage());
                System.out.print("URI: "+ uri);
                holder.imgStudent.setImageURI(uri);
            } catch (Exception e) {
                holder.imgStudent.setImageResource(R.drawable.baseline_person_24);
            }
        } else {
            holder.imgStudent.setImageResource(R.drawable.baseline_person_24);
        }

        // ✅ Popup menu appears when icon is tapped
        holder.menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.menuButton);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.mypopup, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item, position));
            popupMenu.show();
        });
    }


    private boolean handleMenuClick(MenuItem item, int position) {
        Student s = studentList.get(position);
        DBHelper db = new DBHelper(context);

        if (item.getItemId() == R.id.edit) {
            // ✅ Create intent to open AddMenu for editing
            Intent intent = new Intent(context, AddMenu.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("id", s.getId());
            intent.putExtra("name", s.getName());
            intent.putExtra("course", s.getCourse());
            intent.putExtra("image", s.getImage());

            // ✅ Start AddMenu
            context.startActivity(intent);
            return true;

        } else if (item.getItemId() == R.id.delete) {
            db.deleteStudent(s.getId());
            studentList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Deleted " + s.getName(), Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // --- Filtering method for SearchView/EditText ---
    public void filterList(String query) {
        studentList.clear();
        if (query.isEmpty()) {
            studentList.addAll(allStudents);
        } else {
            query = query.toLowerCase().trim();
            for (Student s : allStudents) {
                if (s.getName().toLowerCase().contains(query) ||
                        s.getCourse().toLowerCase().contains(query)) {
                    studentList.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    // ==========================
    // 🎓 INNER HOLDER CLASS
    // ==========================
    public class StudentHolder extends RecyclerView.ViewHolder {
        ImageView imgStudent, menuButton;
        TextView txtName, txtCourse;

        public StudentHolder(@NonNull View itemView) {
            super(itemView);
            imgStudent = itemView.findViewById(R.id.studentImage);
            txtName = itemView.findViewById(R.id.studentName);
            txtCourse = itemView.findViewById(R.id.studentCourse);
            menuButton = itemView.findViewById(R.id.menuButton); // added menu icon reference

        }
    }
}
