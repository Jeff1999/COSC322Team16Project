package ubc.cosc322;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the movement of queens and arrows in the Game of Amazons
 */
public class QueenActions {
    
    // Board dimensions
    public static final int BOARD_SIZE = 10;
    // Piece types in the game state
    public static final int EMPTY = 0;
    public static final int WHITE_QUEEN = 1;
    public static final int BLACK_QUEEN = 2;
    public static final int ARROW = 3;
    // Direction vectors for queen movement (8 directions)
    private static final int[][] DIRECTIONS = {
        {-1, 0},  // Up
        {1, 0},   // Down
        {0, -1},  // Left
        {0, 1},   // Right
        {-1, -1}, // Up-Left
        {-1, 1},  // Up-Right
        {1, -1},  // Down-Left
        {1, 1}    // Down-Right

    };
    
    /**
     * Gets all possible moves for a queen at the specified position
     * 
     * @param gameState Current game state
     * @param row Row (1-10)
     * @param col Column (1-10)
     * @return List of int[] with {row, col} of possible moves
     */
    public static List<int[]> getQueenMoves(ArrayList<Integer> gameState, int row, int col) {
        List<int[]> validMoves = new ArrayList<>();
        // Try each of the 8 directions
        for (int[] dir : DIRECTIONS) {
            int r = row;
            int c = col;
            
            // Move in current direction until hitting edge or piece
            while (true) {
                r += dir[0];
                c += dir[1];
                
                // Check if still on board
                if (r < 1 || r > 10 || c < 1 || c > 10) {
                    break;
                }
                
                // Check if position is empty
                int index = rcToIndex(r, c);
                if (index < gameState.size() && gameState.get(index) == EMPTY) {
                    validMoves.add(new int[] {r, c});
                } else {
                    break; // Hit a piece
                }
            }
        }
        
        return validMoves;
    }
    
    /**
     * Gets all valid arrow shots from a position
     * 
     * @param gameState Current game state
     * @param row Starting row (1-10)
     * @param col Starting column (1-10)
     * @return List of int[] with {row, col} of possible arrow shots
     */
    public static List<int[]> getArrowShots(ArrayList<Integer> gameState, int row, int col) {
        // Arrow movement follows the same rules as queen movement
        return getQueenMoves(gameState, row, col);
    }
    
    /**
     * Executes a move on the board
     * 
     * @param gameState Current game state
     * @param startRow Queen's starting row (1-10)
     * @param startCol Queen's starting column (1-10)
     * @param endRow Queen's ending row (1-10)
     * @param endCol Queen's ending column (1-10)
     * @param arrowRow Arrow's row (1-10)
     * @param arrowCol Arrow's column (1-10)
     * @return Updated game state
     */
    public static ArrayList<Integer> executeMove(ArrayList<Integer> gameState, 
                                                int startRow, int startCol,
                                                int endRow, int endCol,
                                                int arrowRow, int arrowCol) {
        ArrayList<Integer> newState = new ArrayList<>(gameState);
        
        // Get indices in the game state array
        int startIndex = rcToIndex(startRow, startCol);
        int endIndex = rcToIndex(endRow, endCol);
        int arrowIndex = rcToIndex(arrowRow, arrowCol);
        
        // Get the queen type
        int queenType = newState.get(startIndex);

        // Execute move
        boolean valid = isValidMove(gameState, startRow, startCol, endRow, endCol, arrowRow, arrowCol);
        if(valid){
        newState.set(startIndex, QueenActions.EMPTY);
        newState.set(endIndex, queenType);
        newState.set(arrowIndex, QueenActions.ARROW);
        }
        return newState;
    }
    
