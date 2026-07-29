package com.python.ide;

public class Project {

    private final String name;
    private final String path;

    public Project(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}