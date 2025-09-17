import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9999;
        try (Socket socket = new Socket(host, port);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Connected! Type messages, /quit to exit.");
            System.out.print(">>");
            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        System.out.print("\r[>" + msg + "\n>>") ;
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected!");
                }
            });
            reader.setDaemon(true);
            reader.start();

            while(true) {
                String line = sc.nextLine();
                if (line.equalsIgnoreCase("/quit")) break;
                out.writeUTF(line);
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}