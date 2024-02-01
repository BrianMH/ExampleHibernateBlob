package model;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Entity
@Table
@NamedQueries({
        @NamedQuery(name = "getAllDepts", query = "FROM Department d"),
        @NamedQuery(name = "getDeptByID", query = "FROM Department d WHERE deptId = :id")
})
public class Department implements Serializable  {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy=GenerationType.IDENTITY )
    private int deptId;
    private String deptName;
    @OneToMany(mappedBy = "department")
    private List<Teacher> teacherList;

    public Department(int deptId, String deptName) {
        super();
        this.deptId = deptId;
        this.deptName = deptName;
        this.teacherList = new ArrayList<>();
    }

    public Department(int deptId, String deptName, List<Teacher> tList) {
        super();
        this.deptId = deptId;
        this.deptName = deptName;
        this.teacherList = tList;
    }

    public Department() {}

    public Department(String deptName) {
        this.deptName = deptName;
    }

    public List<Teacher> getTeacherList() {
        return this.teacherList;
    }

    public void removeAllTeachers() {
        this.teacherList.forEach(curTeach -> curTeach.setDepartment(null));
        this.teacherList = new ArrayList<>();
    }

    public void addTeacherToList(Teacher toAdd) {
        this.teacherList.add(toAdd);
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    @Override
    public String toString() {
        return String.format("ID: %s, DeptName: %s", this.deptId, this.deptName);
    }

    public static Department buildAssistedObject() {
        // create obj
        Department newObj = new Department();

        // ask user for specific entries
        Scanner inStream = new Scanner(System.in);
        System.out.print("Name: ");
        newObj.setDeptName(inStream.nextLine().strip());

        return newObj;
    }
}
