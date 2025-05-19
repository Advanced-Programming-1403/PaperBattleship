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
    private boolean usernameSet;
    private String username;
    ClientHandler enemy;

    public void sendData(String request) {
        try {
            out.writeUTF(request);
            out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
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
        this.isFirst = isFirst;
        this.usernameSet = false;
        in = new DataInputStream(client.getInputStream());
        out = new DataOutputStream(client.getOutputStream());
    }

    public void run() {
        try {
            while(true) {
                String request = in.readUTF();
                if (request.startsWith("set-username")) {

                    int playerNumber = isFirst?0:1;
                    username = request.replace("set-username|", "");
                    out.writeUTF("set-player-number|" + playerNumber);
                    out.flush();
                    this.usernameSet = true;

                    while (enemy == null) {
                        Thread.onSpinWait();
                    }
                    while (!enemy.isUsernameSet()) {
                        Thread.onSpinWait();
                    }

                    out.writeUTF("enemy-joined");
                    out.flush();
                }
                else if (request.startsWith("attack") && !request.startsWith("attack-result")) {
                    int row = 0;
                    int col = 0;

                    //attack|row,col
                    request = request.replace("attack|", "");
                    row = Integer.parseInt(request.split(",")[0]);
                    col = Integer.parseInt(request.split(",")[1]);

                    enemy.out.writeUTF("incoming-attack|" + row + "," + col);
                }
                else if (request.startsWith("attack-result")) {

                    request = request.replace("attack-result|", "");
                    enemy.out.writeUTF("enemy-result|" + request);
                    enemy.out.flush();
                }
                else if (request.equals("enemy-won")) {
                    enemy.out.writeUTF("win");
                    enemy.out.flush();
                }
                else if (request.equals("gameover")) {
                    out.writeUTF("gameover"); //to terminate ClientNetworkReceiver
                    out.flush();
                    return;
                }
            }
        } catch (IOException e) {
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

    public boolean isUsernameSet() {
        return usernameSet;
    }
}