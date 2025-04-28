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

                // Autenticação do usuário
                String action = in.readLine();
                String username = in.readLine();
                String password = in.readLine();

                if ("LOGIN".equals(action)) {
                    if (authenticate(username, password)) {
                        out.println("Login efetuado com sucesso!");
                        out.flush();
                    } else {
                        out.println("Falha no login. Verifique as credenciais e tente novamente.");
                        out.flush();
                        socket.close();
                        continue;
                    }
                } else if ("REGISTER".equals(action)) {
                    if (registerUser(username, password)) {
                        out.println("Registro efetuado com sucesso!");
                        out.flush();
                    } else {
                        out.println("Falha no registro. Tente novamente.");
                        out.flush();
                        socket.close();
                        continue;
                    }
                } else {
                    out.println("Ação inválida.");
                    socket.close();
                    continue;
                }

                // Loop de opções
                boolean conectado = true;
                while (conectado) {
                    String opcao = in.readLine();
                    if (opcao == null) {
                        conectado = false;
                        break;
                    }

                    switch (opcao) {
                        case "1": // Lista arquivos
                            sendFileList(out, username);
                            break;

                        case "2": // Insere arquivo
                            out.println("OK_UPLOAD");
                            receiveFile(in, socket.getInputStream(), username, socket);  // Adicione socket como parâmetro
                            break;

                        case "3": // Baixa arquivo
                            out.println("OK_DOWNLOAD");
                            String downloadPasta = in.readLine();
                            String downloadFile = in.readLine();
                            sendFile(out, socket.getOutputStream(), username, downloadPasta, downloadFile);
                            break;

                        case "4": // Sair
                            out.println("Saindo...");
                            conectado = false;
                            break;

                        default:
                            out.println("Opção inválida. Tente novamente.");
                            break;
                    }
                }

                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean authenticate(String username, String password) {
        List<String[]> users = loadUsers();
        for (String[] credentials : users) {
            if (credentials[0].equals(username) && credentials[1].equals(password)) {
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

        // Cria diretórios
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
        out.println("FIM_LISTA"); // Sinaliza o fim da lista
    }

    private static void receiveFile(BufferedReader in, InputStream socketIn, String username, Socket socket) throws IOException {
        String pasta = in.readLine();
        String fileName = in.readLine();
        long fileSize = Long.parseLong(in.readLine());

        System.out.println("Recebendo arquivo: " + fileName + " para a pasta " + pasta);

        File userFolder = new File("armazenamento/" + username + "/" + pasta);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        File outputFile = new File(userFolder, fileName);

        try (BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;

            while (totalRead < fileSize &&
                    (bytesRead = socketIn.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            fileOut.flush();
        }

        System.out.println("Arquivo " + fileName + " recebido com sucesso na pasta " + pasta + "!");

        // Envia a confirmação usando o socket
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("UPLOAD_COMPLETO");
    }

    private static void sendFile(PrintWriter out, OutputStream socketOut, String username, String pasta, String fileName) throws IOException {
        File file = new File("armazenamento/" + username + "/" + pasta + "/" + fileName);

        if (!file.exists()) {
            out.println("ERROR");
            out.println("Arquivo não encontrado.");
            return;
        }

        out.println("OK");
        out.println(file.length()); // Envia o tamanho do arquivo

        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                socketOut.write(buffer, 0, bytesRead);
            }
            socketOut.flush();
        }
        System.out.println("Arquivo " + fileName + " enviado para o cliente.");
    }
}
