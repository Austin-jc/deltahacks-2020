//package com.example.camx
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.Rect
//import android.view.View
//import com.google.android.gms.vision.CameraSource
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//
//
//import android.util.AttributeSet
//
//
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//
//
//
//
//
//
//
//
//
//
//
//
//class GraphicOverlay : View {
//    private val lock = Any()
//    private var previewWidth: Int = 0
//    private var widthScaleFactor = 1.0f
//    private var previewHeight: Int = 0
//    private var heightScaleFactor = 1.0f
//    private var facing = CameraSource.CAMERA_FACING_BACK
//    private var graphics = ArrayList<Graphic>()
//
//    /**
//     * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
//     * this and implement the {@link Graphic#draw(Canvas)} method to define the graphics element. Add
//     * instances to the overlay using {@link GraphicOverlay#add(Graphic)}.
//     */
//    class Graphic {
//        private lateinit var overlay: GraphicOverlay
//
//        fun Graphic(overlay: GraphicOverlay) {
//            this.overlay = overlay
//        }
//
//
//        /**
//         * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
//         * to view coordinates for the graphics that are drawn:
//         *
//         * <ol>
//         *   <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of the
//         *       supplied value from the preview scale to the view scale.
//         *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
//         *       coordinate from the preview's coordinate system to the view coordinate system.
//         * </ol>
//         *
//         * @param canvas drawing canvas
//         */
//        fun draw(canvas: Canvas, rect: Rect) {
//
//        }
//
//        fun scaleX(horizontal: Float): Float {
//            return horizontal * overlay.widthScaleFactor
//        }
//
//        fun scaleY(vertical: Float): Float {
//            return vertical * overlay.heightScaleFactor
//        }
//
//        fun getApplicationContext(): Context {
//            return overlay.getContext().getApplicationContext()
//        }
//
////        fun translateX(x: Float) : Float{
////            if (overlay.facing == CameraSource.CAMERA_FACING_BACK){
////                return overlay.getWidth
////            }
////        }
//
//        fun translateX(x: Float) : Float{
//            return scaleX(x)
//        }
//
//        fun translateY(y: Float) : Float{
//            return scaleY(y)
//        }
//
//        fun postInvalidate() {
//            overlay.postInvalidate()
//        }
//    }
//
//    fun GraphicOverlay(context: Context, attrs: AttributeSet) {
//
//    }
//
//    fun clear{
//        synchronized(lock){
//            graphics.clear()
//        }
//        postInvalidate()
//    }
//
//    fun add(graphic: Graphic) {
//        synchronized(lock) {
//            graphics.add(graphic)
//        }
//    }
//
//    fun remove(graphic: Graphic) {
//        synchronized(lock) {
//            graphics.remove(graphic)
//        }
//        postInvalidate()
//    }
//
//    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: Int) {
//        synchronized(lock) {
//            this.previewWidth = previewWidth
//            this.previewHeight = previewHeight
//            this.facing = facing
//        }
//        postInvalidate()
//    }
//
//
//    override fun onDraw(canvas: Canvas){
//        super.onDraw(canvas)
//
//        synchronized(lock){
//            if ((previewWidth!=0) && previewHeight != 0)){
//            widthScaleFactor = (Float) getWidth() / previewWidth
//            heightScaleFactor = (Float) getHeight() / previewHeight
//        }
//        }
//
//        for (graphic in graphics) {
//            graphic.draw(canvas)
//        }
//    }
//
//}