package by.astontrainee.controllers;

import by.astontrainee.dao.EmployeeDao;
import by.astontrainee.dto.Employee;
import by.astontrainee.dto.Project;
import by.astontrainee.exceptions.BadRequestException;
import by.astontrainee.exceptions.ErrorResponse;
import by.astontrainee.utils.JsonReaderUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Alex Mikhalevich
 */
@WebServlet(value = "/assignment/*")
public class AssignmentController extends HttpServlet {

    private final EmployeeDao employeeDao = new EmployeeDao();
    private static final Gson GSON = new GsonBuilder().create();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String resultJson;
        if (uri.substring("/assignment/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("Cannot process the request");
            resultJson = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            int id = Integer.parseInt(uri.substring("/assignment/".length()));
            String json = JsonReaderUtils.readInputStream(req.getInputStream());
            Project project = GSON.fromJson(json, Project.class);
            try {
                Employee assignedEmployee = employeeDao.assignProject(id, project.getName());
                resultJson = GSON.toJson(assignedEmployee);
                resp.setStatus(200);
            } catch (ErrorResponse e) {
                resultJson = GSON.toJson(e.getMessage());
                resp.setStatus(500);
            }
        }
        resp.setHeader("Content-Type", "application/json");
        resp.getOutputStream().println(resultJson);
    }
}
