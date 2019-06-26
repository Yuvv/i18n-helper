package com.jd.ads.platform.misc.generator;

/**
 * MsgKeyGenerator
 *
 * @author yuweiwei3@jd.com
 * @date 2019-06-26
 */
public class MsgKeyGenerator implements Generator {

    private static final String KEY_CHARS = "0123456789";
    private static final String KEY_PREFIX = "SAMPLE";

    private int keyLen;

    public MsgKeyGenerator() {
        // 默认长为 12
        this(12);
    }

    public MsgKeyGenerator(int keyLen) {
        this.keyLen = keyLen;
    }

    @Override
    public String generate(CharSequence input) {
        return KEY_PREFIX + input.hashCode();
    }
}
