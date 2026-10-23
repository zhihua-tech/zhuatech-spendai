/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.spendai.practical;

import static cn.zhuatech.spendai.practical.PracticalInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class PracticalService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "currency", "topN", "lines");
    String currency = text(r, "currency", 3);
    if (!currency.matches("[A-Z]{3}")) throw new IllegalArgumentException("币种须为3位大写代码");
    int top = integer(r, "topN", 1, 20);
    Set<String> ids = new HashSet<>();
    Map<String, BigDecimal> suppliers = new TreeMap<>();
    BigDecimal total = BigDecimal.ZERO;
    for (var line : rows(r, "lines", 1, 500)) {
      keys(line, "id", "supplier", "amount", "currency");
      unique(ids, id(line, "id"));
      String supplier = id(line, "supplier");
      if (!currency.equals(text(line, "currency", 3)))
        throw new IllegalArgumentException("禁止混合币种汇总");
      var amount = money(line, "amount");
      suppliers.merge(supplier, amount, BigDecimal::add);
      total = total.add(amount);
    }
    var order = new ArrayList<>(suppliers.entrySet());
    order.sort(
        Map.Entry.<String, BigDecimal>comparingByValue()
            .reversed()
            .thenComparing(Map.Entry.comparingByKey()));
    List<Map<String, Object>> out = new ArrayList<>();
    BigDecimal topAmount = BigDecimal.ZERO;
    double hhi = 0;
    for (int i = 0; i < order.size(); i++) {
      var entry = order.get(i);
      double share =
          total.signum() == 0
              ? 0
              : entry.getValue().divide(total, 16, RoundingMode.HALF_UP).doubleValue();
      hhi += share * share * 10000;
      if (i < top) topAmount = topAmount.add(entry.getValue());
      out.add(
          Map.of(
              "supplier",
              entry.getKey(),
              "amount",
              entry.getValue(),
              "sharePercent",
              round(share * 100)));
    }
    return Map.of(
        "currency",
        currency,
        "totalSpend",
        total,
        "sharesDefined",
        total.signum() > 0,
        "suppliers",
        out,
        "hhi",
        round(hhi),
        "topNPercent",
        total.signum() == 0
            ? BigDecimal.ZERO
            : topAmount.multiply(new BigDecimal("100")).divide(total, 6, RoundingMode.HALF_UP),
        "automaticRiskDecision",
        false);
  }
}
