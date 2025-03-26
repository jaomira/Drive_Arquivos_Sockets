import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 4000);
        Scanner scanner = new Scanner(System.in);

        ClienteThread clienteThread = new ClienteThread(socket);
        clienteThread.start();
        PrintStream saida = new PrintStream(socket.getOutputStream());
        System.out.println("Digite uma mensagem para o servidor: ");
        String teclado = scanner.nextLine();
        saida.println(teclado);
    }
}