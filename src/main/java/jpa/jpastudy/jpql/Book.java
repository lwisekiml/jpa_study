package jpa.jpastudy.jpql;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@DiscriminatorValue("BB")
public class Book extends Item {
    private String author;
    private String isbn;
}
