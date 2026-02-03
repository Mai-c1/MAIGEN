# 使用通过加速器拉取的 JDK 镜像
FROM docker.1ms.run/eclipse-temurin:17-jdk-focal

# 1. 更换 APT 镜像源 (以阿里云为例)
# 这里通过 sed 命令直接修改 Ubuntu 的 sources.list
RUN sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
    sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list

# 2. 安装编译器和工具
# 换源后，这里的速度会提升 10 倍以上
RUN apt-get update && apt-get install -y \
    g++ \
    python3 \
    python3-pip \
    zip \
    libcap2-bin \
    && rm -rf /var/lib/apt/lists/*

# 3. 更换 pip3 镜像源并安装 cyaron
# 使用清华源安装，避免 pip install 卡死
RUN pip3 install --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple && \
    pip3 install cyaron -i https://pypi.tuna.tsinghua.edu.cn/simple

# 4. 准备 MAIGEN 目录结构
RUN mkdir -p /maigen/share/judge /maigen/share/data /etc/go-judge && \
    chmod -R 777 /maigen/share

# 5. 拷贝本地文件
COPY go-judge /usr/local/bin/go-judge
COPY mount.yaml /etc/go-judge/mount.yaml
COPY app.jar /app/app.jar
RUN chmod +x /usr/local/bin/go-judge

# 6. 生成 mount.yaml 配套文件
RUN touch /etc/go-judge/dotenv && \
    echo "root:x:0:0:root:/root:/bin/bash" > /etc/go-judge/containerPasswd.txt

WORKDIR /app
# 7. 启动脚本逻辑
RUN echo '#!/bin/bash\n\
/usr/local/bin/go-judge -dir /maigen/share/judge -mount-conf /etc/go-judge/mount.yaml -http-addr :5050 &\n\
if [ -f "/app/app.jar" ]; then\n\
  exec java -jar /app/app.jar\n\
else\n\
  echo "--- [MAIGEN] go-judge is running, waiting for app.jar ---"\n\
  tail -f /dev/null\n\
fi' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh

EXPOSE 5050 13003
ENTRYPOINT ["/app/entrypoint.sh"]