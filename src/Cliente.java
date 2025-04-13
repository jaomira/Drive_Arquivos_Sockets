import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String [] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Bem-vindo ao seu drive de arquivos!");
            System.out.println("Escolha uma opção: \n1 - Login \n2 - Cadastro");
            String choice = console.readLine();

            String action = choice.equals("1") ? "LOGIN" : "REGISTER";

            System.out.print("Usuário: ");
            String username = console.readLine();
            System.out.print("Senha: ");
            String password = console.readLine();

            out.println(action);
            out.println(username);
            out.println(password);

            String response = in.readLine();
            System.out.println("Resposta do servidor: " + response);

            // Recebe a listagem de arquivos
            String line;
            while ((line = in.readLine()) != null && !line.equals("FIM_LISTA")) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
