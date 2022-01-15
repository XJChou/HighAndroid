# 插件化 和 热更新 原理

### 名词解释

插件化：是给原有APK增加功能，与组件化相比，增加了动态部署，但需要提前插桩(调用逻辑代码)

热更新：是原有APK的类修改功能

### 环境

Android虚拟机（API27）；对于API28以上的虚拟机，Demo中的全局替换是不行

* 在API27的虚拟机表现与JVM虚拟机一致，只有发生以下时机才会进行类加载
    1. 遇到 new、getstatic、putstatic或invokestatic 这四条字节码指令时，如果类型没有进行过初始化，则需要先触发其初始化阶段。
    2. 使用 java.lang.reflect 包的方法对类型进行反射调用的时候，如果类型没有进行过初始化，则需要先触发其初始化。
    3. 当初始化类型的时候，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化。
    4. 当虚拟机启动时，用户需要指定一个要执行的主类（包含main()方法的那个类），虚拟机会先初始化这个主类。
    5. 当使用JDK7新加入的动态语言支持时，如果一个java.lang.invoke.MethodHandle实例最后的解析结果为REF_getStatic、REF_putStatic、REF_invokeStatic、REF_newInvokeSpecial四种类型的方法句柄，并且这个方法句柄对应的类没有进行过初始化，则需要先触发其初始化。
    6. 当一个接口中定义了JDK8新加入的默认方法（被default关键字修饰的接口方法）时，如果这个接口的实现类发生了初始化，那该接口要在其之前被初始化。

* 在API28以上的虚拟机，在Demo中，当MainActivity加载的时候，Title此时的类也发生了加载，导致全量替换失败，因为此时Title类已加入缓存，替换已无效了(
  目前只知道表现如此，但具体暂无文档考证)

### 前期准备
ClassLoader、BaseDexClassLoader、DexClassLoader、PathClassLoader源码
* git clone https://android.googlesource.com/platform/libcore (需要梯子)
* git clone https://gitee.com/mirrors_android_googlesource/libcore.git (国内镜像)

### 涉及知识
1. 反射
2. 类加载流程

### 插件化过程
1. 需要插件包(apk/dex/jar) [plugin.apk](https://github.com/XJChou/HighAndroid/tree/master/component/src/main/assets/plugin.apk)
2. 需要给插件化内容增加调用

由于插件化是原先apk没有的东西，所以只能通过反射进行调用；并且要一个工具能够读取插件包(DexClassLoader/PathClassLoader)
```kotlin
object PluginHelper {
    fun onPlugin(context: Context) {
        kotlin.runCatching {
            // 1、将插件包存入一个手机目录下，assets目录是读取不到的
            val file = context.assetToCacheFile("plugin.apk", "plugin.apk")

            // 2、classloader工具加载插件包
            val classLoader = DexClassLoader(file.path, context.cacheDir.path, null, null)

            // 3、使用含有插件包classLoader加载指定的类并使用反射调用指定方法
            val pluginClass = classLoader.loadClass("com.zxj.highandroid.Plugin")
            val plugin = pluginClass.newInstance()
            val toastMethod = pluginClass.getDeclaredMethod("toast", Context::class.java)
            toastMethod.invoke(plugin, context)
        }
    }
}
```

### 热更新过程
原理：把补丁插在 ClassLoader 的 dexElements 最前面，使之先加载补丁的类文件，达到热修复的目

结论查找过程：
```java
class ClassLoader {
    
    // ...省略代码

    // 每个类加载都会经过 loadClass 方法
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1、查找一份缓存
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        // 2、向父类进行查找指定name
                        c = parent.loadClass(name, false);
                    } else {
                        // 默认返回null
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                }
        
                if (c == null) {
                    long t1 = System.nanoTime();
                    // 3、加载类
                    c = findClass(name);
                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
              resolveClass(c);
            }
            return c;
        }
    }
    
    // ...省略代码

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // First, check whether the class is present in our shared libraries.
        if (sharedLibraryLoaders != null) {
            for (ClassLoader loader : sharedLibraryLoaders) {
                try {
                    return loader.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        // Check whether the class in question is present in the dexPath that
        // this classloader operates on.
        List<Throwable> suppressedExceptions = new ArrayList<Throwable>();
        Class c = pathList.findClass(name, suppressedExceptions);
        if (c == null) {
            ClassNotFoundException cnfe = new ClassNotFoundException(
                    "Didn't find class \"" + name + "\" on path: " + pathList);
            for (Throwable t : suppressedExceptions) {
                cnfe.addSuppressed(t);
            }
            throw cnfe;
        }
        return c;
    }

    // ...省略代码
}

class DexPathList {
    
    // ...省略代码
  
    DexPathList(ClassLoader definingContext, String dexPath,
                String librarySearchPath, File optimizedDirectory, boolean isTrusted) {
        // ...省略代码
        this.dexElements = makeDexElements(splitDexPath(dexPath), optimizedDirectory,
                suppressedExceptions, definingContext, isTrusted);
        // ...省略代码
    }

    // ...省略代码

    @UnsupportedAppUsage
    private static NativeLibraryElement[] makePathElements(List<File> files) {
        NativeLibraryElement[] elements = new NativeLibraryElement[files.size()];
        int elementsPos = 0;
        for (File file : files) {
            String path = file.getPath();
      
            if (path.contains(zipSeparator)) {
                String split[] = path.split(zipSeparator, 2);
                File zip = new File(split[0]);
                String dir = split[1];
                elements[elementsPos++] = new NativeLibraryElement(zip, dir);
            } else if (file.isDirectory()) {
                // We support directories for looking up native libraries.
                elements[elementsPos++] = new NativeLibraryElement(file);
            }
        }
        if (elementsPos != elements.length) {
            elements = Arrays.copyOf(elements, elementsPos);
        }
        return elements;
    }

    // ...省略代码
    
    public Class<?> findClass(String name, List<Throwable> suppressed) {
        // dexElements 数组一个一个尝试查找
        for (Element element : dexElements) {
            Class<?> clazz = element.findClass(name, definingContext, suppressed);
            if (clazz != null) {
                return clazz;
            }
        }
    
        if (dexElementsSuppressedExceptions != null) {
            suppressed.addAll(Arrays.asList(dexElementsSuppressedExceptions));
        }
        return null;
    }

    // ...省略代码
    
    static class Element {
        public Class<?> findClass(String name, ClassLoader definingContext,
                                  List<Throwable> suppressed) {
            return dexFile != null ? dexFile.loadClassBinaryName(name, definingContext, suppressed)
                     : null;
        }
    }

    // ...省略代码
}
```

从上述代码分析可得，一个类的加载会经过3个过程
1. findLoadedClass(判断是否加载过此类) -> 假如加载过则直接返回
2. parent.loadClass(让父加载器查找) - 假如查找成功则直接返回
3. 自身classLoader.findClass -> pathList.findClass -> element.findClass

Tip: rengwuxian(朱凯老师)把类加载过程总结成一句话[一个带缓存从上而下进行类加载的一个模型]（很精辟）

切回正题，从代码分析可得，如果能在一个类加载前，能把补丁插在 element 数组的前面，即可使补丁生效，具体实现代码 [HotfixHelper](https://github.com/XJChou/HighAndroid/tree/master/component/src/main/java/com/zxj/component/helper/HotfixHelper.kt)
