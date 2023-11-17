package zx.code.utils.directory.strategy.Impl;

import zx.code.utils.directory.strategy.AbstructFilterString;

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
