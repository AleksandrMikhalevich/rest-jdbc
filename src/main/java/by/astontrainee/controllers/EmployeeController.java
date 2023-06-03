package by.astontrainee.controllers;

import by.astontrainee.dao.Dao;
import by.astontrainee.dao.EmployeeDao;
import by.astontrainee.dto.Employee;
import by.astontrainee.exceptions.BadRequestException;
import by.astontrainee.exceptions.ErrorResponse;
import by.astontrainee.exceptions.NotFoundException;
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
@WebServlet(value = "/employee/*")
public class EmployeeController extends HttpServlet {

    private final Dao<Employee> employeeDao = new EmployeeDao();
    private static final Gson GSON = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String json;
        if (uri.substring("/employee/".length()).isEmpty()) {
            json = GSON.toJson(employeeDao.selectAll());
            resp.setStatus(200);
        } else {
            int id = Integer.parseInt(uri.substring("/employee/".length()));
            try {
                json = GSON.toJson(employeeDao.selectOne(id));
                resp.setStatus(200);
            } catch (NotFoundException e) {
                json = GSON.toJson(e.getMessage());
                resp.setStatus(404);
            }
        }
        resp.setHeader("Content-Type", "application/json");
        resp.getOutputStream().println(json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String responceJson;
        if (!uri.substring("/employee/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("Cannot process the request");
            responceJson = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            String json = JsonReaderUtils.readInputStream(req.getInputStream());
            Employee employee = GSON.fromJson(json, Employee.class);
            try {
                Employee createdEmployee = employeeDao.insert(employee);
                responceJson = GSON.toJson(createdEmployee);
                resp.setStatus(201);
            } catch (ErrorResponse e) {
                responceJson = GSON.toJson(e.getMessage());
                resp.setStatus(500);
            }
        }
        resp.setHeader("Content-Type", "application/json");
        resp.getOutputStream().println(responceJson);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String resultJson;
        Employee employee;
        if (uri.substring("/employee/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("Cannot process the request");
            resultJson = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            int id = Integer.parseInt(uri.substring("/employee/".length()));
            String json = JsonReaderUtils.readInputStream(req.getInputStream());
            employee = GSON.fromJson(json, Employee.class);
            employee.setId(id);
            try {
                Employee updatedEmployee = employeeDao.update(employee);
                resultJson = GSON.toJson(updatedEmployee);
                resp.setStatus(200);
            } catch (ErrorResponse e) {
                resultJson = GSON.toJson(e.getMessage());
                resp.setStatus(500);
            }
        }
        resp.setHeader("Content-Type", "application/json");
        resp.getOutputStream().println(resultJson);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String json = null;
        if (uri.substring("/employee/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("No id provided!");
            json = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            int id = Integer.parseInt(uri.substring("/employee/".length()));
            try {
                employeeDao.delete(id);
            } catch (NotFoundException e) {
                json = GSON.toJson(e.getMessage());
                resp.setStatus(200);
            } catch (ErrorResponse e) {
                json = GSON.toJson(e.getMessage());
                resp.setStatus(500);
            }
        }
        resp.setHeader("Content-Type", "application/json");
        resp.getOutputStream().println(json);
    }
}
