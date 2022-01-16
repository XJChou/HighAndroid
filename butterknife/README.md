# Annotation Processor

### 实现方案
ButterKnife 是一个自动findViewById的库，目前有2种实现方案

当 BindView注解 Retention == AnnotationRetention.RUNTIME 的时候
* 使用反射读取含有BindView的注解field，自动赋值具体view对象；当有多个view field的时候，需要反射多次，造成性能损耗

当 BindView注解 Retention == AnnotationRetention.SOURCE 的时候 
* 使用 Annotation Processor，自动生成 findViewById 相关代码，使用反射调用生成文件，每一次onCreate只需反射一次即可

### 项目组成结构[采用方案二]
* ButterKnife
    - lib            给予 SimpleButterKnife.bind 绑定视图入口，负责调用生成的 Binding 文件
    - lib-annotation 声明注解
    - lib-processor  解析 lib-annotation 的注解，并生成对应的 Binding 文件

### 操作
1. lib-processor module 生成一个[MineProcessor](lib-processor/src/main/java/com/zxj/lib_processor/MineProcessor.kt)类，继承 javax.annotation.processing 包下的 AbstractProcessor
2. lib-processor main 下生成 resources/META-INF/services/javax.annotation.processing.Processor 文件，填入步骤一的类
3. 主module  kapt project(':butterknife:lib-processor')，kapt是完全兼容java的，如果有kotlin代码，使用 kapt 代替 annotationProcessor

### Tip
RoundEnvironment
* rootElements 为注解的载体(本例子是activity)
* Element.enclosingElement 是包裹 rootElement 的载体(本例子是 package)
* Element.enclosedElements 是载体中的相关元素(field、method、class等)


