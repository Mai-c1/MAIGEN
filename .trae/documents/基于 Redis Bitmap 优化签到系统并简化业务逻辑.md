## 签到系统优化方案：简化逻辑与 Redis Bitmap 集成

### 1. 逻辑简化与常量调整
- **移除周期奖励**：删除 `SIGN_IN_CONTINUOUS_7_REWARD` 等连签奖励常量，统一为固定奖励。
- **简化 VO**：移除 `SignInResultVO` 中关于连签和额外奖励的字段。

### 2. Redis Bitmap 状态控制 (核心改进)
- **精准跨天校验**：
  - 弃用 `@RateLimit`。
  - 使用 Redis Bitmap 存储用户签到状态。Key 格式：`sign_in:status:{userId}:{yyyyMM}`。
  - 签到前通过 `GETBIT` 判断当天位值是否为 1，若为 1 则直接抛出“今天已经签到过了”异常。
  - 签到成功后同步执行 `SETBIT` 将位值设为 1。
- **本月状态查询优化**：
  - `getMonthSignInDays` 接口改为从 Redis Bitmap 读取，通过 `BITFIELD` 一次性获取整月状态，性能极高。

### 3. 积分系统对接完善
- **事务处理**：在 `UserSignInServiceImpl` 中维持事务一致性。
- **流水记录**：继续保留 `points_record` 的生成，但移除其中关于“连签奖励”的描述描述。

### 4. 验证计划
- **跨天测试**：模拟 23:59 签到和 00:01 签到，确保两次都能成功。
- **重复签到测试**：确保当天第二次请求能被 Redis Bitmap 准确拦截。
- **性能验证**：验证本月签到列表接口是否能快速返回。