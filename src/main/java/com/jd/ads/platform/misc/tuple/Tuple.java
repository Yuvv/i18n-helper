package com.jd.ads.platform.misc.tuple;

/**
 * Tuple
 *
 * @author Yuvv
 * @date 2019/6/23
 */
public class Tuple {

    public static <T1, T2> Tuple2<T1, T2> tuple(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> tuple(T1 v1, T2 v2, T3 v3) {
        return new Tuple3<>(v1, v2, v3);
    }
}
