package xin.manong.darwin.common.model;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 范围数据
 *
 * @author frankcl
 * @date 2023-03-21 19:46:31
 */
@Setter
@Accessors(chain = true)
public class RangeValue<T extends Number> {

    public boolean includeLower = false;
    public boolean includeUpper = false;
    public T start;
    public T end;
}
