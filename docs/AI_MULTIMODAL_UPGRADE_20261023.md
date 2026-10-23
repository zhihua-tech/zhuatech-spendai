# 供应商支出集中度分析

> 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。

## 已实现

汇总实际支出行，计算供应商份额、Top N占比和HHI，零支出明确不可计算，不编造采购风险结论。

最多500支出行，非负且最多2位小数，单行≤10^12；严格单币种、不做汇率换算或退款净额处理。汇总后按金额降序/供应商编号排序，HHI=sum(份额×100)^2。0总额时sharesDefined=false，HHI/占比0仅为占位；不套用竞争法或风险评级阈值。

## 入口与身份

从原工作台点击“专项业务工具”，或访问同源`/practical.html`。原页面与架构保留，不替换为新框架。

沿用现有工作台数据库会话，后端通过Auth.current验证账号启用状态及会话有效期；不接受请求自报角色，不绕过停用/注销。请求时从原sessionStorage读取当前会话，不保存额外账号密码。



接口`POST /api/practical/supplier-spend-concentration`：JSON请求，`Authorization: Bearer <配置令牌或当前会话>`。200返回`success:true,data`；凭据缺失/无效401；参数错误400。不读取其他租户记录、不读取文件、不执行命令、不上传第三方模型、不实际审批/发布/发信/记账。

## 合成样例

```json
{
  "currency": "CNY",
  "topN": 1,
  "lines": [
    {
      "id": "L1",
      "supplier": "A",
      "amount": 60,
      "currency": "CNY"
    },
    {
      "id": "L2",
      "supplier": "B",
      "amount": 40,
      "currency": "CNY"
    }
  ]
}
```

## 参数与显示限制

- 页面按UTF-8字节限制128 KiB，超时15秒，无离线成功兜底，后端异常会直接提示。
- 后端校验最大12层、10000节点、总文本128000字符、单文本64000字符；通用数值≤30位有效数字、尺度绝对值≤12。各业务数组和文本有更小上限，详见实现。
- 日期限定1900–2200；金额只接受非负且最多2位小数；不暗中换算币种或忽略非法记录。
- 字段未知、标识重复和错误引用按各业务规则明确拒绝；所有结果来自本次输入，不改变输入对象。
- 结果表预览最多100行；JSON导出包含完整数据。
- 返回字符串使用DOM.textContent显示，来源文字不注入HTML。真实令牌不写浏览器持久存储、不进入导出文件。
- 服务端校验在JSON解析之后；生产网关仍需HTTPS、请求体上限、速率限制、日志脱敏和明确的访问控制，不能用这些校验替代网关防护。

## 测试与部署

Java21：`cd backend && mvn test`，包含原有用例、新算法单元和真实随机端口HTTP测试。新增接口测试覆盖匿名/错误凭据、有效调用、参数缺失、未知字段、非法JSON、数组根请求及已注销会话拒绝。

前端：`node --test frontend/test/practical-format.test.mjs`；保持原Vue工具链并运行`cd frontend && npm ci && npm test && npm run build`。

本次未增加数据库迁移。5个业务项目沿用原MySQL部署及初始化方式；轻量H5保持原有演示边界，不因此被描述成完整企业生产系统。完整执行结果以本批交付报告为准，不以本说明代替测试证据。

## Scope

Supplier spend concentration analysis is implemented as a deterministic computation over caller-provided, authorized data. This endpoint does not call external inference, fetch URLs, inspect private files or persist workflow decisions. Algorithm-specific limits above remain part of its contract. Existing source-available/non-commercial licensing, website and contacts are preserved.

### 新接口JSON精度防护

新增接口单独使用BigDecimal解析小数，拒绝重复JSON字段和尾随JSON对象；不修改旧接口的全局解析配置。数字在业务计算前统一校验精度/尺度，极端指数不能先下溢成0再进入金额或数量计算。前端只检查JSON语法和根对象，发送用户输入原文，不对数值重编码；后端响应内的BigDecimal统一以十进制字符串表示，避免浏览器解析和导出时丢失金额尾数。整数计数和布尔字段仍保持原类型。
