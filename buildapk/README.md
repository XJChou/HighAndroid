# 手动打包流程

### 准备工具
使用aapt(Android Asset Packaging Tool)进行最简单的apk手动打包

使用工具：aapt2、d8、apksigner、kotlinc、javac

在 sdk 的 build-tools 存在 aapt2、d8、apksigner 工具

kotlinc 在 [kotlin](https://github.com/JetBrains/kotlin/releases/tag/v1.6.10) 可以下载，然后配置环境变量

javac 网上搜配置 jdk 环境变量即可

### 工具说明
aapt2 是一种构建工具，会解析资源、为资源编制索引，并将资源编译为针对 Android 平台进行过优化的二进制格式

kotlinc 和 javac 分别把 kt文件 和 java文件 转换成 class文件 

d8 是把 class文件 转换成 dex文件

apksigner 是用于apk的签名

### 工作流程
1. res资源文件转换成flat二进制文件格式，使用zip包含所有flat文件
    + aapt2 compile res资源文件，生成zip包[aapt2 compile -o (target.zip) --dir res]
    + 产物：target.zip(资源flat文件)

2. 链接资源文件
    + aapt2 link (target.zip) --manifest (AndroidManifest.xml) --java (R文件输出目录) --I (android.jar) -o (target.ap_)
    + 产物：R.java、target.ap_(res文件夹、resources.arsc、AndroidManifest.xml)

3. 生成class文件
    + kt文件：kotlinc -d (class输出目录) -cp (android.jar) (R.java)
    + java文件：javac -d (class输出目录) -cp (android.jar) (R.java)
    + 产物：*.class

4. class文件 转换成 dex文件
    + d8 (class文件) --lib (android.jar) --output (dex输出目录)
    + 产物：*.dex

5. 合并成一个完整的apk文件[window没有zip命令，但本质就是放入]
    + 步骤2的产物(target.ap_)使用压缩工具打开，放入*.dex,并修改文件名为target.apk
    + 产物：target.apk(未签名的apk)

6. 对apk进行签名
    + apksigner sign --ks (证书) (target.apk)
    + 产物：target.apk(已签名的apk)

### 图说明 - 引用高杰老师的课件
<image src='./images/packaging.png'/>