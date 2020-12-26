package io.github.yuvv.i18nhelper.misc.encounter;

/**
 * Encounter
 *
 * @author Yuvv
 * @date 2019-06-25
 */
public interface Encounter {

    /**
     * 获取当前计数
     *
     * @return 当前计数
     */
    Long getCount();

    /**
     * 计数加 1
     *
     * @return 加 1 之后的计数
     */
    Long increase();

    /**
     * 计数减 1
     *
     * @return 减 1 之后的计数
     */
    Long decrease();

    /**
     * 计数加 {@code q}
     *
     * @return 加 {@code q} 之后的计数
     */
    Long increase(Long q);

    /**
     * 计数减 {@code q}
     *
     * @return 减 {@code q} 之后的计数
     */
    Long decrease(Long q);
}
