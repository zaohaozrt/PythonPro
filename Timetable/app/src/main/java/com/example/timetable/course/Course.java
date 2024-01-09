package com.example.timetable.course;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Course implements Cloneable, Serializable {
    private int id;
    private String courseName;//课程名
    private String teacherName;//教师名
    private List<Map<String,Integer>> listWeeks;   //几个开始结束周次
    private String classroom;//教室
    private String weekType;//单双周类型
    private int day;//星期几
    private int s_section;//开始节次
    private int e_section;//结束节次


    public List<Map<String, Integer>> getListWeeks() {
        return listWeeks;
    }

    public void setListWeeks(List<Map<String, Integer>> listWeeks) {
        this.listWeeks = listWeeks;
    }

    public int getS_section() {
        return s_section;
    }

    public void setS_section(int s_section) {
        this.s_section = s_section;
    }

    public int getE_section() {
        return e_section;
    }

    public void setE_section(int e_section) {
        this.e_section = e_section;
    }




    public Course() {
    }

    public Course(int day,int s_section,int e_section,String classroom,List<Map<String,Integer>> listWeeks,String weekType, String courseName) {
        this.day =day;
        this.courseName = courseName;
        this.classroom = classroom;
        this.courseName = courseName;
        this.weekType = weekType;
        this.listWeeks = listWeeks;

    }

    public String getWeekType() {
        return weekType;
    }

    public void setWeekType(String weekType) {
        this.weekType = weekType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }



    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }





}
