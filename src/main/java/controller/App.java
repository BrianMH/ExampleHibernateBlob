package controller;

import jakarta.persistence.TypedQuery;
import model.Department;
import model.Teacher;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class App {
    // keeps our factory alive for the duration of the application
    private static SessionFactory curSessFactory = null;

    public static void main(String[] args) {
        try {
            initializeAppSessFactory();
            beginInteractiveProgram();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if(curSessFactory != null && !curSessFactory.isClosed())
//                curSessFactory.close(); // this deletes the entire database on execution
        }
    }

    // ========================================================
    // ============== DATABASE MODIFICATION ===================
    // ========================================================

    /**
     * Guides the user through a process to add a user to the teacher table
     */
    public static void manageTeacherTableAdd() {
        // First prompt user for numbers
        System.out.print("Enter the number of teachers to add: ");
        int numToAdd = getUserParsedInteger(0);

        // Then prompt for the teacher attributes
        ArrayList<Teacher> toPersist = new ArrayList<>(numToAdd);
        while(toPersist.size() < numToAdd) {
            System.out.println("\nBuilding Entry #" + (toPersist.size()+1));
            toPersist.add(Teacher.buildAssistedObject());
        }

        // And then use the database connector to persist these objects
        Session session = App.curSessFactory.openSession();
        Transaction transaction = session.beginTransaction();
        toPersist.forEach(session::persist);
        transaction.commit();
        session.close();
    }

    /**
     * Guides the user through a process to delete an entry from the teacher table
     */
    public static void manageTeacherTableDelete() {
        List<Teacher> listOfTeachers = queryAllTeachers();
        Map<Integer, Teacher> idTeachMap = listOfTeachers.stream().collect(Collectors.toMap(Teacher::getTeacherId,
                                                                                            Function.identity()));
        System.out.println("Currently registered Teachers: ");
        listOfTeachers.forEach(curTeach -> System.out.println("\t" + curTeach));

        System.out.print("Please enter ID of teacher to delete: ");
        Scanner inStream = new Scanner(System.in);
        int idToRemove = inStream.nextInt();

        // begin removal via session
        Session remSess = App.curSessFactory.openSession();
        Transaction transaction = remSess.beginTransaction();
        try {
            remSess.remove(idTeachMap.getOrDefault(idToRemove, null));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid deletion parsed... Quitting operation.");
        }
        transaction.commit();
        remSess.close();
    }

    /**
     * Guides a user through the process to modify a row of the teacher table
     */
    public static void manageTeacherTableModify() {
        List<Teacher> listOfTeachers = queryAllTeachers();
        Map<Integer, Teacher> idTeachMap = listOfTeachers.stream().collect(Collectors.toMap(Teacher::getTeacherId,
                                                                                            Function.identity()));
        System.out.println("Currently registered Teachers: ");
        listOfTeachers.forEach(curTeach -> System.out.println("\t" + curTeach));

        // begin modification
        int idTModify = -1;
        Scanner inStream = new Scanner(System.in);
        while(!idTeachMap.containsKey(idTModify)) {
            System.out.print("Please enter ID of teacher to modify: ");
            idTModify = inStream.nextInt();
        }

        // manipulate object and commit it
        Session curSess = App.curSessFactory.openSession();
        Transaction curTrans = curSess.beginTransaction();

        Teacher tModify = idTeachMap.get(idTModify);
        System.out.println("Enter desired new values...");
        Teacher newObj = Teacher.buildAssistedObject();
        tModify.setTeacherName(newObj.getTeacherName());
        tModify.setSalary(newObj.getSalary());
        curSess.merge(tModify);

        curTrans.commit();
        curSess.close();
    }

    /**
     * Guides the user through a process to add entries to the department table
     */
    public static void manageDepartmentTableAdd() {
        // First prompt user for numbers
        System.out.print("Enter the number of departments to add: ");
        int numToAdd = getUserParsedInteger(0);

        // Then prompt for the teacher attributes
        ArrayList<Department> toPersist = new ArrayList<>(numToAdd);
        while(toPersist.size() < numToAdd) {
            System.out.println("\nBuilding Entry #" + (toPersist.size()+1));
            toPersist.add(Department.buildAssistedObject());
        }

        // And then use the database connector to persist these objects
        Session session = App.curSessFactory.openSession();
        Transaction transaction = session.beginTransaction();
        toPersist.forEach(session::persist);
        transaction.commit();
        session.close();
    }

    /**
     * Guides the user through the process to delete an entry from the department table
     */
    public static void manageDepartmentTableDelete() {
        List<Department> listOfDepts = queryAllDepts();
        Map<Integer, Department> idDeptMap = listOfDepts.stream().collect(Collectors.toMap(Department::getDeptId,
                                                                                                Function.identity()));
        System.out.println("Currently registered departments: ");
        listOfDepts.forEach(curDept -> System.out.println("\t" + curDept));

        System.out.print("Please enter ID of department to delete: ");
        Scanner inStream = new Scanner(System.in);
        int idToRemove = inStream.nextInt();

        // begin removal via session
        Session remSess = App.curSessFactory.openSession();
        Transaction transaction = remSess.beginTransaction();
        try {
            Department toRemove = idDeptMap.getOrDefault(idToRemove, null);
            toRemove = remSess.merge(toRemove); // detached -> attach to session
            toRemove.removeAllTeachers();
            remSess.remove(toRemove); // then remove all references
            transaction.commit();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid deletion parsed... Quitting operation.");
            transaction.rollback();
        }
        remSess.close();
    }

    /**
     * Guides the user through a process to modify a particular entry in the department table
     */
    public static void manageDepartmentTableModify() {
        List<Department> listOfDepts = queryAllDepts();
        Map<Integer, Department> idDeptMap = listOfDepts.stream().collect(Collectors.toMap(Department::getDeptId,
                                                                                           Function.identity()));
        System.out.println("Currently registered departments: ");
        listOfDepts.forEach(curDept -> System.out.println("\t" + curDept));

        // begin modification
        int idTModify = -1;
        Scanner inStream = new Scanner(System.in);
        while(!idDeptMap.containsKey(idTModify)) {
            System.out.print("Please enter ID of department to modify: ");
            idTModify = inStream.nextInt();
        }

        // manipulate object and commit it
        Session curSess = App.curSessFactory.openSession();
        Transaction curTrans = curSess.beginTransaction();

        Department tModify = idDeptMap.get(idTModify);
        System.out.println("Enter desired new values...");
        Department newObj = Department.buildAssistedObject();
        tModify.setDeptName(newObj.getDeptName());
        curSess.merge(tModify);

        curTrans.commit();
        curSess.close();
    }

    /**
     * Since this is essentially just a modification for the teacher element, everything
     * gets encapsulated in here. Removal is the same as the assignment of null to the given
     * teacher's department.
     */
    public static void manageTeacherDeptConns() {
        // We will use this scanner for quite some time
        Scanner inStream = new Scanner(System.in);

        // first show our teachers
        List<Teacher> listOfTeachers = queryAllTeachers();
        Map<Integer, Teacher> idTeachMap = listOfTeachers.stream().collect(Collectors.toMap(Teacher::getTeacherId,
                Function.identity()));
        System.out.println("Currently registered Teachers: ");
        listOfTeachers.forEach(curTeach -> System.out.println("\t" + curTeach));

        int tIDTUpdate = -1;
        while(!idTeachMap.containsKey(tIDTUpdate)) {
            System.out.print("Select teacher ID to modify: ");
            tIDTUpdate = inStream.nextInt();
        }
        Teacher curTObject = idTeachMap.get(tIDTUpdate);

        // and now ask for subjects to associate with teacher via ID
        System.out.println("Now select a subject to associate with teacher (" + curTObject + ") : ");
        List<Department> listOfDepts = queryAllDepts();
        Map<Integer, Department> idDeptMap = listOfDepts.stream().collect(Collectors.toMap(Department::getDeptId,
                Function.identity()));
        idDeptMap.put(0, null);
        System.out.println("\tID: 0, null");
        listOfDepts.forEach(curDept -> System.out.println("\t" + curDept));

        int dIDTUpdate = -1;
        while(!idDeptMap.containsKey(dIDTUpdate)) {
            System.out.print("Select department ID to assign to teacher: ");
            dIDTUpdate = inStream.nextInt();
        }
        Department curDObject = idDeptMap.get(dIDTUpdate);

        // and associate in a transaction
        Session connSess = App.curSessFactory.openSession();
        Transaction curTrans = connSess.beginTransaction();

        curTObject.setDepartment(curDObject);
        connSess.merge(curTObject);

        curTrans.commit();
        connSess.close();
    }

    // ==========================================================
    // ================= USER INTERFACE FUNCTIONS ===============
    // ==========================================================
    // (A collection of functions used solely to control the user experience)

    /**
     * Start point for the interactive program. Prompts a user for their choices until
     * the application quits
     */
    public static void beginInteractiveProgram() {
        // initialize user input
        String userInput = "";
        Scanner inStream = new Scanner(System.in);

        // perform user input loop
        while(userInput != null && !userInput.equals("q")) {
            // greet
            printGreeting();

            // take input
            userInput = inStream.next();

            // parse input for options
            switch (userInput) {
                case "1": manageTeacherTableEntry(); break;
                case "2": manageDepartmentTableEntry(); break;
                case "3": manageTeacherDeptConns(); break;
                case "q": break;
                case null, default: System.out.println("Not a valid option!\n");
            }
        }
    }

    /**
     * Entry point for the department table sub-interface
     */
    public static void manageDepartmentTableEntry() {
        // now set up inner loop for management
        Scanner inStream = new Scanner(System.in);
        String userInput = "";
        while(userInput != null && !userInput.equals("q")) {
            // greet
            printManagementMessage("Department");

            // take input
            userInput = inStream.next();

            // parse input for options
            switch (userInput) {
                case "1": manageDepartmentTableAdd(); break;
                case "2": manageDepartmentTableDelete(); break;
                case "3": manageDepartmentTableModify(); break;
                case "q": break;
                case null, default: System.out.println("Not a valid option!\n");
            }
        }
    }

    /**
     * An entry point for the teacher table sub-interface
     */
    public static void manageTeacherTableEntry() {
        // now set up inner loop for management
        Scanner inStream = new Scanner(System.in);
        String userInput = "";
        while(userInput != null && !userInput.equals("q")) {
            // greet
            printManagementMessage("Teacher");

            // take input
            userInput = inStream.next();

            // parse input for options
            switch (userInput) {
                case "1": manageTeacherTableAdd(); break;
                case "2": manageTeacherTableDelete(); break;
                case "3": manageTeacherTableModify(); break;
                case "q": break;
                case null, default: System.out.println("Not a valid option!\n");
            }
        }
    }

    /**
     * Controlled input user specification for ints
     * @param lowerLim - the lowest value to take for the input
     * @return the associated integer received from the user
     */
    public static int getUserParsedInteger(int lowerLim) {
        Scanner inStream = new Scanner(System.in);
        int numParsed = lowerLim - 1;
        while(numParsed < lowerLim) {
            try {
                numParsed = inStream.nextInt();
            } catch(Exception e) {
                System.out.println("Not a valid number.");
            } finally {
                inStream.nextLine();
            }
        }

        return numParsed;
    }

    // ==========================================================
    // ================= QUERY RELATED FUNCTIONS ================
    // ==========================================================

    /**
     * Uses NamedQuery from Teacher class to get all the teachers currently in database.
     * @return A list of teachers in the database
     */
    public static List<Teacher> queryAllTeachers() {
        Session session = App.curSessFactory.openSession();
        TypedQuery<Teacher> tQuery = session.createNamedQuery("getAllTeachers", Teacher.class);
        List<Teacher> tResList = tQuery.getResultList();
        session.close();

        return tResList;
    }

    /**
     * Uses a NamedQuery from the Department class to get all the departments currently in the database.
     * @return A list of departments in the database
     */
    public static List<Department> queryAllDepts() {
        Session session = App.curSessFactory.openSession();
        TypedQuery<Department> dQuery = session.createNamedQuery("getAllDepts", Department.class);
        List<Department> dResList = dQuery.getResultList();
        session.close();

        return dResList;
    }

    // ============================================================
    // ================ SESSION FACTORY CONTROLLER ================
    // ============================================================

    /**
     * Initializes our session factory if it hasn't been initialized yet. Performs no operation if it
     * is already initialized.
     */
    public static void initializeAppSessFactory() {
        if(App.curSessFactory == null)
            App.curSessFactory = new Configuration().configure().buildSessionFactory();
    }

    // ====================================================
    // ================ MESSAGE FUNCTIONS =================
    // ====================================================

    public static void printManagementMessage(String tableName) {
        System.out.println("\n======= " + tableName + " Management ======");
        System.out.println("1) Add");
        System.out.println("2) Remove");
        System.out.println("3) Modify");
        System.out.println("\nq) Exit Sub-interface");
        System.out.print("\nUser Option: ");
    }

    public static void printGreeting() {
        System.out.println("\nWelcome to school database management system. Select an option:");
        System.out.println("1) Delete/Add/Modify Teachers");
        System.out.println("2) Delete/Add/Modify Departments");
        System.out.println("3) Add teacher to department");
        System.out.println("\nq) Quit Application");
        System.out.print("\nUser Option: ");
    }
}