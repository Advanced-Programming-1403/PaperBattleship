package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientNetworkReceiver implements Runnable{
    private Client client;
    private DataInputStream in;
    private Socket clientSocket;

    public ClientNetworkReceiver(Socket clientSocket, Client client) throws IOException {
        // initialize client
        // initialize in
        this.clientSocket = clientSocket;
        this.client = client;
        in = new DataInputStream(clientSocket.getInputStream());
    }
    @Override
    public void run() {
        while(true) {
            //TODO: get response
            try {
                String response = in.readUTF();

                if (response.startsWith("set-player-number")) // TODO: set player number ( 0 or 1)
                {
                    int number = Integer.parseInt(response.replace("set-player-number|", ""));
                    client.setPlayerNumber(number);
                } else if (response.startsWith("enemy-joined")) // TODO: other player joined the game
                {
                    client.setOtherPlayerJoined();
                } else if (response.startsWith("incoming-attack")) { // TODO: handle incoming hit
                    int row = Integer.parseInt(response.split(",")[0]);
                    int col = Integer.parseInt(response.split(",")[1]);
                    String message = client.getHit(row, col);
                    //TODO: inform server
                    client.out.writeUTF("attack-result|" + message);
                    client.out.flush();
                } else if (response.startsWith("enemy-result")) //TODO: handle hit result from opponent
                {
                    response = response.replace("enemy-result|" , "");
                    String hit = response.split(",")[0];
                    String shipName = response.split(",")[1];
                    client.setEnemyResult(hit.equals("HIT"), shipName);
                }
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}