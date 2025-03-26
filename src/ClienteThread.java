import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;

public class ClienteThread extends Thread {
    private Socket socket;

    public ClienteThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStreamReader inputReader = new InputStreamReader(socket.getInputStream());
            BufferedReader reader = new BufferedReader(inputReader);
            String x;
            while ((x = reader.readLine()) != null) {
                System.out.println("Cliente: "+ x);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
