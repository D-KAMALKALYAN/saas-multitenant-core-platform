package com.saasplatform.common.context;


import java.util.UUID;

public class AuditContext {
    private static final ThreadLocal<UUID> AUDIT_TENANT = new ThreadLocal<>();

    public static void setTenantId(UUID id) { AUDIT_TENANT.set(id); }
    public static UUID getTenantId() { return AUDIT_TENANT.get(); }
    public static void clear() { AUDIT_TENANT.remove(); }
}