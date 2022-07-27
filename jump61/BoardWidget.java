
package jump61;

import ucb.gui2.Pad;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;


import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import static jump61.Side.*;

/**
 * A GUI component that displays a Jump61 board, and converts mouse clicks
 * on that board to commands that are sent to the current Game.
 *
 * @author Bernie Miao
 */
class BoardWidget extends Pad {

    /**
     * Length of the side of one square in pixels.
     */
    private static final int SQUARE_SIZE = 50;
    /**
     * Width and height of a spot.
     */
    private static final int SPOT_DIM = 8;
    /**
     * Minimum separation of center of a spot from a side of a square.
     */
    private static final int SPOT_MARGIN = 10;
    /**
     * Width of the bars separating squares in pixels.
     */
    private static final int SEPARATOR_SIZE = 3;
    /**
     * Width of square plus one separator.
     */
    private static final int SQUARE_SEP = SQUARE_SIZE + SEPARATOR_SIZE;

    /**
     * Colors of various parts of the displayed board.
     */
    private static final Color
            NEUTRAL = Color.WHITE,
            SEPARATOR_COLOR = Color.BLACK,
            SPOT_COLOR = Color.BLACK,
            RED_TINT = new Color(255, 200, 200),
            BLUE_TINT = new Color(200, 200, 255);

    /**
     * A new BoardWidget that monitors and displays a game Board, and
     * converts mouse clicks to commands to COMMANDQUEUE.
     */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        _side = 6 * SQUARE_SEP + SEPARATOR_SIZE;
        setMouseHandler("click", this::doClick);
    }

    /* .update and .paintComponent are synchronized because they are called
     *  by three different threads (the main thread, the thread that
     *  responds to events, and the display thread).  We don't want the
     *  saved copy of our Board to change while it is being displayed. */

    /**
     * Update my display to show BOARD.  Here, we save a copy of
     * BOARD (so that we can deal with changes to it only when we are ready
     * for them), and recompute the size of the displayed board.
     */
    synchronized void update(Board board) {
        if (board.equals(_board)) {
            return;
        }
        if (_board != null && _board.size() != board.size()) {
            invalidate();
        }
        _board = new Board(board);
        _side = _board.size() * SQUARE_SEP + SEPARATOR_SIZE;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(_side, _side);
    }

    /**
     * Color and display the spots on the square at row R and column C
     * on G.  (Used by paintComponent).
     */
    private ArrayList<Rectangle> rectangles = new ArrayList<>();

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        if (_board == null) {
            return;
        }
        Rectangle rec;
        int width = _board.size();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                rec = new Rectangle(SQUARE_SIZE * i + SEPARATOR_SIZE,
                        SQUARE_SIZE * j + SEPARATOR_SIZE,
                        SQUARE_SIZE, SQUARE_SIZE);
                g.setColor(Color.DARK_GRAY);
                g.draw(rec);
                if (_board.get(i + 1, j + 1).getSide() == RED) {
                    g.setColor(RED_TINT);
                    g.fill(rec);
                } else if (_board.get(i + 1, j + 1).getSide() == BLUE) {
                    g.setColor(BLUE_TINT);
                    g.fill(rec);
                }
                rectangles.add(rec);

                double xCoordinate = rec.getCenterX();
                double yCoordinate = rec.getCenterY();
                int spots = _board.get(i + 1, j + 1).getSpots();
                if (spots == 1) {
                    spot(g, (int) xCoordinate, (int) yCoordinate);
                }
                if (spots == 2) {
                    spot(g, (int) xCoordinate - 10, (int) yCoordinate
                            + 10);
                    spot(g, (int) xCoordinate + 10, (int) yCoordinate
                            - 10);
                }
                if (spots == 3) {
                    spot(g, (int) xCoordinate - 10, (int) yCoordinate
                            + 10);
                    spot(g, (int) xCoordinate, (int) yCoordinate);
                    spot(g, (int) xCoordinate + 10, (int) yCoordinate
                            - 10);
                }
                if (spots == 4) {
                    spot(g, (int) xCoordinate - 10, (int) yCoordinate
                            + 10);
                    spot(g, (int) xCoordinate + 10, (int) yCoordinate
                            + 10);
                    spot(g, (int) xCoordinate - 10, (int) yCoordinate
                            - 10);
                    spot(g, (int) xCoordinate + 10, (int) yCoordinate
                            - 10);
                }

            }
        }
    }

    /**
     * Color and display the spots on the square at row R and column C
     * on G.  (Used by paintComponent).
     */
    private void displaySpots(Graphics2D g, int r, int c) {
        g.setColor(Color.BLACK);
        int desiredNIndex = _board.size();

    }

    /**
     * Draw one spot centered at position (X, Y) on G.
     */
    private void spot(Graphics2D g, int x, int y) {
        g.setColor(SPOT_COLOR);
        g.fillOval(x - SPOT_DIM / 2,
                y - SPOT_DIM / 2, SPOT_DIM, SPOT_DIM);
    }

    /**
     * Draw one spot centered at position (X, Y) on G.
     */
    static final double MAX_DISTANCE = 25.5;

    /**
     * Respond to the mouse click depicted by EVENT.
     */
    public void doClick(String dummy, MouseEvent event) {
        if (_board.getWinner() != null) {
            System.out.println("Winner is found, game has ended");
            return;
        }
        boolean moveSelected = false;
        int x = event.getX() - SEPARATOR_SIZE,
                y = event.getY() - SEPARATOR_SIZE;

        int r = 0;
        int c = 0;
        x = event.getX() - SEPARATOR_SIZE;
        y = event.getY() - SEPARATOR_SIZE;
        for (int i = 0; i < rectangles.size(); i++) {
            Rectangle rec = rectangles.get(i);
            double xCoordinate = rec.getCenterX();
            double yCoordinate = rec.getCenterY();
            assert rectangles.size() == _board.size() * _board.size();
            System.out.println("Mouse click at: x = " + x + " y = " + y);

            if (y > yCoordinate - (SQUARE_SIZE / 2)
                    && y < yCoordinate + (SQUARE_SIZE / 2)) {
                if (x > xCoordinate - (SQUARE_SIZE / 2)
                        && x < xCoordinate + (SQUARE_SIZE / 2)) {
                    r = _board.row(i);
                    c = _board.col(i);
                    System.out.println("This is close to square : row = "
                            + r + " col = " + c);
                    System.out.println("Which is located at  : xCoordinate = "
                            + xCoordinate + " Ycoodrinate = " + yCoordinate);
                    break;
                }
            }

        }
        if (r == 0 || c == 0) {
            System.out.println("Computation error, no suitable squares found, "
                    + "fix your distance formula bro");
            return;
        }
        _commandQueue.offer(String.format("%d %d", r, c));
    }

    /**
     * The Board I am displaying.
     */
    private Board _board;
    /**
     * Dimension in pixels of one side of the board.
     */
    private int _side;
    /**
     * Destination for commands derived from mouse clicks.
     */
    private ArrayBlockingQueue<String> _commandQueue;
}
