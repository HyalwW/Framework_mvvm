package com.example.kleaner.base.floats

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.kleaner.events.ActionObserver
import com.example.kleaner.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.reflect.ParameterizedType

/**
 * 悬浮窗基类（待测试）
 */
abstract class BaseFloatService<DB : ViewDataBinding, M : BaseFloatModel> : Service() {
    private var init: Boolean = false
    private val TAG: String = javaClass.simpleName
    lateinit var dataBinding: DB
    lateinit var model: M
    var statusBarHeight: Int = 0
    var navigationBarHeight: Int = 0
    var needRegister: Boolean = false
    lateinit var windowManager: WindowManager
    lateinit var params: WindowManager.LayoutParams
    private var isShowing: Boolean = false
    var displayWidth: Int = 0
    var displayHeight: Int = 0
    lateinit var measuredMatrix: IntArray
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var isClick: Boolean = false
    private val mTouchSlop: Int = 10

    override fun onCreate() {
        super.onCreate()
        dataBinding = DataBindingUtil.inflate(LayoutInflater.from(this), layoutId(), null, false)
        initModel()
        statusBarHeight = getStatusHeight()
        navigationBarHeight = getNavBarHeight()
        initData()
        initView()
        initWindow()
        registerEventBus()
        model.timeControl.observe(object : ActionObserver.Listener<Any> {
            override fun call(t: Any?) {
                onTimesUp()
            }
        })
        model.exitAction.observe(object : ActionObserver.Listener<Any> {
            override fun call(t: Any?) {
                stopSelf()
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        model.parseIntent(intent?.extras)
        if (!init) {
            show()
            init = true
        }
        return START_NOT_STICKY
    }

    protected open fun stopTimer() {
        model.stopTimer()
    }

    protected open fun resetTimer() {
        model.resetTimer()
    }

    protected open fun resetTimer(millers: Long) {
        model.resetTimer(millers)
    }

    /**
     * 触计时结束时发，要操作需子类重写该方法
     */
    private fun onTimesUp() {
        Log.e(TAG, "time's up")
    }

    /**
     * 初始化model
     */
    private fun initModel() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val clazz = type.actualTypeArguments[1] as Class<M>
            model = if (clazz.constructors.isNotEmpty()) {
                this.model()!!
            } else {
                clazz.newInstance()
            }
        }
        dataBinding.setVariable(modelId(), model)
        model.create()
    }

    /**
     * 初始化window
     */
    private fun initWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams()
        params.type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        params.format = PixelFormat.TRANSPARENT
        params.gravity = Gravity.START or Gravity.TOP
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        if (getAnimId() != 0) params.windowAnimations = getAnimId()
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        displayWidth = metrics.widthPixels
        displayHeight = metrics.heightPixels
        measuredMatrix = measureView(dataBinding.root)
        params.x = if (showX() == 0) (displayWidth - measuredMatrix.get(0)) / 2 else showX()
        params.y = if (showY() == 0) (displayHeight - measuredMatrix.get(1)) / 2 else showY()
    }

    /**
     * 获取状态栏高度
     */
    @SuppressLint("PrivateApi")
    private fun getStatusHeight(): Int {
        var statusHeight = -1
        try {
            val clazz = Class.forName("com.android.internal.R\$dimen")
            val `object` = clazz.newInstance()
            val height = clazz.getField("status_bar_height")[`object`].toString().toInt()
            statusHeight = resources.getDimensionPixelSize(height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return statusHeight
    }

    /**
     * 获取底部导航栏高度
     */
    private fun getNavBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    /**
     * 测量某个View高宽
     *
     * @param view view
     * @return 返回{宽，高}
     */
    protected open fun measureView(view: View): IntArray {
        val size = IntArray(2)
        val width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(width, height)
        size[0] = view.measuredWidth
        size[1] = view.measuredHeight
        return size
    }

    /**
     * 注册EventBus
     */
    private fun registerEventBus() {
        needRegister = isNeedRegister(javaClass)
        if (needRegister) {
            EventBus.getDefault().register(this)
        }
    }

    /**
     * 判断是否需要注册EventBus
     */
    private fun isNeedRegister(clazz: Class<*>): Boolean {
        needRegister = false
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(Subscribe::class.javaObjectType)) {
                return true
            }
        }
        if (clazz.superclass != null) {
            return isNeedRegister(clazz.superclass)
        }
        return false
    }

