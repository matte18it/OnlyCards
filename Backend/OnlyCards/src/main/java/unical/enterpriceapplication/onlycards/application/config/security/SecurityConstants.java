package unical.enterpriceapplication.onlycards.application.config.security;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class SecurityConstants {
    public static final long EXPIRATION_TIME =1 ;
    public static final TemporalUnit EXPIRATION_UNIT= ChronoUnit.HOURS;

    public static final long EXPIRATION_REFRESH_TOKEN_TIME = 20;
    public static final TemporalUnit EXPIRATION_UNIT_REFRESH_TOKEN = ChronoUnit.DAYS;
    public static final String BEARER_TOKEN_PREFIX ="Bearer " ;
    public static final String LOGIN_URI_ENDING ="/login" ;
    public static final String HEADER_REFRESH_TOKEN ="Refresh-Token" ;
    public static final String REFRESH_TOKEN_URI_ENDING ="/refresh-token" ;
    public static final String BASIC_TOKEN_PREFIX = "Basic ";
}
