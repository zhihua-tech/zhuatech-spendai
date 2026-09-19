/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.spendai;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import static cn.zhuatech.spendai.Model.*;
import static cn.zhuatech.spendai.Engine.*;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 private final InsightProvider insight;
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Domain(InsightProvider insight){this.insight=insight;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String text(Row r,String key){return txt(r.data(),key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void exactMoney(Map<String,Object>d,String key){require(num(d,key).stripTrailingZeros().scale()<=2,"金额最多两位小数");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){
  if(module.equals("suppliers")){exactMoney(d,"baselineAmount");require(e.all(u,"suppliers").stream().noneMatch(x->text(x,"taxId").equalsIgnoreCase(txt(d,"taxId"))),"供应商信用代码重复");}
  if(module.equals("expenses")){Row supplier=e.ref(u,d,"supplier","suppliers");require(supplier.state().equals("ACTIVE"),"供应商未启用");require(!date(d,"spendDate").isAfter(LocalDate.now()),"支出日期不能是未来");exactMoney(d,"amount");}
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("suppliers")){exactMoney(d,"baselineAmount");require(e.all(u,"suppliers").stream().noneMatch(x->!x.id().equals(r.id())&&text(x,"taxId").equalsIgnoreCase(txt(d,"taxId"))),"供应商信用代码重复");}
  if(r.module().equals("expenses")){e.ref(u,d,"supplier","suppliers");require(!date(d,"spendDate").isAfter(LocalDate.now()),"支出日期不能是未来");exactMoney(d,"amount");}
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  switch(r.module()+"."+action){
   case "expenses.analyze" -> {
    Row supplier=e.ref(u,d,"supplier","suppliers");
    var report=insight.analyze(r,supplier,e.all(u,"expenses"));
    e.ledger(u,"analyses","RECORDED",Map.of("expense",r.id(),"supplier",supplier.id(),"category",report.category(),"riskScore",report.riskScore(),"reasons",report.reasons(),"method","LOCAL_RULES_V1","analyzedAt",Instant.now().toString()));
    d.put("category",report.category());d.put("riskScore",report.riskScore());d.put("riskReasons",report.reasons());d.put("analyzedAt",Instant.now().toString());d.put("method","LOCAL_RULES_V1");
    return report.riskScore()>=50?"REVIEW":"CLEARED";
   }
   case "expenses.approve" -> {d.put("reviewedBy",u.username());d.put("reviewedAt",Instant.now().toString());}
   case "expenses.reject" -> {d.put("rejectReason",txt(i,"reason"));d.put("reviewedBy",u.username());}
   case "expenses.post" -> {require(d.containsKey("analyzedAt"),"尚未完成支出分析");d.put("postedAt",Instant.now().toString());d.put("postedBy",u.username());}
  }
  return null;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){var expenses=e.all(u,"expenses");BigDecimal posted=expenses.stream().filter(x->x.state().equals("POSTED")).map(x->num(x.data(),"amount")).reduce(BigDecimal.ZERO,BigDecimal::add);return Map.of("待复核异常",expenses.stream().filter(x->x.state().equals("REVIEW")).count(),"已入账笔数",expenses.stream().filter(x->x.state().equals("POSTED")).count(),"已入账金额",money(posted));}
}
