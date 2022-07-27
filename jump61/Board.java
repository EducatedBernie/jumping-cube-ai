package jump61;

import java.util.ArrayDeque;
import java.util.Formatter;
import java.util.Stack;
import java.util.ArrayList;

import java.util.function.Consumer;

import static java.lang.System.arraycopy;
import static jump61.Side.*;

/**
 * Represents the state of a Jump61 game.  Squares are indexed either by
 * row and column (between 1 and size()), or by square number, numbering
 * squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 * row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 * <p>
 * A Board may be given a notifier---a Consumer<Board> whose
 * .accept method is called whenever the Board's contents are changed.
 *
 * @author Bernie Miao
 */
class Board {

    /**
     * An uninitialized Board.  Only for use by subtypes.
     */
    protected Board() {
        _notifier = NOP;
    }

    /**
     * A holder for the _cells and _active instance variables of this
     * Model.
     */
    private int _numMoves;
    /**
     * A holder for the _cells and _active instance variables of this
     * Model.
     */
    private int _numSquares;
    /**
     * A holder for the _cells and _active instance variables of this
     * Model.
     */
    private Stack<GameState> _undoHistory = new Stack<GameState>();

    /**
     * An N x N board in initial configuration.
     */
    Board(int N) {
        this();
        _numSquares = N * N;
        _squares = new Square[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                _squares[i][j] = Square.square(WHITE, 1);
            }
        }
    }

    /**
     * A holder for the _cells and _active instance variables of this
     * Model. NEWSIZE
     */
    void initializeBoard(int newSize) {
        _squares = new Square[newSize][newSize];
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                _squares[i][j] = Square.square(WHITE, 1);
            }
        }
    }

    /**
     * A board whose initial contents are copied from BOARD0, but whose
     * undo history is clear, and whose notifier does nothing.
     */
    Board(Board board0) {
        this(board0.size());
        setNotifier(NOP);
        internalCopy(board0);
        _readonlyBoard = new ConstantBoard(this);
    }

    /**
     * Returns a readonly version of this board.
     */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /**
     * (Re)initialize me to a cleared board with N squares on a side. Clears
     * the undo history and sets the number of moves to 0.
     */
    void clear(int N) {
        _numMoves = 0;
        initializeBoard(N);
        _undoHistory.clear();
        announce();
    }

    /**
     * Copy the contents of BOARD into me. The difference is this clears
     * history and resets numMoves
     */
    void copy(Board board) {

        if (board.size() != size()) {
            throw new GameException("Board.copy() has encountered a difference "
                    + "in game board size between dst board and src board");
        }
        internalCopy(board);
        _numMoves = 0;
        _undoHistory.clear();
    }

    /**
     * Copy the contents of BOARD into me, without modifying my undo
     * history. Assumes BOARD and I have the same size.
     */
    private void internalCopy(Board board) {
        assert size() == board.size();

        for (int i = 0; i < size(); i++) {
            Square[] row = board.getBoard()[i];
            System.arraycopy(row, 0, _squares[i], 0, size());
        }
    }

    /**
     * Copy contents of SRC into DEST.  SRC and DEST must both be
     * rectangular, with identical dimensions.
     */
    static void deepCopy(Square[][] src, Square[][] dest) {
        assert src.length == dest.length && src[0].length == dest[0].length;
        for (int i = 0; i < src.length; i += 1) {
            arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
    }

    /**
     * Returns a deep copy of SRC: the result has no shared state with SRC.
     */
    static Square[][] deepCopyOf(Square[][] src) {
        Square[][] result = new Square[src.length][src[0].length];
        deepCopy(src, result);
        return result;
    }

    /**
     * Return the number of rows and of columns of THIS.
     */
    int size() {
        return getBoard()[1].length;
    }

    /**
     * Returns the contents of the square at row R, column C
     * 1 <= R, C <= size ().
     */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /**
     * Returns the contents of square #N, numbering squares by rows, with
     * squares in row 1 number 0 - size( )-1, in row 2 numbered
     * size() - 2*size() - 1, etc.
     */
    Square get(int n) {
        return getBoard()[row(n) - 1][col(n) - 1];
    }

    /**
     * A holder for the _cells and _active instance variables of this
     * Model.
     * @return nihao
     */
    Square[][] getBoard() {
        return _squares;
    }

    /**
     * Returns the total number of spots on the board.
     */
    int numPieces() {
        int num = 0;
        for (Square[] col : _squares) {
            for (Square row : col) {
                num += row.getSpots();
            }
        }
        return num;
    }

    /**
     * Returns the Side of the player who would be next to move.  If the
     * game is won, this will return the loser (assuming legal position).
     */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /**
     * Return true iff row R and column C denotes a valid square.
     */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /**
     * Return true iff S is a valid square number.
     */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /**
     * Return the row number for square #N.
     */
    final int row(int n) {
        return n / size() + 1;
    }

    /**
     * Return the column number for square #N.
     */
    final int col(int n) {
        return n % size() + 1;
    }

    /**
     * Return the square number of row R, column C.
     */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /**
     * Return a string denoting move (ROW, COL)N.
     */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /**
     * Return a string denoting move N.
     */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /**
     * Returns true iff it would currently be legal for PLAYER to add a spot
     * to square at row R, column C.
     */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /**
     * Returns true iff it would currently be legal for PLAYER to add a spot
     * to square #N.
     */
    boolean isLegal(Side player, int n) {
        if (isLegal(player)) {
            return (get(n).getSide() == WHITE || get(n).getSide() == player);
        }
        return false;
    }

    /**
     * Returns true iff PLAYER is allowed to move at this point.
     */
    boolean isLegal(Side player) {
        return whoseMove() == player;
    }

    /**
     * Returns the winner of the current position, if the game is over,
     * and otherwise null.
     */
    final Side getWinner() {
        _numSquares = size() * size();
        if (numOfSide((RED)) == _numSquares) {
            return RED;
        } else if (numOfSide(BLUE) == _numSquares) {
            return BLUE;
        }
        return null;
    }

    /**
     * Return the number of squares of given SIDE.
     */
    int numOfSide(Side side) {
        int count = 0;
        for (Square[] row : getBoard()) {
            for (Square element : row) {
                if (element.getSide().equals(side)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Add a spot from PLAYER at row R, column C.  Assumes
     * isLegal(PLAYER, R, C).
     */
    void addSpot(Side player, int r, int c) {
        _numMoves++;
        markUndo();
        int tempNumSpots = get(r, c).getSpots();
        internalSet(r, c, tempNumSpots + 1, player);
        jump(sqNum(r, c));
    }

    /**
     * Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N).
     */
    void addSpot(Side player, int n) {
        addSpot(player, row(n), col(n));
    }

    /**
     * Set the square at row R, column C to NUM spots (0 <= NUM), and give
     * it color PLAYER if NUM > 0 (otherwise, white).
     */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }

    /**
     * Set the square at row R, column C to NUM spots (0 <= NUM), and give
     * it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     * changes.
     */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /**
     * Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     * if NUM > 0 (otherwise, white). Does not announce changes.
     */
    private void internalSet(int n, int num, Side player) {

        _squares[row(n) - 1][col(n) - 1] = Square.square(player, num);
    }


    /**
     * Undo the effects of one move (that is, one addSpot command).  One
     * can only undo back to the last point at which the undo history
     * was cleared, or the construction of this Board.
     */
    void undo() {
        GameState pastState = _undoHistory.pop();
        pastState.restoreState();
    }

    /**
     * Record the beginning of a move in the undo history.
     */
    private void markUndo() {
        GameState currState = new GameState();
        currState.saveState();
        _undoHistory.add(currState);
    }

    /**
     * Add DELTASPOTS spots of side PLAYER to row R, column C,
     * updating counts of numbers of squares of each color.
     */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /**
     * Add DELTASPOTS spots of color PLAYER to square #N,
     * updating counts of numbers of squares of each color.
     */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /**
     * Used in jump to keep track of squares needing processing.  Allocated
     * here to cut down on allocations.
     */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /**
     * Returns ture if N is overfull.
     */
    private boolean overfull(int N) {
        return (get(N).getSpots() > neighbors(N));
    }

    /**
     * If neighbour is valid, then add to them, then if they're overfull.
     * then add that very neighbour into the _workQueue INITIALSIDE, N
     * @return IDK
     */
    private int addToValidNeighbours(int N, Side initialSide) {
        Square curr = get(N);
        int pointsLost = 0;
        int row = row(N);
        int col = col(N);
        ArrayList<Integer> neighbourArray = new ArrayList<Integer>();

        if (exists(row + 1, col)) {
            simpleAdd(initialSide, sqNum(row + 1, col), 1);
            pointsLost++;
            neighbourArray.add(sqNum(row + 1, col));
        }
        if (exists(row - 1, col)) {
            simpleAdd(initialSide, sqNum(row - 1, col), 1);
            pointsLost++;
            neighbourArray.add(sqNum(row - 1, col));
        }
        if (exists(row, col + 1)) {
            simpleAdd(initialSide, sqNum(row, col + 1), 1);
            pointsLost++;
            neighbourArray.add(sqNum(row, col + 1));
        }

        if (exists(row, col - 1)) {
            simpleAdd(initialSide, sqNum(row, col - 1), 1);
            pointsLost++;
            neighbourArray.add(sqNum(row, col - 1));
        }

        for (int neighbour : neighbourArray) {
            if (overfull(neighbour) && !_workQueue.contains(neighbour)) {
                _workQueue.add(neighbour);
            }
        }
        return pointsLost;

    }

    /**
     * Do all jumping on this board, assuming that initially, S is the only
     * square that might be over-full.
     */
    private void jump(int S) {
        int process;
        int pointsLost;
        Side initialSide = get(S).getSide();
        if (getWinner() != null) {
            return;
        }
        if (overfull(S)) {
            _workQueue.add(S);
        }

        while (!_workQueue.isEmpty() && getWinner() == null) {
            process = _workQueue.pop();
            if (overfull(process)) {
                pointsLost = addToValidNeighbours(process, initialSide);
                internalSet(process, get(process).getSpots()
                        - neighbors(process), initialSide);
            }
        }
    }

    /**
     * A holder for the _cells and _active instance variables of this
     * Model. SIDE
     * @return
     */
    public String sideToString(String side) {
        if (side.equals("white")) {
            return "-";
        } else if (side.equals("red")) {
            return "r";
        } else if (side.equals("blue")) {
            return "b";
        } else {
            throw new GameException("sideToString error: no side found");
        }
    }

    /**
     * Returns my dumped representation.
     */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        String entireRow = "";
        String entireTable = "===";

        for (Square[] row : _squares) {
            entireRow = "    ";
            for (Square col : row) {
                int spots = col.getSpots();
                String side = col.getSide().toString();
                entireRow += spots + sideToString(side) + " ";
            }
            entireTable += "\n" + entireRow;
        }

        return entireTable + "\n" + "===";
    }

    /**
     * Returns an external rendition of me, suitable for human-readable
     * textual display, with row and column numbers.  This is distinct
     * from the dumped representation (returned by toString).
     */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /**
     * Returns the number of neighbors of the square at row R, column C.
     */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /**
     * Returns the number of neighbors of square #N.
     */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            return this == obj;
        }
    }

    @Override
    public int hashCode() {
        return numPieces();
    }

    /**
     * Set my notifier to NOTIFY.
     */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /**
     * A holder for the _cells and _active instance variables of this
     * Model.
     * @return
     */
    public int getNumMoves() {
        return _numMoves;
    }

    /**
     * Take any action that has been set for a change in my state.
     */
    private void announce() {
        _notifier.accept(this);
    }

    /**
     * A notifier that does nothing.
     */
    private static final Consumer<Board> NOP = (s) -> {
    };

    /**
     * 2d array to store that hist.
     */
    private Square[][] _squares;

    /**
     * A read-only version of this Board.
     */
    private ConstantBoard _readonlyBoard;

    /**
     * Use _notifier.accept(B) to announce changes to this board.
     */
    private Consumer<Board> _notifier;


    /**
     * Represents enough of the state of a game to allow undoing and
     * redoing of moves.
     */
    private class GameState {
        /**
         * A holder for the _cells and _active instance variables of this
         * Model.
         */
        private Square[][] _savedSquares;
        /**
         * A holder for the _cells and _active instance variables of this
         * Model.
         */
        private int savedNumMoves;

        /**
         * A holder for the _cells and _active instance variables of this
         * Model.
         */
        GameState() {
            _savedSquares = new Square[size()][size()];
            savedNumMoves = 0;
        }

        /**
         * A holder for the _cells and _active instance variables of this
         * Model.
         */
        void saveState() {
            deepCopy(getBoard(), _savedSquares);
            savedNumMoves = getNumMoves();
        }

        /**
         * A holder for the _cells and _active instance variables of this
         * Model.
         */
        void restoreState() {
            deepCopy(_savedSquares, _squares);
            _numMoves = savedNumMoves;

        }

    }

}
