package by.astontrainee.dao;

import by.astontrainee.dto.Employee;
import by.astontrainee.exceptions.ErrorResponse;
import by.astontrainee.exceptions.NotFoundException;
import by.astontrainee.utils.ConnectionUtils;
import org.postgresql.util.PSQLException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Mikhalevich
 */
public class EmployeeDao implements Dao<Employee> {

    @Override
    public List<Employee> selectAll() {
        List<Employee> employees = new ArrayList<>();
        try (Statement statement = ConnectionUtils.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT employees.id, employee_name, department_name, STRING_AGG(project_name, ', ') as project_names FROM employees\n" +
                    "    JOIN departments ON employees.department_id = departments.id\n" +
                    "    LEFT JOIN employees_projects ON employees.id = employees_projects.employee_id\n" +
                    "    LEFT JOIN projects ON projects.id = employees_projects.project_id\n" +
                    "    GROUP BY employees.id, department_name");
            while (resultSet.next()) {
                Employee employee = new Employee();
                employee.setId(resultSet.getInt("id"));
                employee.setName(resultSet.getString("employee_name"));
                employee.setDepartment(resultSet.getString("department_name"));
                employee.setProjects(resultSet.getString("project_names"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public Employee selectOne(int id) {
        Employee employee = new Employee();
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("SELECT employees.id, employee_name, department_name, STRING_AGG(project_name, ', ') as project_names FROM employees\n" +
                "    JOIN departments ON employees.department_id = departments.id\n" +
                "    LEFT JOIN employees_projects ON employees.id = employees_projects.employee_id\n" +
                "    LEFT JOIN projects ON projects.id = employees_projects.project_id\n" +
                "    WHERE employees.id = ? GROUP BY employees.id, department_name")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                employee.setId(resultSet.getInt("id"));
                employee.setName(resultSet.getString("employee_name"));
                employee.setDepartment(resultSet.getString("department_name"));
                employee.setProjects(resultSet.getString("project_names"));
                return employee;
            } else {
                throw new NotFoundException("Employee with id " + id + " doesn't exist!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        Employee updatedEmployee = new Employee();
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("UPDATE employees SET employee_name = ?, department_id = (SELECT id FROM departments WHERE department_name = ?) WHERE id = ?")) {
            statement.setString(1, employee.getName());
            statement.setString(2, employee.getDepartment());
            statement.setInt(3, employee.getId());
            statement.executeUpdate();
            try (PreparedStatement secondStatement = ConnectionUtils.getConnection().prepareStatement("SELECT employees.id, employee_name, department_name FROM employees\n" +
                    "    JOIN departments ON employees.department_id = departments.id WHERE employees.id = ?")) {
                secondStatement.setInt(1, employee.getId());
                ResultSet resultSet = secondStatement.executeQuery();
                if (resultSet.next()) {
                    updatedEmployee.setId(resultSet.getInt("id"));
                    updatedEmployee.setName(resultSet.getString("employee_name"));
                    updatedEmployee.setDepartment(resultSet.getString("department_name"));
                } else {
                    throw new ErrorResponse("Failed to update employee with id " + employee.getId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedEmployee;
    }

    @Override
    public void delete(int id){
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("DELETE FROM employees_projects WHERE employee_id = ?; DELETE FROM employees WHERE id = ?")) {
            statement.setInt(1, id);
            statement.setInt(2, id);
            statement.executeUpdate();
            try (PreparedStatement confirmationStatement = ConnectionUtils.getConnection().prepareStatement("SELECT id, employee_name FROM employees WHERE id = ?")) {
                confirmationStatement.setInt(1, id);
                try {
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        throw new ErrorResponse("Employee with id " + id + " can not be deleted!");
                    }
                } catch (PSQLException e) {
                    throw new NotFoundException("Employee with id " + id + " doesn't exist!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Employee insert(Employee employee) {
        Employee newEmployee = new Employee();
        try (PreparedStatement firstStatement = ConnectionUtils.getConnection().prepareStatement("INSERT INTO employees (employee_name, department_id)\n" +
                        "SELECT ?, departments.id FROM departments WHERE department_name = ?",
                Statement.RETURN_GENERATED_KEYS)) {
            firstStatement.setString(1, employee.getName());
            firstStatement.setString(2, employee.getDepartment());
            firstStatement.executeUpdate();
            try (ResultSet generatedKeys = firstStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    try (PreparedStatement secondStatement = ConnectionUtils.getConnection().prepareStatement("SELECT employees.id, employee_name, department_name FROM employees\n" +
                            "    JOIN departments ON employees.department_id = departments.id WHERE employees.id = ?")) {
                        secondStatement.setInt(1, generatedKeys.getInt(1));
                        ResultSet resultSet = secondStatement.executeQuery();
                        if (resultSet.next()) {
                            newEmployee.setId(resultSet.getInt("id"));
                            newEmployee.setName(resultSet.getString("employee_name"));
                            newEmployee.setDepartment(resultSet.getString("department_name"));
                        }
                    }
                } else {
                    throw new ErrorResponse("Failed to create new employee, no id obtained!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newEmployee;
    }

    public Employee assignProject(int id, String project) {
        Employee assignedEmployee = new Employee();
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("INSERT INTO employees_projects VALUES (?, (SELECT id FROM projects WHERE project_name = ?))")) {
            statement.setInt(1, id);
            statement.setString(2, project);
            statement.executeUpdate();
            try (PreparedStatement secondStatement = ConnectionUtils.getConnection().prepareStatement("SELECT employees.id, employee_name, department_name, STRING_AGG(project_name, ', ') as project_names FROM employees\n" +
                    "JOIN departments ON employees.department_id = departments.id\n" +
                    "LEFT JOIN employees_projects ON employees.id = employees_projects.employee_id\n" +
                    "LEFT JOIN projects ON projects.id = employees_projects.project_id\n" +
                    "WHERE employees.id = ? GROUP BY employees.id, department_name")) {
                secondStatement.setInt(1, id);
                ResultSet resultSet = secondStatement.executeQuery();
                if (resultSet.next()) {
                    assignedEmployee.setId(resultSet.getInt("id"));
                    assignedEmployee.setName(resultSet.getString("employee_name"));
                    assignedEmployee.setDepartment(resultSet.getString("department_name"));
                    assignedEmployee.setProjects(resultSet.getString("project_names"));
                } else {
                    throw new ErrorResponse("Failed to assign employee with id " + id);
                }
            }
        } catch (SQLException e) {
            throw new ErrorResponse("Failed to assign employee with id " + id + " to project " + project + " because they are already assigned!");
        }
        return assignedEmployee;
    }
}



