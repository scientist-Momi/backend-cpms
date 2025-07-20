package dev.olaxomi.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.olaxomi.backend.enums.AdminAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "admin_activity_logs")
public class AdminActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(nullable = false)
    private AdminAction action;

    @Column(nullable = false, updatable = false )
    private LocalDateTime timestamp = LocalDateTime.now();
}
