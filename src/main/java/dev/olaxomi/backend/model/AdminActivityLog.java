package dev.olaxomi.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.enums.Status;
import dev.olaxomi.backend.enums.TargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "admin_activity_log")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private Permission actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id",nullable = false)
    private String targetId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
