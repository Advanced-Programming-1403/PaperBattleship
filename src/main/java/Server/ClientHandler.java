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

    public void sendData(String request) {
        //TODO: write to output and flush
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
                // TODO: Read a UTF-encoded string from the client
                String request = in.readUTF();
                if (request.startsWith("Set-username")) { //TODO: Set Username
                    int playerNumber = isFirst?0:1;
                    // TODO: Send player number to client

                    username = request.replace("Set-username|" , "");//TODO extract username
                    //TODO: wait till enemy is specified
                    //TODO: tell the clients that the other player joined
                    out.writeUTF("set-player-number|" + playerNumber);
                    out.flush();
                    while (enemy == null) {
                        Thread.sleep(10);
                    }
                    out.writeUTF("enemy-joined");
                    out.flush();
                }
                else if (request.startsWith("attack") && !request.startsWith("attack-result")) { //TODO: Handle Attack
                    request =  request.replace("attack|" , "");
                    int row = Integer.parseInt(request.split(",")[0]);
                    int col = Integer.parseInt(request.split(",")[1]);//TODO: extract row and col
                    enemy.out.writeUTF("incoming-attack|" + row + "," + col);
                    enemy.out.flush();
                    //TODO: inform enemy of the attack
                }
                else if (request.startsWith("attack-result")) { //TODO: Handle Attack Result

                    String message = request.replace("attack-result" , "");

                    enemy.out.writeUTF("enemy-result|" + message);
                    enemy.out.flush();
                    // TODO: Notify the attacker about the result of their attack (hit or miss)
                }
                //TODO(for later): handle winning and losing
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally
        {
            // close in
            // close out
            // close client
        }
    }
}