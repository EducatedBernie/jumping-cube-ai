package jump61;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static jump61.Side.*;

/**
 * An automated Player.
 *
 * @author P. N. Hilfinger
 */
class AI extends Player {
    /**
     * A new player of GAME initially COLOR that chooses moves automatically.
     * SEED provides a random-number seed used for choosing moves.
     */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }


    @Override
    String getMove() {
        Board board = getGame().getBoard();
        assert getSide() == board.whoseMove();
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }


    /**
     * Return a move after searching the game tree to DEPTH>0 moves
     * from the current position. Assumes the game is not over.
     */
    private int searchForMove() {
        Board work = new Board(getBoard());

        int value;
        _random = new Random();
        assert getSide() == work.whoseMove();
        _foundMove = -1;
        if (getSide() == RED) {
            value = minMax(work, 3, true, 1,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);

        } else {
            value = minMax(work, 3, true, -1,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);

        }
        if (_foundMove == -1) {
            throw new GameException("Found move has returned -1, this is bad.");
        } else if (_foundMove == -3) {
            throw new GameException("Best move is never initialized");
        }
        return _foundMove;
    }

    /**
     * The main method serves to test AI methods.
     *
     * @param args ARGS
     */
    public static void main(String[] args) {
        Board B = new Board(2);
        System.out.println(B.toString());
        ArrayList<Integer> redPossibleMoves = validMoves(B, RED);
        ArrayList<Integer> bluePossibleMoves = validMoves(B, BLUE);
        ArrayList<Board> redChildBoards = nextBoardStates(B,
                RED, redPossibleMoves);
        HashMap<Board, Integer> redBoardMovePairings = new HashMap<>();
        assert redPossibleMoves.equals(bluePossibleMoves);
        System.out.println("Possible moves for red:");
        System.out.println(redPossibleMoves);
        System.out.println("Since there are " + redPossibleMoves.size()
                + " moves for red, we can make " + redChildBoards.size()
                + " child boards");
        System.out.println("Future boards states for red:");

        for (int i = 0; i < redPossibleMoves.size(); i++) {
            redBoardMovePairings.put(redChildBoards.get(i),
                    redPossibleMoves.get(i));
        }
        System.out.println(redBoardMovePairings.toString());

    }

    /**
     * Exhaustively searches the board for valid moves, returns arraylist.
     * of Boards that come from executing those validMoves SIDE BOARD
     */
    private static ArrayList<Integer> validMoves(Board board, Side side) {
        ArrayList<Integer> moves = new ArrayList<>();
        assert (side == board.whoseMove());
        for (int i = 0; i < board.size() * board.size(); i++) {
            if (board.isLegal(side, i)) {
                moves.add(i);
            }
        }
        return moves;
    }

    /**
     * Uses your validMoves, and execute them, new board for each validMove.
     * VALIDMOVES SIDE BOARD
     *
     * @return All possible board states that can result executing validMoves
     */
    private static ArrayList<Board> nextBoardStates(
            Board board, Side side, ArrayList<Integer> validMoves) {
        ArrayList<Board> nextBoardStates = new ArrayList<>();
        assert (side == board.whoseMove());
        for (int i = 0; i < validMoves.size(); i++) {
            board.addSpot(side, validMoves.get(i));
            nextBoardStates.add(new Board(board));
            board.undo();
        }
        return nextBoardStates;
    }

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        int bestMove = 0 - 3;
        int minMaxEval = -1;
        ArrayList<Integer> possibleMoves;
        ArrayList<Board> nextStates;
        HashMap<Board, Integer> boardMovePairings;
        if (depth == 0 || board.getWinner() != null) {
            return staticEval(board, 0);
        }
        if (sense == 1) {
            minMaxEval = Integer.MIN_VALUE;
            possibleMoves = validMoves(board, RED);
            nextStates = nextBoardStates(board, RED, possibleMoves);
            assert (possibleMoves.size() == nextStates.size());
            boardMovePairings = new HashMap<>();
            for (int i = 0; i < possibleMoves.size(); i++) {
                boardMovePairings.put(nextStates.get(i), possibleMoves.get(i));
            }
            for (Board childBoard : nextStates) {
                int eval = minMax(childBoard,
                        depth - 1, false, -1, alpha, beta);
                if (eval >= minMaxEval) {
                    bestMove = boardMovePairings.get(childBoard);
                }
                minMaxEval = Math.max(minMaxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
        } else {
            minMaxEval = Integer.MAX_VALUE;
            possibleMoves = validMoves(board, BLUE);
            nextStates = nextBoardStates(board, BLUE, possibleMoves);
            boardMovePairings = new HashMap<>();
            for (int i = 0; i < possibleMoves.size(); i++) {
                boardMovePairings.put(nextStates.get(i), possibleMoves.get(i));
            }
            for (Board childBoard : nextStates) {
                int eval = minMax(childBoard, depth - 1, false, 1, alpha, beta);
                if (eval <= minMaxEval) {
                    bestMove = boardMovePairings.get(childBoard);
                }
                minMaxEval = Math.min(minMaxEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        if (saveMove) {
            _foundMove = bestMove;
        }
        return minMaxEval;
    }

    /**
     * Return a heuristic estimate of the value of board position B.
     * Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     * indicate a win for Blue.
     */
    private int staticEval(Board b, int winningValue) {
        int value = 0;
        if (b.getWinner() == RED) {
            return Integer.MAX_VALUE;
        } else if (b.getWinner() == BLUE) {
            return Integer.MIN_VALUE;
        }
        value = b.numOfSide(RED) - b.numOfSide(BLUE);
        return value;
    }

    /**
     * A random-number generator used for move selection.
     */
    private Random _random;

    /**
     * Used to convey moves discovered by minMax.
     */
    private int _foundMove;
}
