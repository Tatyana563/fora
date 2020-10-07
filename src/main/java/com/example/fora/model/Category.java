package com.example.fora.model;

import javax.persistence.*;

@Entity
@Table
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String url;
    @ManyToOne
    private MainGroup group;

    public Category() {
    }

    public Category(String name, String url, MainGroup group) {
        this.name = name;
        this.url = url;
        this.group = group;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String categoryName) {
        this.name = categoryName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String categoryURL) {
        this.url = categoryURL;
    }

    public MainGroup getGroup() {
        return group;
    }

    public void setGroup(MainGroup group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", categoryName='" + name + '\'' +
                ", categoryURL='" + url + '\'' +
                '}';
    }
}
