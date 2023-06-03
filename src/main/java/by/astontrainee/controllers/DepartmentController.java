package by.astontrainee.controllers;

import by.astontrainee.dao.Dao;
import by.astontrainee.dao.DepartmentDao;
import by.astontrainee.dto.Department;
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
@WebServlet(value = "/department/*")
public class DepartmentController extends HttpServlet {

    private final Dao<Department> departmentDao = new DepartmentDao();
    private static final Gson GSON = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String json;
        if (uri.substring("/department/".length()).isEmpty()) {
            json = GSON.toJson(departmentDao.selectAll());
            resp.setStatus(200);
        } else {
            int id = Integer.parseInt(uri.substring("/department/".length()));
            try {
                json = GSON.toJson(departmentDao.selectOne(id));
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
        if (!uri.substring("/department/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("Cannot process the request");
            responceJson = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            String json = JsonReaderUtils.readInputStream(req.getInputStream());
            Department department = GSON.fromJson(json, Department.class);
            try {
                Department createdDepartment = departmentDao.insert(department);
                responceJson = GSON.toJson(createdDepartment);
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
        Department department;
        if (uri.substring("/department/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("Cannot process the request");
            resultJson = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            int id = Integer.parseInt(uri.substring("/department/".length()));
            String json = JsonReaderUtils.readInputStream(req.getInputStream());
            department = GSON.fromJson(json, Department.class);
            department.setId(id);
            try {
                Department updatedDepartment = departmentDao.update(department);
                resultJson = GSON.toJson(updatedDepartment);
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
        if (uri.substring("/department/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("No id provided!");
            json = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            int id = Integer.parseInt(uri.substring("/department/".length()));
            try {
                departmentDao.delete(id);
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
