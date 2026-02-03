# 获取脚本所在目录的绝对路径
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# 获取脚本所在目录的父目录名称
IMAGE_NAME=$(basename $(dirname "$DIR") | tr '[:upper:]' '[:lower:]')
# 输出父目录名称
echo "当前打包目录为 ${DIR}"
echo "当前打包服务名为: ${IMAGE_NAME}"
echo "复制 ${DIR}/../target/app.jar 到 ${DIR}/app.jar "
cp ${DIR}/../target/app.jar ${DIR}

echo "镜像 ${IMAGE_NAME} 打包中..."
docker build -t ${IMAGE_NAME} ${DIR}
docker tag ${IMAGE_NAME} registry.cn-hangzhou.aliyuncs.com/mai-gen/${IMAGE_NAME}
docker push registry.cn-hangzhou.aliyuncs.com/mai-gen/${IMAGE_NAME}
echo "已上传至阿里云镜像仓库：registry.cn-hangzhou.aliyuncs.com/mai-gen/${IMAGE_NAME}"

REMOTE_COMMAND="
cd /home/mai/maigen/slave &&
docker pull registry.cn-hangzhou.aliyuncs.com/mai-gen/${IMAGE_NAME} &&
docker-compose up -d --build ${IMAGE_NAME}
"

#需要推送的服务器列表
#SERVERS=(
#    "root@139.224.222.200"
##    "root@47.103.111.151"
#    "root@118.178.130.175"
#)
#echo "开始执行远程命令"
## 读取服务器列表并进行操作
#for SERVER in "${SERVERS[@]}"; do
#    echo "在服务器： $SERVER 执行命令中"
#
#    # 执行命令
#    ssh "$SERVER" "$REMOTE_COMMAND"
#done
#echo "操作完成"