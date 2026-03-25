import java.util.*;
public class EmployeeManagementSystem {
    static ArrayList<Employee> employees = new ArrayList<>();

    // Add Employee
    static void addEmployee(int id, String name, double salary) {
        employees.add(new Employee(id, name, salary));
        System.out.println("Employee added successfully");
    }

    // View Employees
    static void viewEmployees() {
        if (employees.isEmpty()) {
            System.out.println("No employees found");
            return;
        }

        for (Employee emp : employees) {
            emp.display();
        }
    }

    // Update Employee
    static void updateEmployee(int id, String newName, double newSalary) {
        for (Employee emp : employees) {
            if (emp.id == id) {
                emp.name = newName;
                emp.salary = newSalary;
                System.out.println("Employee updated successfully");
                return;
            }
        }
        System.out.println("Employee not found");
    }

    // Delete Employee
    static void deleteEmployee(int id) {
        for (Employee emp : employees) {
            if (emp.id == id) {
                employees.remove(emp);
                System.out.println("Employee deleted successfully");
                return;
            }
        }
        System.out.println("Employee not found");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1.Add 2.View 3.Update 4.Delete 5.Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter ID: ");
                    int id = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Salary: ");
                    double salary = sc.nextDouble();

                    addEmployee(id, name, salary);
                    break;

                case 2:
                    viewEmployees();
                    break;

                case 3:
                    System.out.print("Enter ID to update: ");
                    id = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter new name: ");
                    name = sc.nextLine();

                    System.out.print("Enter new salary: ");
                    salary = sc.nextDouble();

                    updateEmployee(id, name, salary);
                    break;

                case 4:
                    System.out.print("Enter ID to delete: ");
                    id = sc.nextInt();
                    deleteEmployee(id);
                    break;

                case 5:
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice");
            }
        }
    }
}
