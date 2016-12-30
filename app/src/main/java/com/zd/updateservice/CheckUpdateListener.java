package com.zd.updateservice;

public interface CheckUpdateListener<T> {
    /**
     * 当检查更新有结果时调用
     *
     * @param result 检查结果
     * @return True 如果想要消费掉此结果，就返回True.
     */
    boolean onCheckResult(T result);
}