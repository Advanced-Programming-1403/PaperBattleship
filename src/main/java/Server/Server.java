package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int PORT = 3737;
    private static ArrayList<ClientHandler> clients = new ArrayList();
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    static ClientHandler playerOne;
    static ClientHandler playerTwo;


    public static void main(String[] args) {
        ServerSocket listener = null;

        try {
            listener = new ServerSocket(PORT);
            int i = 0;
            while(true) {
                Socket socket = listener.accept();
                ClientHandler clientHandler = new ClientHandler(socket, i == 0);
                if(i == 0) {
                    playerOne = clientHandler;
                }
                if(i == 1) {
                    playerTwo = clientHandler;
                    playerOne.setEnemy(playerTwo);
                    playerTwo.setEnemy(playerOne);
                }
                i++;
                clients.add(clientHandler);
                pool.execute(clientHandler);
                //TODO(for later): handle the situation where more than two players join at the same time
                //TODO(for later): handle the situation where more than one game happens
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (listener != null) {
                try {
                    listener.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            pool.shutdown();
        }
    }
}