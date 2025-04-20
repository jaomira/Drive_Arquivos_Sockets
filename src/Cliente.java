import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String [] args) {
        Scanner scanner = new Scanner(System.in);
        boolean ativo = true;

        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Bem-vindo ao seu drive de arquivos!");
            System.out.println("Escolha uma opção: \n1 - Login \n2 - Cadastro");
            String choice = console.readLine();

            String action = choice.equals("1") ? "LOGIN" : "REGISTER";

            System.out.print("Insira o seu nome de usuário: ");
            String username = console.readLine();
            System.out.print("Insira a sua senha: ");
            String password = console.readLine();

            out.println(action);
            out.println(username);
            out.println(password);

            String response = in.readLine();

            switch (response) {
                case "Registro efetuado com sucesso!" -> {
                    System.out.println(response+ " Seus diretórios estão sendo criados.");
                }
                case "Falha no registro. Tente novamente." -> {
                    System.out.println(response);
                }
            }

            System.out.println("\nBem-vindo(a), "+username+"!");
            int opcao;
            System.out.println("Escolha uma opção:\n1 - Listar arquvios\n2 - Inserir arquivo\n3 - Baixar arquivos\n4 " +
                    "- Sair");
            opcao = scanner.nextInt();
            switch(opcao) {
                case 1 -> {
                    System.out.println("[listagem]");
                    String line;
                    while ((line = in.readLine()) != null && !line.equals("FIM_LISTA")) {
                        System.out.println(line);
                    }
                }
                case 2 -> {
                    System.out.println("[inserção]");
                }
                case 3 -> {
                    System.out.println("[baixar]");
                }
                case 4 -> {
                    System.out.println("[sair]");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
