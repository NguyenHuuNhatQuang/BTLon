package com.auction.integration;

import com.auction.server.network.AuctionServer;
import com.auction.shared.Message;
import com.auction.shared.MessageType;
import com.auction.shared.Payloads;
import model.auction.Auction;
import model.auction.AuctionManager;
import model.item.Electronics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Socket E2E test - chạy Server + Client cùng JVM.
 * Verify socket compatibility giữa C-Team Client và S-Team Server.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SocketE2ETest {

    private static AuctionServer server;
    private static final int PORT = 17070;  // port riêng để không clash production

    @BeforeAll
    static void startServer() throws InterruptedException {
        // Seed 1 auction để test
        Auction a = new Auction(
            "A001",
            new Electronics("I001", "MacBook Test", 12),
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusMinutes(30),
            42_000_000.0, 500_000.0);
        AuctionManager.getInstance().addAuction(a);
        a.startAuction();

        server = new AuctionServer(PORT);
        server.registerAuction(a);
        server.start();
        Thread.sleep(500);  // wait server bind port
        System.out.println("✅ Test server started on port " + PORT);
    }

    @AfterAll
    static void stopServer() {
        if (server != null) server.stop();
    }

    @Test
    @Order(1)
    void test1_socketConnects() throws Exception {
        try (Socket s = new Socket("localhost", PORT)) {
            assertTrue(s.isConnected(), "Socket phải kết nối được port " + PORT);
            System.out.println("✅ Test 1: Socket connect OK");
        }
    }

    @Test
    @Order(2)
    void test2_loginRequestResponse() throws Exception {
        try (Socket s = new Socket("localhost", PORT)) {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            Message req = new Message(MessageType.LOGIN_REQUEST,
                new Payloads.LoginPayload("alice", "1234"));
            out.reset(); out.writeObject(req); out.flush();

            Object resp = in.readObject();
            assertInstanceOf(Message.class, resp);
            Message m = (Message) resp;
            assertEquals(MessageType.LOGIN_RESPONSE, m.getType());
            assertInstanceOf(Payloads.LoginResponsePayload.class, m.getPayload());
            Payloads.LoginResponsePayload p = (Payloads.LoginResponsePayload) m.getPayload();
            assertTrue(p.isSuccess());
            assertNotNull(p.sessionToken());
            System.out.println("✅ Test 2: LOGIN_REQUEST → LOGIN_RESPONSE OK");
            System.out.println("   Session: " + p.sessionToken());
        }
    }

    @Test
    @Order(3)
    void test3_getAuctions() throws Exception {
        try (Socket s = new Socket("localhost", PORT)) {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            Message req = new Message(MessageType.GET_AUCTIONS_REQUEST, Payloads.FetchFilter.all());
            out.reset(); out.writeObject(req); out.flush();

            Message resp = (Message) in.readObject();
            assertEquals(MessageType.GET_AUCTIONS_RESPONSE, resp.getType());
            Payloads.AuctionListPayload p = (Payloads.AuctionListPayload) resp.getPayload();
            assertNotNull(p.items());
            assertTrue(p.totalCount() > 0);
            System.out.println("✅ Test 3: GET_AUCTIONS_REQUEST OK, total=" + p.totalCount());
        }
    }

    @Test
    @Order(4)
    void test4_placeBidValid() throws Exception {
        try (Socket s = new Socket("localhost", PORT)) {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            Message req = new Message(MessageType.BID_REQUEST,
                new Payloads.BidPayload("A001", "alice", 43_000_000.0));
            out.reset(); out.writeObject(req); out.flush();

            // Có thể nhận BID_RESPONSE hoặc UPDATE_AUCTION (broadcast) trước
            Object firstResp = in.readObject();
            assertInstanceOf(Message.class, firstResp);
            Message m = (Message) firstResp;
            assertTrue(m.getType() == MessageType.BID_RESPONSE
                    || m.getType() == MessageType.UPDATE_AUCTION,
                "Expected BID_RESPONSE or UPDATE_AUCTION, got " + m.getType());
            System.out.println("✅ Test 4: BID_REQUEST → " + m.getType() + " OK");
        }
    }
}
