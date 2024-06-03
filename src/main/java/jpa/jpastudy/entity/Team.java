package jpa.jpastudy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Team {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private int age;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}
