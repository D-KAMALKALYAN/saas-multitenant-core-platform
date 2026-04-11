package com.saasplatform.user.entity;


import com.saasplatform.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_tenant_id", columnList = "tenant_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "idx_users_email_tenant",
                        columnNames = {"email", "tenant_id"}
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role = RoleType.MEMBER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status = StatusType.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}