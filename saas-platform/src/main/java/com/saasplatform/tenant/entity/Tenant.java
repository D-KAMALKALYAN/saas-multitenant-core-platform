package com.saasplatform.tenant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tenants" ,
        indexes = {
                @Index(name = "idx_tenants_slug", columnList = "slug"),
                @Index(name = "idx_tenants_status", columnList = "status")
        }
)
public class Tenant {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false , unique = true, length = 100)
    private String slug;

    @Column(nullable = false , unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan = PlanType.BASIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status = StatusType.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return this.status == StatusType.ACTIVE;
    }

}
