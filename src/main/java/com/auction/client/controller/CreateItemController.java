package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneRouter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class CreateItemController implements Initializable {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> conditionBox;
    @FXML private TextArea descArea;
    @FXML private TextField startPriceField;
    @FXML private TextField stepPriceField;
    @FXML private ComboBox<String> durationBox;
    @FXML private TextField buyNowField;
    @FXML private Label errorLabel;
    @FXML private StackPane imageDrop;

    private File selectedImage;
    private byte[] selectedImageBytes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categoryBox.setItems(FXCollections.observableArrayList(
            "Điện tử", "Nghệ thuật", "Phương tiện", "Trang sức"));
        categoryBox.getSelectionModel().selectFirst();

        conditionBox.setItems(FXCollections.observableArrayList(
            "Mới", "Như mới", "Tốt", "Đã qua sử dụng"));
        conditionBox.getSelectionModel().selectFirst();

        durationBox.setItems(FXCollections.observableArrayList(
            "1 giờ", "6 giờ", "12 giờ", "24 giờ", "3 ngày", "7 ngày"));
        durationBox.getSelectionModel().select(3);
    }

    @FXML
    private void handlePickImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh sản phẩm");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Ảnh", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Tất cả file", "*.*"));

        Window owner = imageDrop.getScene() != null ? imageDrop.getScene().getWindow() : null;
        File f = chooser.showOpenDialog(owner);
        if (f == null) return;

        // Kiểm tra kích thước (tối đa 5MB)
        long size = f.length();
        if (size > 5L * 1024 * 1024) {
            AlertHelper.error("Ảnh quá lớn",
                "File " + (size / 1024 / 1024) + "MB vượt giới hạn 5MB.");
            return;
        }

        try {
            selectedImage = f;
            selectedImageBytes = Files.readAllBytes(f.toPath());

            // Load và hiển thị preview
            Image img;
            try (FileInputStream fis = new FileInputStream(f)) {
                img = new Image(fis);
            }
            if (img.isError()) {
                throw new IllegalArgumentException("File không phải ảnh hợp lệ.");
            }

            ImageView preview = new ImageView(img);
            preview.setPreserveRatio(true);
            preview.setFitHeight(200);
            preview.setFitWidth(360);

            Label changeHint = new Label("Click để đổi ảnh khác · " + f.getName()
                + " (" + (size / 1024) + " KB)");
            changeHint.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.6);"
                + "-fx-padding: 6 12; -fx-background-radius: 50; -fx-font-size: 11;");

            VBox wrap = new VBox(preview);
            wrap.setAlignment(Pos.CENTER);

            imageDrop.getChildren().setAll(wrap, changeHint);
            StackPane.setAlignment(changeHint, Pos.BOTTOM_CENTER);
            StackPane.setMargin(changeHint, new javafx.geometry.Insets(0, 0, 12, 0));

            imageDrop.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                + "-fx-border-color: -fx-primary; -fx-border-radius: 12;"
                + "-fx-border-width: 2; -fx-cursor: hand;");

        } catch (Exception ex) {
            AlertHelper.error("Lỗi đọc ảnh", ex.getMessage());
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            validate();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            AlertHelper.error("Form không hợp lệ", ex.getMessage());
            return;
        }

        String imgInfo = selectedImage != null
            ? "\nẢnh: " + selectedImage.getName() + " (" + (selectedImageBytes.length / 1024) + " KB)"
            : "\n(chưa chọn ảnh)";
        AlertHelper.info("Đăng thành công",
            "Phiên đấu giá đã được gửi." + imgInfo
            + "\n\nKhi Server sẵn sàng: gửi qua Socket dạng CREATE_ITEM_REQUEST với imageBytes.");
        SceneRouter.go("dashboard");
    }

    @FXML
    private void handleDraft() {
        AlertHelper.info("Đã lưu nháp", "Bạn có thể tiếp tục sau ở mục Sản phẩm của tôi.");
    }

    @FXML
    private void handleCancel() {
        if (AlertHelper.confirm("Hủy?", "Dữ liệu đã nhập sẽ bị mất."))
            SceneRouter.go("dashboard");
    }

    private void validate() {
        if (nameField.getText().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm.");
        if (descArea.getText().length() < 10)
            throw new IllegalArgumentException("Mô tả phải có ít nhất 10 ký tự.");

        double start = parsePositive(startPriceField.getText(), "Giá khởi điểm");
        double step  = parsePositive(stepPriceField.getText(),  "Bước giá");

        if (step > start)
            throw new IllegalArgumentException("Bước giá không được lớn hơn giá khởi điểm.");

        String buyNowRaw = buyNowField.getText();
        if (buyNowRaw != null && !buyNowRaw.isBlank()) {
            double buyNow = parsePositive(buyNowRaw, "Giá mua ngay");
            if (buyNow <= start)
                throw new IllegalArgumentException("Giá mua ngay phải lớn hơn giá khởi điểm.");
        }
    }

    private double parsePositive(String raw, String fieldName) {
        try {
            double v = Double.parseDouble(raw.trim().replaceAll("[,\\s]", ""));
            if (v <= 0) throw new IllegalArgumentException(fieldName + " phải > 0.");
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
    }
}
