package com.jd.ads.platform.misc.generator;

/**
 * MsgKeyGenerator
 *
 * @author yuweiwei3@jd.com
 * @date 2019-06-26
 */
public class MsgKeyGenerator implements Generator {

    private static final String KEY_CHARS = "0123456789";

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
        char[] chars = new char[keyLen];
        int csLen = input.length();
        int csIndex = 0;
        while (csIndex < csLen) {
            for (int i = 0; i < keyLen && csIndex < csLen; i++, csLen++) {
                chars[i] ^= input.charAt(csIndex);
            }
        }

        // 生成 key
        int keyCharLen = KEY_CHARS.length();
        for (int i = 0; i< keyLen; i++) {
            chars[i] = KEY_CHARS.charAt(chars[i] % keyCharLen);
        }
        return new String(chars);
    }
}
