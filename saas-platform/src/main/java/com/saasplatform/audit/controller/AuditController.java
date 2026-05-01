package com.saasplatform.audit.controller;

import com.saasplatform.audit.entity.AuditAction;
import com.saasplatform.audit.service.AuditService;
import com.saasplatform.common.response.StandardApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit API")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<StandardApiResponse<?>> getAuditLogs(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(required = false)
            AuditAction action,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to
    ) {

        StandardApiResponse<?> response =
                auditService.getAuditLogs(
                        page,
                        size,
                        action,
                        from,
                        to
                );

        return ResponseEntity.ok(response);
    }
}