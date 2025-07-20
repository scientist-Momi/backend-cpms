package dev.olaxomi.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.utils.PermissionConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "user_permissions")
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Convert(converter = PermissionConverter.class)
    @Column(name = "permissions", columnDefinition = "json")
    private Set<Permission> permissions = new HashSet<>();

//    @ElementCollection
//    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    @Convert(converter = PermissionConverter.class)
//    @Column(name = "permissions", columnDefinition = "json")
//    private Set<Permission> permissions = new HashSet<>();

    public UserPermission() {
    }
}
