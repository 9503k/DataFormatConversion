package zx.code.utils.strategy.Impl;

import zx.code.utils.strategy.AbstructFilterString;
import zx.code.utils.strategy.FilterStrategy;

/**
 * @author: loubaole
 * @date: 2023/11/16 13:54
 * @@Description: 前缀过滤
 */
public class PrefixFilterStrategy extends AbstructFilterString {


    public PrefixFilterStrategy(String filterName){
        super.filterString = filterName;
    }

    @Override
    public Boolean Filter(String fileName) {
        return fileName.startsWith(filterString);
    }
}
