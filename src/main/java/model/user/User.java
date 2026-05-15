package model.user;
import model.Entity;

public abstract class User extends Entity {
    private String username;
    private String hashedPassword; // Lưu mật khẩu đã được băm mã hóa

    public User(String id, String username, String password) {
        super(id);
        this.username = username;
        this.hashedPassword = hashPassword(password);
    }

    public String getUsername() {
        return username;
    }

    // TUYỆT ĐỐI KHÔNG CÓ getPassword()

    /**
     * Phương thức phục vụ Đăng nhập: Trả về true nếu mật khẩu nhập vào khớp
     */
    public boolean verifyPassword(String inputPassword) {
        // Băm mật khẩu người dùng vừa nhập và so sánh với mã băm đã lưu
        String hashedInput = hashPassword(inputPassword);
        return this.hashedPassword.equals(hashedInput);
    }

    /**
     * Phương thức đổi mật khẩu an toàn: Yêu cầu mật khẩu cũ phải đúng
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (verifyPassword(oldPassword)) {
            this.hashedPassword = hashPassword(newPassword);
            return true;
        }
        return false; // Sai mật khẩu cũ, từ chối đổi
    }

    /**
     * Hàm băm mật khẩu (Mô phỏng).
     * Trong dự án thực tế, bạn sẽ dùng thư viện như BCrypt hoặc SHA-256 ở đây.
     */
    private String hashPassword(String plainText) {
        // Mô phỏng mã hóa đơn giản bằng cách thêm muối (salt) và đảo chuỗi
        return plainText + "_ENCRYPTED_SALT_2026";
    }
}