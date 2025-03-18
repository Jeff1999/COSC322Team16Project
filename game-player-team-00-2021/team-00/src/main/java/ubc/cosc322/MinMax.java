package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinMax {
   
    public static int f_funtion(ArrayList<Integer> gamestate, boolean isWhite){
        // get all white queens and black queens position
        List<int[]> whiteQueens = QueenActions.getQueenPositions(gamestate, true);

        List<int[]> blackQueens = QueenActions.getQueenPositions(gamestate, false);
        // get all valid moves
        int white_moves = 0;
        int black_moves = 0;
        for(int [] queen : whiteQueens){
            white_moves += QueenActions.getQueenMoves(gamestate, queen[0], queen[1]).size();
        }
        for(int [] queen : blackQueens){
            black_moves += QueenActions.getQueenMoves(gamestate, queen[0], queen[1]).size();
        }
        // basic function evaluate the moves of differences between white and black queens. 
        int f_value = 0;
        if (isWhite == true){
            f_value = white_moves-black_moves;
        }
        else{
            f_value = black_moves-white_moves;
        }
        return f_value;

    }

    public static ArrayList<ArrayList<Integer>> alpha_beta (ArrayList<Integer> gamestate, int depth, int alpha, int beta,  boolean isMaxingPlayer, boolean isWhite){
        
        if(depth == 0 || isGameOver(gamestate)){
            ArrayList<ArrayList<Integer>> result = new ArrayList<>();
            result.add(new ArrayList<>(Arrays.asList(f_funtion(gamestate, isWhite))));
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
                        ArrayList<Integer> newState = QueenActions.simulateMove(gamestate, queen[0], queen[1], move[0], move[1], arrow[0], arrow[1]);
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
                        ArrayList<Integer> newState = QueenActions.simulateMove(gamestate, queen[0], queen[1], move[0], move[1], arrow[0], arrow[1]);
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
