package com.example.droxbox.pojo;

import java.util.ArrayList;

public class File {
    private String name;
    private String url;
    private ArrayList<String> history;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<String> history) {
        this.history = history;
    }

    @Override
    public String toString() {
        return "File{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", history=" + history +
                '}';
    }
}
