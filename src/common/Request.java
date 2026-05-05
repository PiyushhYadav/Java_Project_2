package common;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Action { ADD, UPDATE, DELETE, GET_ALL, GET_BY_ID }

    private Action action;
    private Student student;
    private int studentId;

    public Request(Action action) {
        this.action = action;
    }

    public Request(Action action, Student student) {
        this.action = action;
        this.student = student;
    }

    public Request(Action action, int studentId) {
        this.action = action;
        this.studentId = studentId;
    }

    public Action getAction() { return action; }
    public Student getStudent() { return student; }
    public int getStudentId() { return studentId; }
}