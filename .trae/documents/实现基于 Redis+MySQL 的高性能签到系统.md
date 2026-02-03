## 签到系统细节完善方案

### 1. 功能梳理与补齐
- **幂等性保障**：Service 层增加 `LocalDate` 维度的数据库查重。
- **连续签到逻辑**：
  - 维护 `continuous_days` 逻辑：判断最后一次签到日期是否为 `LocalDate.now().minusDays(1)`。
- **阶梯奖励机制**：
  - 连签 7 天：额外奖励 20 积分。
  - 连签 30 天：额外奖励 100 积分。
- **积分系统深度集成**：
  - 封装 `updateUserPoints` 事务方法，同步更新 `user.points` 并产生 `points_record` 流水。

### 2. 接口增强
- **POST `/points/sign-in`**: 
  - 返回对象改为 `SignInResultVO` (包含：本次奖励、当前总积分、连续天数、是否触发连签奖励)。
- **GET `/points/sign-in/month` (新增)**: 
  - 返回当前用户本月所有已签到的日期列表，供前端日历组件渲染。

### 3. 代码结构调整
- **Constants**: 在 `PointsConstants` 中定义连签奖励数值。
- **VO**: 创建 `SignInResultVO` 用于结果展示。
- **Service**: 在 `UserSignInServiceImpl` 中实现上述所有计算逻辑。

### 4. 验证要点
- 验证跨月连续签到是否正常计算。
- 验证断签后连续天数是否正确重置为 1。
- 验证并发请求下（虽然有限流）数据库层面是否能保持唯一性。