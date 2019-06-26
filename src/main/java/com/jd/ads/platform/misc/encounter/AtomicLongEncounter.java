package com.jd.ads.platform.misc.encounter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * AtomicLongEncounter
 *
 * @author yuweiwei3@jd.com
 * @date 2019-06-26
 */
public class AtomicLongEncounter implements Encounter {

    private AtomicLong count;

    public AtomicLongEncounter() {
        this(0L);
    }

    public AtomicLongEncounter(Long count) {
        this.count = new AtomicLong(count);
    }

    @Override
    public Long getCount() {
        return count.get();
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
        return count.addAndGet(q);
    }

    @Override
    public Long decrease(Long q) {
        return increase(-q);
    }
}
