package com.example.fora.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String url;

    @OneToMany(mappedBy = "section")
    private Set<MainGroup> groups;

    public Section() {
    }

    public Section(String name, String url) {
        this.name = name;
        this.url = url;
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

    public Set<MainGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<MainGroup> groups) {
        this.groups = groups;
    }
}
