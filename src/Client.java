import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.nio.Buffer;

public class Client implements Runnable {
    
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private boolean closeConnection;

    public Client() {
        closeConnection = false;
    }

    public void run() {
        try {
            socket = new Socket("127.0.0.1", 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            InputHandler iHandler = new InputHandler();
            Thread t = new Thread(iHandler);
            t.start();
            String msgFromServer;
            while ((msgFromServer = in.readLine()) != null) {
                System.out.println(msgFromServer);
            }
        } catch (Exception e) {
            close();
        }

    }

    public void close() {
        closeConnection = true;
        try {
            in.close();
            out.close();
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
        }
    }


    class InputHandler implements Runnable {

        private BufferedReader inputReader;

        public void run () {
            try {
                inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (!closeConnection) {
                    String msg = inputReader.readLine();
                    if (msg.equals("/exit")) {
                        out.println("/exit");
                        inputReader.close();
                        close();
                    } else {
                        out.println(msg);
                    }
                }
            } catch (Exception e) {
                close();
            }
        }
        
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

}
