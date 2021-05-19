package com;

/**
 * @Author: yaozh
 * @Description:
 */
public class ExceptionUtil {
    private ExceptionUtil() {

    }

    public static void mockException() {
        if (System.currentTimeMillis() > 1) {
            throw new RuntimeException("人为模拟异常，判断当前逻辑是否处理正常！");
        }
    }
}
