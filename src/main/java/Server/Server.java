package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int PORT = 3737;
    // TODO: Create a collection to keep track of connected clients
    private static ArrayList<ClientHandler> clients = new ArrayList();
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    static ClientHandler playerOne;
    static ClientHandler playerTwo;


    public static void main(String[] args) {
        ServerSocket listener = null;

        try {
            // TODO: Create a ServerSocket to listen for incoming connections
            //int i = 0;
            while(true) {

                // TODO: Accept a client connection
                // TODO: Create a ClientHandler named clientThread for the connected client
                // TODO: uncomment this code
                //if(i == 0) {
                //    playerOne = clientThread;
                //}
                //if(i == 1) {
                //    playerTwo = clientThread;
                //    playerOne.setEnemy(playerTwo);
                //    playerTwo.setEnemy(playerOne);
                //}
                //i++;
                //TODO add clientThread to clients and executor service

                //TODO(for later): handle the situation where more than two players join at the same time
                //TODO(for later): handle the situation where more than one game happens
            }
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