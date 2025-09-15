import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientSoc {
    private static  void sendData(DatagramSocket socket, InetAddress localAddr, int port, String msg)
    throws Exception {
        byte[] data = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, localAddr, port);
        socket.send(packet);
    }

    private static String recData(DatagramSocket socket) throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public static void main(String[] args) {
        final int SERVER_PORT = 9999;
        Scanner scanner = new Scanner(System.in);

        try(DatagramSocket socket = new DatagramSocket()) {
            InetAddress localAddr = InetAddress.getLocalHost();


            while (true) {
                String msg = "Successful";
                sendData(socket, localAddr, SERVER_PORT, msg);
                System.out.println("Server reply: " + recData(socket));

                System.out.print("Enter a number: ");
                int val1 = scanner.nextInt();
                System.out.print("Enter another number: ");
                int val2 = scanner.nextInt();
                scanner.nextLine();

                String numberMsg = val1 + "," + val2;
                sendData(socket, localAddr, SERVER_PORT, numberMsg);
                System.out.println("sum = " + recData(socket));

                System.out.print("continue? (y/n): ");
                String choice = scanner.nextLine();
                if (choice.equalsIgnoreCase("n")) break;
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
