package by.astontrainee.dao;

import by.astontrainee.dto.Department;
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
public class DepartmentDao implements Dao<Department> {

    @Override
    public List<Department> selectAll() {
        List<Department> departments = new ArrayList<>();
        try (Statement statement = ConnectionUtils.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT departments.id, department_name, STRING_AGG(employee_name, ', ') as employee_names FROM departments\n" +
                    "LEFT JOIN employees ON departments.id = employees.department_id\n" +
                    "GROUP BY departments.id\n" +
                    "ORDER BY departments.id ASC");
            while (resultSet.next()) {
                Department department = new Department();
                department.setId(resultSet.getInt("id"));
                department.setName(resultSet.getString("department_name"));
                department.setEmployees(resultSet.getString("employee_names"));
                departments.add(department);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    @Override
    public Department selectOne(int id) {
        Department department = new Department();
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("SELECT departments.id, department_name, STRING_AGG(employee_name, ', ') as employee_names FROM departments\n" +
                "LEFT JOIN employees ON departments.id = employees.department_id\n" +
                "WHERE departments.id = ?\n" +
                "GROUP BY departments.id")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                department.setId(resultSet.getInt("id"));
                department.setName(resultSet.getString("department_name"));
                department.setEmployees(resultSet.getString("employee_names"));
                return department;
            } else {
                throw new NotFoundException("Department with id " + id + " doesn't exist!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return department;
    }

    @Override
    public Department update(Department department) {
        Department updatedDepartment = new Department();
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("UPDATE departments SET department_name = ? WHERE id = ?")) {
            statement.setString(1, department.getName());
            statement.setInt(2, department.getId());
            statement.executeUpdate();
            try (PreparedStatement secondStatement = ConnectionUtils.getConnection().prepareStatement("SELECT departments.id, department_name FROM departments WHERE departments.id = ?")) {
                secondStatement.setInt(1, department.getId());
                ResultSet resultSet = secondStatement.executeQuery();
                if (resultSet.next()) {
                    updatedDepartment.setId(resultSet.getInt("id"));
                    updatedDepartment.setName(resultSet.getString("department_name"));
                } else {
                    throw new ErrorResponse("Failed to update department with id " + department.getId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedDepartment;
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("UPDATE employees SET department_id = null WHERE department_id = ?; DELETE FROM departments WHERE id = ?")) {
            statement.setInt(1, id);
            statement.setInt(2, id);
            statement.executeUpdate();
            try (PreparedStatement confirmationStatement = ConnectionUtils.getConnection().prepareStatement("SELECT id, department_name FROM departments WHERE id = ?")) {
                confirmationStatement.setInt(1, id);
                try {
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        throw new ErrorResponse("Department with id " + id + " can not be deleted!");
                    }
                } catch (PSQLException e) {
                    throw new NotFoundException("Department with id " + id + " doesn't exist!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Department insert(Department department) {
        Department newDepartment = new Department();
        try (PreparedStatement firstStatement = ConnectionUtils.getConnection().prepareStatement("INSERT INTO departments (department_name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            firstStatement.setString(1, department.getName());
            firstStatement.executeUpdate();
            try (ResultSet generatedKeys = firstStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    try (PreparedStatement secondStatement = ConnectionUtils.getConnection().prepareStatement("SELECT departments.id, department_name FROM departments\n" +
                            "     WHERE departments.id = ?")) {
                        secondStatement.setInt(1, generatedKeys.getInt(1));
                        ResultSet resultSet = secondStatement.executeQuery();
                        if (resultSet.next()) {
                            newDepartment.setId(resultSet.getInt("id"));
                            newDepartment.setName(resultSet.getString("department_name"));
                        }
                    }
                } else {
                    throw new ErrorResponse("Failed to create new department, no id obtained!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newDepartment;
    }
}
