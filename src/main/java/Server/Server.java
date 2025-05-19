package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Server.ClientHandler;

public class Server {
    private static int PORT = 3737;
    private static ArrayList<ClientHandler> clients = new ArrayList();
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    static ClientHandler playerOne;
    static ClientHandler playerTwo;


    public static void main(String[] args) {
        ServerSocket listener = null;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            int i = 0;
            while(true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientThread = new ClientHandler(socket, i % 2 == 0);

                if(i % 2 == 0) {
                    playerOne = clientThread;
                }
                else {
                    playerTwo = clientThread;
                    playerOne.setEnemy(playerTwo);
                    playerTwo.setEnemy(playerOne);
                }
                i++;
                clients.add(clientThread);
                pool.execute(clientThread);

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