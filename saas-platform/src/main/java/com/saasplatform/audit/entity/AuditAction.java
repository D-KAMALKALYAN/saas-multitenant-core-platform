package com.saasplatform.audit.entity;

public enum AuditAction {

    // Tenant actions
    TENANT_CREATED,
    TENANT_UPDATED,
    TENANT_DELETED,

    // Tenant validation actions
    TENANT_VALIDATED_BY_SLUG,
    TENANT_VALIDATED_BY_ID,


    // User actions
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,

    // Auth actions
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    TOKEN_REFRESHED

}
