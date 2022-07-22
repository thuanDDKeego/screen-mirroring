package com.abc.sreenmirroring.floatingbubble

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.abc.sreenmirroring.helper.logIfError
import com.abc.sreenmirroring.helper.onComplete

class ExpandableDrawingToolView(
    private val builder: BuilderDrawingTool,
) : BaseFloatingView(builder.context) {
    init {
        setupLayoutParams()
    }


    fun show() = logIfError {
        builder.rootView?.let { super.show(it) }
    }.onComplete {
        builder.listener.onOpenDrawingToolView()
    }

    fun remove() = logIfError {
        builder.rootView?.let { super.remove(it) }
    }.onComplete {
        builder.listener.onCloseDrawingToolView()
    }

    override fun setupLayoutParams() {
        super.setupLayoutParams()
        logIfError {
            windowParams?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP
                flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                dimAmount = builder.dim
            }
        }
    }

    interface Action {
        fun backToBubble() {}
        fun onOpenDrawingToolView() {}
        fun onCloseDrawingToolView() {}
        fun choosePents() {}
        fun chooseColors() {}
        fun chooseEraser() {}
    }

    class BuilderDrawingTool : IExpandableDrawingToolViewBuilder {
        lateinit var context: Context

        var rootView: View? = null
        var listener = object : Action {}
        var dim = 0f

        override fun with(context: Context): BuilderDrawingTool {
            this.context = context
            return this
        }

        override fun setDrawingToolView(view: View): BuilderDrawingTool {
            this.rootView = view
            return this
        }

        override fun addDrawingToolViewListener(action: Action): BuilderDrawingTool {
            this.listener = action
            return this
        }

        override fun setDimAmount(dimAmount: Float): BuilderDrawingTool {
            this.dim = dimAmount
            return this
        }

        override fun build(): ExpandableDrawingToolView {
            return ExpandableDrawingToolView(this)
        }
    }

    interface IExpandableDrawingToolViewBuilder {

        fun with(context: Context): IExpandableDrawingToolViewBuilder

        fun setDrawingToolView(view: View): IExpandableDrawingToolViewBuilder

        fun addDrawingToolViewListener(action: ExpandableDrawingToolView.Action): IExpandableDrawingToolViewBuilder

        fun setDimAmount(dimAmount: Float): IExpandableDrawingToolViewBuilder

        fun build(): ExpandableDrawingToolView

    }
}