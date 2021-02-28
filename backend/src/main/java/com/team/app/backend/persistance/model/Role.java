package com.team.app.backend.persistance.model;
import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {

    public static final String USER = "R_USER";
    public static final String ADMIN = "R_ADMIN";

    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name;
    }

}
