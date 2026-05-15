package com.auction.client.util;

/**
 * Session - lưu thông tin user đang login.
 * Singleton đơn giản, share giữa các Controller.
 */
public final class Session {

    private static String username;
    private static String role = "Bidder";
    private static String sessionToken;

    private Session() {}

    public static void login(String username, String role, String token) {
        Session.username = username;
        if (role != null && !role.isBlank()) Session.role = role;
        Session.sessionToken = token;
    }

    public static void logout() {
        username = null;
        role = "Bidder";
        sessionToken = null;
    }

    public static String getUsername()     { return username != null ? username : "guest"; }
    public static String getRole()         { return role; }
    public static String getSessionToken() { return sessionToken; }
    public static String getAvatarLetter() {
        String u = getUsername();
        return u.isEmpty() ? "?" : u.substring(0, 1).toUpperCase();
    }
    public static boolean isLoggedIn() { return username != null; }
}
