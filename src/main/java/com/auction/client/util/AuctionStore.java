package com.auction.client.util;

import com.auction.client.model.AuctionView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * AuctionStore - in-memory store cho danh sách auction.
 *
 * Dùng chung giữa DashboardController (đọc) và CreateItemController (ghi).
 * Khi user "Đăng phiên đấu giá" mới → add vào đây → Dashboard auto refresh.
 *
 * Khi có Server (sau Tuần 11): thay bằng fetch từ NetworkManager.
 */
public final class AuctionStore {

    private static final ObservableList<AuctionView> AUCTIONS =
        FXCollections.observableArrayList();

    static {
        // Seed mock data ban đầu
        AUCTIONS.addAll(List.of(
            new AuctionView("A001", "MacBook Pro M3 14\"", "Điện tử",     "minh.tran",   42500000, "LIVE",     "02:34", ""),
            new AuctionView("A002", "iPhone 15 Pro Max",   "Điện tử",     "hoa.nguyen",  28900000, "LIVE",     "00:48", ""),
            new AuctionView("A003", "Tranh sơn dầu phố cổ", "Nghệ thuật", "art.studio",   8500000, "OPEN",     "1d 2h", ""),
            new AuctionView("A004", "Honda Civic 2020",    "Phương tiện", "auto.house", 580000000, "OPEN",     "3d",    ""),
            new AuctionView("A005", "Đồng hồ Rolex cổ",    "Trang sức",   "luxury.vn",  120000000, "LIVE",     "05:12", ""),
            new AuctionView("A006", "Sony A7 IV body",      "Điện tử",    "camera.pro",  45000000, "OPEN",     "12h",   ""),
            new AuctionView("A007", "Lego Millennium Falcon","Nghệ thuật","brick.fan",   12500000, "FINISHED", "Hết",    ""),
            new AuctionView("A008", "Nhẫn kim cương 1ct",   "Trang sức",  "diamond.co", 350000000, "OPEN",     "2d 6h", "")
        ));
    }

    private AuctionStore() {}

    /** Đọc list để hiển thị Dashboard. */
    public static ObservableList<AuctionView> getAuctions() {
        return AUCTIONS;
    }

    /** Thêm phiên mới (từ CreateItem). Tự gen ID. */
    public static AuctionView add(String itemName, String category, String sellerName,
                                   double startPrice, String timeLeft) {
        String id = String.format("A%03d", AUCTIONS.size() + 1);
        AuctionView a = new AuctionView(id, itemName, category, sellerName,
            startPrice, "OPEN", timeLeft, "");
        AUCTIONS.add(0, a);  // thêm lên đầu để dễ thấy
        return a;
    }

    public static int size() { return AUCTIONS.size(); }
}
