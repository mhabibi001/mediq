package com.quantumx.mediq.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String role;
    private boolean forcePasswordChange;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "user_exam_categories",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "exam_category_id")
    )
    private Set<ExamCategory> examCategories = new HashSet<>();

    @Transient
    private List<Long> examCategoryIds;  // Holds incoming category IDs from the request

    @Column(name = "education_level_id")
    private Long educationLevelId;

    @ManyToOne
    @JoinColumn(name = "time_zone_id", insertable = false, updatable = false) // Ensures the column is referenced but not managed directly
    private TimeZone timeZone;

    @Column(name = "time_zone_id")
    private Long timeZoneId;

    private String institution;
    private String profilePictureUrl;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean agreedToTerms = false;

    public void setEducationLevelId(Long educationLevelId) {
        this.educationLevelId = educationLevelId;
    }

    public Long getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(Long timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Implementing UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
