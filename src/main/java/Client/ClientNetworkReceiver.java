package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ClientNetworkReceiver implements Runnable{

    private Socket clientSocket;
    private Client client;
    private DataInputStream in;

    public ClientNetworkReceiver(Socket clientSocket, Client client) throws IOException {
        this.clientSocket = clientSocket;
        this.client = client;
        in = new DataInputStream(clientSocket.getInputStream());
    }
    @Override
    public void run() {
        while(true) {
            try {
                String response = in.readUTF();
                if(response.startsWith("set-player-number"))
                {
                    response = response.replace("set-player-number|", "");
                    int number = Integer.parseInt(response);
                    client.setPlayerNumber(number);
                }
                else if(response.startsWith("enemy-joined"))
                {
                    client.setOtherPlayerJoined();
                }
                else if(response.startsWith("incoming-attack")){
                    //"incoming-attack|row,col
                    response = response.replace("incoming-attack|" , "");
                    int row = Integer.parseInt(response.split(",")[0]);
                    int col = Integer.parseInt(response.split(",")[1]);
                    String message = client.getHit(row, col);
                    client.out.writeUTF("attack-result|" + message);
                    client.out.flush();
                }
                else if(response.startsWith("enemy-result"))
                {
                    //enemy-result|HIT,SHIPNAME
                    //enemy-result|HIT,NONE
                    //enemy-result|MISS,NONE
                    response = response.replace("enemy-result|", "");
                    client.setEnemyResult(Objects.equals(response.split(",")[0], "HIT"), response.split(",")[1]);
                }
                else if (response.startsWith("game-result")) {
                    //game-result|WON
                    //game-result|LOST
                    String message = response.replace("game-result|", "");
                    client.finishGame(message.equals("WON"));
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

