package com.example.studentdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    static final String DATABASE = "school.db";
    static final String STUDENTS = "students";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE, null, 4);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + STUDENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name VARCHAR(25), " +
                "course VARCHAR(10), " +
                "image VARCHAR(50)" +
                ")";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + STUDENTS);
        onCreate(sqLiteDatabase);
    }

    // ------------------------------
    // CREATE
    // ------------------------------
    public long addStudent(Student student) {
        long result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", student.getName());
        cv.put("course", student.getCourse());
        cv.put("image", student.getImage());
        result = db.insert(STUDENTS, null, cv);
        db.close();
        return result;
    }

    // ------------------------------
    // READ ALL
    // ------------------------------
    public ArrayList<Student> getAllStudents() {
        ArrayList<Student> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(STUDENTS, null, null, null, null, null, "name");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String course = cursor.getString(cursor.getColumnIndexOrThrow("course"));
                String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));

                // ✅ Add the student to the list
                list.add(new Student(id, name, course, image));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }


    // ------------------------------
    // READ BY ID
    // ------------------------------
    public Student getStudentById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                STUDENTS,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Student s = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String course = cursor.getString(cursor.getColumnIndexOrThrow("course"));
            String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
            s = new Student(id, name, course, image);
        }

        cursor.close();
        db.close();
        return s;
    }

    // ------------------------------
    // UPDATE
    // ------------------------------
    public int updateStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", student.getName());
        cv.put("course", student.getCourse());
        cv.put("image", student.getImage());

        int result = db.update(STUDENTS, cv, "id = ?", new String[]{String.valueOf(student.getId())});
        db.close();
        return result;
    }

    // ------------------------------
    // DELETE
    // ------------------------------
    public int deleteStudent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(STUDENTS, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }
}
