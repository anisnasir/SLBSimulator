package slb;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;

import org.junit.Test;

import com.gihub.gdfm.shobaidogu.IOUtils;

public class StreamItemReaderTest {

    @Test
    public void testNextItem() throws IOException {
        BufferedReader testInput = IOUtils.getBufferedReader("/tweets.txt");
        StreamItemReader sir = new StreamItemReader(testInput);
        StreamItem item;
        int lines = 0, words = 0;
        while ((item = sir.nextItem()) != null) {
            lines++;
            words += item.getWordsSize();
        }
        assertEquals(10, lines);
        assertEquals(86, words);
    }
}