    /**
     * 设置触发悬浮窗移动的View
     */
    fun triggerMove(view: View) {
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        startY = event.y
                        view.isActivated = true
                        isClick = true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (Math.abs(startX - event.x) >= mTouchSlop
                            || Math.abs(startY - event.y) >= mTouchSlop
                        ) {
                            isClick = false
                        }
                        params.x = (event.rawX.toInt() - startX).toInt()
                        params.y = (event.rawY.toInt() - startY).toInt()
                        if (params.y <= statusBarHeight) {
                            params.y = statusBarHeight
                        }
                        update()
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            view.performClick()
                        }
                        view.isActivated = false
                    }
                }
                return true
            }
        })
    }

    /**
     * 添加悬浮窗展示
     */
    protected open fun show() {
        if (!isShowing) {
            addToWindow()
            isShowing = true
        }
    }

    /**
     * 移除悬浮窗（不销毁service）
     */
    protected open fun remove() {
        if (isShowing) {
            removeFromWindow()
            isShowing = false
        }
    }

    /**
     * 更新悬浮窗位置
     */
    private fun update() {
        if (dataBinding != null && windowManager != null) {
            updateToWindow()
        }
    }

    private fun addToWindow(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "未开启悬浮窗权限", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return if (windowManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!dataBinding.root.isAttachedToWindow) {
                    windowManager.addView(dataBinding.root, params)
                    true
                } else {
                    false
                }
            } else {
                try {
                    if (dataBinding.root.parent == null) {
                        windowManager.addView(dataBinding.root, params)
                    }
                    true
                } catch (e: java.lang.Exception) {
                    false
                }
            }
        } else {
            false
        }
    }

    private fun updateToWindow() {
        if (windowManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (dataBinding.root.isAttachedToWindow) {
                    windowManager.updateViewLayout(dataBinding.root, params)
                }
            } else {
                try {
                    if (dataBinding.root.parent != null) {
                        windowManager.updateViewLayout(dataBinding.root, params)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun removeFromWindow() {
        try {
            if (windowManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (dataBinding.root.isAttachedToWindow) {
                        Log.e(TAG, "float is attachedToWindow")
                        //dataBinding.getRoot().onDetachWindow()
                        windowManager.removeViewImmediate(dataBinding.root)
                    } else {
                        if (dataBinding.root.parent != null) {
                            windowManager.removeView(dataBinding.root)
                            //                            windowManager.removeViewImmediate(view);
                        }
                    }
                } else {
                    if (dataBinding.root.parent != null) {
                        windowManager.removeViewImmediate(dataBinding.root)
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        remove()
        model.destroy()
        if (needRegister) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

    /**
     *如果子悬浮窗model初始化有构造函数必须重写方法返回初始化的model
     */
    fun model(): M? {
        return null
    }


    abstract fun layoutId(): Int
    abstract fun modelId(): Int
    abstract fun initData()
    abstract fun initView()
    abstract fun getAnimId(): Int
    abstract fun showY(): Int
    abstract fun showX(): Int

    companion object FloatMethods {
        /**
         * BaseFloatService<ViewDataBinding, BaseFloatModel>
         *     像Java不想在此处传入两类参数，如何解决？？？
         */
        fun <F : BaseFloatService<ViewDataBinding, BaseFloatModel>> start(
            context: Context,
            argument: Bundle?,
            clz: Class<F>
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    Toast.makeText(Utils.context, "未开启悬浮窗权限", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            val intent = Intent(context, clz)
            if (argument != null) {
                intent.putExtras(argument)
            }
            context.startService(intent)
        }
    }

    fun <F : BaseFloatService<ViewDataBinding, BaseFloatModel>> stop(
        context: Context,
        clz: Class<F>
    ) {
        context.stopService(Intent(context, clz))
    }
}
