package slb;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class StreamItemTest {
    private static final StreamItem item = new StreamItem(10, Arrays.asList("Hello", "world", "!"));

    @Test
    public void testGetWordsSize() {
        assertEquals(3, item.getWordsSize());
    }

    @Test
    public void testToString() {
        assertEquals("[Hello, world, !]", item.toString());
    }
}
