import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

            while (running) {
                    Socket client = ss.accept();
                    ClientHandler handler = new ClientHandler(client, this);
                    clients.add(handler);
                    pool.submit(handler);
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
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
            while (true) {
                String msg = in.readUTF();
                System.out.println("[" + socket.getPort() + "] " + msg);
                server.broadcast(msg, this);
            }
        } catch (IOException e) {
            System.out.println("Client Left: " + e.getMessage());
        } finally {
            server.removeClient(this);
        }
    }

    void send(String m) {
        try { out.writeUTF(m); }
        catch (IOException ignored) {}
    }
}
