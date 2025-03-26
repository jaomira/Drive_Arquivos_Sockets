import java.io.*;
import java.net.*;

public class Servidor {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4000);
        Socket socket = serverSocket.accept();
        System.out.println("Um cliente foi conectado");

        InputStreamReader inputReader = new InputStreamReader(socket.getInputStream());
        PrintStream saida = new PrintStream(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(inputReader);
        String x;
        while ((x = reader.readLine()) != null) {
            saida.println("Servidor: " + x);
        }
    }
}