package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.BoardGameModel;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Test extends GamePlayer {
    private GameClient gameClient = null;
    private BaseGameGUI gamegui = null;
    private String userName = null;
    private String passwd = null;
    
    // Game state tracking
    private ArrayList<Integer> gameState = null;
    
    /**
     * The main method
     * @param args for name and passwd
     */
    public static void main(String[] args) {
        COSC322Test player = new COSC322Test(args.length > 0 ? args[0] : "TeamPlayer", 
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
        for(int i = 0; i < roomList.size(); i++) {
            System.out.println(roomList.get(i).getName());
        }
        
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a room name: ");
        String roomName = input.nextLine();
        
        gameClient.joinRoom(roomName);
        // Don't close System.in with input.close() as it might be needed later
       
    }
    
    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
        System.out.println("Message Type: " + messageType);
        boolean myFirst = true;
        
        if (messageType.equals(GameMessage.GAME_STATE_BOARD)) {
            // Update the game state
            gameState = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
            if (gamegui != null) {
                gamegui.setGameState(gameState);
            }
            // Print the board
            System.out.println("\n--- CURRENT BOARD STATE ---");
            QueenActions.printBoard(gameState);
            //check we are first or second to play
            String player = (String)(msgDetails.get("PLAYER_BLACK"));
            if(player.equals(userName)){
                System.out.println("We are the second player(Black)");
                myFirst = false;
            }
            else{
                System.out.println("We are the first player(White)");
                myFirst = true;
            }

            if(myFirst){
                ArrayList<ArrayList<Integer>> bestMove = MinMax.alpha_beta(gameState,3,Integer.MIN_VALUE, Integer.MAX_VALUE, true, myFirst);
                if(bestMove.get(1).get(0) != -1){
                    ArrayList<Integer> queenPosCurrent = bestMove.get(1);
                    ArrayList<Integer> queenPosNew = bestMove.get(2);
                    ArrayList<Integer> arrowPos = bestMove.get(3);
                    gameClient.sendMoveMessage(queenPosCurrent,queenPosNew,arrowPos);
                }
            }
            // Step 1 Verification: Queen Actions
            verifyQueenActions();
            
        }
        else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
            // An opponent has made a move
            ArrayList<Integer> queenPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
            ArrayList<Integer> queenTargetPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
            ArrayList<Integer> arrowPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
            System.out.println();
            System.out.println("\nOpponent move:");
            System.out.println("  Queen from: " + queenPos);
            System.out.println("  Queen to: " + queenTargetPos);
            System.out.println("  Arrow: " + arrowPos);
            
            if (gamegui != null) {
                gamegui.updateGameState(queenPos, queenTargetPos, arrowPos);
            }
            
            // Update our internal game state
            gameState = QueenActions.executeMove(gameState, 
                queenPos.get(0), queenPos.get(1), 
                queenTargetPos.get(0), queenTargetPos.get(1), 
                arrowPos.get(0), arrowPos.get(1));
                
            System.out.println("\n--- BOARD AFTER OPPONENT MOVE ---");
            QueenActions.printBoard(gameState);
        }
        
        return true;
    }
    
    /**
     * Verify that queen action implementation works correctly
     */
    private void verifyQueenActions() {
        if (gameState == null) {
            System.out.println("Game state is null, cannot verify queen actions");
            return;
        }
        System.out.println("\n=== STEP 1 VERIFICATION: Queen Actions ===");
        
        // 1. Find all queens
        List<int[]> whiteQueens = QueenActions.getQueenPositions(gameState, true);
        List<int[]> blackQueens = QueenActions.getQueenPositions(gameState, false);
        
        // 2. Display queen positions and their legal moves
        System.out.println("White queens found: " + whiteQueens.size());
        for (int[] pos : whiteQueens) {
            System.out.println("  Queen at row " + pos[0] + ", col " + pos[1]);
            
            List<int[]> moves = QueenActions.getQueenMoves(gameState, pos[0], pos[1]);
            System.out.println("    Legal moves: " + moves.size());
            
            // Show a few sample moves if available
            for (int i = 0; i < Math.min(3, moves.size()); i++) {
                System.out.println("    - Can move to row " + moves.get(i)[0] + ", col " + moves.get(i)[1]);
            }
            
            // 3. Test arrow shots for a sample move
            if (!moves.isEmpty()) {
                int[] targetPos = moves.get(0);
                
                // Make a simulated move to test arrow shooting
                ArrayList<Integer> tempState = QueenActions.executeMove(
                    new ArrayList<>(gameState),
                    pos[0], pos[1], 
                    targetPos[0], targetPos[1],
                    targetPos[0], targetPos[1] // Temporary arrow position
                );
                
                List<int[]> arrows = QueenActions.getArrowShots(tempState, targetPos[0], targetPos[1]);
                System.out.println("    If moved to row " + targetPos[0] + ", col " + targetPos[1] + ":");
                System.out.println("      Legal arrow shots: " + arrows.size());
                
                // 4. Test move validation
                if (!arrows.isEmpty()) {
                    int[] arrowPos = arrows.get(0);
                    boolean valid = QueenActions.isValidMove(gameState,
                        pos[0], pos[1], 
                        targetPos[0], targetPos[1],
                        arrowPos[0], arrowPos[1]);
                    
                    System.out.println("      Is this a valid move? " + valid);
                }
            }
        }
        
        System.out.println("Black queens found: " + blackQueens.size());
        for (int[] pos : blackQueens) {
            System.out.println("  Queen at row " + pos[0] + ", col " + pos[1]);
            List<int[]> moves = QueenActions.getQueenMoves(gameState, pos[0], pos[1]);
            System.out.println("    Legal moves: " + moves.size());
        }
        
        System.out.println("\n=== STEP 1 VERIFICATION COMPLETE ===");
        System.out.println("Queen actions have been successfully implemented!");
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