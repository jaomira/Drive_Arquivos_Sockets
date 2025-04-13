import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final String FILE_NAME = "users.txt";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor aguardando conexões...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado!");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String action = in.readLine(); // LOGIN ou REGISTER
                String username = in.readLine();
                String password = in.readLine();

                if ("LOGIN".equals(action)) {
                    if (authenticate(username, password)) {
                        out.println("Login efetuado com sucesso!");
                    } else {
                        out.println("Falha no login. Verifique as credenciais e tente novamente.");
                    }
                } else if ("REGISTER".equals(action)) {
                    if (registerUser(username, password)) {
                        out.println("Registro efetuado com sucesso!");
                    } else {
                        out.println("Falha no registro. Tente novamente.");
                    }
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Autentica usuário verificando o arquivo
    private static boolean authenticate(String user, String pass) {
        List<String[]> users = loadUsers();
        for (String[] credentials : users) {
            if (credentials[0].equals(user) && credentials[1].equals(pass)) {
                return true;
            }
        }
        return false;
    }

    private static boolean registerUser(String user, String pass) {
        List<String[]> users = loadUsers();

        // Verifica se o usuário já existe
        for (String[] credentials : users) {
            if (credentials[0].equals(user)) {
                return false;
            }
        }

        // Adiciona novo usuário e salva no arquivo
        users.add(new String[]{user, pass});
        saveUsers(users);

        // Cria a estrutura de pastas do usuário
        String basePath = "armazenamento/" + user;
        File userFolder = new File(basePath);
        if (!userFolder.exists()) {
            userFolder.mkdirs(); // cria a pasta do usuário
        }

        // Subpastas por tipo de arquivo
        String[] tipos = {"pdf", "txt", "png"};
        for (String tipo : tipos) {
            File subFolder = new File(basePath + "/" + tipo);
            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }
        }

        return true;
    }

    // Lê o arquivo e carrega os usuários salvos
    private static List<String[]> loadUsers() {
        List<String[]> users = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        users.add(parts);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    // Salva a lista de usuários no arquivo
    private static void saveUsers(List<String[]> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (String[] user : users) {
                writer.println(user[0] + ":" + user[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}