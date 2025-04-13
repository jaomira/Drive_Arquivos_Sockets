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
                        sendFileList(out, username); // <- NOVO
                    } else {
                        out.println("Falha no login. Verifique as credenciais e tente novamente.");
                        out.println("FIM_LISTA"); // <- para não travar o cliente
                    }
                } else if ("REGISTER".equals(action)) {
                    if (registerUser(username, password)) {
                        out.println("Registro efetuado com sucesso!");
                        sendFileList(out, username); // <- NOVO
                    } else {
                        out.println("Falha no registro. Tente novamente.");
                        out.println("FIM_LISTA");
                    }
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        for (String[] credentials : users) {
            if (credentials[0].equals(user)) {
                return false;
            }
        }

        users.add(new String[]{user, pass});
        saveUsers(users);

        String basePath = "armazenamento/" + user;
        File userFolder = new File(basePath);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        String[] tipos = {"pdf", "txt", "png"};
        for (String tipo : tipos) {
            File subFolder = new File(basePath + "/" + tipo);
            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }
        }

        return true;
    }

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

    private static void saveUsers(List<String[]> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (String[] user : users) {
                writer.println(user[0] + ":" + user[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Exibe os arquivos disponíveis para o usuário
    private static void sendFileList(PrintWriter out, String username) {
        String basePath = "armazenamento/" + username;
        File userFolder = new File(basePath);
        if (userFolder.exists()) {
            out.println("Arquivos disponíveis:");

            String[] tipos = {"pdf", "txt", "png"};
            for (String tipo : tipos) {
                File subFolder = new File(userFolder, tipo);
                if (subFolder.exists()) {
                    out.println("[" + tipo + "]");
                    File[] files = subFolder.listFiles();
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            out.println(" - " + file.getName());
                        }
                    } else {
                        out.println(" (vazio)");
                    }
                }
            }
        } else {
            out.println("Nenhuma pasta de usuário encontrada.");
        }
        out.println("FIM_LISTA");
    }
}
