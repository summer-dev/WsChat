package net.age.chat;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void ss(){
        String[] args = {"hello","world"};
        try {
            ChatServer.main(args);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void peroperty(){
        Map<String,String> mapp = new HashMap<>();
        String pc = System.getenv("COMPUTERNAME");
        Properties p = System.getProperties();
        String m;
    }
}