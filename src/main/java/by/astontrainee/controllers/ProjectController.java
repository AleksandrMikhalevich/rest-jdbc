package by.astontrainee.controllers;

import by.astontrainee.dao.Dao;
import by.astontrainee.dao.ProjectDao;
import by.astontrainee.dto.Project;
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
@WebServlet(value = "/project/*")
public class ProjectController extends HttpServlet {

    private final Dao<Project> projectDao = new ProjectDao();

    private static final Gson GSON = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String json;
        if (uri.substring("/project/".length()).isEmpty()) {
            json = GSON.toJson(projectDao.selectAll());
            resp.setStatus(200);
        } else {
            int id = Integer.parseInt(uri.substring("/project/".length()));
            try {
                json = GSON.toJson(projectDao.selectOne(id));
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
        if (!uri.substring("/project/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("Cannot process the request");
            responceJson = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            String json = JsonReaderUtils.readInputStream(req.getInputStream());
            Project project = GSON.fromJson(json, Project.class);
            try {
                Project createdProject = projectDao.insert(project);
                responceJson = GSON.toJson(createdProject);
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
        Project project;
        if (uri.substring("/project/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("Cannot process the request");
            resultJson = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            int id = Integer.parseInt(uri.substring("/project/".length()));
            String json = JsonReaderUtils.readInputStream(req.getInputStream());
            project = GSON.fromJson(json, Project.class);
            project.setId(id);
            try {
                Project updatedProject = projectDao.update(project);
                resultJson = GSON.toJson(updatedProject);
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
        if (uri.substring("/project/".length()).isEmpty()) {
            BadRequestException badRequestException = new BadRequestException("No id provided!");
            json = GSON.toJson(badRequestException.getMessage());
            resp.setStatus(400);
        } else {
            int id = Integer.parseInt(uri.substring("/project/".length()));
            try {
                projectDao.delete(id);
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
