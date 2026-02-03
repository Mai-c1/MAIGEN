这份技术文档旨在指导 **MAIGEN-sandbox** 模块通过 **go-judge (executorserver)** 实现高性能、零 Base64 开销的数据生成逻辑。

---

## 技术规范：MAIGEN 自动化数据生成方案 (V2.0)

### 1. 核心设计原理

方案利用 `go-judge` 的 **文件系统挂载 (Mounts)** 功能，将沙箱内部的打包产物直接写入宿主机的共享物理目录。通过这种方式，避开了大数据量下的 Base64 编码损耗，利用本地 I/O 实现最高并发性能。

### 2. 环境配置要求

* **共享目录结构**:
* `/maigen/share/judge`: 沙箱工作底座 (启动参数 `-dir` 指向此处)。
* `/maigen/share/data`: 数据交付目录 (需执行 `chmod 777` 确保沙箱可写)。


* **依赖预装**: 容器内需预装 `g++`, `python3`, `pip3 install cyaron`, `zip`

---

### 3. 沙箱内部指挥脚本 (`run.sh`)

此脚本作为 `copyIn` 的一部分传入沙箱，负责调度整个生成流程。

```bash
#!/bin/bash
# 遇到任何命令失败立即终止并返回非0状态码
set -e

# 1. 编译标准程序
# 报错信息将通过 stderr 汇聚给 MAIGEN-sandbox
g++ std.cpp -o std.exe -O2

# 2. 创建临时存放目录
mkdir -p data

# 3. 生成输入数据 (.in)
# 确保 gen.py 内部配置生成路径为 ./data/
python3 gen.py

# 4. 运行标程生成输出数据 (.out)
for in_file in data/*.in; do
    [ -e "$in_file" ] || continue
    ./std.exe < "$in_file" > "data/$(basename "$in_file" .in).out"
done

# 5. 打包并直出物理路径
# 直接打包到挂载点 /output/，映射宿主机的 /maigen/share/data
# 此处TASK_ID在编写命令时动态拼接
zip -j "/output/data_{TASK_ID}.zip" data/*

```

---

### 4. API 请求报文规范 (POST `/run`)
待补充
---

### 5. MAIGEN-sandbox 完整处理逻辑

1. **任务触发**: 从 MQ 获取 `taskId`，从 Redis 获取 `std.cpp` 和 `gen.py`。
2. **构造请求**: 将代码填充至 JSON，动态拼接最终的路径"/output/data_{TASK_ID}.zip"
3. **调用 API**: 发送同步请求至 `go-judge`。
4. **结果校验**:
* 若响应中 `exitStatus != 0`：读取 `files.stderr` 的内容，反馈编译或运行错误。
* 若响应成功：进入步骤 5。


5. **数据外发**:
* 访问物理路径 `/maigen/share/data/data_1024.zip`。
* 调用 MinIO SDK 流式上传该文件。

6. **资源清理**: 执行 `os.Remove("/maigen/share/data/data_1024.zip")`。

---

### 6. 异常处理对照表

| 错误阶段 | 现象 | 判定依据 |
| --- | --- | --- |
| **编译失败** | `stderr` 包含 "error:" | `run.sh` 退出，返回值非 0 |
| **生成器错误** | `stderr` 包含 Python Traceback | `run.sh` 退出，返回值非 0 |
| **挂载失败** | `stderr` 包含 "Read-only file system" | 检查 `mounts` 里的 `rw` 标志 |
| **超时** | 响应状态为 `Time Limit Exceeded` | 调大 `cpuLimit` |

---

**下一步建议：**
由于该方案涉及多个系统路径挂载（`/usr`, `/lib` 等），如果你的容器环境比较特殊，我们可以写一个简单的 **Go 验证程序** 来测试这个 `mounts` 结构是否能正常在你的宿主机环境运行。需要我提供这个测试程序吗？