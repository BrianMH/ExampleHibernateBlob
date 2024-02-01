package model;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Scanner;
import java.util.Set;

@Entity
@Table(name="Teacher")
@NamedQueries({
        @NamedQuery(name = "getAllTeachers", query = "FROM Teacher t"),
        @NamedQuery(name = "getTeacherByID", query = "FROM Teacher t WHERE id = :id")
})
public class Teacher implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue( strategy=GenerationType.IDENTITY )
    private int teacherId;
    private String salary;
    private String teacherName;
    @ManyToOne
    private Department department;

    public Teacher(String salary, String teacherName) {
        super();
        this.salary = salary;
        this.teacherName = teacherName;    }

    public Teacher() {}

    public Teacher(String salary, String teacherName, Department department) {
        this.salary = salary;
        this.teacherName = teacherName;
    }

    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(Department dept) {
        this.department = dept;
    }

    public int getTeacherId() {
        return teacherId;
    }
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
    public String getSalary() {
        return salary;
    }
    public void setSalary(String salary) {
        this.salary = salary;
    }
    public String getTeacherName() {
        return teacherName;
    }
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;    }

    @Override
    public String toString() {
        return String.format("ID: %s, Name: %s, Salary %s, Dept: {%s}", this.teacherId,
                                                                        this.teacherName,
                                                                        this.salary,
                                                                        this.department);
    }

    public static Teacher buildAssistedObject() {
        // create obj
        Teacher newObj = new Teacher();

        // ask user for specific entries
        Scanner inStream = new Scanner(System.in);
        System.out.print("Salary: ");
        newObj.setSalary(Integer.toString(inStream.nextInt()));
        inStream.nextLine(); // flush
        System.out.print("Name: ");
        newObj.setTeacherName(inStream.nextLine().strip());

        return newObj;
    }
}
