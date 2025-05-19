package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ClientNetworkReceiver implements Runnable{
    private Client client;
    private DataInputStream in;

    public ClientNetworkReceiver(DataInputStream clientSocket, Client client) throws IOException {
        this.client = client;
        this.in = clientSocket;
    }
    @Override
    public void run() {
        while(true) {

            String response;
            try {
                response = in.readUTF();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

                if(response.startsWith("set-player-number|"))
                {
                    int number = Integer.parseInt(response.replace("set-player-number|", ""));
                    client.setPlayerNumber(number);
                }
                else if(response.startsWith("enemy-joined"))
                {
                    client.setOtherPlayerJoined();
                }
                else if(response.startsWith("incoming-attack|")){
                    int row = 0;
                    int col = 0;
                    //incoming-attack|row,col
                    response = response.replace("incoming-attack|", "");
                    row = Integer.parseInt(response.split(",")[0]);
                    col = Integer.parseInt(response.split(",")[1]);
                    client.getHit(row, col);
                }
                else if(response.startsWith("enemy-result|"))
                {
                    response = response.replace("enemy-result|", "");
                    //enemy-result|HIT,SHIPNAME
                    //enemy-result|HIT,NONE
                    //enemy-result|MISS,NINE
                    client.setEnemyResult(Objects.equals(response.split(",")[0], "HIT"), response.split(",")[1]);
                }
                else if(response.equals("win"))
                {
                    client.win();
                }
                else if (response.equals("gameover"))
                {
                    return;
                }
            }
        }
    }
