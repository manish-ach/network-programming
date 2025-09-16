import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private final int port;
    private final ExecutorService pool;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    public Server(int port, int maxThreads) {
        this.port = port;
        this.pool = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() {
        try(ServerSocket ss = new ServerSocket(port)) {
            serverSocket = ss;
            System.out.println("server listening on port: " + port);

            // graceful Ctrl+C shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    shutdown();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }));

            while(running) {
                try {
                    Socket client = ss.accept();
                    System.out.println("client connected: " + client.getRemoteSocketAddress());
                    pool.submit(() -> handleClient(client));
                } catch (SocketException e) {
                    if(running) e.printStackTrace();
                    break;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws InterruptedException, IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        pool.shutdown();

        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            pool.shutdownNow();
        }
        System.out.println("Server stopped!");
    }


    public void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            while (!socket.isClosed()) {
                int length;
                try {
                    length = in.readInt();
                } catch (EOFException e) {
                    break;
                }

                byte type = in.readByte();
                byte[] payload = new byte[length];
                in.readFully(payload);
                String message = new String(payload, StandardCharsets.UTF_8);

                System.out.printf("client %s -> [type=%d] %s%n",
                        socket.getRemoteSocketAddress(), type, message);

                out.writeInt(payload.length);
                out.writeByte(type);
                out.write(payload);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server(9999, 10);
        server.start();
    }
}
