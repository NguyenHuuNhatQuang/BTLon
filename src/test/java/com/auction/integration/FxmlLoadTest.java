package com.auction.integration;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test load tất cả 9 FXML để chắc chắn không có lỗi property/syntax.
 * Chạy: ./mvnw test -Dtest=FxmlLoadTest
 */
public class FxmlLoadTest {

    private static final String[] FXML_FILES = {
        "/view/login.fxml",
        "/view/register.fxml",
        "/view/dashboard.fxml",
        "/view/auction-detail.fxml",
        "/view/live-auction.fxml",
        "/view/watchlist.fxml",
        "/view/notifications.fxml",
        "/view/profile.fxml",
        "/view/my-bids.fxml",
        "/view/create-item.fxml"
    };

    @BeforeAll
    static void initJfx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @Test
    void loadAllFxml() throws Exception {
        StringBuilder errors = new StringBuilder();
        int pass = 0;
        for (String path : FXML_FILES) {
            URL res = getClass().getResource(path);
            if (res == null) {
                errors.append("MISSING: ").append(path).append("\n");
                continue;
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Exception> error = new AtomicReference<>();
            Platform.runLater(() -> {
                try {
                    new FXMLLoader(res).load();
                } catch (Exception e) {
                    error.set(e);
                } finally {
                    latch.countDown();
                }
            });
            latch.await();

            if (error.get() != null) {
                errors.append("FAIL ").append(path).append(": ")
                    .append(error.get().getMessage()).append("\n");
            } else {
                pass++;
                System.out.println("✅ " + path);
            }
        }
        System.out.println("\nPassed: " + pass + "/" + FXML_FILES.length);
        assertEquals(0, errors.length(), "\nFXML load errors:\n" + errors);
    }
}
