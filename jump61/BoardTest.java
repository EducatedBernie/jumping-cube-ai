
package jump61;

import static jump61.Side.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests of Boards.
 *
 * @author Bernie Miao
 */

public class BoardTest {
    private static final String NL = System.getProperty("line.separator");

    @Test
    public void simple2x2() {
        Board B = new Board(2);
        System.out.println("Upon initialization, 1, 2, has value:");
        System.out.println(B.get(1, 2).getSpots());
        B.addSpot(RED, 1, 1);
        B.addSpot(BLUE, 1, 2);
        System.out.println("after add , 1, 2, has value:");
        System.out.println(B.get(1, 2).getSpots());
        System.out.println(B.getWinner());
        B.addSpot(RED, 2, 1);
        System.out.println("Before second add, 1, 2, has value:");
        System.out.println(B.getWinner());
        B.addSpot(BLUE, 1, 2);
        System.out.println(B.get(1, 2).getSpots());
        System.out.println("After win, then adding, 1, 2, has value");
        System.out.println(B.get(1, 2).getSpots());
        System.out.println(B.getWinner());
    }

    @Test
    public void testG() {
        Board b = new Board(3);
        b.set(1, 1, 2, RED);
        b.set(1, 2, 3, BLUE);
        b.set(2, 1, 1, BLUE);
        b.set(2, 2, 3, RED);
        b.set(2, 3, 3, RED);
        b.set(3, 1, 2, RED);
        b.set(3, 2, 2, RED);
        b.set(3, 3, 2, BLUE);
        System.out.println(b.toString());
        b.addSpot(BLUE, 3, 3);
        System.out.println("Get winner " + b.getWinner());
    }

    @Test
    public void jumpTest1() {
        Board B = new Board(3);
        B.set(1, 1, 1, BLUE);
        B.set(1, 2, 3, RED);
        B.set(1, 3, 2, BLUE);
        B.set(2, 1, 3, RED);
        B.set(2, 2, 3, RED);
        B.set(2, 3, 1, BLUE);
        B.set(3, 1, 1, BLUE);
        B.set(3, 2, 3, BLUE);
        B.set(3, 3, 1, BLUE);
        B.addSpot(BLUE, 1, 1);
        System.out.println(B.toString());
        System.out.println("Making move [n = 3]");
        B.addSpot(RED, 2, 1);
        System.out.println(B.toString());
        System.out.println(B.getWinner());
    }


    @Test
    public void testSize() {
        Board B = new Board(5);
        assertEquals("bad length", 5, B.size());
        ConstantBoard C = new ConstantBoard(B);
        assertEquals("bad length", 5, C.size());
        Board D = new Board(C);
        assertEquals("bad length", 5, D.size());
    }

    @Test
    public void testSet() {
        Board B = new Board(5);
        B.set(2, 2, 1, RED);
        assertEquals("wrong number of spots", 1, B.get(2, 2).getSpots());
        assertEquals("wrong color", RED, B.get(2, 2).getSide());
        assertEquals("wrong count", 1, B.numOfSide(RED));
        assertEquals("wrong count", 0, B.numOfSide(BLUE));
        assertEquals("wrong count", 24, B.numOfSide(WHITE));
    }

    @Test
    public void constantBoardCopyTest() {
        Board B = new Board(5);
        B.addSpot(RED, 1, 1);
        B.addSpot(RED, 2, 2);
        B.addSpot(RED, 3, 3);
    }

    @Test
    public void testMove() {
        Board B = new Board(6);
        checkBoard("#0", B);
        B.addSpot(RED, 1, 1);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
        B.undo();
        checkBoard("#2U", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.undo();
        checkBoard("#1U", B, 1, 1, 2, RED);
        B.undo();
        checkBoard("#0U", B);
    }

    /**
     * Checks that B conforms to the description given by CONTENTS.
     * CONTENTS should be a sequence of groups of 4 items:
     * r, c, n, s, where r and c are row and column number of a square of B,
     * n is the number of spots that are supposed to be there and s is the
     * color (RED or BLUE) of the square.  All squares not listed must
     * be WHITE with one spot.  Raises an exception signaling a unit-test
     * failure if B does not conform.
     */
    private void checkBoard(String msg, Board B, Object... contents) {
        for (int k = 0; k < contents.length; k += 4) {
            String M = String.format("%s at %d %d", msg, contents[k],
                    contents[k + 1]);
            assertEquals(M, (int) contents[k + 2],
                    B.get((int) contents[k],
                            (int) contents[k + 1]).getSpots());
            assertEquals(M, contents[k + 3],
                    B.get((int) contents[k],
                            (int) contents[k + 1]).getSide());
        }
        int c;
        c = 0;
        for (int i = B.size() * B.size() - 1; i >= 0; i -= 1) {
            assertTrue("bad white square #" + i,
                    (B.get(i).getSide() != WHITE)
                            || (B.get(i).getSpots() == 1));
            if (B.get(i).getSide() != WHITE) {
                c += 1;
            }
        }
        assertEquals("extra squares filled", contents.length / 4, c);
    }

}
