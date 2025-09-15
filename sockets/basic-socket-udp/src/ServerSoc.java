import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerSoc {
    private static int sum(int val1, int val2) {
        return val1 + val2;
    }

    private static DatagramPacket recPacket(DatagramSocket serverSocket) throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        serverSocket.receive(packet);
        return packet;
    }

    private static void sendPacket(DatagramSocket serverSocket, InetAddress addr, int port, String msg)
            throws Exception {
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, addr, port);
        serverSocket.send(packet);
    }

    private static String pacString(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }

    public static void main(String[] args) {
        final int PORT = 9999;
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Server running on Port: " + PORT);

            while(true) {
                DatagramPacket initPacket = recPacket(serverSocket);
                InetAddress clientAddr = initPacket.getAddress();
                int clientPort = initPacket.getPort();

                String msg = pacString(initPacket);
                System.out.println("Connection status: " + msg + " Client Port: " + clientPort);
                sendPacket(serverSocket, clientAddr, clientPort, msg);
                System.out.println("sent connection status!");

                DatagramPacket numPacket = recPacket(serverSocket);
                String numbers = pacString(numPacket);
                String[] parts = numbers.split(",");
                int val1 = Integer.parseInt(parts[0].trim());
                int val2 = Integer.parseInt(parts[1].trim());
                int numSum = sum(val1, val2);
                sendPacket(serverSocket, clientAddr, clientPort, String.valueOf(numSum));
                System.out.println("sent data!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
