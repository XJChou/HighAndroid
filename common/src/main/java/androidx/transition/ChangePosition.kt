/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Build
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.TypedArrayUtils
import androidx.core.view.ViewCompat
import org.xmlpull.v1.XmlPullParser

/**
 * This Transition captures scale and rotation for Views before and after the
 * scene change and animates those changes during the transition.
 *
 * A change in parent is handled as well by capturing the transforms from
 * the parent before and after the scene change and animating those during the
 * transition.
 */
class ChangePosition : Transition {
    /**
     * Returns whether changes to parent should use an overlay or not. When the parent
     * change doesn't use an overlay, it affects the transforms of the child. The
     * default value is `true`.
     *
     *
     * Note: when Overlays are not used when a parent changes, a view can be clipped when
     * it moves outside the bounds of its parent. Setting
     * [android.view.ViewGroup.setClipChildren] and
     * [android.view.ViewGroup.setClipToPadding] can help. Also, when
     * Overlays are not used and the parent is animating its location, the position of the
     * child view will be relative to its parent's final position, so it may appear to "jump"
     * at the beginning.
     *
     * @return `true` when a changed parent should execute the transition
     * inside the scene root's overlay or `false` if a parent change only
     * affects the transform of the transitioning view.
     */
    /**
     * Sets whether changes to parent should use an overlay or not. When the parent
     * change doesn't use an overlay, it affects the transforms of the child. The
     * default value is `true`.
     *
     *
     * Note: when Overlays are not used when a parent changes, a view can be clipped when
     * it moves outside the bounds of its parent. Setting
     * [android.view.ViewGroup.setClipChildren] and
     * [android.view.ViewGroup.setClipToPadding] can help. Also, when
     * Overlays are not used and the parent is animating its location, the position of the
     * child view will be relative to its parent's final position, so it may appear to "jump"
     * at the beginning.
     *
     * @param reparentWithOverlay `true` when a changed parent should execute the
     * transition inside the scene root's overlay or `false`
     * if a parent change only affects the transform of the
     * transitioning view.
     */
    var reparentWithOverlay/* synthetic access */ = true
    /**
     * Returns whether parent changes will be tracked by the ChangeTransform. If parent
     * changes are tracked, then the transform will adjust to the transforms of the
     * different parents. If they aren't tracked, only the transforms of the transitioning
     * view will be tracked. Default is true.
     *
     * @return whether parent changes will be tracked by the ChangeTransform.
     */
    /**
     * Sets whether parent changes will be tracked by the ChangeTransform. If parent
     * changes are tracked, then the transform will adjust to the transforms of the
     * different parents. If they aren't tracked, only the transforms of the transitioning
     * view will be tracked. Default is true.
     *
     * @param reparent Set to true to track parent changes or false to only track changes
     * of the transitioning view without considering the parent change.
     */
    var reparent = true
    private val mTempMatrix = Matrix()

    constructor() {}

