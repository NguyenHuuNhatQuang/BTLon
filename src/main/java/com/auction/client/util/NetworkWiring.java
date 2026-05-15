package com.auction.client.util;

import com.auction.client.model.AuctionView;
import com.auction.client.network.MessageBus;
import com.auction.client.network.NetworkManager;
import com.auction.shared.MessageType;
import com.auction.shared.Payloads;

/**
 * NetworkWiring - cầu nối giữa tầng Network và tầng UI.
 *
 * Gọi NetworkWiring.bootstrap() đúng 1 lần khi app khởi động.
 * Sau đó:
 *  - UPDATE_AUCTION từ Server → AuctionView → NetworkBridge.fireAuctionUpdate
 *  - BID_RESPONSE → NetworkBridge.fireBidAccepted / fireBidRejected
 *  - ERROR → NetworkBridge.fireConnectionLost
 *
 * 2 mode:
 *  - ONLINE: kết nối Server (port 7070) → realtime
 *  - OFFLINE: không có Server → fallback mock data (UI vẫn chạy)
 */
public final class NetworkWiring {

    private static boolean wired = false;
    private static boolean online = false;

    private NetworkWiring() {}

    public static void bootstrap() {
        if (wired) return;
        wired = true;
        subscribeMessageBus();
        tryConnectAsync();
    }

    public static boolean isOnline() { return online; }

    private static void subscribeMessageBus() {
        MessageBus bus = MessageBus.getInstance();

        bus.subscribe(MessageType.UPDATE_AUCTION, msg -> {
            if (msg.getPayload() instanceof Payloads.AuctionSummaryPayload p) {
                NetworkBridge.fireAuctionUpdate(AuctionView.fromSummary(p));
            }
        });

        bus.subscribe(MessageType.BID_RESPONSE, msg -> {
            if (msg.getPayload() instanceof Payloads.BidResponsePayload p) {
                if (p.success()) {
                    NetworkBridge.fireBidAccepted("Đặt giá thành công: " + p.newCurrentBid() + " ₫");
                } else {
                    NetworkBridge.fireBidRejected(
                        p.errorMessage() != null ? p.errorMessage() : "Đặt giá thất bại");
                }
            }
        });

        bus.subscribe(MessageType.ERROR, msg -> {
            String reason = msg.getPayload() instanceof Payloads.ErrorPayload p
                ? p.message()
                : (msg.getPayload() != null ? msg.getPayload().toString() : "Unknown error");
            if (reason != null && reason.toLowerCase().contains("connection")) {
                online = false;
                NetworkBridge.fireConnectionLost();
            }
        });
    }

    private static void tryConnectAsync() {
        Thread t = new Thread(() -> {
            try {
                boolean ok = NetworkManager.getInstance().connect();
                if (ok) {
                    online = true;
                    NetworkBridge.fireConnectionRestored();
                    System.out.println("[NetworkWiring] ONLINE - đã kết nối Server");
                } else {
                    online = false;
                    System.out.println("[NetworkWiring] OFFLINE - dùng mock data");
                }
            } catch (Exception e) {
                online = false;
                System.out.println("[NetworkWiring] OFFLINE - " + e.getMessage());
            }
        }, "NetworkWiring-Bootstrap");
        t.setDaemon(true);
        t.start();
    }

    public static boolean placeBid(String auctionId, String bidderId, double amount) {
        if (!online) return false;
        return NetworkManager.getInstance().send(MessageType.BID_REQUEST,
            new Payloads.BidPayload(auctionId, bidderId, amount));
    }

    public static boolean fetchAuctions() {
        if (!online) return false;
        return NetworkManager.getInstance().send(MessageType.GET_AUCTIONS_REQUEST,
            Payloads.FetchFilter.all());
    }

    public static void shutdown() {
        try { NetworkManager.getInstance().disconnect(); } catch (Exception ignored) {}
        online = false;
    }
}
