CREATE TABLE departments
(
    id SERIAL PRIMARY KEY,
    department_name VARCHAR(50) NOT NULL
);

ALTER TABLE departments
    OWNER TO aleksandr;

CREATE TABLE employees
(
    id SERIAL PRIMARY KEY,
    employee_name VARCHAR(50) NOT NULL,
    department_id INTEGER
        CONSTRAINT employees_departments_fk
            references departments
);

ALTER TABLE employees
    OWNER TO aleksandr;

CREATE TABLE projects
(
    id SERIAL PRIMARY KEY,
    project_name VARCHAR(50) NOT NULL
);

ALTER TABLE projects
    OWNER TO aleksandr;

CREATE TABLE employees_projects
(
    employee_id INTEGER NOT NULL
        CONSTRAINT employees_projects_employee_id_fk
            REFERENCES employees,
    project_id INTEGER NOT NULL
        CONSTRAINT employees_projects_project_id_fk
            REFERENCES projects,
    PRIMARY KEY (employee_id, project_id)
);

ALTER TABLE employees_projects
    OWNER TO aleksandr;