import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private final int port;
    private final ExecutorService pool;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    public Server(int port, int maxThreads) {
        this.port = port;
        this.pool = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() {
        try (ServerSocket ss = new ServerSocket(port)) {
            serverSocket = ss;
            System.out.println("Server listening on port: " + port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    shutdown();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }));
            while (running) {
                    Socket client = ss.accept();
                    ClientHandler handler = new ClientHandler(client, this);
                    clients.add(handler);
                    pool.submit(handler);
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("Client Disconnected!");
            };
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


    public void broadcast(String msg, ClientHandler sender) {
        for (ClientHandler c: clients) {
            if (c != sender) c.send(msg);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        new Server(9999, 20).start();
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Server server;
    private String DB_URL;
    private String DB_USER;
    private String DB_PASS;

    ClientHandler(Socket s, Server server) throws IOException {
        this.socket = s;
        this.server = server;
        this.in = new DataInputStream(s.getInputStream());
        this.out = new DataOutputStream(s.getOutputStream());
        System.out.println("Client Connected: " + s.getRemoteSocketAddress());
    }

    @Override
    public void run() {
        try {
            List<String> lines = Files.readAllLines(Path.of("./src/login.txt"));
            DB_URL = lines.get(0);
            DB_USER = lines.get(1);
            DB_PASS = lines.get(2);
            System.out.println(DB_URL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("DB_URL=" + DB_URL);
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            int option = in.readInt(); // 1 or signin, 2 for signup
            String username = in.readUTF();
            String password = in.readUTF();

            boolean success = false;
            if (option == 1) {
                success = checkUser(conn, username, password);
            } else if (option == 2) {
                success = createUser(conn, username, password);
            }

            if (success) {
                out.writeUTF("Connected!");
                System.out.println(username + " logged in.");
                serverLoop();
            } else {
                out.writeUTF("Invalid credentials");
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            server.removeClient(this);
        }
    }

    private boolean checkUser(Connection conn, String username, String password) throws SQLException {
        String sql = "SELECT 1 from users WHERE username=? AND password=?";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean createUser(Connection conn, String user, String pass) throws SQLException {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            return ps.executeUpdate() > 0;
        }
    }

    private void serverLoop() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if ("/quit".equalsIgnoreCase(msg)) break;
            server.broadcast(msg, this);
        }
    }

    void send(String m) {
        try { out.writeUTF(m); }
        catch (IOException ignored) {}
    }
}
