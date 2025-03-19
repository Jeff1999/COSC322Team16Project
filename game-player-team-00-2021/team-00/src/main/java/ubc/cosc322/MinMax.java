package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinMax {
   
    public static int f_function(ArrayList<Integer> gamestate, boolean isWhite) {
        // Get all white and black queen positions
        List<int[]> whiteQueens = QueenActions.getQueenPositions(gamestate, true);
        List<int[]> blackQueens = QueenActions.getQueenPositions(gamestate, false);
        
        // Calculate mobility (available moves)
        int whiteMoves = 0;
        int blackMoves = 0;
        
        // Store all possible move positions for territory analysis
        List<int[]> whiteMovePositions = new ArrayList<>();
        List<int[]> blackMovePositions = new ArrayList<>();
        
        // Calculate white mobility and collect move positions
        for (int[] queen : whiteQueens) {
            List<int[]> moves = QueenActions.getQueenMoves(gamestate, queen[0], queen[1]);
            whiteMoves += moves.size();
            whiteMovePositions.addAll(moves);
        }
        
        // Calculate black mobility and collect move positions
        for (int[] queen : blackQueens) {
            List<int[]> moves = QueenActions.getQueenMoves(gamestate, queen[0], queen[1]);
            blackMoves += moves.size();
            blackMovePositions.addAll(moves);
        }
        
        // Calculate territory control (unique squares each player can reach)
        int whiteTerritory = countUniquePositions(whiteMovePositions);
        int blackTerritory = countUniquePositions(blackMovePositions);
        
        // Calculate queen proximity (tactical advantage when queens are close to enemy)
        int queenProximity = calculateQueenProximity(whiteQueens, blackQueens);
        
        // Calculate board center control
        int whiteCenterControl = calculateCenterControl(whiteMovePositions);
        int blackCenterControl = calculateCenterControl(blackMovePositions);
        
        // Weights for different evaluation components
        double mobilityWeight = 1.0;
        double territoryWeight = 1.5;
        double proximityWeight = 0.5;
        double centerControlWeight = 0.8;
        
        // Combine all factors for final evaluation
        double whiteScore = (mobilityWeight * whiteMoves) + 
                            (territoryWeight * whiteTerritory) + 
                            (proximityWeight * queenProximity) + 
                            (centerControlWeight * whiteCenterControl);
                            
        double blackScore = (mobilityWeight * blackMoves) + 
                            (territoryWeight * blackTerritory) - 
                            (proximityWeight * queenProximity) + 
                            (centerControlWeight * blackCenterControl);
        
        // Return evaluation score based on player color
        if (!isWhite) {
            return (int)(whiteScore - blackScore);
        } else {
            return (int)(blackScore - whiteScore);
        }
    }
    
    // Count unique board positions from a list of positions
    private static int countUniquePositions(List<int[]> positions) {
        // Use a Set to track unique positions
        java.util.Set<String> uniquePositions = new java.util.HashSet<>();
        
        for (int[] pos : positions) {
            uniquePositions.add(pos[0] + "," + pos[1]);
        }
        
        return uniquePositions.size();
    }
    
    // Calculate advantage based on queen proximity
    private static int calculateQueenProximity(List<int[]> whiteQueens, List<int[]> blackQueens) {
        int proximityScore = 0;
        
        for (int[] whiteQueen : whiteQueens) {
            for (int[] blackQueen : blackQueens) {
                // Manhattan distance between queens
                int distance = Math.abs(whiteQueen[0] - blackQueen[0]) + Math.abs(whiteQueen[1] - blackQueen[1]);
                
                // Closer queens are more threatening - add to white's advantage
                // The max distance on a 10x10 board is 18 (Manhattan), so we subtract from that
                proximityScore += (18 - distance);
            }
        }
        
        return proximityScore;
    }
    
    // Calculate control of the central area of the board
    private static int calculateCenterControl(List<int[]> positions) {
        int centerScore = 0;
        
        for (int[] pos : positions) {
            // Calculate distance from center (assuming 10x10 board)
            int rowDistFromCenter = Math.abs(pos[0] - 5);
            int colDistFromCenter = Math.abs(pos[1] - 5);
            
            // Higher score for positions closer to center
            centerScore += (10 - (rowDistFromCenter + colDistFromCenter));
        }
        
        return centerScore;
    }

    public static ArrayList<ArrayList<Integer>> alpha_beta (ArrayList<Integer> gamestate, int depth, int alpha, int beta,  boolean isMaxingPlayer, boolean isWhite){
        
        if(depth == 0 || isGameOver(gamestate)){
            ArrayList<ArrayList<Integer>> result = new ArrayList<>();
            result.add(new ArrayList<>(Arrays.asList(f_function(gamestate, isWhite))));
            result.add(new ArrayList<>(Arrays.asList(-1,-1)));// queen current position
            result.add(new ArrayList<>(Arrays.asList(-1,-1)));// queen new position
            result.add(new ArrayList<>(Arrays.asList(-1,-1)));// arrow position

            return result;
        }
        // get queens position base on which color we play
        List<int[]> queens = isMaxingPlayer ? QueenActions.getQueenPositions(gamestate, isWhite) : QueenActions.getQueenPositions(gamestate, !isWhite);
        
        // check which color we play and choose max or min base on which color we play
        if(isMaxingPlayer){
            int bestValue = Integer.MIN_VALUE;
            // move include start row, start col, end row, end col, arrow row, arrow col
            ArrayList<Integer> bestQueenPosCurrent = new ArrayList<>(Arrays.asList(-1,-1));
            ArrayList<Integer> bestQueenPosNew = new ArrayList<>(Arrays.asList(-1,-1));
            ArrayList<Integer> bestArrowPos = new ArrayList<>(Arrays.asList(-1,-1));
            //get queen moves, arrow moves 
            for(int[] queen: queens){
                List<int[]> moves = QueenActions.getQueenMoves(gamestate, queen[0], queen[1]);

                for(int [] move : moves){
                    List<int[]> arrowMoves = QueenActions.getArrowShots(gamestate, move[0], move[1]);
                    for (int[] arrow : arrowMoves){
                        ArrayList<Integer> newState = QueenActions.executeMove(gamestate, queen[0], queen[1], move[0], move[1], arrow[0], arrow[1]);
                        ArrayList<ArrayList<Integer>> result = alpha_beta(newState, depth-1, alpha, beta, false, isWhite);
                        int value = result.get(0).get(0);
                        // get better moves
                        if(value > bestValue){
                            bestValue = value;
                            bestQueenPosCurrent.clear();
                            bestQueenPosCurrent.add(queen[0]);
                            bestQueenPosCurrent.add(queen[1]);
                            bestQueenPosNew.clear();
                            bestQueenPosNew.add(move[0]);
                            bestQueenPosNew.add(move[1]);
                            bestArrowPos.clear();
                            bestArrowPos.add(arrow[0]);
                            bestArrowPos.add(arrow[1]);
                        }
                        // pruning
                        alpha = Math.max(alpha, bestValue);
                        if(beta <= alpha){
                            break;
                        }
                    }
                    if(beta <= alpha){
                        break;
                    }
                }
                if(beta <= alpha){
                    break;
                }
            }
            // return the best moves
            ArrayList<ArrayList<Integer>> Finalmove = new ArrayList<>();
            Finalmove.add(new ArrayList<>(Arrays.asList(bestValue)));
            Finalmove.add(bestQueenPosCurrent);
            Finalmove.add(bestQueenPosNew);
            Finalmove.add(bestArrowPos);
            return Finalmove;
        }
        else{
            int bestValue = Integer.MAX_VALUE;
            ArrayList<Integer> bestQueenPosCurrent = new ArrayList<>(Arrays.asList(-1,-1));
            ArrayList<Integer> bestQueenPosNew = new ArrayList<>(Arrays.asList(-1,-1));
            ArrayList<Integer> bestArrowPos = new ArrayList<>(Arrays.asList(-1,-1));
            for(int [] queen : queens){
                List<int[]> moves = QueenActions.getQueenMoves(gamestate, queen[0], queen[1]);
                for (int[] move : moves){
                    List<int[]> arrowMoves = QueenActions.getArrowShots(gamestate, move[0], move[1]);
                    for(int[] arrow : arrowMoves){
                        ArrayList<Integer> newState = QueenActions.executeMove(gamestate, queen[0], queen[1], move[0], move[1], arrow[0], arrow[1]);
                        ArrayList<ArrayList<Integer>> result = alpha_beta(newState, depth-1, alpha, beta, true, isWhite);
                        int value = result.get(0).get(0);
                        if(value < bestValue){
                            bestValue = value;
                            bestQueenPosCurrent.clear();
                            bestQueenPosCurrent.add(queen[0]);
                            bestQueenPosCurrent.add(queen[1]);
                            bestQueenPosNew.clear();
                            bestQueenPosNew.add(move[0]);
                            bestQueenPosNew.add(move[1]);
                            bestArrowPos.clear();
                            bestArrowPos.add(arrow[0]);
                            bestArrowPos.add(arrow[1]);
                        }
                        beta = Math.min(beta, bestValue);
                        if(beta <= alpha){
                            break;
                        }
                    }
                    if(beta <= alpha){
                        break;
                    }
                }
                if(beta <= alpha){
                    break;
                }
            }
            ArrayList<ArrayList<Integer>> Finalmove = new ArrayList<>();
            Finalmove.add(new ArrayList<>(Arrays.asList(bestValue)));
            Finalmove.add(bestQueenPosCurrent);
            Finalmove.add(bestQueenPosNew);
            Finalmove.add(bestArrowPos);
            return Finalmove;
        }
    }
    //check the game is over or not
    public static boolean isGameOver(ArrayList<Integer> gamestate) {
        List<int[]> whiteQueens = QueenActions.getQueenPositions(gamestate, true);
        List<int[]> blackQueens = QueenActions.getQueenPositions(gamestate, false);
        
        // Check if white can move
        boolean whiteCanMove = false;
        for(int[] queen : whiteQueens) {
            if(!QueenActions.getQueenMoves(gamestate, queen[0], queen[1]).isEmpty()) {
                whiteCanMove = true;
                break;
            }
        }
        
        // Check if black can move - Fixed the semicolon bug
        boolean blackCanMove = false;
        for(int[] queen : blackQueens) {
            if(!QueenActions.getQueenMoves(gamestate, queen[0], queen[1]).isEmpty()) {
                blackCanMove = true;
                break;
            }
        }
        
        // Game is over if either player cannot move
        return !whiteCanMove || !blackCanMove;
    }
}
