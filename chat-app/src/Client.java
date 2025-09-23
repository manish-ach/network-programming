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

            while(true) {
                System.out.println("Welcome to Chat-app!");
                System.out.println("1. SignIn");
                System.out.println("2. SignUp");
                System.out.print(">> ");
                int option = sc.nextInt();
                out.writeInt(option);
                out.flush();
                sc.nextLine();

                System.out.println("Enter your Username");
                System.out.print(">> ");
                String username = sc.nextLine();
                out.writeUTF(username);
                out.flush();
                System.out.println("Enter password");
                System.out.print(">> ");
                String password = sc.nextLine();
                out.writeUTF(password);
                out.flush();

                String response = in.readUTF();
                System.out.println(response);

                if (response.equals("Connected!")) break;
            }


            System.out.println("Connected! Type messages, /quit to exit.");
            System.out.print(">> ");

            Thread reader = getThread(in);
            reader.start();

            while(true) {
                String line = sc.nextLine();
                if (line.equalsIgnoreCase("/quit")) break;
                System.out.print(">> ");
                out.writeUTF(line);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Bye bye");
        }
    }

    private static Thread getThread(DataInputStream in) {
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
        return reader;
    }
}