package com.jd.ads.platform.misc.encounter;

/**
 * LongEncounter
 *
 * @author Yuvv
 * @date 2019/6/23
 */
public class LongEncounter implements Encounter {

    private Long count;

    public LongEncounter() {
        this(0L);
    }

    public LongEncounter(Long count) {
        this.count = count;
    }

    @Override
    public Long getCount() {
        return count;
    }

    @Override
    public Long increase() {
        return increase(1L);
    }

    @Override
    public Long decrease() {
        return increase(-1L);
    }

    @Override
    public Long increase(Long q) {
        count += q;
        return count;
    }

    @Override
    public Long decrease(Long q) {
        return increase(-q);
    }
}
