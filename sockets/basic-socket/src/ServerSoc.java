import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSoc {
    private static int sum(int val1, int val2) {
        return val1 + val2;
    }
    public static void main(String[] args) {
        int port = 9999;

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            Socket clientSocket = serverSocket.accept();
            

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            while(true) {
                out.writeUTF("Connected successfully");
                out.flush();

                int val1 = in.readInt();
                int val2 = in.readInt();
                System.out.println("received value: " + val1 + " " + val2);

                int sum = sum(val1,val2);
                out.writeInt(sum);
                out.flush();

                String choice = in.readUTF();
                System.out.println("choice: " + choice);
                if(choice.equalsIgnoreCase("n")) {
                    System.out.println("Client Disconnected! Closing Server...");
                    break;
                }
            }
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
