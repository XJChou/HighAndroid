import okhttp3.Request
import retrofit2.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

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