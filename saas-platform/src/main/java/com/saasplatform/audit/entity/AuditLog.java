package com.saasplatform.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs" ,
        indexes = {
        @Index(name = "idx_audit_tenant_created" , columnList = "tenant_id , created_at"),
        @Index(name = "idx_audit_user_id" , columnList = "user_id , created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id" , nullable = true)
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email", length = 225)
    private String userEmail;

    @Column(name = "action" , nullable = false , length = 100)
    private AuditAction action;

    @Column(name = "entity_type" , length = 100)
    private String entityType;

    @Column(name = "entity_id" , length = 255)
    private String entityId;

    @Column(name = "status" , nullable = false , length = 20)
    private String status;

    @Column(name = "ip_address" , length = 45)
    private String ipAddress;

    @Column(name = "user_agent" , length = 500)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata" , columnDefinition = "jsonb")
    private Map<String , Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at" , nullable = false , updatable = false)
    private LocalDateTime createdAt;

}
