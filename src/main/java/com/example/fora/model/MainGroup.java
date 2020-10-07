package com.example.fora.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
public class MainGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String url;

    @ManyToOne
    private Section section;

    @OneToMany(mappedBy = "group")
    private Set<Category> categories;

    public MainGroup(String name, String url, Section section) {
        this.name = name;
        this.url = url;
        this.section = section;
    }

    public MainGroup() {
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

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

}
