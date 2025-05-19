package Client;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import Client.utils.AnsiColor;

public class Client {
    private static final String SPACING = "     ";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT_NUMBER = 3737;
    static final int SIZE = 10;
    static final Random rand = new Random();

    private final Socket clientSocket;
    BattleShipCell[][] playerBoard;
    BattleShipCell[][] enemyBoard;

    static int lastEnemyRow;
    static int lastEnemyCol;


    volatile boolean isBoardSet = false;
    boolean isTurn = false;
    volatile boolean otherPlayerJoined = false;
    DataOutputStream out;

    boolean won = false;
    boolean lost = false;

    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;

        try {
            this.out = new DataOutputStream(clientSocket.getOutputStream());
            new Thread(new ClientNetworkReceiver(new DataInputStream(clientSocket.getInputStream()), this)).start();

            System.out.println("Enter Username: ");
            Scanner usernameScanner = new Scanner(System.in);

            String username = usernameScanner.next();
            sendUsername(username);
            initializeBoards();

            System.out.println("Waiting for the other player to join");
            while(!otherPlayerJoined)
            {
                Thread.onSpinWait();
            }

            printBoards();
            mainLoop : while(!won && !lost)
            {
                while(!isTurn)
                {
                    Thread.onSpinWait();
                    if (won || lost) {
                        break mainLoop;
                    }
                }

                System.out.println("Your Turn Now!");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                Scanner scanner = new Scanner(System.in);

                lastEnemyRow = -1;
                lastEnemyCol = -1;

                while (lastEnemyRow < 0 || lastEnemyRow >= 10 || lastEnemyCol < 0 || lastEnemyCol >= 10) {
                    System.out.println("Type Row and Column (e.g., A1):");

                    String input = scanner.nextLine().trim().toUpperCase();

                    if (input.matches("^[A-J]([1-9]|10)$")) {
                        lastEnemyCol = input.charAt(0) - 'A';
                        lastEnemyRow = Integer.parseInt(input.substring(1)) - 1;

                        if (lastEnemyRow >= 0 && lastEnemyRow < 10 && lastEnemyCol >= 0 && lastEnemyCol < 10) {
                            System.out.println("You selected Row: " + (lastEnemyRow + 1) + ", Column: " + (char)('A' + lastEnemyCol));
                        } else {
                            System.out.println("Invalid input! Row must be between 1 and 10, and Column must be between A and J.");
                            lastEnemyRow = -1;
                            lastEnemyCol = -1;
                        }
                    } else {
                        System.out.println("Invalid format! Please enter a valid input like A1, B10, J5.");
                    }
                }

                System.out.println("Move is being sent. Waiting for processing");
                attack();
            }
        } catch (IOException e) {
            System.out.println("Could not connect to the server!\n\n");
        }
    }

    //Actions
    private void attack() {
        String message = "attack|" + lastEnemyRow + "," + lastEnemyCol;
        try {
            out.writeUTF(message);
        } catch (Exception e) {
            System.out.println("Could not send message to the server!\n\n");
        }

        isTurn = false;
    }
    private void sendUsername(String username) throws IOException {
        String message = "set-username|" + username;
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Could not send message to the server!\n\n");
        }
    }

    public void setEnemyResult(boolean hit, String shipName) {
        enemyBoard[lastEnemyRow][lastEnemyCol].setEnemyShip(hit);
        enemyBoard[lastEnemyRow][lastEnemyCol].setMarked(true);
        printBoards();
        playSound((hit?"hit_sound.wav":"miss_sound.wav"));
        System.out.println((hit?"You hit the target!":"You missed!"));
    }
    public void setPlayerNumber(int i) {
        if(i == 0) isTurn = true;
    }
    public void setOtherPlayerJoined() {
        otherPlayerJoined = true;
    }
    public void finishGame(boolean won){
    }
    public void getHit(int row, int col){
        String message = "";
        playerBoard[row][col].setMarked(true);
        String sankBattleShipName = "NONE";
        if(playerBoard[row][col].isShip() && playerBoard[row][col].getBattleShip().isAllHit()){
            sankBattleShipName = playerBoard[row][col].getBattleShip().getType();
        }
        message = playerBoard[row][col].isShip() ? ("HIT," + sankBattleShipName) : "MISS,NONE";
        printBoards();
        boolean hit = playerBoard[row][col].isShip();
        System.out.println((hit?"They hit a target!":"They missed!"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        try {
            out.writeUTF("attack-result|" + message);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // checking game status
        boolean lost = true;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (playerBoard[i][j].isShip() && !playerBoard[i][j].isMarked()) {
                    lost = false;
                }
            }
        }

        if (lost) {
            try {
                out.writeUTF("enemy-won");
                out.flush();
                lose();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            isTurn = true;
        }
    }
    private static void playSound(String soundFileName) {
        try {
            // Load the sound file from the resources folder
            URL soundFileURL = Client.class.getClassLoader().getResource( soundFileName);

            if (soundFileURL == null) {
                System.out.println("Sound file not found: " + soundFileName);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFileURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    //Board
    public void initializeBoards() {
        playerBoard = createBoard();
        enemyBoard = new BattleShipCell[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++){
                enemyBoard[i][j] = new BattleShipCell();
            }
        }
        isBoardSet = true;
    }
    public void printBoards() {
        clearScreen();

        System.out.printf("    %-40s%s%-40s%n", "PLAYER BOARD", SPACING ,"    ENEMY BOARD");

        printColumnHeaders();
        System.out.print(SPACING);
        printColumnHeaders();
        System.out.println();

        for (int row = 0; row < SIZE; row++) {
            printRowSeparator();
            System.out.print(SPACING);
            printRowSeparator();
            System.out.println();

            printRowContent(playerBoard[row], row);
            System.out.print(SPACING);
            printRowContent(enemyBoard[row], row);
            System.out.println();
        }

        printRowSeparator();
        System.out.print(SPACING);
        printRowSeparator();
        System.out.println();
    }
    private void clearScreen() {
        for (int i = 0; i < 20; i++) {
            System.out.println();
        }
    }
    private void printColumnHeaders() {
        System.out.print("    ");
        for (char c : "ABCDEFGHIJ".toCharArray()) {
            System.out.print(AnsiColor.CYAN + " " + c + "  " + AnsiColor.RESET);
        }
    }
    private void printRowSeparator() {
        System.out.print(AnsiColor.BORDER + "   +" + AnsiColor.RESET);
        for (int i = 0; i < SIZE; i++) {
            System.out.print(AnsiColor.BORDER + "---+" + AnsiColor.RESET);
        }
    }
    private void printRowContent(BattleShipCell[] rowData, int rowNum) {
        System.out.printf(AnsiColor.BORDER +"%2d |" + AnsiColor.RESET, rowNum + 1);
        for (BattleShipCell cell : rowData) {
            System.out.printf( " %s " , cell);
            System.out.print(AnsiColor.BORDER +"|" + AnsiColor.RESET);
        }
    }
    public BattleShipCell[][] createBoard() {
        BattleShipCell[][] board = new BattleShipCell[SIZE][SIZE];

        // Initialize grids with water
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = new BattleShipCell(); // water
            }
        }

        for (Map.Entry<String, Integer> entry : BattleShip.ships.entrySet()) {
            BattleShip battleShip = new BattleShip(entry.getKey());
            placeShip(board, battleShip);
        }
        return board;
    }
    public static void placeShip(BattleShipCell[][] board, BattleShip battleShip) {
        boolean placed = false;

        while(!placed)
        {
            boolean horizontal = rand.nextBoolean();
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);
            placed = tryPlace(board, row, col, horizontal, battleShip);
        }

    }
    private static boolean tryPlace(BattleShipCell[][] board, int row, int col, boolean horizontal, BattleShip battleShip) {

        int length = battleShip.getLength();
        if(horizontal && col + length <= SIZE)
        {
            for(int i = 0; i < length; i++)
            {
                if(board[row][col + i].isShip())
                {
                    return false;
                }
            }
            for(int i = 0; i < length; i++)
            {
                board[row][col + i].setBattleShip(battleShip);
            }
            return true;
        }

        if(!horizontal && row + length  <= SIZE)
        {
            for(int i = 0; i < length; i++)
            {
                if(board[row + i][col].isShip())
                {
                    return false;
                }
            }
            for(int i = 0; i < length; i++)
            {
                board[row + i][col].setBattleShip(battleShip);
                battleShip.addShipCell(board[row + i][col]);
            }
            return true;
        }

        return false;
    }

    public void win() {
        try {
            out.writeUTF("gameover"); //to terminate ClientHandler and ClientNetworkReceiver
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 20; i++) {
            System.out.println();
        }
        System.out.println(AnsiColor.CYAN + "Congratulations! You won the game! wait for the next round..." + AnsiColor.RESET + "\n\n");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        won = true;
    }
    public void lose() {
        for (int i = 0; i < 20; i++) {
            System.out.println();
        }
        System.out.println(AnsiColor.RED + "What a shame! You lost! wait fot the next round..." + AnsiColor.RESET + "\n\n");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        lost = true;
    }

    public static void main(String[] args) throws IOException {
        Socket clientSocket = null;

        try {
            while (true) {
                clientSocket = new Socket("localhost", PORT_NUMBER);
                new Client(clientSocket);
            }
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

}
