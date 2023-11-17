package zx.code.utils.directory.strategy.Impl;

import zx.code.utils.directory.strategy.AbstructFilterString;

/**
 * @author: loubaole
 * @date: 2023/11/16 13:54
 * @@Description: 包含过滤
 */
public class ContainFilterStrategy extends AbstructFilterString {


    public ContainFilterStrategy(String filterName){
        super.filterString = filterName;
    }

    @Override
    public Boolean Filter(String fileName) {
        return fileName.contains(filterString);
    }
}
