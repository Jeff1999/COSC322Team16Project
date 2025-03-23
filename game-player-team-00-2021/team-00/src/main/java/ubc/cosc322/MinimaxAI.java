package ubc.cosc322;

import java.util.ArrayList;
import java.util.List;

public class MinimaxAI {
    private boolean isBlackPlayer; // Whether the AI is playing as black
    public long startTime = System.currentTimeMillis();// start time
    public long timeLimted = 8000; //time limited 30s;
    public MinimaxAI(boolean isBlackPlayer) {
        this.isBlackPlayer = isBlackPlayer;
    }

    
      //Find the best move using the Minimax algorithm
     
      public Move findBestMoveWithDynamicDepth(ArrayList<Integer> gameState) {
        // Reset timer for new move calculation
        startTime = System.currentTimeMillis();
        
        Move bestMove = null;
        int depth = 2; // start with depth 2
        
        while(true) {
            if(System.currentTimeMillis() - startTime > timeLimted) {
                break;
            }
    
            // Check if we've completely solved the position
            List<Move> possibleMoves = generateMoves(gameState, true);
        
            // If we detect that we've exhausted the search (fully solved the position)
            boolean fullyExhausted = (depth > possibleMoves.size() * 2);
            if (fullyExhausted) {
                System.out.println("Exhausted all options at depth " + (depth-1) + " with " + possibleMoves.size() + " moves");
                break;
            }
            
            // search with default depth
            MinimaxResult result = minimax(gameState, depth, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            //update the best move
            if(result.move != null) {
                bestMove = result.move;
            }
    
            // increase the depth if we have enough time
            depth++;
        }
        
        System.out.println("how deep we search: " + (depth-1));
        return bestMove;
    }
    
    
    // Minimax algorithm with alpha-beta pruning
    
    private MinimaxResult minimax(ArrayList<Integer> gameState, int depth, boolean maximizingPlayer, int alpha, int beta) {
        //check if time limit is exceeded
        if(System.currentTimeMillis() - startTime > timeLimted){
            return new MinimaxResult(evaluateState(gameState),null);
        }
        
        if (depth == 0 || isGameOver(gameState)) {
            return new MinimaxResult(evaluateState(gameState), null);
        }

        List<Move> possibleMoves = generateMoves(gameState, maximizingPlayer);

        if (maximizingPlayer) {
            MinimaxResult maxEval = new MinimaxResult(Integer.MIN_VALUE, null);
            for (Move move : possibleMoves) {
                ArrayList<Integer> newState = applyMove(gameState, move);
                MinimaxResult eval = minimax(newState, depth - 1, false, alpha, beta);
                if (eval.score > maxEval.score) {
                    maxEval.score = eval.score;
                    maxEval.move = move;
                }
                alpha = Math.max(alpha, eval.score);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            MinimaxResult minEval = new MinimaxResult(Integer.MAX_VALUE, null);
            for (Move move : possibleMoves) {
                ArrayList<Integer> newState = applyMove(gameState, move);
                MinimaxResult eval = minimax(newState, depth - 1, true, alpha, beta);
                if (eval.score < minEval.score) {
                    minEval.score = eval.score;
                    minEval.move = move;
                }
                beta = Math.min(beta, eval.score);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    
     // Generate all possible moves for the current player
     
     public List<Move> generateMoves(ArrayList<Integer> gameState, boolean maximizingPlayer) {
        List<Move> moves = new ArrayList<>();
        int queenType = maximizingPlayer ? (isBlackPlayer ? 1 : 2) : (isBlackPlayer ? 2 : 1);
    
        for (int i = 0; i < gameState.size(); i++) {
            if (gameState.get(i) == queenType) {
                int[] pos = indexToRC(i);
                List<int[]> queenMoves = getQueenMoves(gameState, pos[0], pos[1]);
                for (int[] move : queenMoves) {
                    // Create a temporary board state with the queen moved
                    ArrayList<Integer> tempState = new ArrayList<>(gameState);
                    int startIdx = rcToIndex(pos[0], pos[1]);
                    int endIdx = rcToIndex(move[0], move[1]);
                    tempState.set(startIdx, 0); // Empty the starting position
                    tempState.set(endIdx, queenType); // Move the queen
                    
                    // Now get arrow shots using the updated board state
                    List<int[]> arrowShots = getArrowShots(tempState, move[0], move[1]);
                    for (int[] arrow : arrowShots) {
                        moves.add(new Move(pos, move, arrow));
                    }
                }
            }
        }
    
        return moves;
    }

    
     // Apply a move to the game state
     
    private ArrayList<Integer> applyMove(ArrayList<Integer> gameState, Move move) {
        ArrayList<Integer> newState = new ArrayList<>(gameState);
        int startIdx = rcToIndex(move.queenPos[0], move.queenPos[1]);
        int endIdx = rcToIndex(move.queenTargetPos[0], move.queenTargetPos[1]);
        int arrowIdx = rcToIndex(move.arrowPos[0], move.arrowPos[1]);

        int queenType = newState.get(startIdx);

        newState.set(startIdx, 0);
        newState.set(endIdx, queenType);
        newState.set(arrowIdx, 3);

        return newState;
    }

    // f_value
    private int evaluateState(ArrayList<Integer> gameState) {
        // mobility
        int myMoves = generateMoves(gameState, true).size();
        int opponentMoves = generateMoves(gameState, false).size();
        int mobilityScore = myMoves - opponentMoves;
        // queen position
        int queenPositionScore = evaluateQueenPosition(gameState);
        int arrowPositionScore = evaluateArrowBlocking(gameState);
        int total = mobilityScore*6 + queenPositionScore*4 + arrowPositionScore * 3;
        return total;
    }
    // Evaluate the position of the queens
    private int evaluateQueenPosition(ArrayList<Integer> gameState){
            int myQueenScore = 0;
            int opponentQueenScore = 0;
            for (int i = 0; i < gameState.size(); i++) {
                if(gameState.get(i) == (isBlackPlayer ? 1:2)){
                    int [] pos = indexToRC(i);
                        myQueenScore += getPositionValue(pos[0],pos[1]);
                }else if(gameState.get(i) == (isBlackPlayer? 2:1)){
                    int[] pos = indexToRC(i);
                    opponentQueenScore += getPositionValue(pos[0], pos[1]);
                }
            }
        return myQueenScore-opponentQueenScore;
        
    }
    // value of a position on the board
    private int getPositionValue(int r, int c){
        int centerR = 5;
        int centerC = 5;
        int distanceFromCenter = Math.abs(r-centerR) + Math.abs(c-centerC);
        // closer to center, get higher value       
        return 10 - distanceFromCenter;
        
    }
    // evaluate the blocking 
    private int evaluateArrowBlocking(ArrayList<Integer> gameState) {
        int myBlockingScore = 0;
        int opponentBlockingScore = 0;
    
        for (int i = 0; i < gameState.size(); i++) {
            if (gameState.get(i) == 3) { // Arrow
                int[] pos = indexToRC(i);
                // Check if the arrow blocks opponent's queen
                if (isBlockingOpponentQueen(gameState, pos[0], pos[1])) {
                    myBlockingScore += 1;
                }
            }
        }
    
        return myBlockingScore - opponentBlockingScore;
    }
    // arrow blocks an opponent's queen
    private boolean isBlockingOpponentQueen(ArrayList<Integer> gameState, int row, int col ) {
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, 
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
    
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
    
            while (r >= 1 && r <= 10 && c >= 1 && c <= 10) {
                int index = rcToIndex(r, c);
                if (gameState.get(index) == (isBlackPlayer ? 2 : 1)) { // Opponent's queen
                    return true;
                } else if (gameState.get(index) != 0) { // Hit a piece or arrow
                    break;
                }
    
                r += dir[0];
                c += dir[1];
            }
        }
    
        return false;
    }
    
    // Check if the game is over
     
    private boolean isGameOver(ArrayList<Integer> gameState) {
        List<Move> myMoves = generateMoves(gameState, true);
        List<Move> opponentMoves = generateMoves(gameState, false);
        return myMoves.isEmpty() || opponentMoves.isEmpty();
    }

   
    // Convert row and column to game state index
    
    private int rcToIndex(int row, int col) {
        return (row) * 11 + (col);
    }

    
    // Convert game state index to row and column
     
    private int[] indexToRC(int index) {
        int row = (index / 11);
        int col = (index % 11);
        if (row >= 11 || col < 0) {
            return null;
        }
        return new int[] {row, col};
    }

    
    //Get valid moves for a queen
    
    private List<int[]> getQueenMoves(ArrayList<Integer> gameState,int row, int col) {
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

    
    // Get valid arrow shots from a position
     
    private List<int[]> getArrowShots(ArrayList<Integer> gameState,int row, int col) {
        // Arrow movement follows the same rules as queen movement
        return getQueenMoves(gameState,row, col);
    }

    
    // Class to hold the result of the Minimax algorithm
     
    private class MinimaxResult {
        int score;
        Move move;

        MinimaxResult(int score, Move move) {
            this.score = score;
            this.move = move;
        }
    }

    
    // Class to represent a move
     
    public class Move {
        public int[] queenPos;
        public int[] queenTargetPos;
        public int[] arrowPos;

        public Move(int[] queenPos, int[] queenTargetPos, int[] arrowPos) {
            this.queenPos = queenPos;
            this.queenTargetPos = queenTargetPos;
            this.arrowPos = arrowPos;
        }
    }
}