    /**
     * Checks if a move is valid
     * 
     * @param gameState Current game state
     * @param startRow Queen's starting row (1-10)
     * @param startCol Queen's starting column (1-10)
     * @param endRow Queen's ending row (1-10)
     * @param endCol Queen's ending column (1-10)
     * @param arrowRow Arrow's row (1-10)
     * @param arrowCol Arrow's column (1-10)
     * @return true if move is valid, false otherwise
     */
    public static boolean isValidMove(ArrayList<Integer> gameState,
                                     int startRow, int startCol,
                                     int endRow, int endCol,
                                     int arrowRow, int arrowCol) {
        // Check if queen start position is valid and contains a queen
        int startIndex = rcToIndex(startRow, startCol);
        if(startIndex < 0 || startIndex >= gameState.size() || (gameState.get(startIndex) != WHITE_QUEEN && gameState.get(startIndex) != BLACK_QUEEN)){
            //System.out.println("Invalid start position");
            return false;
        }
        // check the end position is valid and empty
        int endIndex = rcToIndex(endRow, endCol);
        if(endIndex < 0 || endIndex >= gameState.size() || gameState.get(endIndex) != EMPTY){
            //System.out.println("Invalid end position");
            return false;
        }
        // check arrow position is valid and empty
        int arrowIndex = rcToIndex(arrowRow, arrowCol);
        if(arrowIndex < 0 || arrowIndex >= gameState.size() || gameState.get(arrowIndex) != EMPTY){
            //System.out.println("Invalid arrow");
            return false;
        }
        // check the queen move is valid
        List<int[]> queenMove = getQueenMoves(gameState, arrowIndex, arrowCol);
        boolean validQueenMove = false;
        for(int[] move : queenMove){
            if(move[0] == endRow && move[0] == endCol){
                validQueenMove = true;
                break;
            }
        }
        if(!validQueenMove){
            //System.out.println("Invalid queen move"+ startRow + "," + startCol + "," + endRow + "," + endCol);
            return false;
        }
        // simulate queen move for arrow validation
        ArrayList<Integer> tempState = new ArrayList<>(gameState);
        int queenType = tempState.get(startIndex);
        tempState.set(startIndex, EMPTY);// remove the queen from start position
        tempState.set(endIndex, queenType);// place the queen to end position
        // check if the arrow shot is valid
        List<int[]> arrowMove = getArrowShots(tempState, endRow, endCol);
        boolean validArrowMove = false;
        for(int[] move : arrowMove){
            if(move[0] == arrowRow && move[1] == arrowCol){
                validArrowMove = true;
                break;
            }
        }
        if(!validArrowMove){
            //System.out.println("Invalid arrow shot");
            return false;
        }
        // check if the arrow position is not occupied by an arrow
        if(gameState.get(arrowIndex) == ARROW){
            //System.out.println("Invalid arrow position");
        }
        return true;

    }
    
    /**
     * Gets the positions of all queens of the specified color
     * 
     * @param gameState Current game state
     * @param isWhite true for white queens, false for black queens
     * @return List of int[] {row, col} for the queens
     */
    public static List<int[]> getQueenPositions(ArrayList<Integer> gameState, boolean isWhite) {
        List<int[]> positions = new ArrayList<>();
        int queenType = isWhite ? WHITE_QUEEN : BLACK_QUEEN;
        for (int i = 0; i < gameState.size(); i++) {
            if (i < gameState.size() && gameState.get(i) == queenType) {
                int[] pos = indexToRC(i);
                // Only include positions that are within the valid board range
                    positions.add(pos);
            
            }
        }
        
        return positions;
    }
    
    /**
     * Converts row and column to an index in the game state array
     * 
     * @param row Row (1-10)
     * @param col Column (1-10)
     * @return Index in game state array
     */
    public static int rcToIndex(int row, int col) {
        // Based on analyzing the game state, this is the mapping function

        return (row) * 11 + (col);
    }
    
    /**
     * Converts index in game state array to row and column
     * 
     * @param index Index in game state array
     * @return int[] containing {row, col} (1-10)
     */
    public static int[] indexToRC(int index) {
        int row = (index/ 11);
        int col = (index% 11);
        if (row >= 11 || col < 0) {
            return null;
        }
        return new int[] {row, col};
    }
    
    /**
     * Prints the current board state
     * 
     * @param gameState Current game state
     */
    public static void printBoard(ArrayList<Integer> gameState) {
        System.out.println("    1 2 3 4 5 6 7 8 9 10");
        System.out.println("   ---------------------");
        
        for (int row = 1; row <= 10; row++) {
            System.out.print(row < 10 ? row + "  |" : row + " |");
            
            for (int col = 1; col <= 10; col++) {
                int index = rcToIndex(row, col);
                char symbol = '.';
                
                if (index < gameState.size()) {
                    switch (gameState.get(index)) {
                        case EMPTY: symbol = '.'; break;
                        case WHITE_QUEEN: symbol = 'W'; break;
                        case BLACK_QUEEN: symbol = 'B'; break;
                        case ARROW: symbol = 'X'; break;
                        default: symbol = '?';
                    }
                }
                
                System.out.print(" " + symbol);
            }
            
            System.out.println(" |");
        }
        System.out.println("   ---------------------");
    }
    public static ArrayList<Integer> simulateMove(ArrayList<Integer> gameState, int startRow, int startCol, int endRow, int endCol, int arrowRow, int arrowCol){
        ArrayList<Integer> newstate = new ArrayList<>(gameState);
        int queenType = newstate.get(QueenActions.rcToIndex(startRow, startCol));
        // remove the queen from start position
        newstate.set(QueenActions.rcToIndex(startRow, startCol), QueenActions.EMPTY);
        // set the queen to end position
        newstate.set(QueenActions.rcToIndex(endRow, endCol), queenType);
        // shoot arrow
        newstate.set(QueenActions.rcToIndex(arrowRow, arrowCol), QueenActions.ARROW);
        
        return newstate;
    }
}