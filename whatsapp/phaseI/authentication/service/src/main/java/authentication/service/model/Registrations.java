package authentication.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "registration")
public class Registrations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;      // Unique ID generation

    @Column(unique = true)
    private String phone;       // Phone number to be distinct

    private String password;        // The password can be same for two different users

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;
}
