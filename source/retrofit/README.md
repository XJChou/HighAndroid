# Retrofit协程不使用Result处理异常的一种CallAdapter

### 环境
* Retrofit 2.6.0+: 自带支持协程

### 初级版
刚开始使用Retrofit调用网络接口的时候，都会使用下面2种写法，但每次写的时候就会感觉很繁琐，重复性代码很多，但又不能不写

```kotlin
fun main() {
    GlobalScope.launch {
        try {
            // 进行api访问
        } catch (e: Exception) {
            // 处理异常
        }
    }
}
```
或者
```kotlin
fun main() {
    GlobalScope.launch(CoroutineExceptionHandler{ _, _ -> 
        // 处理异常
    }) {
        // 进行api访问
    }
}
```

### 中级版
看过 Retrofit 源码的时候，并且知晓Kotlin有 Result<T>，就产生了 ResultCallAdapter
* 


### 高级版
* CallAdapter 中 responseType() 函数是用于提供 Converter 转换类型使用的
* 通过包装一层Call吸收异常处理，并且不影响原先逻辑

```kotlin
class API<Data>(val code: Int, val msg: String?, val data: Data? = null) {

    fun onSuccess(block: API<Data>.(data: Data?) -> Unit) {
        if (code == 200) {
            this.block(data)
        }
    }

    fun onError(block: API<Data>.(msg: String) -> Unit) {
        if (code != 200) {
            this.block(msg ?: "网络异常")
        }
    }
}


/**
 * 统一异常处理
 */
fun Throwable.toAPI(): API<*> {
    return when (this) {
        is IOException -> {
            API(400, "网络异常", null)
        }
        else -> {
            API(400, this.message, null)
        }
    }
}


/**
 * Retrofit CallAdapter 工厂
 */
class APICallAdapterFactory : CallAdapter.Factory() {

    /**
     * 根据返回值解析内容
     */
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java || returnType !is ParameterizedType) {
            return null
        }

        val firstType = getParameterUpperBound(0, returnType)
        if (getRawType(firstType) != API::class.java || firstType !is ParameterizedType) {
            return null
        }

        return APICallAdapter(firstType)
    }

}


class APICallAdapter(private val responseType: Type) : CallAdapter<API<*>, Call<API<*>>> {

    /**
     * 代表Convert转化的内容
     */
    override fun responseType() = responseType

    override fun adapt(call: Call<API<*>>) = APICall(call)
}


/**
 * 套一层的作用：主要用来捕获异常防止向上抛出
 */
class APICall(private val originCall: Call<API<*>>) : Call<API<*>> {

    override fun execute(): Response<API<*>> {
        return try {
            originCall.execute()
        } catch (e: Exception) {
            Response.success(e.toAPI())
        }
    }

    override fun enqueue(callback: Callback<API<*>>) {
        originCall.enqueue(object : Callback<API<*>> {
            override fun onResponse(call: Call<API<*>>, response: Response<API<*>>) {
                callback.onResponse(this@APICall, response)
            }

            override fun onFailure(call: Call<API<*>>, t: Throwable) {
                callback.onResponse(this@APICall, Response.success(t.toAPI()))
            }
        })
    }

    override fun isExecuted() = originCall.isExecuted

    override fun cancel() = originCall.cancel()

    override fun isCanceled() = originCall.isCanceled

    override fun request(): Request = originCall.request()

    override fun clone(): Call<API<*>> = APICall(originCall.clone())

}
```
