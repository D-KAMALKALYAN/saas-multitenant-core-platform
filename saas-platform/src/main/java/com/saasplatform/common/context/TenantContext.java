package com.saasplatform.common.context;

import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<TenantInfo> TENANT = new ThreadLocal<>();

    public static void setTenant(TenantInfo tenant) { TENANT.set(tenant); }
    public static TenantInfo getTenant() { return TENANT.get(); }
    public static UUID getTenantId() { return TENANT.get().id(); }
    public static String getTenantSlug() { return TENANT.get().slug(); }
    public static void clear() { TENANT.remove(); }

}
