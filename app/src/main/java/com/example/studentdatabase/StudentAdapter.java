package com.example.studentdatabase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentHolder> {

    private final Context context;
    private final List<Student> studentList;
    private final List<Student> allStudents;

    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = new ArrayList<>(studentList);
        this.allStudents = new ArrayList<>(studentList);
    }

    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_layout, parent, false);
        return new StudentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentHolder holder, int position) {
        Student student = studentList.get(position);

        holder.txtName.setText(student.getName());
        holder.txtCourse.setText(student.getCourse());

        // --- Load & resize image ---
        if (student.getImage() != null && !student.getImage().isEmpty()) {
            try {
                Uri uri = Uri.parse(student.getImage());
                resizeAndSetImage(holder.imgStudent, uri);
            } catch (Exception e) {
                holder.imgStudent.setImageResource(R.drawable.baseline_person_24);
            }
        } else {
            holder.imgStudent.setImageResource(R.drawable.baseline_person_24);
        }

        // --- Popup menu (Edit/Delete) ---
        holder.menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.menuButton);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.mypopup, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int safePos = holder.getBindingAdapterPosition();
                if (safePos == RecyclerView.NO_POSITION || safePos >= studentList.size())
                    return false;
                return handleMenuClick(item, safePos);
            });

            popupMenu.show();
        });
    }

    // --- Resize image before showing ---
    private void resizeAndSetImage(ImageView imageView, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (originalBitmap != null) {
                int maxSize = 500;
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                float ratio = (float) width / height;

                int finalWidth = ratio > 1 ? maxSize : (int) (maxSize * ratio);
                int finalHeight = ratio > 1 ? (int) (maxSize / ratio) : maxSize;

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true);
                imageView.setImageBitmap(resizedBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.baseline_person_24);
            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Handle edit/delete actions ---
    private boolean handleMenuClick(MenuItem item, int position) {
        if (position < 0 || position >= studentList.size()) return false;
        Student s = studentList.get(position);
        DBHelper db = new DBHelper(context);

        if (item.getItemId() == R.id.edit) {
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                Intent intent = new Intent(context, AddMenu.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("id", s.getId());
                intent.putExtra("name", s.getName());
                intent.putExtra("course", s.getCourse());  // ex: BSIT
                intent.putExtra("image", s.getImage());
                activity.startActivityForResult(intent, 1);
            }
            return true;

        } else if (item.getItemId() == R.id.delete) {
            db.deleteStudent(s.getId());
            studentList.remove(position);
            allStudents.remove(s);
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

    // --- Filter by name or course ---
    public void filterList(String query) {
        studentList.clear();
        if (query == null || query.trim().isEmpty()) {
            studentList.addAll(allStudents);
        } else {
            query = query.toLowerCase().trim();
            for (Student s : allStudents) {
                if ((s.getName() != null && s.getName().toLowerCase().contains(query)) ||
                        (s.getCourse() != null && s.getCourse().toLowerCase().contains(query))) {
                    studentList.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    // --- Refresh data when returning from edit ---
    public void updateData(List<Student> newList) {
        studentList.clear();
        studentList.addAll(newList);
        allStudents.clear();
        allStudents.addAll(newList);
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    public static class StudentHolder extends RecyclerView.ViewHolder {
        ImageView imgStudent, menuButton;
        TextView txtName, txtCourse;

        public StudentHolder(@NonNull View itemView) {
            super(itemView);
            imgStudent = itemView.findViewById(R.id.studentImage);
            txtName = itemView.findViewById(R.id.studentName);
            txtCourse = itemView.findViewById(R.id.studentCourse);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}
