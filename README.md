java -jar ... 命令没有反应

java环境没有正确配置。在命令行窗口输入java、javac、java -version等命令，命令行都无反应.。
打开cmd（命令行窗口），在命令行中输入where java，可以查看环境变量中的Java环境配置。
自己两个手动配置的环境变量jdk,移动到软件自动配置的两个环境变量的前面。

.getResource("").getPath();方法返回的是一个 URL 格式的路径，它会包含 file:/ 前缀。这是因为资源通常是以 URL 的形式定位的，尤其是在使用类加载器加载资源时。因此，你会看到路径以 file:/ 开头。
String filePath = Main.class.getResource("").getPath();
String normalizedFilePath = filePath.replace("\\", "/").replace("file:/", "");
