package com.jd.ads.platform.misc;

/**
 * Encounter
 *
 * @author Yuvv
 * @date 2019/6/23
 */
public class Encounter {

    private Long count;

    public Encounter() {
        this(0L);
    }

    public Encounter(Long count) {
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public long increase() {
        return increase(1L);
    }

    public long decrease() {
        return increase(-1L);
    }

    public long increase(Long q) {
        synchronized (count) {
            count += q;
        }
        return count;
    }

    public long decrease(Long q) {
        return increase(-q);
    }
}
