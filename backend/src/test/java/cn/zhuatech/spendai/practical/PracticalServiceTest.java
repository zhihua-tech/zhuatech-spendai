/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.spendai.practical;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.util.*;
import org.junit.jupiter.api.Test;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
class PracticalServiceTest {
  private final PracticalService service = new PracticalService();

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private Map<String, Object> sample() {
    return Map.<String, Object>ofEntries(
        Map.entry("currency", "CNY"),
        Map.entry("topN", 1),
        Map.entry(
            "lines",
            List.of(
                Map.<String, Object>ofEntries(
                    Map.entry("id", "L1"),
                    Map.entry("supplier", "A"),
                    Map.entry("amount", 60),
                    Map.entry("currency", "CNY")),
                Map.<String, Object>ofEntries(
                    Map.entry("id", "L2"),
                    Map.entry("supplier", "B"),
                    Map.entry("amount", 40),
                    Map.entry("currency", "CNY")))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void computesActualFeatureResult() {
    var result = service.execute(sample());
    assertEquals(new BigDecimal("5.2E+3"), result.get("hhi"));
    assertEquals(new BigDecimal("60.000000"), result.get("topNPercent"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsFeatureBoundary() {
    var invalid = new HashMap<>(sample());
    invalid.put("topN", 0);
    assertThrows(IllegalArgumentException.class, () -> service.execute(invalid));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary1() {
    var r = new HashMap<>(sample());
    r.put("lines", List.of(Map.of("id", "X", "supplier", "A", "amount", 0, "currency", "CNY")));
    assertEquals(false, service.execute(r).get("sharesDefined"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary2() {
    var r = new HashMap<>(sample());
    r.put("lines", List.of(Map.of("id", "X", "supplier", "A", "amount", 1, "currency", "USD")));
    assertThrows(IllegalArgumentException.class, () -> service.execute(r));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary3() {
    var r = new HashMap<>(sample());
    r.put("lines", List.of(Map.of("id", "X", "supplier", "A", "amount", 100, "currency", "CNY")));
    assertEquals(new BigDecimal("1E+4"), service.execute(r).get("hhi"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary4() {
    var r = new HashMap<>(sample());
    r.put("topN", 20);
    assertEquals(new BigDecimal("100.000000"), service.execute(r).get("topNPercent"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void deterministicWithoutMutatingInput() {
    var input = sample();
    String before = input.toString();
    assertEquals(service.execute(input), service.execute(input));
    assertEquals(before, input.toString());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsNull() {
    assertThrows(IllegalArgumentException.class, () -> service.execute(null));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsMissingFields() {
    assertThrows(IllegalArgumentException.class, () -> service.execute(Map.of()));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsUnknownFields() {
    var input = new HashMap<>(sample());
    input.put("__unknown", true);
    assertThrows(IllegalArgumentException.class, () -> service.execute(input));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsOversizedText() {
    assertThrows(
        IllegalArgumentException.class, () -> service.execute(Map.of("text", "x".repeat(64001))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsDeepInput() {
    Map<String, Object> input = Map.of("v", 1);
    for (int i = 0; i < 14; i++) input = Map.of("v", input);
    final var value = input;
    assertThrows(IllegalArgumentException.class, () -> service.execute(value));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsExtremeNumberScale() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PracticalInput.decimal(Map.of("v", new BigDecimal("1e-1000000")), "v", 0, 1));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PracticalInput.decimal(
                Map.of("v", new BigDecimal("0.1234567890123456789012345678901")), "v", 0, 1));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsUnsupportedType() {
    assertThrows(
        IllegalArgumentException.class, () -> PracticalInput.validate(Map.of("v", new Object())));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsOversizedArray() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PracticalInput.validate(Map.of("v", Collections.nCopies(501, 1))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsNormalizedBlank() {
    assertThrows(IllegalArgumentException.class, () -> PracticalInput.normalize("\u00a0"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void parserPreservesDecimalPrecision() {
    var parsed = PracticalInput.parseJson("{\"v\":0.123456789012}");
    assertEquals(new BigDecimal("0.123456789012"), parsed.get("v"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void parserRejectsDuplicateFields() {
    assertThrows(
        IllegalArgumentException.class, () -> PracticalInput.parseJson("{\"v\":1,\"v\":2}"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void decimalResponsesAreLosslessStrings() {
    var safe =
        (Map<?, ?>)
            PracticalInput.jsonSafe(Map.of("amount", new BigDecimal("9007199254740993.01")));
    assertEquals("9007199254740993.01", safe.get("amount"));
  }
}