    @SuppressLint("RestrictedApi") // remove once core lib would be released with the new
    // LIBRARY_GROUP_PREFIX restriction. tracking in b/127286008
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, Styleable.CHANGE_TRANSFORM)
        reparentWithOverlay = TypedArrayUtils.getNamedBoolean(
            a,
            (attrs as XmlPullParser?)!!,
            "reparentWithOverlay", Styleable.ChangeTransform.REPARENT_WITH_OVERLAY, true
        )
        reparent = TypedArrayUtils.getNamedBoolean(
            a,
            (attrs as XmlPullParser?)!!,
            "reparent", Styleable.ChangeTransform.REPARENT, true
        )
        a.recycle()
    }

    override fun getTransitionProperties(): Array<String>? {
        return sTransitionProperties
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        if (view.visibility == View.GONE) {
            return
        }
        transitionValues.values[PROPNAME_PARENT] =
            view.parent
        val transforms = Transforms(view)
        transitionValues.values[PROPNAME_TRANSFORMS] =
            transforms
        var matrix = view.matrix
        matrix = if (matrix == null || matrix.isIdentity) {
            null
        } else {
            Matrix(matrix)
        }
        transitionValues.values[PROPNAME_MATRIX] =
            matrix
        if (reparent) {
            val parentMatrix = Matrix()
            val parent = view.parent as ViewGroup
            ViewUtils.transformMatrixToGlobal(parent, parentMatrix)
            parentMatrix.preTranslate(-parent.scrollX.toFloat(), -parent.scrollY.toFloat())
            transitionValues.values[PROPNAME_PARENT_MATRIX] =
                parentMatrix
            transitionValues.values[PROPNAME_INTERMEDIATE_MATRIX] =
                view.getTag(R.id.transition_transform)
            transitionValues.values[PROPNAME_INTERMEDIATE_PARENT_MATRIX] =
                view.getTag(R.id.parent_matrix)
        }
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
        if (!SUPPORTS_VIEW_REMOVAL_SUPPRESSION) {
            // We still don't know if the view is removed or not, but we need to do this here, or
            // the view will be actually removed, resulting in flickering at the beginning of the
            // animation. We are canceling this afterwards.
            (transitionValues.view.parent as ViewGroup).startViewTransition(
                transitionValues.view
            )
        }
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun createAnimator(
        sceneRoot: ViewGroup, startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null || !startValues.values.containsKey(
                PROPNAME_PARENT
            )
            || !endValues.values.containsKey(PROPNAME_PARENT)
        ) {
            return null
        }
        val startParent = startValues.values[PROPNAME_PARENT] as ViewGroup?
        val endParent = endValues.values[PROPNAME_PARENT] as ViewGroup?
        val handleParentChange = reparent && !parentsMatch(startParent, endParent)
        val startMatrix = startValues.values[PROPNAME_INTERMEDIATE_MATRIX] as Matrix?
        if (startMatrix != null) {
            startValues.values[PROPNAME_MATRIX] =
                startMatrix
        }
        val startParentMatrix = startValues.values[PROPNAME_INTERMEDIATE_PARENT_MATRIX] as Matrix?
        if (startParentMatrix != null) {
            startValues.values[PROPNAME_PARENT_MATRIX] =
                startParentMatrix
        }

        // First handle the parent change:
        if (handleParentChange) {
            setMatricesForParent(startValues, endValues)
        }

        // Next handle the normal matrix transform:
        val transformAnimator = createTransformAnimator(
            startValues, endValues,
            handleParentChange
        )
        if (handleParentChange && transformAnimator != null && reparentWithOverlay) {
            createGhostView(sceneRoot, startValues, endValues)
        } else if (!SUPPORTS_VIEW_REMOVAL_SUPPRESSION) {
            // We didn't need to suppress the view removal in this case. Cancel the suppression.
            startParent!!.endViewTransition(startValues.view)
        }
        return transformAnimator
    }

    private fun createTransformAnimator(
        startValues: TransitionValues,
        endValues: TransitionValues, handleParentChange: Boolean
    ): ObjectAnimator? {
        var startMatrix = startValues.values[PROPNAME_MATRIX] as Matrix?
        var endMatrix = endValues.values[PROPNAME_MATRIX] as Matrix?
        if (startMatrix == null) {
            startMatrix = MatrixUtils.IDENTITY_MATRIX
        }
        if (endMatrix == null) {
            endMatrix = MatrixUtils.IDENTITY_MATRIX
        }
//        if (startMatrix == endMatrix) {
//            return null
//        }
        val transforms = endValues.values[PROPNAME_TRANSFORMS] as Transforms?

        // clear the transform properties so that we can use the animation matrix instead
        val view = endValues.view
        setIdentityTransforms(view)
        val startMatrixValues = FloatArray(9)
        startMatrix!!.getValues(startMatrixValues)
        val endMatrixValues = FloatArray(9)
        endMatrix!!.getValues(endMatrixValues)
        val pathAnimatorMatrix = PathAnimatorMatrix(view, startMatrixValues)
        val valuesProperty = PropertyValuesHolder.ofObject(
            NON_TRANSLATIONS_PROPERTY, FloatArrayEvaluator(FloatArray(9)),
            startMatrixValues, endMatrixValues
        )
        val path = pathMotion.getPath(
            startMatrixValues[Matrix.MTRANS_X],
            startMatrixValues[Matrix.MTRANS_Y],
            endMatrixValues[Matrix.MTRANS_X],
            endMatrixValues[Matrix.MTRANS_Y]
        )
        val translationProperty = PropertyValuesHolderUtils.ofPointF(
            TRANSLATIONS_PROPERTY, path
        )
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            pathAnimatorMatrix,
            valuesProperty, translationProperty
        )
        val finalEndMatrix = endMatrix
        val listener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
            private var mIsCanceled = false
            private val mTempMatrix = Matrix()
            override fun onAnimationCancel(animation: Animator) {
                mIsCanceled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!mIsCanceled) {
                    if (handleParentChange && reparentWithOverlay) {
                        setCurrentMatrix(finalEndMatrix)
                    } else {
                        view.setTag(R.id.transition_transform, null)
                        view.setTag(R.id.parent_matrix, null)
                    }
                }
                ViewUtils.setAnimationMatrix(view, null)
                transforms!!.restore(view)
            }

            override fun onAnimationPause(animation: Animator) {
                val currentMatrix = pathAnimatorMatrix.matrix
                setCurrentMatrix(currentMatrix)
            }

            override fun onAnimationResume(animation: Animator) {
                setIdentityTransforms(view)
            }

            private fun setCurrentMatrix(currentMatrix: Matrix?) {
                mTempMatrix.set(currentMatrix)
                view.setTag(R.id.transition_transform, mTempMatrix)
                transforms!!.restore(view)
            }
        }
        animator.addListener(listener)
        AnimatorUtils.addPauseListener(animator, listener)
        return animator
    }

    private fun parentsMatch(startParent: ViewGroup?, endParent: ViewGroup?): Boolean {
        var parentsMatch = false
        if (!isValidTarget(startParent) || !isValidTarget(endParent)) {
            parentsMatch = startParent === endParent
        } else {
            val endValues = getMatchedTransitionValues(startParent, true)
            if (endValues != null) {
                parentsMatch = endParent === endValues.view
            }
        }
        return parentsMatch
    }

    private fun createGhostView(
        sceneRoot: ViewGroup, startValues: TransitionValues,
        endValues: TransitionValues
    ) {
        val view = endValues.view
        val endMatrix = endValues.values[PROPNAME_PARENT_MATRIX] as Matrix?
        val localEndMatrix = Matrix(endMatrix)
        ViewUtils.transformMatrixToLocal(sceneRoot, localEndMatrix)
        val ghostView = GhostViewUtils.addGhost(view, sceneRoot, localEndMatrix) ?: return
        // Ask GhostView to actually remove the start view when it starts drawing the animation.
        ghostView.reserveEndViewTransition(
            startValues.values[PROPNAME_PARENT] as ViewGroup?,
            startValues.view
        )
        var outerTransition: Transition = this
        while (outerTransition.mParent != null) {
            outerTransition = outerTransition.mParent
        }
        val listener = GhostListener(view, ghostView)
        outerTransition.addListener(listener)

        // We cannot do this for older platforms or it invalidates the view and results in
        // flickering, but the view will still be invisible by actually removing it from the parent.
        if (SUPPORTS_VIEW_REMOVAL_SUPPRESSION) {
            if (startValues.view !== endValues.view) {
                ViewUtils.setTransitionAlpha(startValues.view, 0f)
            }
            ViewUtils.setTransitionAlpha(view, 1f)
        }
    }

    private fun setMatricesForParent(startValues: TransitionValues, endValues: TransitionValues) {
        val endParentMatrix = endValues.values[PROPNAME_PARENT_MATRIX] as Matrix?
        endValues.view.setTag(R.id.parent_matrix, endParentMatrix)
        val toLocal = mTempMatrix
        toLocal.reset()
        endParentMatrix!!.invert(toLocal)
        var startLocal = startValues.values[PROPNAME_MATRIX] as Matrix?
        if (startLocal == null) {
            startLocal = Matrix()
            startValues.values[PROPNAME_MATRIX] =
                startLocal
        }
        val startParentMatrix = startValues.values[PROPNAME_PARENT_MATRIX] as Matrix?
        startLocal.postConcat(startParentMatrix)
        startLocal.postConcat(toLocal)
    }

    private class Transforms internal constructor(view: View) {
        val mTranslationX: Float
        val mTranslationY: Float
        val mTranslationZ: Float
        val mScaleX: Float
        val mScaleY: Float
        val mRotationX: Float
        val mRotationY: Float
        val mRotationZ: Float
        fun restore(view: View) {
            setTransforms(
                view, mTranslationX, mTranslationY, mTranslationZ, mScaleX, mScaleY,
                mRotationX, mRotationY, mRotationZ
            )
        }

        override fun equals(that: Any?): Boolean {
            if (that !is Transforms) {
                return false
            }
            val thatTransform = that
            return thatTransform.mTranslationX == mTranslationX && thatTransform.mTranslationY == mTranslationY && thatTransform.mTranslationZ == mTranslationZ && thatTransform.mScaleX == mScaleX && thatTransform.mScaleY == mScaleY && thatTransform.mRotationX == mRotationX && thatTransform.mRotationY == mRotationY && thatTransform.mRotationZ == mRotationZ
        }

        override fun hashCode(): Int {
            var code =
                if (mTranslationX != +0.0f) java.lang.Float.floatToIntBits(mTranslationX) else 0
            code =
                31 * code + if (mTranslationY != +0.0f) java.lang.Float.floatToIntBits(mTranslationY) else 0
            code =
                31 * code + if (mTranslationZ != +0.0f) java.lang.Float.floatToIntBits(mTranslationZ) else 0
            code = 31 * code + if (mScaleX != +0.0f) java.lang.Float.floatToIntBits(mScaleX) else 0
            code = 31 * code + if (mScaleY != +0.0f) java.lang.Float.floatToIntBits(mScaleY) else 0
            code =
                31 * code + if (mRotationX != +0.0f) java.lang.Float.floatToIntBits(mRotationX) else 0
            code =
                31 * code + if (mRotationY != +0.0f) java.lang.Float.floatToIntBits(mRotationY) else 0
            code =
                31 * code + if (mRotationZ != +0.0f) java.lang.Float.floatToIntBits(mRotationZ) else 0
            return code
        }

        init {
            mTranslationX = view.translationX
            mTranslationY = view.translationY
            mTranslationZ = ViewCompat.getTranslationZ(view)
            mScaleX = view.scaleX
            mScaleY = view.scaleY
            mRotationX = view.rotationX
            mRotationY = view.rotationY
            mRotationZ = view.rotation
        }
    }

    private class GhostListener internal constructor(
        private val mView: View,
        private val mGhostView: GhostView
    ) :
        TransitionListenerAdapter() {
        override fun onTransitionEnd(transition: Transition) {
            transition.removeListener(this)
            GhostViewUtils.removeGhost(mView)
            mView.setTag(R.id.transition_transform, null)
            mView.setTag(R.id.parent_matrix, null)
        }

        override fun onTransitionPause(transition: Transition) {
            mGhostView.setVisibility(View.INVISIBLE)
        }

        override fun onTransitionResume(transition: Transition) {
            mGhostView.setVisibility(View.VISIBLE)
        }
    }

    /**
     * PathAnimatorMatrix allows the translations and the rest of the matrix to be set
     * separately. This allows the PathMotion to affect the translations while scale
     * and rotation are evaluated separately.
     */
    private class PathAnimatorMatrix internal constructor(
        private val mView: View,
        values: FloatArray
    ) {
        val matrix = Matrix()
        private val mValues: FloatArray
        private var mTranslationX: Float
        private var mTranslationY: Float
        fun setValues(values: FloatArray?) {
            System.arraycopy(values, 0, mValues, 0, values!!.size)
            setAnimationMatrix()
        }

        fun setTranslation(translation: PointF?) {
            mTranslationX = translation!!.x
            mTranslationY = translation.y
            setAnimationMatrix()
        }

        private fun setAnimationMatrix() {
            mValues[Matrix.MTRANS_X] = mTranslationX
            mValues[Matrix.MTRANS_Y] = mTranslationY
            matrix.setValues(mValues)
            ViewUtils.setAnimationMatrix(mView, matrix)
        }

        init {
            mValues = values.clone()
            mTranslationX = mValues[Matrix.MTRANS_X]
            mTranslationY = mValues[Matrix.MTRANS_Y]
            setAnimationMatrix()
        }
    }

    companion object {
        private const val PROPNAME_MATRIX = "android:changePosition:matrix"
        private const val PROPNAME_TRANSFORMS = "android:changePosition:transforms"
        private const val PROPNAME_PARENT = "android:changePosition:parent"
        private const val PROPNAME_PARENT_MATRIX = "android:changePosition:parentMatrix"
        private const val PROPNAME_INTERMEDIATE_PARENT_MATRIX =
            "android:changePosition:intermediateParentMatrix"
        private const val PROPNAME_INTERMEDIATE_MATRIX =
            "android:changePosition:intermediateMatrix"
        private val sTransitionProperties = arrayOf(
            PROPNAME_MATRIX,
            PROPNAME_TRANSFORMS,
            PROPNAME_PARENT_MATRIX,
            PROPNAME_PARENT
        )

        /**
         * This property sets the animation matrix properties that are not translations.
         */
        private val NON_TRANSLATIONS_PROPERTY: Property<PathAnimatorMatrix, FloatArray> =
            object : Property<PathAnimatorMatrix, FloatArray>(
                FloatArray::class.java, "nonTranslations"
            ) {
                override fun get(matrix: PathAnimatorMatrix): FloatArray? {
                    return null
                }

                override fun set(matrix: PathAnimatorMatrix, value: FloatArray) {
                    matrix.setValues(value)
                }
            }

        /**
         * This property sets the translation animation matrix properties.
         */
        private val TRANSLATIONS_PROPERTY: Property<PathAnimatorMatrix, PointF> =
            object : Property<PathAnimatorMatrix, PointF>(
                PointF::class.java, "translations"
            ) {
                override fun get(matrix: PathAnimatorMatrix): PointF? {
                    return null
                }

                override fun set(matrix: PathAnimatorMatrix, value: PointF?) {
                    matrix.setTranslation(value)
                }
            }

        /**
         * Newer platforms suppress view removal at the beginning of the animation.
         */
        private val SUPPORTS_VIEW_REMOVAL_SUPPRESSION = Build.VERSION.SDK_INT >= 21
        fun setIdentityTransforms(view: View) {
            setTransforms(view, 0f, 0f, 0f, 1f, 1f, 0f, 0f, 0f)
        }

        fun setTransforms(
            view: View, translationX: Float, translationY: Float,
            translationZ: Float, scaleX: Float, scaleY: Float, rotationX: Float,
            rotationY: Float, rotationZ: Float
        ) {
            view.translationX = translationX
            view.translationY = translationY
            ViewCompat.setTranslationZ(view, translationZ)
            view.scaleX = scaleX
            view.scaleY = scaleY
            view.rotationX = rotationX
            view.rotationY = rotationY
            view.rotation = rotationZ
        }
    }
}