package ubc.cosc322;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/**
 * Basic implementation of Game of Amazons that only focuses on turn-taking mechanics
 */
public class COSC322Test extends GamePlayer {
    private GameClient gameClient = null;
    private BaseGameGUI gamegui = null;
    private String userName = null;
    private String passwd = null;
    
    // Game state tracking
    private ArrayList<Integer> gameState = null;
    private boolean isBlackPlayer = false; // Am I playing as black?
    private boolean myTurn = false;        // Is it currently my turn?
    
    /**
     * The main method
     * @param args for name and passwd
     */
    public static void main(String[] args) {
        COSC322Test player = new COSC322Test(args.length > 0 ? args[0] : "TeamPlayer1", 
                                           args.length > 1 ? args[1] : "password");
        
        if(player.getGameGUI() == null) {
            player.Go();
        }
        else {
            BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    player.Go();
                }
            });
        }
    }
    
    /**
     * Constructor
     * @param userName
     * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
        this.userName = userName;
        this.passwd = passwd;
        this.gamegui = new BaseGameGUI(this);
    }
    
    @Override
    public void onLogin() {
        System.out.println("Logged in successfully as: " + gameClient.getUserName());
        
        if(gamegui != null) {
            gamegui.setRoomInformation(gameClient.getRoomList());
        }
        
        List<Room> roomList = gameClient.getRoomList();
        System.out.println("Available rooms:");
        for(int i = 0; i < roomList.size(); i++) {
            System.out.println("  " + roomList.get(i).getName());
        }
        
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a room name: ");
        String roomName = input.nextLine();
        
        gameClient.joinRoom(roomName);
        System.out.println("Joining room: " + roomName);
    }
    
    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
        System.out.println("Received message: " + messageType);
        
        if (messageType.equals(GameMessage.GAME_STATE_BOARD)) {
            // Update the game state when we receive the board
            gameState = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
            if (gamegui != null) {
                gamegui.setGameState(gameState);
            }
            printBoard();
        }
        else if (messageType.equals(GameMessage.GAME_ACTION_START)) {
            // Determine if we're the black player (first player)
            String blackPlayer = (String)msgDetails.get(AmazonsGameMessage.PLAYER_BLACK);
            isBlackPlayer = blackPlayer.equals(userName);
            
            if (isBlackPlayer) {
                System.out.println("I am the first player (Black)");
                myTurn = true;  // Black goes first
                //makeRandomMove();
                makeMinimaxMove();
            } else {
                System.out.println("I am the second player (White)");
                myTurn = false; // Wait for Black's move
            }
        }
        else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
            // The opponent has made a move
            ArrayList<Integer> queenPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
            ArrayList<Integer> queenTargetPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
            ArrayList<Integer> arrowPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
            
            System.out.println("Opponent moved:");
            System.out.println("  Queen from: " + queenPos);
            System.out.println("  Queen to: " + queenTargetPos);
            System.out.println("  Arrow: " + arrowPos);
            
            // Update the board with the opponent's move
            updateGameState(queenPos, queenTargetPos, arrowPos);
            
            // Check if we have any valid moves
            MinimaxAI minimaxAI = new MinimaxAI(isBlackPlayer);
            List<MinimaxAI.Move> myPossibleMoves = minimaxAI.generateMoves(gameState, true);
    
            if (myPossibleMoves.isEmpty()) {
                // We have no valid moves - game is over and we lost
                System.out.println("NO VALID MOVES AVAILABLE - GAME OVER");
                System.out.println("Player " + (isBlackPlayer ? "BLACK" : "WHITE") + " has lost the game.");
                System.out.println("You lose! Resigning from the game...");
                
                // Add a delay before exiting
                try {
                    System.out.println("Game will close in 10 seconds...");
                    Thread.sleep(10000); // 10 second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                gameClient.logout(); // Disconnect from the server
                System.exit(0);     // Exit the program cleanly
            } else {
                // Now it's our turn
                myTurn = true;
                makeMinimaxMove();
            }
        }
        return true;
    }

    private void makeMinimaxMove() {
        MinimaxAI minimaxAI = new MinimaxAI(isBlackPlayer);
        if (!myTurn || gameState == null) {
            return;
        }
    
        System.out.println("Making my move using Minimax...");
        
        // First check if there are any moves available at all
        List<MinimaxAI.Move> availableMoves = minimaxAI.generateMoves(gameState, true);
        System.out.println("Available moves count: " + availableMoves.size());
        
        if (availableMoves.isEmpty()) {
            System.out.println("NO VALID MOVES AVAILABLE - GAME OVER");
            System.out.println("Player " + (isBlackPlayer ? "BLACK" : "WHITE") + " has lost the game.");
            // Resign and exit cleanly
            System.out.println("Resigning from the game");
            gameClient.logout();
            System.exit(0);
            return;
        }
    
        // Get the best move using Minimax
        MinimaxAI.Move bestMove = minimaxAI.findBestMoveWithDynamicDepth(gameState);
    
        if (bestMove != null) {
            ArrayList<Integer> queenPos = new ArrayList<>();
            queenPos.add(bestMove.queenPos[0]);
            queenPos.add(bestMove.queenPos[1]);
    
            ArrayList<Integer> queenTargetPos = new ArrayList<>();
            queenTargetPos.add(bestMove.queenTargetPos[0]);
            queenTargetPos.add(bestMove.queenTargetPos[1]);
    
            ArrayList<Integer> arrowPos = new ArrayList<>();
            arrowPos.add(bestMove.arrowPos[0]);
            arrowPos.add(bestMove.arrowPos[1]);
    
            // Send the move
            System.out.println("My move:");
            System.out.println("  Queen from: " + queenPos);
            System.out.println("  Queen to: " + queenTargetPos);
            System.out.println("  Arrow: " + arrowPos);
    
            // Update our local game state
            updateGameState(queenPos, queenTargetPos, arrowPos);
    
            // Send the move to the server
            gameClient.sendMoveMessage(queenPos, queenTargetPos, arrowPos);
            
            // Check if opponent has any valid moves
            List<MinimaxAI.Move> opponentPossibleMoves = minimaxAI.generateMoves(gameState, false);
            
            if (opponentPossibleMoves.isEmpty()) {
                System.out.println("OPPONENT HAS NO VALID MOVES - GAME OVER");
                System.out.println("Player " + (isBlackPlayer ? "WHITE" : "BLACK") + " has lost the game.");
                // Game is over and we won - we can continue normally as the server will handle this
            }
    
            // Now it's the opponent's turn
            myTurn = false;
        } else {
            // This shouldn't happen since we already checked for available moves,
            // but we'll handle it as a safety measure
            // As a fallback, use the first available move instead of giving up

            MinimaxAI.Move fallbackMove = availableMoves.get(0);
            
            ArrayList<Integer> queenPos = new ArrayList<>();
            queenPos.add(fallbackMove.queenPos[0]);
            queenPos.add(fallbackMove.queenPos[1]);
    
            ArrayList<Integer> queenTargetPos = new ArrayList<>();
            queenTargetPos.add(fallbackMove.queenTargetPos[0]);
            queenTargetPos.add(fallbackMove.queenTargetPos[1]);
    
            ArrayList<Integer> arrowPos = new ArrayList<>();
            arrowPos.add(fallbackMove.arrowPos[0]);
            arrowPos.add(fallbackMove.arrowPos[1]);
    
            System.out.println("Using fallback move:");
            System.out.println("  Queen from: " + queenPos);
            System.out.println("  Queen to: " + queenTargetPos);
            System.out.println("  Arrow: " + arrowPos);
    
            // Update our local game state
            updateGameState(queenPos, queenTargetPos, arrowPos);
    
            // Send the move to the server
            gameClient.sendMoveMessage(queenPos, queenTargetPos, arrowPos);
            
            // Now it's the opponent's turn
            myTurn = false;
        }
    }

    /**
     * Update the game state with a move
     */
    private void updateGameState(ArrayList<Integer> queenPos, ArrayList<Integer> queenTargetPos, ArrayList<Integer> arrowPos) {
        if (gameState == null) return;
        
        int startIdx = rcToIndex(queenPos.get(0), queenPos.get(1));
        int endIdx = rcToIndex(queenTargetPos.get(0), queenTargetPos.get(1));
        int arrowIdx = rcToIndex(arrowPos.get(0), arrowPos.get(1));
        
        int queenType = gameState.get(startIdx);
        
        // Execute the move
        gameState.set(startIdx, 0); // Empty the starting position
        gameState.set(endIdx, queenType); // Move the queen
        gameState.set(arrowIdx, 3); // Place the arrow
        
        // Update the GUI
        if (gamegui != null) {
            gamegui.updateGameState(queenPos, queenTargetPos, arrowPos);
        }
        
        printBoard();
    }

    /**
        * Print the current board state
    */
    private void printBoard() {
        if (gameState == null) return;
    
        System.out.println("\n--- CURRENT BOARD STATE ---");
        System.out.println("    1 2 3 4 5 6 7 8 9 10");
        System.out.println("   ---------------------");
    
        for (int row = 10; row >= 1; row--) {  // Changed to count DOWN from 10 to 1
            System.out.print(row < 10 ? row + "  |" : row + " |");
        
            for (int col = 1; col <= 10; col++) {
                int index = rcToIndex(row, col);
                char symbol = '.';
            
                if (index < gameState.size()) {
                    switch (gameState.get(index)) {
                        case 0: symbol = '.'; break; 
                        case 1: symbol = 'B'; break;
                        case 2: symbol = 'W'; break;
                        case 3: symbol = 'X'; break;
                        default: symbol = '?';
                    }
                }

                System.out.print(" " + symbol);
                }
        
            System.out.println(" |");
            }

        System.out.println("   ---------------------");
    }
    
    /**
     * Get positions of our queens
     */
    public List<int[]> getMyQueens() {
        List<int[]> queens = new ArrayList<>();
        int queenType = isBlackPlayer ? 2 : 1; // BLACK_QUEEN or WHITE_QUEEN
        
        for (int i = 0; i < gameState.size(); i++) {
            if (gameState.get(i) == queenType) {
                int[] pos = indexToRC(i);
                if (pos != null) {
                    queens.add(pos);
                }
            }
        }
        
        return queens;
    }
    
    /**
     * Get valid moves for a queen
     */
    public List<int[]> getQueenMoves(int row, int col) {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, 
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        
        for (int[] dir : directions) {
            int r = row;
            int c = col;
            
            while (true) {
                r += dir[0];
                c += dir[1];
                
                // Check if still on board
                if (r < 1 || r > 10 || c < 1 || c > 10) {
                    break;
                }
                
                // Check if position is empty
                int index = rcToIndex(r, c);
                if (index < gameState.size() && gameState.get(index) == 0) {
                    moves.add(new int[] {r, c});
                } else {
                    break; // Hit a piece
                }
            }
        }
        
        return moves;
    }
    
    /**
     * Convert row and column to game state index
     */
    private int rcToIndex(int row, int col) {
        return (row) * 11 + (col);
    }
    
    /**
     * Convert game state index to row and column
     */
    private int[] indexToRC(int index) {
        int row = (index / 11);
        int col = (index % 11);
        if (row >= 11 || col < 0) {
            return null;
        }
        return new int[] {row, col};
    }
    
    @Override
    public String userName() {
        return userName;
    }
    
    @Override
    public GameClient getGameClient() {
        return this.gameClient;
    }
    
    @Override
    public BaseGameGUI getGameGUI() {
        return gamegui;
    }
    
    @Override
    public void connect() {
        gameClient = new GameClient(userName, passwd, this);
    }

}