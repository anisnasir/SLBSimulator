package slb;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

public class ServerStatsTest {
    private static ServerStats stats = new ServerStats();

    @Test
    public void testUpdate() {
        // double numWords = 1E6;
        double numWords = 1E3;
        for (int i = 0; i < numWords; i++) {
            String word = RandomStringUtils.randomAscii(100);
            stats.update(0, word);
        }
        assertEquals(numWords, stats.dictionarySize(), numWords * ServerStats.RELATIVE_STANDARD_DEVIATION);
        // System.out.println("Set cardinality (1M) = " + stats.dictionary.cardinality());
        // System.out.println("Counter size = " + stats.dictionary.sizeof());
    }
}
