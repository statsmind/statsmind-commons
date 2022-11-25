/*
 * Copyright (c) 2019. 北京指南科技有限公司
 * 版权所有，未经授权，不得擅自复制和传播，公司保留所有诉讼权利。
 * Email: james@zhinantech.com
 * Website: http://www.zhinantech.com/
 */

package com.statsmind.commons;

public final class Status {
    /**
     * 正常数据
     */
    public final static int AVAILABLE = 1;

    /**
     * 数据已失效，不常用
     */
    public final static int UNAVAILABLE = 2;

    /**
     * 私有数据
     */
    public final static int PRIVATE = 3;

    /**
     * 待审核待处理数据
     */
    public final static int PENDING = 4;

    /**
     * 已删除数据
     */
    public final static int REMOVED = 5;

    /**
     * 已锁定数据，不可修改
     */
    public final static int LOCKED = 6;

    /**
     * 由于其他数据删除导致此数据删除
     */
    public final static int AUTO_REMOVED = 7;

    /**
     * 数据已签名
     */
    public final static int SIGNED = 8;

    /**
     * 数据冻结，不能修改
     */
    public final static int FROZEN = 9;

    /**
     * 数据来源已确定
     */
    public final static int SOURCE_DATA_VERIFICATION = 10;

    /**
     * 数据被重置
     */
    public final static int RESET = 11;
}
