package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isFirst;
    private String username;
    ClientHandler enemy;

    public void sendData(String request) throws IOException {
        out.writeUTF(request);
        out.flush();
    }

    public void setEnemy(ClientHandler enemy) throws IOException {
        this.enemy = enemy;
    }

    public String getUsername()
    {
        return username;
    }

    public ClientHandler(Socket client, boolean isFirst) throws IOException {
        this.client = client;
        in = new DataInputStream(client.getInputStream());
        out = new DataOutputStream(client.getOutputStream());
        this.isFirst = isFirst;
    }

    public void run() {
        try {
            while(true) {
                String request = in.readUTF();
                if (request.startsWith("set-username")) {
                    //set-username|Rey
                    int playerNumber = isFirst?0:1;
                    username = request.replace("set-username|", "");
                    out.writeUTF("set-player-number|" + playerNumber);
                    out.flush();
                    while (enemy == null) {
                        Thread.sleep(10);
                    }
                    out.writeUTF("enemy-joined");
                    out.flush();
                }
                else if (request.startsWith("attack") && !request.startsWith("attack-result")) {
                    //attack|row,col
                    request = request.replace("attack|", "");

                    int row = Integer.parseInt(request.split(",")[0]);
                    int col = Integer.parseInt(request.split(",")[1]);

                    enemy.out.writeUTF("incoming-attack|" + row + "," + col);
                    enemy.out.flush();
                }
                else if (request.startsWith("attack-result")) {
                    //attack-result|HIT,SHIPNAME (if ship sank)
                    //attack-result|HIT,NONE (if ship didn't sink)
                    //attack-result|MISS
                    String message = request.replace("attack-result|", "");
                    enemy.out.writeUTF("enemy-result|" + message);
                    enemy.out.flush();
                }
                else if (request.startsWith("game-result")) {
                    //game-result|LOST
                    //game-result|WIN
                    String message = request.replace("game-result|", "");
                    if (message.equals("LOST")) {
                        enemy.out.writeUTF("game-result|WIN");
                    }
                    else {
                        enemy.out.writeUTF("game-result|LOST");
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally
        {
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}