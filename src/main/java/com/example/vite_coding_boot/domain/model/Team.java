package com.example.vite_coding_boot.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String division;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String name;

    protected Team() {
    }

    public Team(String division, String department, String name) {
        this.division = division;
        this.department = department;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getDivision() {
        return division;
    }

    public String getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return division + " > " + department + " > " + name;
    }
}
