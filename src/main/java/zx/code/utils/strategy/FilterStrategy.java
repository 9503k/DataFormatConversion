package zx.code.utils.strategy;

/**
 * @author LBL
 * @date 2023/11/16
 * @Describe: 过滤目录的策略接口，
 */
public interface FilterStrategy{
    Boolean Filter(String fileName);
}
