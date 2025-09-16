import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in);) {

            System.out.println("Connected to server at " + host + ":" + port);
            System.out.println("Type message (or 'exit' to quit):");

            while (true) {
                System.out.print("> ");
                String msg = scanner.nextLine();

                if ("exit".equalsIgnoreCase(msg)) {
                    break;
                }

                byte type = 1;
                byte[] payload = msg.getBytes(StandardCharsets.UTF_8);

                out.writeInt(payload.length);
                out.writeByte(type);
                out.write(payload);
                out.flush();

                int length = in.readInt();
                byte respType = in.readByte();
                byte[] respPayload = new byte[length];
                in.readFully(respPayload);

                String response = new String(respPayload, StandardCharsets.UTF_8);
                System.out.printf("[Server %d] %s%n", respType, response);
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 9999);
        client.start();
    }
}
