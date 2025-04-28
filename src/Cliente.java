import java.io.*;
import java.net.Socket;

public class Cliente {
    private static BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Método menu: login ou cadastro
            menu(console, out);

            // Resposta do servidor após login ou cadastro
            String resposta = in.readLine();
            System.out.println(resposta);

            // Menu principal
            boolean conectado = true;
            while (conectado) {
                System.out.println("\n[MENU]");
                System.out.println("1 - Listar arquivos");
                System.out.println("2 - Inserir arquivo");
                System.out.println("3 - Baixar arquivo");
                System.out.println("4 - Sair");

                System.out.print("Escolha uma opção: ");
                String opcao = console.readLine();
                out.println(opcao);

                switch (opcao) {
                    case "1": // Lista os arquivos
                        listarArquivos(in);
                        break;
                    case "2": // Insere arquivo
                        if (in.readLine().equals("OK_UPLOAD")) {
                            enviarArquivo(socket);
                        } else {
                            System.out.println("Servidor não autorizou envio.");
                        }
                        break;
                    case "3": // Baixa arquivo
                        if (in.readLine().equals("OK_DOWNLOAD")) {
                            baixarArquivo(socket, in);
                        } else {
                            System.out.println("Servidor não autorizou download.");
                        }
                        break;
                    case "4":
                        System.out.println(in.readLine());
                        conectado = false;
                        break;
                    default:
                        System.out.println(in.readLine());
                        break;
                }
            }

            System.out.println("Cliente finalizado.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void menu(BufferedReader console, PrintWriter out) throws IOException {
        System.out.println("Bem-vindo ao seu drive de arquivos!");
        System.out.println("Escolha uma opção: \n1 - Login \n2 - Cadastro");
        String choice = console.readLine();

        String action = choice.equals("1") ? "LOGIN" : "REGISTER";
        out.println(action);

        System.out.print("Insira o seu nome de usuário: ");
        String username = console.readLine();
        out.println(username);

        System.out.print("Insira a sua senha: ");
        String password = console.readLine();
        out.println(password);
        out.flush();
    }

    private static void listarArquivos(BufferedReader in) throws IOException {
        String linha;
        while (!(linha = in.readLine()).equals("FIM_LISTA")) {
            System.out.println(linha);
        }
    }

    private static void enviarArquivo(Socket socket) throws IOException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("Escolha a pasta destino (pdf, txt, png):");
        String pasta = console.readLine();
        out.println(pasta);

        System.out.print("Digite o caminho completo do arquivo a ser enviado: ");
        String caminhoArquivo = console.readLine();
        File file = new File(caminhoArquivo);

        if (!file.exists()) {
            System.out.println("Arquivo não encontrado.");
            return;
        }

        out.println(file.getName());
        out.println(file.length());
        out.flush();

        try (BufferedOutputStream socketOut = new BufferedOutputStream(socket.getOutputStream());
             BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                socketOut.write(buffer, 0, bytesRead);
            }
            socketOut.flush();
        }

        String confirmacao = in.readLine();
        System.out.println(confirmacao.equals("UPLOAD_COMPLETO") ?
                "Arquivo enviado com sucesso!" : "Ocorreu um problema no upload.");
    }

    private static void baixarArquivo(Socket socket, BufferedReader in) throws IOException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Escolhe a pasta
        System.out.println("Escolha a pasta do arquivo (pdf, txt, png):");
        String pasta = console.readLine();
        out.println(pasta);

        // Escolhe o arquivo
        System.out.print("Digite o nome do arquivo a ser baixado: ");
        String fileName = console.readLine();
        out.println(fileName);

        // Recebe resposta do servidor
        String resposta = in.readLine();
        if (resposta.equals("ERROR")) {
            System.out.println(in.readLine()); // Mostra mensagem de erro
            return;
        }

        // Recebe o tamanho do arquivo
        long fileSize = Long.parseLong(in.readLine());

        // Escolhe local para salvar
        System.out.print("Digite o caminho completo para salvar (incluindo nome do arquivo): ");
        String savePath = console.readLine();

        try (BufferedInputStream socketIn = new BufferedInputStream(socket.getInputStream());
             BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(savePath))) {

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

        System.out.println("Arquivo baixado com sucesso em: " + savePath);
    }
}
