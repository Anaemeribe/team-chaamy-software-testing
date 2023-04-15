import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AutograderMutationTest {
    private static Autograder ag;

    @BeforeEach
    public void init() {
        ag = new Autograder();
    }

    /**
     * Test to check default constructor sets visibility to default "hidden".
     */
    @Test
    public void testDefaultConstructorVisibility() {
        assertEquals("hidden", ag.visibility);
    }

    /**
     * Test to check default constructor sets score to default 0.1.
     */
    @Test
    public void testDefaultConstructorScore() {
        assertEquals(0.1, ag.maxScore);
    }

    /**
     * Test to check constructor properly sets valid visibility.
     * Valid visibility is [0, 3].
     */
    @Test
    public void testConstructorVisibility() {
        ag = new Autograder(0, 0);
        assertEquals("visible", ag.visibility);

        ag = new Autograder(1, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(2, 0);
        assertEquals("after_due_date", ag.visibility);

        ag = new Autograder(3, 0);
        assertEquals("after_published", ag.visibility);
    }


    /**
     * Test to check constructor properly sets visibility to default given invalid
     * visibility.
     */
    @Test
    public void testConstructorInvalidVisibility() {
        Autograder ag = new Autograder(-1, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(4, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(Integer.MAX_VALUE, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(Integer.MIN_VALUE, 0);
        assertEquals("hidden", ag.visibility);
    }

    /**
     * Test to check constructor properly sets valid score.
     * Valid score is a positive score.
     */
    @Test
    public void testConstructorScore() {
        double[] scores = {0.000001, Double.MAX_VALUE, Double.MIN_VALUE, 9.2, 7375, 45372.38674};
        for (double score : scores) {
            ag = new Autograder(0, score);
            assertEquals(score, ag.maxScore);
        }
    }


    /**
     * Test to check constructor properly sets invalid score to default 0.1.
     * Invalid score is non-positive.
     */
    @Test
    public void testConstructorInvalidScore() {
        // Zero fails this test
        double[] scores = {
//                0,
                -Double.MAX_VALUE,
                -Double.MIN_VALUE,
                -9.2,
                -7375,
                -45372.38674
        };
        for (double score : scores) {
            ag = new Autograder(0, score);
            assertEquals(0.1, ag.maxScore);
        }
    }
}
