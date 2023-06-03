package by.astontrainee.dao;

import by.astontrainee.dto.Project;
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
public class ProjectDao implements Dao<Project> {

    @Override
    public List<Project> selectAll() {
        List<Project> projects = new ArrayList<>();
        try (Statement statement = ConnectionUtils.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT projects.id, project_name, STRING_AGG(employee_name, ', ') as employee_names FROM projects\n" +
                    "LEFT JOIN employees_projects ON projects.id = employees_projects.project_id\n" +
                    "LEFT JOIN employees ON employees.id = employees_projects.employee_id\n" +
                    "GROUP BY projects.id\n" +
                    "ORDER BY projects.id");
            while (resultSet.next()) {
                Project project = new Project();
                project.setId(resultSet.getInt("id"));
                project.setName(resultSet.getString("project_name"));
                project.setEmployees(resultSet.getString("employee_names"));
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    @Override
    public Project selectOne(int id) {
        Project project = new Project();
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("SELECT projects.id, project_name, STRING_AGG(employee_name, ', ') as employee_names FROM projects\n" +
                "LEFT JOIN employees_projects ON projects.id = employees_projects.project_id\n" +
                "LEFT JOIN employees ON employees.id = employees_projects.employee_id\n" +
                "WHERE projects.id = ?\n" +
                "GROUP BY projects.id")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                project.setId(resultSet.getInt("id"));
                project.setName(resultSet.getString("project_name"));
                project.setEmployees(resultSet.getString("employee_names"));
                return project;
            } else {
                throw new NotFoundException("Project with id " + id + " doesn't exist!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return project;
    }

    @Override
    public Project update(Project project) {
        Project updatedProject = new Project();
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("UPDATE projects SET project_name = ? WHERE id = ?")) {
            statement.setString(1, project.getName());
            statement.setInt(2, project.getId());
            statement.executeUpdate();
            try (PreparedStatement secondStatement = ConnectionUtils.getConnection().prepareStatement("SELECT projects.id, project_name FROM projects WHERE projects.id = ?")) {
                secondStatement.setInt(1, project.getId());
                ResultSet resultSet = secondStatement.executeQuery();
                if (resultSet.next()) {
                    updatedProject.setId(resultSet.getInt("id"));
                    updatedProject.setName(resultSet.getString("project_name"));
                } else {
                    throw new ErrorResponse("Failed to update project with id " + project.getId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedProject;
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement statement = ConnectionUtils.getConnection().prepareStatement("DELETE FROM employees_projects WHERE project_id = ?; DELETE FROM projects WHERE id = ?")) {
            statement.setInt(1, id);
            statement.setInt(2, id);
            statement.executeUpdate();
            try (PreparedStatement confirmationStatement = ConnectionUtils.getConnection().prepareStatement("SELECT id, project_name FROM projects WHERE id = ?")) {
                confirmationStatement.setInt(1, id);
                try {
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        throw new ErrorResponse("Project with id " + id + " can not be deleted!");
                    }
                } catch (PSQLException e) {
                    throw new NotFoundException("Project with id " + id + " doesn't exist!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Project insert(Project project) {
        Project newProject = new Project();
        try (PreparedStatement firstStatement = ConnectionUtils.getConnection().prepareStatement("INSERT INTO projects (project_name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            firstStatement.setString(1, project.getName());
            firstStatement.executeUpdate();
            try (ResultSet generatedKeys = firstStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    try (PreparedStatement secondStatement = ConnectionUtils.getConnection().prepareStatement("SELECT projects.id, project_name FROM projects\n" +
                            "     WHERE projects.id = ?")) {
                        secondStatement.setInt(1, generatedKeys.getInt(1));
                        ResultSet resultSet = secondStatement.executeQuery();
                        if (resultSet.next()) {
                            newProject.setId(resultSet.getInt("id"));
                            newProject.setName(resultSet.getString("project_name"));
                        }
                    }
                } else {
                    throw new ErrorResponse("Failed to create new project, no id obtained!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newProject;
    }
}
