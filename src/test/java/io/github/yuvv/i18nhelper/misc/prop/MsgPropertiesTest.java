package io.github.yuvv.i18nhelper.misc.prop;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * MsgPropertiesTest
 *
 * @author Yuvv
 * @date 2021/1/4
 */
public class MsgPropertiesTest {

    @Test
    public void testLoad() throws IOException {
        MsgProperties msgProperties = new MsgProperties();
        msgProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("msg.properties"));

        // test case
        assertFalse("应该加载完成", msgProperties.isEmpty());
        assertEquals("应该有4条记录", msgProperties.size(), 4);
        assertTrue("应该包含多个该记录", msgProperties.containsKey("parameter.nonnull.businessSource"));
    }

    @Test
    public void testStore() throws IOException {
        MsgProperties msgProperties = new MsgProperties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        msgProperties.load(classLoader.getResourceAsStream("msg.properties"));

        // test case
        msgProperties.store(new FileOutputStream(new File("msg-output.properties")));
    }
}