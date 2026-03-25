package authentication.two.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="registrations")
public class Registration {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long uuid;

    @Column(unique=true, nullable=false)
    private String username;

    @Column(unique=true, nullable=false)
    private String phone;

    private String email;

    private String password;
}
