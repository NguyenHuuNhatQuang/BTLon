// File: model/user/UserFactory.java
package model.user;

public class UserFactory {

  public static User createUser(String type, String id, String username, String password) {
    // Có thể bổ sung các logic kiểm tra độ mạnh của password ở đây trước khi tạo User
    if (password == null || password.length() < 6) {
      throw new IllegalArgumentException("Lỗi: Mật khẩu phải có ít nhất 6 ký tự!");
    }

    if (type.equalsIgnoreCase("Bidder")) {
      return new Bidder(id, username, password);
    } else if (type.equalsIgnoreCase("Seller")) {
      return new Seller(id, username, password);
    } else if (type.equalsIgnoreCase("Admin")) {
      return new Admin(id, username, password);
    }

    throw new IllegalArgumentException("Loại người dùng không hợp lệ: " + type);
  }
}