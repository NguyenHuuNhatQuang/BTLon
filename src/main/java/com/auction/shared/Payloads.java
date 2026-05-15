package com.auction.shared;

import java.io.Serializable;
import java.util.List;

/**
 * Payloads — container DTO chuẩn cho mọi Message.payload.
 *
 * Cả Client và Server import từ package SHARED này.
 * Mỗi inner class:
 *   - implements Serializable
 *   - có serialVersionUID
 *   - dùng Java record cho gọn
 */
public final class Payloads {

    private Payloads() {}

    /* ========== AUTHENTICATION ========== */
    public record LoginPayload(String username, String password) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record LoginResponsePayload(String userId, String username,
                                       String fullName, String role,
                                       String sessionToken, String errorMessage)
            implements Serializable {
        private static final long serialVersionUID = 1L;
        public boolean isSuccess() { return errorMessage == null; }
    }

    public record RegisterPayload(String firstName, String lastName,
                                  String username, String email,
                                  String password, String role)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== AUCTION LIST / DETAIL ========== */
    public record FetchFilter(String category, String status, String keyword,
                              String sortBy, int page, int pageSize)
            implements Serializable {
        private static final long serialVersionUID = 1L;
        public static FetchFilter all() { return new FetchFilter(null, null, null, "newest", 0, 20); }
    }

    public record AuctionListPayload(List<?> items, int totalCount, int currentPage)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record AuctionDetailPayload(Object auction, List<?> history, List<?> topBidders)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== BIDDING ========== */
    public record BidPayload(String auctionId, String bidderId, double amount)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record BidResponsePayload(boolean success, double newCurrentBid,
                                     String currentLeader, String errorMessage)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== CREATE ITEM ========== */
    public record CreateItemPayload(String name, String category, String condition,
                                    String description,
                                    double startPrice, double stepPrice,
                                    Double buyNowPrice,
                                    int durationHours,
                                    byte[] imageBytes)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== WATCHLIST ========== */
    public record WatchlistPayload(String userId, String auctionId)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== NOTIFICATION ========== */
    public record NotificationPayload(String id, String type, String title,
                                      String body, long timestamp, boolean unread)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== PROFILE ========== */
    public record ProfilePayload(String userId, String username, String fullName,
                                 String role, double walletBalance, double escrowAmount,
                                 int totalWonAuctions, int totalBids, double rating)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== ERROR ========== */
    public record ErrorPayload(String code, String message) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ========== AUCTION SUMMARY (lightweight, không serialize cả entity) ========== */
    /**
     * Snapshot 1 phiên đấu giá để gửi qua Socket.
     * Tránh serialize toàn bộ Auction entity (có thể chứa List<Observer> nặng).
     * Server tạo bằng fromAuction(Auction).
     */
    public record AuctionSummaryPayload(
            String id,
            String itemName,
            String sellerName,
            double currentBid,
            String status,
            String timeLeft,
            String imageUrl)
            implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Factory tạo từ Auction entity. Server gọi để gửi update.
         * Dùng Object thay vì Auction để package shared không phụ thuộc model.
         */
        public static AuctionSummaryPayload fromAuction(Object auction) {
            if (auction == null) return new AuctionSummaryPayload("?", "?", "", 0, "OPEN", "", "");
            Class<?> cls = auction.getClass();
            String id        = safeInvokeString(cls, auction, "getId", "?");
            String itemName  = safeInvokeItemName(cls, auction);
            double currentBid = safeInvokeDouble(cls, auction, "getCurrentHighestBid");
            String status    = safeInvokeString(cls, auction, "getStatus", "OPEN");
            return new AuctionSummaryPayload(id, itemName, "", currentBid, status, "", "");
        }

        private static String safeInvokeString(Class<?> cls, Object obj, String method, String fallback) {
            try {
                Object v = cls.getMethod(method).invoke(obj);
                return v != null ? v.toString() : fallback;
            } catch (Exception e) { return fallback; }
        }

        private static double safeInvokeDouble(Class<?> cls, Object obj, String method) {
            try {
                Object v = cls.getMethod(method).invoke(obj);
                if (v instanceof Number n) return n.doubleValue();
            } catch (Exception ignored) {}
            return 0;
        }

        private static String safeInvokeItemName(Class<?> cls, Object obj) {
            try {
                Object item = cls.getMethod("getItem").invoke(obj);
                if (item == null) return "";
                Object name = item.getClass().getMethod("getName").invoke(item);
                return name != null ? name.toString() : "";
            } catch (Exception ignored) { return ""; }
        }

        /** Convenience factory cho trường hợp đã có sẵn field. */
        public static AuctionSummaryPayload fromAuction(String id, String itemName,
                                                        double currentBid, String status,
                                                        String timeLeft) {
            return new AuctionSummaryPayload(id, itemName, "", currentBid, status, timeLeft, "");
        }
    }
}
