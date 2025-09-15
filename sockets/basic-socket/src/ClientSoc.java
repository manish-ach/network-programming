import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientSoc {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9999;

        Scanner scanner = new Scanner(System.in);

        try(Socket socket = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            InetAddress ia = InetAddress.getLocalHost();
            System.out.println("localhost :- " + ia);

            while (true) {
                String response = in.readUTF();
                System.out.println("Server message: " + response);

                System.out.print("Enter a number: ");
                int val1 = scanner.nextInt();
                System.out.print("Enter another number: ");
                int val2 = scanner.nextInt();
                scanner.nextLine();

                out.writeInt(val1);
                out.flush();
                out.writeInt(val2);
                out.flush();

                int sum = in.readInt();
                System.out.println("sum is " + sum);

                System.out.print("continue? (y/n): ");
                String choice = scanner.nextLine();
                out.writeUTF(choice);
                out.flush();
                if (choice.equalsIgnoreCase("n")) {
                    break;
                }
            }
            socket.close();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}