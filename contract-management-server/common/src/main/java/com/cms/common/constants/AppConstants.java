package com.cms.common.constants;

public final class AppConstants {

    private AppConstants() {
        // Utility class - prevent instantiation
    }

    // Date formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    // Pagination defaults
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // Contract defaults
    public static final int DEFAULT_NOTICE_PERIOD_DAYS = 30;
    public static final int DEFAULT_EXPIRY_WARNING_DAYS = 30;
    public static final String DEFAULT_CURRENCY = "USD";

    // Performance review
    public static final double MIN_RATING = 1.0;
    public static final double MAX_RATING = 5.0;
    public static final double RENEWAL_ELIGIBILITY_THRESHOLD = 3.0;

    // Notification
    public static final int MAX_RETRY_COUNT = 3;
    public static final int[] NOTIFICATION_DAYS_BEFORE_EXPIRY = {30, 14, 7, 1};

    // API versioning
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;

    // Error codes
    public static final String ERR_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERR_DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String ERR_VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String ERR_BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
    public static final String ERR_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
}
