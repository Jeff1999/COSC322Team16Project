package ubc.cosc322;

import java.util.ArrayList;
import java.util.List;

public class MinimaxAI {
    private boolean isBlackPlayer; // Whether the AI is playing as black
    public long startTime = System.currentTimeMillis();// start time
    public long timeLimted = 25000; //time limited 30s;
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
        // Get the game phase (early, mid, late) based on number of moves
        int game = countEmptySpot(gameState);
        //int totalMoves = generateMoves(gameState, true).size() + generateMoves(gameState, false).size();
        //boolean isEndgame = totalMoves < 50; // Adjust this threshold as needed
        
        // Mobility (more important in endgame)
        int myMoves = generateMoves(gameState, true).size();
        int opponentMoves = generateMoves(gameState, false).size();
        int mobilityScore = myMoves - opponentMoves;
        
        // Queen position and arrow blocking
        int queenPositionScore = evaluateQueenPosition(gameState);
        int arrowPositionScore = evaluateArrowBlocking(gameState);
        
        // New territory and partitioning evaluations
        int territoryScore = evaluateTerritory(gameState);
        int partitioningScore = evaluatePartitioning(gameState);
        int regionSizeScore = evaluateRegionSizes(gameState);
        int regionSizeWeight = 12; // High weight because this directly targets the winning strategy
    

        
        // Adjust weights based on game phase
        /*int mobilityWeight = isEndgame ? 4 : 8;
        int territoryWeight = isEndgame ? 10 : 15;
        int partitioningWeight = isEndgame ? 15 : 11;
        int queenPositionWeight = isEndgame ? 7 : 8;
        int arrowBlockingWeight = isEndgame ? 7 : 5;*/
        int mobilityWeight = 0;
        int territoryWeight = 0;
        int partitioningWeight = 0;
        int queenPositionWeight = 0;
        int arrowBlockingWeight = 0;
        if(game > 60){ // empty spots are greater than 60, early game
            mobilityWeight = 10;
            territoryWeight = 14;
            partitioningWeight = 8;
            queenPositionWeight = 9;
            arrowBlockingWeight = 4;
        }
        else if (game > 30 && game < 60){ // empty spots are greater than 30, mid game
            mobilityWeight = 8;
            territoryWeight = 15;
            partitioningWeight = 11;
            queenPositionWeight = 8;
            arrowBlockingWeight = 5;
        }
        else{ // end game 
            mobilityWeight = 5;
            territoryWeight = 12;
            partitioningWeight = 11;
            queenPositionWeight = 6;
            arrowBlockingWeight = 5;
        }
        
        // Calculate final score
        int total = (mobilityScore * mobilityWeight) + 
                (territoryScore * territoryWeight) + 
                (partitioningScore * partitioningWeight) + 
                (queenPositionScore * queenPositionWeight) + 
                (arrowPositionScore * arrowBlockingWeight) +
                (regionSizeScore * regionSizeWeight);
    
