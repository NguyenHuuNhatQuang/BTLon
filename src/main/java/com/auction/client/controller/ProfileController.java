package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.AuctionStore;
import com.auction.client.util.SceneRouter;
import com.auction.client.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ProfileController - load thông tin user thật từ Session.
 * Stats tính từ AuctionStore (số phiên Seller user đăng).
 * Mỗi menu item có hành động cụ thể.
 */
public class ProfileController implements Initializable {

    @FXML private Label avatarLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label joinedLabel;
    @FXML private Label wonLabel;
    @FXML private Label bidsLabel;
    @FXML private Label walletLabel;
    @FXML private Label escrowLabel;
    @FXML private Label escrowSubLabel;
    @FXML private Label ordersBadge;

    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String user = Session.getUsername();
        avatarLabel.setText(Session.getAvatarLetter());
        fullNameLabel.setText(prettify(user));
        usernameLabel.setText("@" + user + " • " + Session.getRole() + " cấp Vàng 🏅");
        joinedLabel.setText("Tham gia: 13/04/2026 • " + AuctionStore.size() + " phiên trong hệ thống");

        wonLabel.setText("0");
        bidsLabel.setText("0");

        walletLabel.setText(money.format(100_000_000) + " ₫");

        long mySellerCount = AuctionStore.getAuctions().stream()
            .filter(a -> a.getSellerName().equalsIgnoreCase(user)).count();
        double mySellerTotal = AuctionStore.getAuctions().stream()
            .filter(a -> a.getSellerName().equalsIgnoreCase(user))
            .mapToDouble(a -> a.getCurrentBid()).sum();
        escrowLabel.setText(money.format(mySellerTotal) + " ₫");
        escrowSubLabel.setText(mySellerCount == 0
            ? "Chưa đăng phiên nào"
            : "Đang giữ cho " + mySellerCount + " phiên đấu giá");

        ordersBadge.setText(mySellerCount > 0 ? mySellerCount + " phiên" : "");
    }

    /** "nam_dev" -> "Nam Dev". */
    private String prettify(String username) {
        if (username == null || username.isBlank()) return "Khách";
        String[] parts = username.replace('_', ' ').replace('.', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)))
              .append(p.substring(1).toLowerCase()).append(' ');
        }
        return sb.toString().trim();
    }

    @FXML private void handleBack() { SceneRouter.go("dashboard"); }

    @FXML private void handleLogout() {
        if (AlertHelper.confirm("Đăng xuất", "Bạn chắc chắn muốn thoát?")) {
            Session.logout();
            SceneRouter.go("login");
        }
    }

    @FXML private void handleDeposit() {
        AlertHelper.info("Nạp tiền", "Cổng thanh toán sẽ tích hợp ở Tuần 13-14.");
    }
    @FXML private void handleWithdraw() {
        AlertHelper.info("Rút tiền", "Tính năng rút tiền đang phát triển.");
    }
    @FXML private void handleWalletHistory() {
        AlertHelper.info("Lịch sử ví", "Chưa có giao dịch nào.");
    }

    @FXML private void handleBidHistory()      { SceneRouter.go("my-bids"); }
    @FXML private void handleOrders() {
        AlertHelper.info("Đơn hàng / Vận chuyển", "Bạn chưa có đơn hàng nào đang giao.");
    }
    @FXML private void handleMyAuctions() {
        long count = AuctionStore.getAuctions().stream()
            .filter(a -> a.getSellerName().equalsIgnoreCase(Session.getUsername())).count();
        if (count == 0) {
            AlertHelper.info("Phiên đang đăng",
                "Bạn chưa đăng phiên nào. Click '+ Đăng' ở navbar để đăng phiên đấu giá đầu tiên.");
        } else {
            AlertHelper.info("Phiên đang đăng (" + count + ")",
                "Bạn đang quản lý " + count + " phiên. Quay lại Dashboard để xem chi tiết.");
        }
    }
    @FXML private void handleChangePassword() {
        AlertHelper.info("Đổi mật khẩu", "Tính năng đang phát triển. Liên hệ admin@bidnow.vn.");
    }
    @FXML private void handleNotiSettings() {
        AlertHelper.info("Cài đặt thông báo", "Sẽ bổ sung push notification ở phiên bản tới.");
    }
    @FXML private void handleSupport() {
        AlertHelper.info("Hỗ trợ",
            "Email: support@bidnow.vn\nHotline: 1900-xxxx\nGiờ làm việc: 8h-22h hàng ngày.");
    }
}
