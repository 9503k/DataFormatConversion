package zx.code.utils.strategy.Impl;

import zx.code.utils.strategy.AbstructFilterString;
import zx.code.utils.strategy.FilterStrategy;

/**
 * @author: loubaole
 * @date: 2023/11/16 13:56
 * @@Description: 相等过滤
 */
public class EqualFilterStrategy extends AbstructFilterString {


    public EqualFilterStrategy(String filterName){
        super.filterString = filterName;
    }

    @Override
    public Boolean Filter(String fileName) {
        return null;
    }

}
