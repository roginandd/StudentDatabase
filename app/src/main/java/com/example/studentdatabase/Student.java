package com.example.studentdatabase;

public class Student {
    private int id;          // Database primary key
    private String name;     // Student name
    private String course;   // Course name (e.g., BSCS, BSIT, etc.)
    private String image;    // Path or URI to the student's image

    // ---- Constructors ----
    public Student() {
    }

    // Constructor without ID (for adding new students)
    public Student(String name, String course, String image) {
        this.name = name;
        this.course = course;
        this.image = image;
    }

    // Constructor with ID (for reading/updating from DB)
    public Student(int id, String name, String course, String image) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.image = image;
    }

    // ---- Getters and Setters ----
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
