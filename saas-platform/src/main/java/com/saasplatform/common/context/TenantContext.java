package com.saasplatform.common.context;

import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<TenantInfo> TENANT = new ThreadLocal<>();

    public static void setTenant(TenantInfo tenant) { TENANT.set(tenant); }

    public static TenantInfo getTenant() {
        TenantInfo info = TENANT.get();
        if (info == null) {
            throw new IllegalStateException(
                    "TenantContext accessed outside of tenant-scoped request"
            );
        }
        return info;
    }

    public static UUID getTenantId()     { return getTenant().id(); }
    public static String getTenantSlug() { return getTenant().slug(); }

    public static void clear() { TENANT.remove(); }

}
