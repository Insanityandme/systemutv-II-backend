package se.myhappyplants.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class StartServerTest {

    @Test
    void testServerStart() {
        // Port where the server is expected to be running
        int serverPort = 2555;

        // Create an instance of StartServer in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                StartServer.main(new String[]{});
            } catch (Exception e) {
                fail("Exception thrown during server execution: " + e.getMessage());
            }
        });

        // Start the server in a new thread
        serverThread.start();

        // Wait for a brief moment to ensure the server has started
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Attempt to create a socket connection to the server
        try (Socket socket = new Socket("localhost", serverPort)) {
            // If the socket connection is successful, the server is considered running
            assertTrue(socket.isConnected());
        } catch (IOException e) {
            fail("Failed to connect to the server: " + e.getMessage());
        }

        // Stop the server thread (this would normally be done gracefully in a real test environment)
        serverThread.interrupt();
    }
}