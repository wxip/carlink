# 车机投屏
## 只在android11的手机上测试过,其余平台请自行测试
## 特性
- 镜像视频与音频(android 11以上)
- 车机控制手机
- 手机黑屏推流
## 使用流程
- 手机端
1. 手机端打开USB调试,USB调试安全模式
2. adb tcpip 5555 (打开网络调试)
3. adb push server/build/outputs/apk/debug/server-debug.apk /data/local/tmp/carlink-server.jar
- 车机端
1. 安装client
2. 输入手机IP,点击连接
3. 开始控制