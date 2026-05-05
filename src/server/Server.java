package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT        = 5000;
    private static final int THREAD_POOL = 10;   // max simultaneous clients

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL);

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  Student Management Server  v1.0     ║");
        System.out.println("║  Listening on port " + PORT + " ...         ║");
        System.out.println("╚══════════════════════════════════════╝");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[Server] Fatal error: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}