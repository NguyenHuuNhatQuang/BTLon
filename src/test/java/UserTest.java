import exception.AuctionClosedException;
import exception.InvalidBidException;
import model.auction.Auction;
import model.item.Item;
import model.item.ItemFactory;
import model.user.User;
import model.user.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

// Import các hàm assert của JUnit 5
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
  public static void main(String[] args) {
    // 1. Tạo User thông qua Factory
    System.out.println("--- KHỞI TẠO NGƯỜI DÙNG ---");
    User alice = UserFactory.createUser("Bidder", "USR001", "Alice2026", "MySecret@123");
    User admin = UserFactory.createUser("Admin", "ADM001", "SuperAdmin", "AdminPass999");

    System.out.println("Tạo thành công: " + alice.getUsername() + " (" + alice.getClass().getSimpleName() + ")");

    // 2. Test Đăng nhập (Verify Password)
    System.out.println("\n--- TEST ĐĂNG NHẬP ---");
    System.out.println("Alice thử đăng nhập với 'SaiMatKhau': " + alice.verifyPassword("SaiMatKhau")); // false
    System.out.println("Alice thử đăng nhập với 'MySecret@123': " + alice.verifyPassword("MySecret@123")); // true

    // 3. Test Đổi mật khẩu (Change Password)
    System.out.println("\n--- TEST ĐỔI MẬT KHẨU ---");
    boolean failChange = alice.changePassword("MatKhauCuSai", "NewPass123");
    System.out.println("Cố tình đổi mật khẩu khi nhập sai pass cũ: " + failChange); // false

    boolean successChange = alice.changePassword("MySecret@123", "NewPass123");
    System.out.println("Đổi mật khẩu với pass cũ đúng: " + successChange); // true

    System.out.println("Alice đăng nhập lại bằng pass cũ: " + alice.verifyPassword("MySecret@123")); // false
    System.out.println("Alice đăng nhập lại bằng pass mới: " + alice.verifyPassword("NewPass123")); // true
  }
}