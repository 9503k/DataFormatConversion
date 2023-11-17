package zx.code.utils.directory.strategy;

/**
 * @author: loubaole
 * @date: 2023/11/16 13:59
 * @@Description:
 */
public abstract class AbstructFilterString implements FilterStrategy {

    protected String filterString;



    public abstract Boolean Filter(String fileName);


}
