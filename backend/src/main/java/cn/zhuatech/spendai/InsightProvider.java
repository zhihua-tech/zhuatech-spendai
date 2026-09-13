/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.spendai;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import static cn.zhuatech.spendai.Model.*;
import static cn.zhuatech.spendai.Engine.*;

/** 支出分析扩展点；本地规则版可独立完成分类和异常识别。 */
public interface InsightProvider {
 record Insight(String category,int riskScore,List<String> reasons){}
 Insight analyze(Row expense,Row supplier,List<Row> existing);
}
@Component class LocalInsightProvider implements InsightProvider {
 public Insight analyze(Row expense,Row supplier,List<Row> existing){
  String description=txt(expense.data(),"description").toLowerCase(Locale.ROOT);
  String category=description.matches(".*(软件|许可|云|服务器|saas).*")?"SOFTWARE":description.matches(".*(差旅|机票|酒店|交通).*")?"TRAVEL":description.matches(".*(办公|文具|耗材).*")?"OFFICE":"OTHER";
  List<String> reasons=new ArrayList<>();int score=0;
  boolean duplicate=existing.stream().anyMatch(x->!x.id().equals(expense.id())&&txt(x.data(),"supplier").equals(txt(expense.data(),"supplier"))&&txt(x.data(),"invoiceNo").equalsIgnoreCase(txt(expense.data(),"invoiceNo"))&&!x.state().equals("REJECTED"));
  if(duplicate){score+=70;reasons.add("同供应商票据号重复");}
  BigDecimal amount=num(expense.data(),"amount"),baseline=num(supplier.data(),"baselineAmount");
  if(amount.compareTo(baseline.multiply(BigDecimal.valueOf(2)))>0){score+=30;reasons.add("金额超过供应商常规单笔金额两倍");}
  if(reasons.isEmpty())reasons.add("未命中重复票据和金额异常规则");
  return new Insight(category,Math.min(100,score),List.copyOf(reasons));
 }
}
