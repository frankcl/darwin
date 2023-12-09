package xin.manong.darwin.service.notify;

import xin.manong.weapon.base.common.Context;

/**
 * 完成通知
 *
 * @author frankcl
 * @date 2023-12-09 16:10:20
 */
public interface CompleteNotifier<E> {

    /**
     * 事件完成通知
     *
     * @param e 完成事件对象
     * @param context 上下文对象
     */
    void onComplete(E e, Context context);
}