        return total;
    }
    // check how many empty spots that left on the board
    private int countEmptySpot(ArrayList<Integer> gameStage){
        int emptySpot = 0;
        for (int i = 0; i < gameStage.size(); i++) {
            if(gameStage.get(i) == 0){
                emptySpot++;
            }
        }
        return emptySpot;
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

    private int evaluateTerritory(ArrayList<Integer> gameState) {
        // Store unique squares each player can reach
        java.util.Set<String> myTerritorySet = new java.util.HashSet<>();
        java.util.Set<String> opponentTerritorySet = new java.util.HashSet<>();
        
        // Get positions of queens for both players
        List<int[]> myQueens = new ArrayList<>();
        List<int[]> opponentQueens = new ArrayList<>();
        
        int myQueenType = isBlackPlayer ? 1 : 2;
        int opponentQueenType = isBlackPlayer ? 2 : 1;
        
        // Find all queens
        for (int i = 0; i < gameState.size(); i++) {
            if (gameState.get(i) == myQueenType) {
                myQueens.add(indexToRC(i));
            } else if (gameState.get(i) == opponentQueenType) {
                opponentQueens.add(indexToRC(i));
            }
        }
        
        // Calculate territory for my queens
        for (int[] queen : myQueens) {
            List<int[]> moves = getQueenMoves(gameState, queen[0], queen[1]);
            for (int[] move : moves) {
                myTerritorySet.add(move[0] + "," + move[1]);
            }
        }
        
        // Calculate territory for opponent queens
        for (int[] queen : opponentQueens) {
            List<int[]> moves = getQueenMoves(gameState, queen[0], queen[1]);
            for (int[] move : moves) {
                opponentTerritorySet.add(move[0] + "," + move[1]);
            }
        }
        
        // Calculate exclusive territory (areas only one player can access)
        java.util.Set<String> myExclusiveTerritory = new java.util.HashSet<>(myTerritorySet);
        myExclusiveTerritory.removeAll(opponentTerritorySet);
        
        java.util.Set<String> opponentExclusiveTerritory = new java.util.HashSet<>(opponentTerritorySet);
        opponentExclusiveTerritory.removeAll(myTerritorySet);
        
        // Territories that can be accessed by both players are contested
        int myTerritorySize = myTerritorySet.size();
        int opponentTerritorySize = opponentTerritorySet.size();
        int myExclusiveSize = myExclusiveTerritory.size();
        int opponentExclusiveSize = opponentExclusiveTerritory.size();
        
        // We want to maximize our territory and especially our exclusive territory
        // while minimizing the opponent's territory
        return (myTerritorySize * 1) + (myExclusiveSize * 2) - (opponentTerritorySize * 1) - (opponentExclusiveSize * 2);
    }

    /*private int evaluatePartitioning(ArrayList<Integer> gameState) {
        int partitionScore = 0;
        
        // Get positions of all queens
        List<int[]> myQueens = new ArrayList<>();
        List<int[]> opponentQueens = new ArrayList<>();
        
        int myQueenType = isBlackPlayer ? 1 : 2;
        int opponentQueenType = isBlackPlayer ? 2 : 1;
        
        // Find all queens
        for (int i = 0; i < gameState.size(); i++) {
            if (gameState.get(i) == myQueenType) {
                myQueens.add(indexToRC(i));
            } else if (gameState.get(i) == opponentQueenType) {
                opponentQueens.add(indexToRC(i));
            }
        }
        
        // Count arrows adjacent to empty spaces
        // More adjacent empty spaces = less partitioning effect
        for (int i = 0; i < gameState.size(); i++) {
            if (gameState.get(i) == 3) { // Arrow
                int[] pos = indexToRC(i);
                int arrowEmptyNeighbors = countEmptyNeighbors(gameState, pos[0], pos[1]);
                
                // Check if this arrow separates our queen from opponents
                boolean separatesQueens = isArrowSeparatingQueens(gameState, pos[0], pos[1], myQueens, opponentQueens);
                
                // Arrows with fewer empty neighbors are better at partitioning
                // Arrows that separate queens are even better
                partitionScore += (8 - arrowEmptyNeighbors) + (separatesQueens ? 5 : 0);
            }
        }
        
        return partitionScore;
    }*/
    // check the connection 
    private int evaluatePartitioning(ArrayList<Integer> gameState) {

            int myRegion = countConnectedRegions(gameState, isBlackPlayer? 1:2);
            int opponentRegion = countConnectedRegions(gameState, isBlackPlayer ? 2:1);
        
        return (opponentRegion - myRegion) * 10; // opponent has more regions, the value is lager
    
    }
    // flood fill to count the connection regions
    private int countConnectedRegions(ArrayList<Integer> gameState, int player){
            boolean [] visited = new boolean[gameState.size()];
            int regions = 0;

            for (int i = 0; i < gameState.size(); i++) {
                if(!visited[i] && gameState.get(i) == player){
                    regions++;
                    int[] pos = indexToRC(i);
                    floodFill(gameState,visited,i,pos[0],pos[1],player);
                }
                
            }
        return regions;
        
    }
    private void floodFill(ArrayList<Integer> gameState, boolean[] visited, int i, int x,int y, int player){
        if(i<0 || i >= gameState.size() || visited[i] || gameState.get(i) != player){ 
            return;
        } 

        visited[i] = true;

        floodFill(gameState, visited, i, x+1, y, player);
        floodFill(gameState, visited, i, x-1, y, player);
        floodFill(gameState, visited, i, x, y+1, player);
        floodFill(gameState, visited, i, x, y-1, player);
    }
    
    private int countEmptyNeighbors(ArrayList<Integer> gameState, int row, int col) {
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, 
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        
        int emptyCount = 0;
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            
            if (r >= 1 && r <= 10 && c >= 1 && c <= 10) {
                int index = rcToIndex(r, c);
                if (gameState.get(index) == 0) { // Empty space
                    emptyCount++;
                }
            }
        }
        
        return emptyCount;
    }
    
    private boolean isArrowSeparatingQueens(ArrayList<Integer> gameState, int arrowRow, int arrowCol, 
                                          List<int[]> myQueens, List<int[]> opponentQueens) {
        // Create a copy of the game state without this arrow to see if it's important for separation
        ArrayList<Integer> tempState = new ArrayList<>(gameState);
        tempState.set(rcToIndex(arrowRow, arrowCol), 0); // Remove the arrow temporarily
        
        // Check connectivity between queens in the modified state
        for (int[] myQueen : myQueens) {
            for (int[] opponentQueen : opponentQueens) {
                if (canQueensConnect(tempState, myQueen, opponentQueen)) {
                    // If queens can connect without this arrow, then the arrow is separating them
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean canQueensConnect(ArrayList<Integer> gameState, int[] queen1, int[] queen2) {
        // Simple check - can a path be traced from queen1 to queen2?
        // This is a simplified approach using BFS
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        
        queue.add(queen1);
        visited.add(queen1[0] + "," + queen1[1]);
        
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, 
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            
            // Check if we've reached queen2
            if (current[0] == queen2[0] && current[1] == queen2[1]) {
                return true;
            }
            
            // Try each direction
            for (int[] dir : directions) {
                int r = current[0] + dir[0];
                int c = current[1] + dir[1];
                
                if (r >= 1 && r <= 10 && c >= 1 && c <= 10) {
                    String key = r + "," + c;
                    if (!visited.contains(key)) {
                        int index = rcToIndex(r, c);
                        if (gameState.get(index) == 0 || (r == queen2[0] && c == queen2[1])) {
                            queue.add(new int[]{r, c});
                            visited.add(key);
                        }
                    }
                }
            }
        }
        
        return false;
    }

    // Add a method to evaluate region size for each queen
private int evaluateRegionSizes(ArrayList<Integer> gameState) {
    int myRegionScore = 0;
    int opponentRegionScore = 0;
    
    int myQueenType = isBlackPlayer ? 1 : 2;
    int opponentQueenType = isBlackPlayer ? 2 : 1;
    
    // For each queen, calculate the size of its "region"
    for (int i = 0; i < gameState.size(); i++) {
        if (gameState.get(i) == myQueenType || gameState.get(i) == opponentQueenType) {
            int[] pos = indexToRC(i);
            int regionSize = calculateRegionSize(gameState, pos[0], pos[1]);
            
            if (gameState.get(i) == myQueenType) {
                myRegionScore += regionSize;
            } else {
                opponentRegionScore += regionSize;
            }
        }
    }
    
    return myRegionScore - opponentRegionScore;
}

// Calculate the size of a connected region a queen can move in
private int calculateRegionSize(ArrayList<Integer> gameState, int row, int col) {
    java.util.Set<String> visited = new java.util.HashSet<>();
    java.util.Queue<int[]> queue = new java.util.LinkedList<>();
    
    queue.add(new int[]{row, col});
    visited.add(row + "," + col);
    
    int[][] directions = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}, 
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };
    
    while (!queue.isEmpty()) {
        int[] current = queue.poll();
        
        for (int[] dir : directions) {
            int r = current[0];
            int c = current[1];
            
            // Check each direction until hitting a non-empty square
            while (true) {
                r += dir[0];
                c += dir[1];
                
                // Check if still on board
                if (r < 1 || r > 10 || c < 1 || c > 10) {
                    break;
                }
                
                String key = r + "," + c;
                if (!visited.contains(key)) {
                    int index = rcToIndex(r, c);
                    if (gameState.get(index) == 0) { // Empty space
                        queue.add(new int[]{r, c});
                        visited.add(key);
                    } else {
                        break; // Hit a piece or arrow
                    }
                } else {
                    break; // Already visited
                }
            }
        }
    }
    
    // Region size is the number of squares visited
    return visited.size();
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