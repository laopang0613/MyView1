package com.example.pangyinglei.myview1;

import android.animation.AnimatorInflater;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by pangyinglei on 2016/12/30.
 */

public class MyCustomView extends View {

    private static final String TAG = "myview1.MyCustomView";
    private Paint mPaint;
    private String mText;
    private int mTextColor;
    private float mTextSize;
    //private Rect mRound;
    private float lineWidth = 960f;
    private float lineHeight = 55f;
    private float txtTopXStart = 60f;
    private float txtTopYStart = 150f;
    //private int lineNumInPage = 26;
    private float txtSize = 55f;

    private float lineSpace = 27f;
    private float paraSpace = 55f;
    private float firstPageYStart = 500f;
    private float titleSize = 80f;
    private float titleHeight = 80f;
    //private float titleX = 40f;
    private float titleY = 200f;
    private float littleTitleY = 40f;
    private float littleTitleSize = 40f;

    //private int pageBeginIndx = 0;
    private int pageEndIndx = 0;
    private float pageIndxSize = 30f;


    private boolean isTouchScroll = false;
    private float touchPointX;
    private float touchPointY;
    private Path path = new Path();
    private Path pathTwo = new Path();

    //画阴影
    private Path pathThree = new Path();
    private ColorFilter shadowColorFilter;

    //使用双缓冲避免闪烁，定义内存的图片。
    private  Bitmap cacheBitmap;
    private Canvas cacheCanvas;

    //背面字符的颜色矩阵
    private ColorFilter backColorFilter;


    private float tmpScrollX = -1;
    //定义左右滑动方向。
    private enum FlingDirection{
        LEFT,RIGHT,NOTALL
    };
    private FlingDirection flingDirection;
    //缓存方向变化的点
    private List<Float> changeDirPoints = new ArrayList<Float>();
    private enum TurnPageAnimDirection{
        LEFT,RIGHT,NOTALL
    };
    //翻页方向
    private TurnPageAnimDirection turnPageAnimDirection;

    //定义翻页起始位置
    private enum TurnPageStartPos{
        UP,MIDDLE,DOWN,NOTALL
    }
    private TurnPageStartPos turnPageStartPos = TurnPageStartPos.NOTALL;

    private boolean isPermitTurnPage = false;

    private String nextPageContent;


    private PopupWindow mPopupWindow;

    private GestureDetectorCompat gestureDetectorCompat;

    public MyCustomView(Context context) {
        this(context,null);
    }

    public MyCustomView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public MyCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,R.styleable.MyCustomView,defStyleAttr,0);
        int n = ta.getIndexCount();
        Log.d(TAG,"n = "+n);
        for(int i = 0;i<n;i++){
            int attr = ta.getIndex(i);
            switch(attr){
                case  R.styleable.MyCustomView_MyText:
                    mText = ta.getString(attr);
                   //Log.d(TAG,"mText = "+mText);
                   break;
                case R.styleable.MyCustomView_MyTextColor:
                    mTextColor = ta.getColor(attr,Color.BLACK);
                    break;
                case R.styleable.MyCustomView_MyTextSize:
                    mTextSize = ta.getDimension(R.styleable.MyCustomView_MyTextSize,55);
                    Log.d(TAG,"mTextSize = "+ mTextSize);
                    break;
            }
        }
        ta.recycle();
        init();
    }

    private void init(){
        int mWidth = MyFileUtils.getAppWidth();
        int mHeight = MyFileUtils.getAppHeight();
//        touchPointX = mWidth;
//        touchPointY = mHeight;

        mPaint = new Paint();
        mPaint.setTextSize(mTextSize);
        //mRound = new Rect(0,0,1000,1800);
        float tmpSize = mPaint.getTextSize();
        Log.d(TAG,"tmpSize = "+tmpSize);

        //双缓冲
        cacheBitmap = Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();
        cacheCanvas.setBitmap(cacheBitmap);

        float[] colorMatrix = new float[]{
                1,0,0,0,0,
                0,1,0,0,0,
                0,0,1,0,0,
                0,0,0,0.2f,0
        };
        backColorFilter = new ColorMatrixColorFilter(colorMatrix);

        shadowColorFilter = new ColorMatrixColorFilter(new float[]{
                0.1f,0,0,0,0,
                0,0.1f,0,0,0,
                0,0,0.1f,0,0,
                0,0,0,0.2f,0
        });
        //Paint.FontMetrics fm = mPaint.getFontMetrics();
        //Log.d(TAG,"mText.length = "+mText.length());
        gestureDetectorCompat = new GestureDetectorCompat(BookshelfApp.getBookshelfApp(),new MyGestureDetector());
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
        this.postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG,"onDraw");
        super.onDraw(canvas);
        //int c = mPaint.getColor();

        Chapter currChapter = BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapter();
        String currChapterName = currChapter.getName();
        int currPageIndx = currChapter.getCurrPageNumIndx();
        int totalPageNum = currChapter.getPageTotal();
        if (isTouchScroll) {
            Log.d(TAG,"ondraw cacheBitmap");
            canvas.drawBitmap(cacheBitmap, 0, 0, mPaint);
        } else {
            drawPageContent(canvas, mText, currPageIndx, totalPageNum,currChapterName);
        }

//        if(isPermitTurnPage){
//            if(flingDirection == FlingDirection.LEFT && turnPageAnimDirection == TurnPageAnimDirection.LEFT){
//                turnNextPage();
//                isPermitTurnPage = false;
//            }
//        }

    }

    private void drawTurnNextPageAnimationUp(Canvas canvas){
        Chapter currChapter = BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapter();
        String currChapterName = currChapter.getName();
        int currPageIndx = currChapter.getCurrPageNumIndx();
        int totalPageNum = currChapter.getPageTotal();
        int viewWidth = MyFileUtils.getAppWidth();
        int viewHeight = MyFileUtils.getAppHeight();
        path.reset();
        pathTwo.reset();
        pathThree.reset();

        //先刷一层背景色。
        int color = mPaint.getColor();
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(0,0,viewWidth,viewHeight,mPaint);
        mPaint.setColor(color);

        float bx, by;
        float cx, cy;
        float dx, dy;
        float ex, ey;
        float fx, fy;
        float gx, gy;
        float hx, hy;
        float ix, iy;
        float jx, jy;
        float kx, ky;

        Log.d(TAG,"touchPointX ="+touchPointX+" touchPointY="+touchPointY);
        bx = MyFileUtils.getAppWidth();
        by = 0;
        Log.d(TAG, "bx =" + bx + " by=" + by);
        ex = (touchPointX + bx) / 2;
        ey = (touchPointY + by) / 2;
        Log.d(TAG, "ex =" + ex + " ey =" + ey);
        cx = bx;
        cy = ey + (bx - ex)*(bx - ex)/(ey - by);
        Log.d(TAG, "cx =" + cx + " cy=" + cy);
        dx = ex - (ey - by) * (ey - by)/(bx - ex);
        dy = by;
        Log.d(TAG, "dx =" + dx + " dy =" + dy);
        fx = (cx + touchPointX) / 2;
        fy = (cy + touchPointY) / 2;
        Log.d(TAG, "fx =" + fx + " fy=" + fy);
        gx = (touchPointX + dx) / 2;
        gy = (touchPointY + dy) / 2;
        Log.d(TAG, "gx =" + gx + " gy=" + gy);
        hx = bx;
        hy = (cy - by) / 2 * 3;
        Log.d(TAG, "hx =" + hx + " hy=" + hy);
        ix = dx - (bx - dx)/2;
        iy = by;
        Log.d(TAG, "ix =" + ix + " iy=" + iy);
        jx = ((cx + hx) / 2 + (cx + fx) / 2) / 2;
        jy = ((cy + hy) / 2 + (cy + fy) / 2) / 2;
        Log.d(TAG, "jx=" + jx + " jy=" + jy);
        kx = ((gx + dx) / 2 + (ix + dx) / 2) / 2;
        ky = ((gy + dy) / 2 + (iy + dy) / 2) / 2;
        Log.d(TAG, "kx=" + kx + " ky=" + ky);

        path.moveTo(hx, hy);
        path.quadTo(cx, cy, fx, fy);
        path.lineTo(touchPointX, touchPointY);
        path.lineTo(gx, gy);
        path.quadTo(dx, dy, ix, iy);
        path.lineTo(bx, by);
        path.close();

        canvas.save();
        canvas.clipRect(0, 0, viewWidth, viewHeight);
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        Log.d(TAG,"ondraw mText.size()" + mText.length());
        drawPageContent(canvas, mText,currPageIndx,totalPageNum,currChapterName);
        mPaint.setColor(mTextColor);
        Paint.Style style = mPaint.getStyle();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setAntiAlias(false);
        canvas.drawPath(path, mPaint);
        canvas.restore();

        mPaint.setStyle(style);
        pathTwo.moveTo(jx, jy);
        pathTwo.lineTo(touchPointX, touchPointY);
        pathTwo.lineTo(kx, ky);
        pathTwo.close();

        //画下一页
        canvas.save();
        canvas.clipPath(pathTwo);
        canvas.clipPath(path, Region.Op.REVERSE_DIFFERENCE);
        drawNextPageContent(canvas);
        canvas.drawLine(jx, jy, kx, ky, mPaint);
        canvas.restore();

        //画背面
        ColorFilter colorFilter = mPaint.getColorFilter();
        canvas.save();
        canvas.clipPath(pathTwo);
        canvas.clipPath(path,Region.Op.INTERSECT);
        Matrix matrix = new Matrix();
        float mk = (touchPointX - bx)/(by - touchPointY);
        float mb = (touchPointY+by)/2 - mk * (touchPointX + bx)/2;
        float ksqr = mk * mk;
        float x1 = (1- ksqr)/(ksqr + 1);
        float x2 = (2*mk)/(ksqr + 1);
        float x3 = (-2 * mb * mk)/(ksqr + 1);
        float x4 = (2 * mk)/(ksqr + 1);
        float x5 = (ksqr - 1)/(ksqr + 1);
        //float x6 = (1 - ksqr) * mb / (ksqr + 1) + mb;
        float x6 = 2*mb/(ksqr+1);
        float[] values = new float[]{
                x1,x2,x3,x4,x5,x6,0,0,1
        };
        Log.d(TAG,"mk = "+mk+" mb = "+mb+" x1="+x1+" x2="+x2+" x3="+x3);
        Log.d(TAG,"x4="+x4+" x5="+x5+" x6="+x6);
        matrix.setValues(values);
        canvas.setMatrix(matrix);
        mPaint.setColorFilter(backColorFilter);
        drawPageContent(canvas, mText,currPageIndx,totalPageNum,currChapterName);
        canvas.restore();

        //画阴影
        Shader shader = mPaint.getShader();
        int alpha = mPaint.getAlpha();
        mPaint.setColorFilter(colorFilter);
        mPaint.setAlpha(100);
        int s = 12;
        float lx = bx + (bx - touchPointX)/s;
        float ly = by + (by - touchPointY)/s;
        LinearGradient linearGradient1 = new LinearGradient(0,by,0,ly,new int[]{Color.BLACK,Color.WHITE},
                null, Shader.TileMode.CLAMP);
        LinearGradient linearGradient2 = new LinearGradient(bx,0,lx,0,new int[]{Color.BLACK,Color.WHITE},
                null,Shader.TileMode.CLAMP);

        float a1x = touchPointX - (bx - touchPointX)/s;
        float a1y = touchPointY - (by - touchPointY)/s;
        float c1x = cx;
        float c1y = cy + (cy - by)/s;
        float d1x = dx - (bx -dx)/s;
        float d1y = by;

        //左上阴影
        pathThree.moveTo(a1x,a1y);
        pathThree.lineTo(d1x,d1y);
        pathThree.lineTo(bx,by);
        //pathThree.lineTo(d1x,d1y);
        pathThree.close();
        canvas.save();
        canvas.clipPath(pathThree);
        canvas.clipPath(path,Region.Op.DIFFERENCE);
//        GradientDrawable gradientDrawable1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
//                new int[]{Color.BLACK,Color.WHITE});
        canvas.setMatrix(matrix);
        mPaint.setShader(linearGradient1);
        canvas.drawRect(0,0,lx,ly,mPaint);
        canvas.restore();

        //左下阴影。
        pathThree.reset();
        pathThree.moveTo(a1x,a1y);
        pathThree.lineTo(c1x,c1y);
        pathThree.lineTo(bx,by);
        canvas.save();
        canvas.clipPath(pathThree);
        canvas.clipPath(path,Region.Op.DIFFERENCE);
        canvas.setMatrix(matrix);
        mPaint.setShader(linearGradient2);
        canvas.drawRect(bx,ly,lx,viewHeight,mPaint);
        canvas.restore();

        //右边底部阴影
        double width = Math.sqrt((bx - touchPointX)*(bx - touchPointX) + (by - touchPointY)*(by - touchPointY))/4;
        double height = Math.sqrt((hx -ix)*(hx - ix)+(hy - iy)*(hy - iy));
        LinearGradient linearGradient3 = new LinearGradient(0,0,(float)width,0,new int[]{Color.BLACK,Color.BLACK,Color.WHITE},
                null,Shader.TileMode.CLAMP);
        //double tanValue = Math.abs(fx - gx)/Math.abs(fy - gy);
        double tanValue = (bx - ix) / (hy - by);
        double angle = Math.toDegrees(Math.atan(tanValue)) * (-1);
        Log.d(TAG,"angle ="+angle+"width ="+width+"height="+height);
        path.reset();
        path.moveTo(hx,hy);
        path.quadTo(cx,cy,fx,fy);
        path.lineTo(gx,gy);
        path.quadTo(dx,dy,ix,iy);
        path.lineTo(dx,dy);
        path.lineTo(cx,cy);
        path.close();
        pathThree.reset();
        pathThree.moveTo(jx,jy);
        pathThree.lineTo(touchPointX,touchPointY);
        pathThree.lineTo(kx,ky);
        pathThree.close();
        canvas.save();
        canvas.clipPath(pathThree);
        canvas.clipPath(path, Region.Op.REVERSE_DIFFERENCE);
        canvas.translate(ix,iy);
        canvas.rotate((float)angle);
        mPaint.setShader(linearGradient3);
        canvas.drawRect(0,0,(float)width,(float)height,mPaint);

        canvas.restore();

        mPaint.setShader(shader);
        mPaint.setAlpha(alpha);
        mPaint.setColorFilter(colorFilter);


        isTouchScroll = false;
    }

    private void drawTurnNextPageAnimationDown(Canvas canvas){

        Chapter currChapter = BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapter();
        String currChapterName = currChapter.getName();
        int currPageIndx = currChapter.getCurrPageNumIndx();
        int totalPageNum = currChapter.getPageTotal();
        path.reset();
        pathTwo.reset();
        pathThree.reset();

        //先刷一层背景色。
        int color = mPaint.getColor();
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(0,0,MyFileUtils.getAppWidth(),MyFileUtils.getAppHeight(),mPaint);
        mPaint.setColor(color);

        float bx, by;
        float cx, cy;
        float dx, dy;
        float ex, ey;
        float fx, fy;
        float gx, gy;
        float hx, hy;
        float ix, iy;
        float jx, jy;
        float kx, ky;

        bx = MyFileUtils.getAppWidth();
        by = MyFileUtils.getAppHeight();


        //Log.d(TAG, "bx =" + bx + " by=" + by);
        ex = (touchPointX + bx) / 2;
        ey = (touchPointY + by) / 2;
        //Log.d(TAG, "ex =" + ex + " ey =" + ey);
        cx = ex - (by - ey) * (by - ey) / (bx - ex);
        cy = by;
        //Log.d(TAG, "cx =" + cx + " cy=" + cy);
        dx = bx;
        dy = ey - (bx - ex) * (bx - ex) / (by - ey);
        //Log.d(TAG, "dx =" + dx + " dy =" + dy);
        fx = (cx + touchPointX) / 2;
        fy = (cy + touchPointY) / 2;
        //Log.d(TAG, "fx =" + fx + " fy=" + fy);
        gx = (touchPointX + dx) / 2;
        gy = (touchPointY + dy) / 2;
        //Log.d(TAG, "gx =" + gx + " gy=" + gy);
        hx = bx - (bx - cx) / 2 * 3;
        hy = by;
        //Log.d(TAG, "hx =" + hx + " hy=" + hy);
        ix = bx;
        iy = by - (by - dy) / 2 * 3;
        //Log.d(TAG, "ix =" + ix + " iy=" + iy);
        jx = ((cx + hx) / 2 + (cx + fx) / 2) / 2;
        jy = (cy + (cy + fy) / 2) / 2;
        //Log.d(TAG, "jx=" + jx + " jy=" + jy);
        kx = ((gx + dx) / 2 + dx) / 2;
        ky = ((gy + dy) / 2 + (iy + dy) / 2) / 2;
        //Log.d(TAG, "kx=" + kx + " ky=" + ky);

        path.moveTo(hx, hy);
        path.quadTo(cx, cy, fx, fy);
        path.lineTo(touchPointX, touchPointY);
        path.lineTo(gx, gy);
        path.quadTo(dx, dy, ix, iy);
        path.lineTo(bx, by);
        path.close();

        canvas.save();
        canvas.clipRect(0, 0, bx, by);
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        if(turnPageAnimDirection == TurnPageAnimDirection.LEFT) {
            drawPageContent(canvas, mText, currPageIndx, totalPageNum, currChapterName);
        }
        else if(turnPageAnimDirection == TurnPageAnimDirection.RIGHT){
            Log.d(TAG,"ondraw drawPrevPageContent currPage");
            drawPrevPageContent(canvas);
        }
        mPaint.setColor(mTextColor);
        Paint.Style style = mPaint.getStyle();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setAntiAlias(false);
        canvas.drawPath(path, mPaint);
        canvas.restore();

        mPaint.setStyle(style);
        pathTwo.moveTo(jx, jy);
        pathTwo.lineTo(touchPointX, touchPointY);
        pathTwo.lineTo(kx, ky);
        pathTwo.close();

        //画下一页
        canvas.save();
        canvas.clipPath(pathTwo);
        canvas.clipPath(path, Region.Op.REVERSE_DIFFERENCE);
        if(turnPageAnimDirection == TurnPageAnimDirection.LEFT) {
            drawNextPageContent(canvas);
        }
        else if(turnPageAnimDirection == TurnPageAnimDirection.RIGHT){
            Log.d(TAG,"ondraw drawPrevPageContent nextpage");
            drawPageContent(canvas, mText, currPageIndx, totalPageNum, currChapterName);
        }
        canvas.drawLine(jx, jy, kx, ky, mPaint);
        canvas.restore();

        //画背面
        ColorFilter colorFilter = mPaint.getColorFilter();
        canvas.save();
        canvas.clipPath(pathTwo);
        canvas.clipPath(path,Region.Op.INTERSECT);
        Matrix matrix = new Matrix();
        float mk = (touchPointX - bx)/(by - touchPointY);
        float mb = (touchPointY+by)/2 - mk * (touchPointX + bx)/2;
        float ksqr = mk * mk;
        float x1 = (1- ksqr)/(ksqr + 1);
        float x2 = (2*mk)/(ksqr + 1);
        float x3 = (-2 * mb * mk)/(ksqr + 1);
        float x4 = (2 * mk)/(ksqr + 1);
        float x5 = (ksqr - 1)/(ksqr + 1);
        //float x6 = (1 - ksqr) * mb / (ksqr + 1) + mb;
        float x6 = 2*mb/(ksqr+1);
        float[] values = new float[]{
          x1,x2,x3,x4,x5,x6,0,0,1
        };
        Log.d(TAG,"mk = "+mk+" mb = "+mb+" x1="+x1+" x2="+x2+" x3="+x3);
        Log.d(TAG,"x4="+x4+" x5="+x5+" x6="+x6);
        matrix.setValues(values);
        canvas.setMatrix(matrix);
        mPaint.setColorFilter(backColorFilter);
        if(turnPageAnimDirection == TurnPageAnimDirection.LEFT) {
            drawPageContent(canvas, mText, currPageIndx, totalPageNum, currChapterName);
        }
        else if(turnPageAnimDirection == TurnPageAnimDirection.RIGHT){
            Log.d(TAG,"ondraw drawPrevPageContent backpage");
            drawPrevPageContent(canvas);
        }
        canvas.restore();
        mPaint.setColorFilter(colorFilter);

        //画阴影
        Shader shader = mPaint.getShader();
        int alpha = mPaint.getAlpha();
        mPaint.setAlpha(100);
        int s = 12;
        float lx = bx + (bx - touchPointX)/s;
        float ly = by + (by - touchPointY)/s;
        LinearGradient linearGradient1 = new LinearGradient(0,by,0,ly,new int[]{Color.BLACK,Color.WHITE},
                null, Shader.TileMode.CLAMP);
        LinearGradient linearGradient2 = new LinearGradient(bx,0,lx,0,new int[]{Color.BLACK,Color.WHITE},
                null,Shader.TileMode.CLAMP);

        float a1x = touchPointX - (bx - touchPointX)/s;
        float a1y = touchPointY - (by - touchPointY)/s;
        float c1x = cx - (bx - cx)/s;
        float c1y = by;
        float d1x = bx;
        float d1y = dy - (by - dy)/s;

        //左上阴影
        pathThree.moveTo(a1x,a1y);
        pathThree.lineTo(c1x,c1y);
        pathThree.lineTo(bx,by);
        //pathThree.lineTo(d1x,d1y);
        pathThree.close();
        canvas.save();
        canvas.clipPath(pathThree);
        canvas.clipPath(path,Region.Op.DIFFERENCE);
//        GradientDrawable gradientDrawable1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
//                new int[]{Color.BLACK,Color.WHITE});
        canvas.setMatrix(matrix);
        mPaint.setShader(linearGradient1);
        canvas.drawRect(0,by,lx,ly,mPaint);
        canvas.restore();

        //右上阴影。
        pathThree.reset();
        pathThree.moveTo(a1x,a1y);
        pathThree.lineTo(d1x,d1y);
        pathThree.lineTo(bx,by);
        canvas.save();
        canvas.clipPath(pathThree);
        canvas.clipPath(path,Region.Op.DIFFERENCE);
        canvas.setMatrix(matrix);
        mPaint.setShader(linearGradient2);
        canvas.drawRect(bx,0,lx,ly,mPaint);
        canvas.restore();

        //底部阴影
        double width = Math.sqrt((bx - touchPointX)*(bx - touchPointX) + (by - touchPointY)*(by - touchPointY))/4;
        double height = Math.sqrt((hx -ix)*(hx - ix)+(hy - iy)*(hy - iy));
        LinearGradient linearGradient3 = new LinearGradient(0,0,(float)width,0,new int[]{Color.BLACK,Color.BLACK,Color.WHITE},
                null,Shader.TileMode.CLAMP);
        //double tanValue = Math.abs(fx - gx)/Math.abs(fy - gy);
        double tanValue = (bx - hx)/(by - iy);
        double angle = Math.toDegrees(Math.atan(tanValue));
        Log.d(TAG,"angle ="+angle+"width ="+width+"height="+height);
        path.reset();
        path.moveTo(hx,hy);
        path.quadTo(cx,cy,fx,fy);
        path.lineTo(gx,gy);
        path.quadTo(dx,dy,ix,iy);
        path.lineTo(dx,dy);
        path.lineTo(cx,cy);
        path.close();
        pathThree.reset();
        pathThree.moveTo(jx,jy);
        pathThree.lineTo(touchPointX,touchPointY);
        pathThree.lineTo(kx,ky);
        pathThree.close();
        canvas.save();
        canvas.clipPath(pathThree);
        canvas.clipPath(path, Region.Op.REVERSE_DIFFERENCE);
        canvas.translate(ix,iy);
        canvas.rotate((float)angle);
        mPaint.setShader(linearGradient3);
        canvas.drawRect(0,0,(float)width,(float)height,mPaint);

        canvas.restore();

        mPaint.setShader(shader);
        mPaint.setAlpha(alpha);


        isTouchScroll = false;
    }

    private void drawPrevPageContent(Canvas canvas){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        Chapter currChapter = mb.getCurrChapter();
        int currPageIndx = currChapter.getCurrPageNumIndx();
        List<Integer> pageList = currChapter.getPageNumList();
        String content = "";
        int prevPageIndx;
        int totalPageNum;
        String chapterName;
        //如果当前页是本章第一页
        if(currPageIndx == 0){
            Chapter prevChapter = mb.getPrevChapter();
            if(prevChapter == null){
                Log.e(TAG,"prevChapter == null");
                return;
            }
            if(prevChapter.getContent().isEmpty()){
                Log.e(TAG,"prevChapter is empty");
                return;
            }
            //获取下章第一页内容
            List<Integer> prevPageList = prevChapter.getPageNumList();
            int endPageNumIndx = prevPageList.size() - 1;
            content = prevChapter.getContent().substring(prevPageList.get(endPageNumIndx - 1),prevPageList.get(endPageNumIndx));
            Log.d(TAG,"drawPrevPage prevchapter endcontent ="+content);
            prevPageIndx = endPageNumIndx;
            totalPageNum = prevChapter.getPageTotal();
            chapterName = prevChapter.getName();
        }
        else{
            if(currPageIndx == 1){
                content = currChapter.getContent().substring(0,pageList.get(0));
            }
            else {
                content = currChapter.getContent().substring(pageList.get(currPageIndx - 2), pageList.get(currPageIndx - 1));
            }
            Log.d(TAG,"drawPrevPage content ="+content);
            prevPageIndx = currPageIndx - 1;
            totalPageNum = currChapter.getPageTotal();
            chapterName = currChapter.getName();
        }

        drawPageContent(canvas,content,prevPageIndx,totalPageNum,chapterName);
    }

    private void drawNextPageContent(Canvas canvas){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        Chapter currChapter = mb.getCurrChapter();
        int currPageIndx = currChapter.getCurrPageNumIndx();
        List<Integer> pageList = currChapter.getPageNumList();
        String content = "";
        int nextPageIndx;
        int totalPageNum;
        String chapterName;
        //如果当前页是本章最后一页
        if(currPageIndx == currChapter.getPageNumList().size() -1){
            Chapter nextChapter = mb.getNextChapter();
            if(nextChapter == null){
                Log.e(TAG,"nextChapter == null");
                return;
            }
            if(nextChapter.getContent().isEmpty()){
                Log.e(TAG,"nextChapter is empty");
                return;
            }
            //获取下章第一页内容
            content = nextChapter.getContent().substring(0,nextChapter.getPageNumList().get(0));
            nextPageIndx = 0;
            totalPageNum = nextChapter.getPageTotal();
            chapterName = nextChapter.getName();
        }
        else{
            content = currChapter.getContent().substring(pageList.get(currPageIndx),pageList.get(currPageIndx+1));
            nextPageIndx = currPageIndx + 1;
            totalPageNum = currChapter.getPageTotal();
            chapterName = currChapter.getName();
        }

        drawPageContent(canvas,content,nextPageIndx,totalPageNum,chapterName);
    }

    private void drawPageContent(Canvas canvas,String content,int currPageIndx,int totalPageNum,String chapterName){
        //mPaint.setColor(Color.WHITE);
        //canvas.drawRect(mRound,mPaint);
        mPaint.setColor(mTextColor);
//        canvas.drawText(content,30f,30f,mPaint);
//        canvas.drawText(content,0,content.length(),TXTTOP_XSTART,TXTTOP_YSTART,mPaint);
        //Log.d(TAG,"content = "+content);
        if(content.length() <= 0){
            return;
        }

//        Chapter chapter = BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapter();
        if(currPageIndx == 0){
            //Log.d(TAG,"chapterPageIndx = "+ chapter.getCurrPageNumIndx()+" chaptertitle = "+chapter.getName());
            drawBigChapterTitle(chapterName,canvas);
        }
        else{
            drawLittleChapterTitle(chapterName,canvas);
        }
        //Log.d(TAG,"content ="+content);
        mPaint.setTextSize(txtSize);
        pageEndIndx = this.justifyedText(content,canvas,currPageIndx);
        //不能在这里计算下次要显示的文本，否则多次重绘会自动翻页。
        //content = content.substring(pageEndIndx,content.length());
        //Log.d(TAG,"pageEndIndx = "+pageEndIndx);
        //mb.getCurrChapter().getPageNumList()
        drawPageIndx(canvas,currPageIndx+1,totalPageNum);
    }

    private void drawPageIndx(Canvas canvas,int currPageIndx,int totalPageNum){
        mPaint.setTextSize(pageIndxSize);
        //int currPageIndx = chapter.getCurrPageNumIndx()+1;
        //int totalPageNum = chapter.getPageTotal();
        float pageIndxY = MyFileUtils.getDisplayMetrics().heightPixels - pageIndxSize;
        canvas.drawText(currPageIndx+"/"+totalPageNum,this.txtTopXStart,pageIndxY,mPaint);
    }

    private void drawLittleChapterTitle(String name,Canvas canvas){
        mPaint.setTextSize(littleTitleSize);
        int len = name.length();
        if(len <= 0){
            Log.e(TAG,"littletitle.len == 0");
            return;
        }
        float[] tmpOneWidth = new float[len];
        float tmpTotalWidth = 0;
        int lineBeginIndx = 0;
        int lineEndIndx = 0;
        int lineNum = 0;
        StringBuffer lineStr = new StringBuffer("");
        for(int i =0;i<name.length();i++){
            tmpOneWidth[i] = mPaint.measureText(name,i,i+1);
            tmpTotalWidth += tmpOneWidth[i];
            if(tmpTotalWidth > lineWidth){
                lineEndIndx = i;
                //计算实际显示的行宽（要减去超过的字符宽度）
                tmpTotalWidth -= tmpOneWidth[i];
                //Log.d(TAG,"960 tmptotalwidth = "+tmpTotalWidth);
                //获取当前行实际要显示的字符串
                lineStr.setLength(0);
                lineStr.append(name.substring(lineBeginIndx,lineEndIndx));
                float averageWidth = (lineWidth - tmpTotalWidth)/(lineEndIndx - lineBeginIndx);
                //Log.d(TAG,"avewidth = "+averageWidth);
                //下一行要显示的起始字符索引。
                int j = lineBeginIndx;

                float sumWidth = 0;
                //由于不满一行字款，需要把每个字符加上字间距一个个画出来，
                StringBuffer ones = new StringBuffer("");
                while(j < lineEndIndx){
                    //获取当前行每个字符的字串形式。
                    //String ones = String.valueOf(pageStr.charAt(j));
                    //Log.d(TAG,"ones = "+ones+" sumWidth = "+sumWidth);
                    //画每个字符。
                    //Log.d(TAG,"titlechar ="+name.charAt(j));
                    canvas.drawText(String.valueOf(name.charAt(j)),
                            txtTopXStart +sumWidth,
                            littleTitleY,
                            mPaint);
                    //计算每个字符在x轴上的偏移量。
                    sumWidth += tmpOneWidth[j]+averageWidth;
                    j++;
                }
                break;
            }
            else{
                if(i == len - 1) {
                    //获取总字串的最后一行字符串。
                    lineStr.setLength(0);
                    lineStr.append(name.substring(lineBeginIndx, len));
                    canvas.drawText(lineStr.toString(), txtTopXStart, littleTitleY, mPaint);
                    break;
                }
            }
        }
    }

    private void drawBigChapterTitle(String name,Canvas canvas){
        int len = name.length();
        if(len <= 0){
            return;
        }
        mPaint.setTextSize(this.titleSize);
        float[] tmpOneWidth = new float[len];
        float tmpTotalWidth = 0;
        int lineBeginIndx = 0;
        int lineEndIndx = 0;
        int lineNum = 0;
        StringBuffer lineStr = new StringBuffer("");
        for(int i =0;i<name.length();i++){
            tmpOneWidth[i] = mPaint.measureText(name,i,i+1);
            tmpTotalWidth += tmpOneWidth[i];
            if(tmpTotalWidth > lineWidth){
                lineEndIndx = i;
                //计算实际显示的行宽（要减去超过的字符宽度）
                tmpTotalWidth -= tmpOneWidth[i];
                //Log.d(TAG,"960 tmptotalwidth = "+tmpTotalWidth);
                //获取当前行实际要显示的字符串
                lineStr.setLength(0);
                lineStr.append(name.substring(lineBeginIndx,lineEndIndx));
                float averageWidth = (lineWidth - tmpTotalWidth)/(lineEndIndx - lineBeginIndx);
                //Log.d(TAG,"avewidth = "+averageWidth);
                //下一行要显示的起始字符索引。
                int j = lineBeginIndx;

                float sumWidth = 0;
                //由于不满一行字款，需要把每个字符加上字间距一个个画出来，
                StringBuffer ones = new StringBuffer("");
                while(j < lineEndIndx){
                    //获取当前行每个字符的字串形式。
                    //String ones = String.valueOf(pageStr.charAt(j));
                    //Log.d(TAG,"ones = "+ones+" sumWidth = "+sumWidth);
                    //画每个字符。
                    //Log.d(TAG,"titlechar ="+name.charAt(j));
                    canvas.drawText(String.valueOf(name.charAt(j)),
                            txtTopXStart +sumWidth,
                            this.titleY+lineNum*titleHeight,
                            mPaint);
                    //计算每个字符在x轴上的偏移量。
                    sumWidth += tmpOneWidth[j]+averageWidth;
                    j++;
                }
                //Log.d(TAG,"linenum ="+lineNum);
                lineNum++;
                tmpTotalWidth = tmpOneWidth[i];
                //下一行字符串的起始字符索引。
                lineBeginIndx = i;
                //i恰好等于最后一个字符，需要再循环一次。
                if(i == len - 1){
                    i = len - 2;
                }
            }
            else{
                if(i == len - 1) {
                    //获取总字串的最后一行字符串。
                    lineStr.setLength(0);
                    lineStr.append(name.substring(lineBeginIndx, len));
                    canvas.drawText(lineStr.toString(), txtTopXStart, titleY + lineNum * titleHeight, mPaint);
                    canvas.drawLine(txtTopXStart,firstPageYStart-5,txtTopXStart+lineWidth,firstPageYStart - 5,mPaint);
                    lineNum++;
                }
            }


            //标题栏超过3行就不画了，以省略号结尾。
            if(lineNum > 3){
                return;
            }
        }
    }

    //画每一页的文本字串显示，并返回下一页的起始字串索引。
    private int justifyedText(String pageStr,Canvas canvas,int currPageIndx){
        //Log.d(TAG,"pageStr = "+pageStr);
        float lastLineHeight;
        Chapter chapter = BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapter();
        if(currPageIndx == 0){
            lastLineHeight = firstPageYStart;
        }
        else{
            lastLineHeight = txtTopYStart;
        }
        int pageEndCharIndx = 0;
        int lenth = pageStr.length();
        float[] tmpOneWidth = new float[lenth];
        float tmpTotalWidth = 0;
        int lineBeginIndx = 0;
        int lineEndIndx = 0;
        //int lineNum = 0;
        //String lineStr = "";
        StringBuffer lineStr = new StringBuffer("");
        //StringBuffer everyWord = new StringBuffer("");
        for(int i = 0; i< lenth;i++){
            //测量单个字符到字宽。
            tmpOneWidth[i] = mPaint.measureText(pageStr,i,i+1);
//            Log.d(TAG,"oneWidth ="+ tmpOneWidth[i]);
            //字符宽度累加
            tmpTotalWidth += tmpOneWidth[i];
            //如果累加到字符宽度大于行宽。
            if(tmpTotalWidth> lineWidth){
                //Log.d(TAG,"tmptotalwidth = "+tmpTotalWidth);
                //获取超过当前行宽的字符的索引。
                lineEndIndx = i;
                //计算实际显示的行宽（要减去超过的字符宽度）
                tmpTotalWidth -= tmpOneWidth[i];
                //Log.d(TAG,"960 tmptotalwidth = "+tmpTotalWidth);
                //获取当前行实际要显示的字符串
                lineStr.setLength(0);
                lineStr.append(pageStr.substring(lineBeginIndx,lineEndIndx));
                //Log.d(TAG,"s = "+lineStr.toString()+ " linebeginindx = "+lineBeginIndx+ " lineendindx = "+ lineEndIndx);
                //canvas.drawText(lineStr.toString(),TXTTOP_XSTART,TXTTOP_YSTART+lineNum*LINEHEIGHT,mPaint);
                //如果当前行字串宽度不占满行宽，计算当前行每个字符需要均分的宽度，即字间距。
                float averageWidth = (lineWidth - tmpTotalWidth)/(lineEndIndx - lineBeginIndx);
                //Log.d(TAG,"avewidth = "+averageWidth);
                //下一行要显示的起始字符索引。
                int j = lineBeginIndx;

                float sumWidth = 0;
                //由于不满一行字款，需要把每个字符加上字间距一个个画出来，
                StringBuffer ones = new StringBuffer("");
                //Log.d(TAG,"lastLineH ="+lastLineHeight+"linestr ="+lineStr.toString());
                lastLineHeight = lastLineHeight + lineHeight;
                while(j < lineEndIndx){
                    //获取当前行每个字符的字串形式。
                    //String ones = String.valueOf(pageStr.charAt(j));
                   // Log.d(TAG,"ones = "+ones+" sumWidth = "+sumWidth);
                    //画每个字符。
                    canvas.drawText(String.valueOf(pageStr.charAt(j)),
                            txtTopXStart +sumWidth,
                            lastLineHeight,
                            mPaint);
                    //计算每个字符在x轴上的偏移量。

                    sumWidth += tmpOneWidth[j]+averageWidth;
                    j++;

                }
                lastLineHeight = lastLineHeight + lineSpace;
                //lineNum++;
                //由于下一行最后一个字符的字宽被舍去，重置当前累加行宽为下一行第一个字符的字宽。
                tmpTotalWidth = tmpOneWidth[i];
                //下一行字符串的起始字符索引。
                lineBeginIndx = i;
                //i恰好等于最后一个字符，需要再循环一次。
                if(i == lenth - 1){
                    i = lenth - 2;
                }
            }
            else {
                if (i != lenth - 1 && pageStr.charAt(i) == '\n' && lineBeginIndx != i) {
                    //如果该行中有换行符，同时换行符不能为行首字符，获取当前行的字串。
                    //如果为行首字符，按照空格字宽计算。
                    lineStr.setLength(0);
                    lineStr.append(pageStr.substring(lineBeginIndx, i));
                    //lastLineHeight = lastLineHeight+lineHeight;
                    //Log.d(TAG,"lastLineH ="+lastLineHeight+"linestr ="+lineStr.toString());
                    lastLineHeight = lastLineHeight+lineHeight;
                    canvas.drawText(lineStr.toString(), txtTopXStart, lastLineHeight, mPaint);
                    lastLineHeight = lastLineHeight+paraSpace;

                    //Log.d(TAG,"linestr = "+lineStr.toString()+" linebeginindx = "+lineBeginIndx+" i = "+i);
                    lineBeginIndx = i;
                    //重置累加字符宽度。
                    tmpTotalWidth = tmpOneWidth[i];
                    //lineNum++;
                }else if(i == lenth - 1) {
                    //获取总字串的最后一行字符串。
                    lineStr.setLength(0);
                    lineStr.append(pageStr.substring(lineBeginIndx, lenth));
                    //Log.d(TAG,"lastLineH ="+lastLineHeight+"linestr ="+lineStr.toString());
                    lastLineHeight = lastLineHeight + lineHeight;
                    canvas.drawText(lineStr.toString(), txtTopXStart, lastLineHeight, mPaint);
                    //lastLineHeight = lastLineHeight + lineSpace;
                    //lineNum++;
                }
            }
            //记录下一页的起始字符索引。
//            if(lineNum >= lineNumInPage){
//                //Log.d(TAG,"pageendstr = "+pageStr.charAt(i)+" linebeginindx = "+i);
//                pageEndCharIndx = i;
//                break;
//            }
//            int maxHeight = MyFileUtils.getDisplayMetrics().heightPixels;
//            Log.d(TAG,"maxHeight ="+maxHeight);
//            if(lastLineHeight > maxHeight - lineHeight){
//                pageEndCharIndx = i;
//                break;
//            }

        }
        return pageEndCharIndx;
    }

    //判断右上翻页左曲线是否超过View左边界,如果越界重新计算触摸点坐标。
    private void turnPageUpCrossBorderRevise(){
        float bx,by,ex,ey,cx,hx;
        bx = MyFileUtils.getAppWidth();
        by = 0;

        //Log.d(TAG, "bx =" + bx + " by=" + by);
        ex = (touchPointX + bx) / 2;
        ey = (touchPointY + by) / 2;
        //Log.d(TAG, "ex =" + ex + " ey =" + ey);
        cx = ex - (by - ey) * (by - ey) / (bx - ex);
        //Log.d(TAG, "cx =" + cx + " cy=" + cy);

        hx = bx - (bx - cx) / 2 * 3;
        if(hx < 0){
            float c1x = bx/3;
            float c1y = by;
            float len = (float)Math.sqrt((touchPointX - c1x)*(touchPointX - c1x) + (touchPointY - c1y)*(touchPointY - c1y));
            float k = (bx - c1x)/len;
            touchPointX = k*(touchPointX - c1x) + c1x;
            touchPointY = k*(touchPointY - c1y) + c1y;
        }
    }

    //判断右下翻页左曲线是否超过View左边界,如果越界重新计算触摸点坐标。
    private void turnPageDownCrossBorderRevise(){
        float bx,by,ex,ey,cx,hx;
        bx = MyFileUtils.getAppWidth();
        by = MyFileUtils.getAppHeight();

        //Log.d(TAG, "bx =" + bx + " by=" + by);
        ex = (touchPointX + bx) / 2;
        ey = (touchPointY + by) / 2;
        //Log.d(TAG, "ex =" + ex + " ey =" + ey);
        cx = ex - (by - ey) * (by - ey) / (bx - ex);
        //Log.d(TAG, "cx =" + cx + " cy=" + cy);

        hx = bx - (bx - cx) / 2 * 3;
        if(hx < 0){
            float c1x = bx/3;
            float c1y = by;
            float len = (float)Math.sqrt((touchPointX - c1x)*(touchPointX - c1x) + (touchPointY - c1y)*(touchPointY - c1y));
            float k = (bx - c1x)/len;
            touchPointX = k*(touchPointX - c1x) + c1x;
            touchPointY = k*(touchPointY - c1y) + c1y;
        }
    }

    private void turnPageDone(MotionEvent event){
        int slop = ViewConfiguration.get(BookshelfApp.getBookshelfApp()).getScaledTouchSlop();
//            Log.d(TAG,"touchslop = "+slop);
        Log.d(TAG,"MyGestureDetector onFling changedirpoints.size= "+changeDirPoints.size());
        float x1 = changeDirPoints.get(changeDirPoints.size() - 1);
        float x2 = changeDirPoints.get(changeDirPoints.size() - 2);
        Log.d(TAG,"MyGestureDetector onFling x1= "+x1+" x2="+x2+" slop="+slop);
        if(x1 - x2 > 0 && Math.abs(x1 - x2) > slop){
            flingDirection = FlingDirection.RIGHT;
        }
        else if(x1 - x2 < 0 && Math.abs(x1 - x2) > slop){
            flingDirection = FlingDirection.LEFT;

        }
        else{
            flingDirection = FlingDirection.NOTALL;
        }

        //翻书方向跟滑动方向不一致，不翻书。
        if(flingDirection == FlingDirection.LEFT && turnPageAnimDirection == TurnPageAnimDirection.LEFT){
//            touchPointX = 0;
//            touchPointY = event.getY();
//            if(turnPageStartPos == TurnPageStartPos.DOWN){
//                Log.d(TAG,"TurnPageStartPos.DOWN next");
//                drawTurnNextPageAnimationDown(cacheCanvas);
//            }
//            else if(turnPageStartPos == TurnPageStartPos.UP){
//                Log.d(TAG,"TurnPageStartPos.UP next");
//                Log.d(TAG,"MyGestureDetector onscroll TurnPageStartPos.UP");
//                drawTurnNextPageAnimationUp(cacheCanvas);
//            }
//            else{
//                Log.d(TAG,"TurnPageStartPos.OTHER next");
//                touchPointY = MyFileUtils.getAppHeight() - 1;
//                drawTurnNextPageAnimationDown(cacheCanvas);
//            }
//            isTouchScroll = true;
//            isPermitTurnPage = true;
//            invalidate();

            turnNextPage();
        }
        else if(flingDirection == FlingDirection.RIGHT && turnPageAnimDirection == TurnPageAnimDirection.RIGHT){
            turnPrevPage();
        }
    }

    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{
        float xDown = 0,yDown = 0,xFling = 0,yFling = 0;
        private float currTime;
        @Override
        public boolean onDown(MotionEvent e) {
            //首先初始化
            touchPointX = e.getX();
            touchPointY = e.getY();
            tmpScrollX = -1 ;
            flingDirection = FlingDirection.NOTALL;
            changeDirPoints.clear();//action_up先于onfling执行，所以不能在action_up中清空。
            turnPageAnimDirection = TurnPageAnimDirection.NOTALL;

            tmpScrollX = e.getX();
            changeDirPoints.add(e.getX());
            Log.d(TAG,"MyGestureDetector ondown e.getX()="+tmpScrollX);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG,"MyGestureDetector onfling"+e1.toString()+e2.toString()+"velocityx ="+velocityX+"velocityy="+velocityY);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(TAG,"MyGestureDetector onscroll"+"distancex ="+distanceX+"distancey ="+distanceY);
            Log.d(TAG,"MyGestureDetector onscroll +e1.x ="+e1.getX()+"   e1.y="+e1.getY());
            Log.d(TAG,"MyGestureDetector onscroll +e2.x="+e2.getX()+"    e2.y="+e2.getY());


            if (e2.getX() > tmpScrollX) {
                tmpScrollX = e2.getX();
                if (flingDirection != FlingDirection.RIGHT) {
                    Log.d(TAG, "MyGestureDetector onscroll FlingDirection.RIGHT e2.getX()=" + e2.getX());
                    changeDirPoints.add(e2.getX());
                    flingDirection = FlingDirection.RIGHT;
                }
            } else if (e2.getX() < tmpScrollX) {
                tmpScrollX = e2.getX();
                if (flingDirection != FlingDirection.LEFT) {
                    Log.d(TAG, "MyGestureDetector onscroll FlingDirection.LEFT e2.getX()=" + e2.getX());
                    changeDirPoints.add(e2.getX());
                    flingDirection = FlingDirection.LEFT;
                }

            }

            if(changeDirPoints.size() == 2){
                if(flingDirection == FlingDirection.LEFT){
                    turnPageAnimDirection = TurnPageAnimDirection.LEFT;
                }
                else if(flingDirection == FlingDirection.RIGHT){
                    turnPageAnimDirection = TurnPageAnimDirection.RIGHT;
                }
//                else if(flingDirection == FlingDirection.RIGHT){
//                    touchPointX = e2.getX();
//                    touchPointY = e2.getY();
//                    drawTurnPrevPageAnimation();
//                    isTouchScroll = true;
//                    postInvalidate();
//                }
            }
            int height = MyFileUtils.getAppHeight();
            //往左翻下一页
            if(turnPageAnimDirection == TurnPageAnimDirection.LEFT){
                touchPointX = e2.getX();
                touchPointY = e2.getY();

                if(turnPageStartPos == TurnPageStartPos.NOTALL){
                    Log.d(TAG,"TurnPageStartPos.NOTALL");
                    if(touchPointY > height*2/3) {
                        Log.d(TAG,"TurnPageStartPos.DOWN first");
                        turnPageStartPos = TurnPageStartPos.DOWN;
                        turnPageDownCrossBorderRevise();
                        drawTurnNextPageAnimationDown(cacheCanvas);
                    }
                    else if(touchPointY < height/3){
                        Log.d(TAG,"TurnPageStartPos.UP fisrt");
                        turnPageStartPos = TurnPageStartPos.UP;
                        turnPageUpCrossBorderRevise();
                        drawTurnNextPageAnimationUp(cacheCanvas);
                    }
                    else{
                        Log.d(TAG,"TurnPageStartPos.OTHER fisrt");
                        turnPageStartPos = TurnPageStartPos.MIDDLE;
                        touchPointY = height - 1;
                        drawTurnNextPageAnimationDown(cacheCanvas);
                    }
                }
                else if(turnPageStartPos == TurnPageStartPos.DOWN){
                    Log.d(TAG,"TurnPageStartPos.DOWN next");
                    turnPageDownCrossBorderRevise();
                    drawTurnNextPageAnimationDown(cacheCanvas);
                }
                else if(turnPageStartPos == TurnPageStartPos.UP){
                    Log.d(TAG,"TurnPageStartPos.UP next");
                    Log.d(TAG,"MyGestureDetector onscroll TurnPageStartPos.UP");
                    turnPageUpCrossBorderRevise();
                    drawTurnNextPageAnimationUp(cacheCanvas);
                }
                else{
                    Log.d(TAG,"TurnPageStartPos.OTHER next");
                    touchPointY = height - 1;
                    drawTurnNextPageAnimationDown(cacheCanvas);
                }

                Log.d(TAG,"MyGestureDetector onscroll,Left anim, e2.getX()= "+e2.getX());
                isTouchScroll = true;
                invalidate();
            }
            else if(turnPageAnimDirection == TurnPageAnimDirection.RIGHT){
                touchPointX = e2.getX() - 100;
                touchPointY = height - 1;
                drawTurnNextPageAnimationDown(cacheCanvas);
                isTouchScroll = true;
                invalidate();
            }

            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG,"onshowpress"+e.toString());
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG,"MyGestureDetector onSingleTapUp "+ e.toString());
            popWindowHandler(e);
            return true;
        }



    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean isDetector =  gestureDetectorCompat.onTouchEvent(event);
//        return super.onTouchEvent(event);
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                //需要把滑动的最后一个点加入，可能该点并未改变方向。
                changeDirPoints.add(event.getX());
                //翻页
                turnPageDone(event);
                Log.d(TAG,"onTouchEvent action up");
                if(turnPageStartPos == TurnPageStartPos.DOWN) {
                    touchPointX = MyFileUtils.getAppWidth();
                    touchPointY = MyFileUtils.getAppHeight();
                }
                else if(turnPageStartPos == TurnPageStartPos.UP){
                    touchPointX = MyFileUtils.getAppWidth();
                    touchPointY = 0;
                }
                isTouchScroll = false;
                invalidate();
                turnPageStartPos = TurnPageStartPos.NOTALL;
                break;
        }
        return isDetector;
    }

    //    @Override
//   public boolean onTouchEvent(MotionEvent event) {
//        float xUp = 0,yUp = 0,xDown = 0,yDown = 0,xMove = 0,yMove = 0;
//       int action = event.getAction();
//       switch(action){
//            case MotionEvent.ACTION_UP:
//                xUp = event.getX();
//                yUp = event.getY();
//                Log.d(TAG,"action_up xUp = "+xUp + "yUp ="+yUp);
//                if(Math.abs(xUp-xDown) > 16 && xUp - xDown > 0){
//                    turnNextPage();
//                }
//                else if(Math.abs(xUp-xDown) > 16 && xUp - xDown < 0){
//                    turnPrevPage();
//                }
//                else{
//                    popWindowHandler(event);
//                }
//                break;
//            case MotionEvent.ACTION_DOWN:
//                xDown = event.getX();
//                yDown = event.getY();
//                Log.d(TAG,"action_down xdown = "+xDown+" ydown ="+yDown);
//                //turnNextPage();
//                //turnPrevPage();
//                //popWindowHandler(event);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG,"action move xMove ="+xMove+" yMove ="+yMove);
//                break;
//        }
//        return false;
//    }


    //翻页
    private void turnNextPage(){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        Chapter chapter = mb.getCurrChapter();

        Log.d(TAG,"pageTotal = "+chapter.getPageTotal());
        int pageNumIndx = chapter.getCurrPageNumIndx();
        Log.d(TAG,"pageNumIndx = "+pageNumIndx);
        pageNumIndx++;
        if(pageNumIndx > chapter.getPageTotal() - 1){
            Chapter nextChapter;
            if(mb.getCurrChapterIndx() == mb.getChapterList().size() - 1){
                return;
            }
            else{
                nextChapter = mb.getNextChapter();
            }
            //检查缓存的下章内容是否为空
            if(nextChapter.isEmpty()) {
                Log.d(TAG,"turnNextPage nextchapter is empty");
                gotoNextChapter();
            }
            else{
                //跳转到下一章之前将上一章内容清空。
                //mb.getCurrChapter().setContent("");
                Log.d(TAG,"turnNextPage currchapterindx = "+mb.getCurrChapterIndx());
                Log.d(TAG,"turnNextPage nextChapter.contentlen="+nextChapter.getContent().length());
                mText = nextChapter.getContent().substring(0,nextChapter.getPageNumList().get(0));
                int currChapterIndx = mb.getCurrChapterIndx();
                currChapterIndx++;
                mb.setCurrChapterIndx(currChapterIndx);
                mb.getCurrChapter().setCurrPageNumIndx(0);
                this.postInvalidate();
                //同时缓存前后章节。
                CacheChapterContent cacheChapterContent = new CacheChapterContent(currChapterIndx);
                cacheChapterContent.execute();

            }
        }
        else {
            //重绘制view.
           // mText = mText.substring(this.pageEndIndx, mText.length());
            Log.d(TAG,"turnNextPage() chapter.len="+chapter.getContent().length()+"pageNumindx ="+pageNumIndx);

            mText = chapter.getContent().substring(chapter.getPageNumList().get(pageNumIndx - 1 ),chapter.getPageNumList().get(pageNumIndx));
            this.postInvalidate();
            chapter.setCurrPageNumIndx(pageNumIndx);
            //BookDBHelper.writePageIndxToDB(pageNumIndx);
        }

    }

    private void turnPrevPage(){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        Chapter chapter = mb.getCurrChapter();

        Log.d(TAG,"pageTotal = "+chapter.getPageTotal());
        int pageNumIndx = chapter.getCurrPageNumIndx();
        Log.d(TAG,"pageNumIndx = "+pageNumIndx);
        pageNumIndx--;
        //Log.d(TAG,"pageNumIndx"+pageNumIndx);
        if(pageNumIndx < 0){
            Chapter prevChapter;
            if(mb.getCurrChapterIndx() == 0) {
                return;
            }
            else{
                prevChapter = mb.getPrevChapter();
            }

            if(prevChapter.isEmpty()) {
                gotoPrevChapter();
            }
            else{
                //跳转到前一章之前清空当前章节内容。
//                mb.getCurrChapter().setContent("");

                List<Integer> pageNumList = prevChapter.getPageNumList();
                int endPageIndx = pageNumList.size() - 1;
                mText = prevChapter.getContent().substring(pageNumList.get(endPageIndx - 1),pageNumList.get(endPageIndx));
                int currChapterIndx = mb.getCurrChapterIndx();
                currChapterIndx--;
                mb.setCurrChapterIndx(currChapterIndx);
                mb.getCurrChapter().setCurrPageNumIndx(endPageIndx);
                this.postInvalidate();

                //同时缓存前后章节。
                CacheChapterContent cacheChapterContent = new CacheChapterContent(currChapterIndx);
                cacheChapterContent.execute();
            }
        }
        else {
            //重绘制view.
            // mText = mText.substring(this.pageEndIndx, mText.length());
            if(pageNumIndx == 0){
                mText = chapter.getContent().substring(0, chapter.getPageNumList().get(pageNumIndx));
            }
            else {
                mText = chapter.getContent().substring(chapter.getPageNumList().get(pageNumIndx - 1), chapter.getPageNumList().get(pageNumIndx));
            }
            this.postInvalidate();
            Log.d(TAG,"pageNumIndx"+pageNumIndx);
            chapter.setCurrPageNumIndx(pageNumIndx);
            //BookDBHelper.writePageIndxToDB(pageNumIndx);
        }
    }

    private void gotoNextChapter(){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        int currChapterIndx = mb.getCurrChapterIndx();
        currChapterIndx++;
        if(currChapterIndx > mb.getChapterList().size()-1){
            Log.d(TAG,"end of file");
            return;
        }
        else{
            Log.d(TAG,"gonextchapter");
            //清空缓存
//            if(mb.getCachePreChapterIndx() != -1){
//                mb.getChapterList().get(mb.getCachePreChapterIndx()).setContent("");
//            }
//            if(mb.getCacheNextChapterIndx() != -1){
//                mb.getChapterList().get(mb.getCacheNextChapterIndx()).setContent("");
//            }
            //跳转下一章之前把当前章节的内容清空。
//            mb.getChapterList().get(currChapterIndx - 1).setContent("");
            mb.setCurrChapterIndx(currChapterIndx);
            //BookDBHelper.writeChapterIndxToDB(currChapterIndx);
            ShowChapterTask sct = new ShowChapterTask(this,true,false);
            sct.execute();
        }
    }

    //翻前一章要显示前一章的最后一页，继续改进。
    private void gotoPrevChapter(){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        int currChapterIndx = mb.getCurrChapterIndx();
        currChapterIndx--;
        if(currChapterIndx < 0){
            Log.d(TAG,"begin of file");
            return;
        }
        else{
            Log.d(TAG,"goPrevChapter");
            //清空缓存
//            if(mb.getCachePreChapterIndx() != -1){
//                mb.getChapterList().get(mb.getCachePreChapterIndx()).setContent("");
//            }
//            if(mb.getCacheNextChapterIndx() != -1){
//                mb.getChapterList().get(mb.getCacheNextChapterIndx()).setContent("");
//            }
//            //跳转到前一章之前把当前章节内容清空。
//            mb.getChapterList().get(currChapterIndx + 1).setContent("");
            mb.setCurrChapterIndx(currChapterIndx);
            //BookDBHelper.writeChapterIndxToDB(currChapterIndx);
            ShowChapterTask sct = new ShowChapterTask(this,true,true);
            sct.execute();
        }
    }

    private void popWindowHandler(MotionEvent event){
        int screenH = BookshelfApp.getBookshelfApp().getResources().getDisplayMetrics().heightPixels;
        Log.d(TAG,"screenH = "+screenH);
        //int imgH = ContextCompat.getDrawable(BookshelfApp.getBookshelfApp(),R.mipmap.setmenbg).getIntrinsicHeight();
        //int popWindowH = mPopupWindow.getHeight();
        LayoutInflater inflater = LayoutInflater.from(BookshelfApp.getBookshelfApp());
        View contentView = inflater.inflate(R.layout.setting_popwindow,null);
        int popWindowH  = contentView.getHeight();
        Log.d(TAG,"popWindowH = "+popWindowH);
        if(mPopupWindow!=null&&mPopupWindow.isShowing()){
            //点击坐标不在弹窗范围内, y坐标小于(屏高 - 弹窗背景高度),隐藏弹窗。
            if(event.getY() < screenH - popWindowH){
                mPopupWindow.dismiss();
                mPopupWindow = null;
            }
        }
        else{
            showPopWindow();
        }
    }

    private void showPopWindow(){
        LayoutInflater inflater = LayoutInflater.from(BookshelfApp.getBookshelfApp());
        View contentView = inflater.inflate(R.layout.setting_popwindow,null);
//        RelativeLayout relativeLayout = (RelativeLayout)contentView.findViewById(R.id.setting_layout);
//        Animation animation = AnimationUtils.loadAnimation(this.getContext(),R.anim.pop_bottomtotop);
//        LayoutAnimationController lac = new LayoutAnimationController(animation);
//        relativeLayout.setLayoutAnimation(lac);
        TextView bottom_line_tv = (TextView)inflater.inflate(R.layout.chapter_content,null).findViewById(R.id.bottom_line);
        mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT,true);
        //mPopupWindow.setAnimationStyle(R.style.SetPopWindow);
        //mPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(BookshelfApp.getBookshelfApp(),R.mipmap.setmenbg));
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        mPopupWindow.showAtLocation(this, Gravity.BOTTOM,0,0);
        Button btn1 = (Button)contentView.findViewById(R.id.btn_chapterList);
        Button btn2 = (Button)contentView.findViewById(R.id.btn_light);
        Button btn3 = (Button)contentView.findViewById(R.id.btn_setting);
        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                gotoChapterList(v);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                gotoChangeLight(v);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                gotoSetting(v);
            }
        });
    }

    //跳转到目录界面。
    private void gotoChapterList(View v){
        Intent intent = new Intent();
        //intent.setClass(this.getContext(),ChapterListActivity.class);
        intent.setClass(this.getContext(),ChapterlistAndBookmark.class);
        this.getContext().startActivity(intent);
        dismissPopWindow();
    }

    private void gotoChangeLight(View v){
        addBookmark();
    }

    private void gotoSetting(View v){

    }

    private void dismissPopWindow(){
        if(mPopupWindow!=null&&mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    private void addBookmark(){
        Log.d(TAG,"addBookmark");
        BookMark bookMark = new BookMark();
        Log.d(TAG,"mText = "+mText);
        bookMark.setContent(mText);
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        bookMark.setChapterIndx(mb.getCurrChapterIndx());
        Chapter chapter = mb.getCurrChapter();
        int pageNumIndx = chapter.getCurrPageNumIndx();
        bookMark.setPageNumIndx(pageNumIndx);
        int charBeginIndx = chapter.getBeginCharIndex()+ chapter.getPageNumList().get(pageNumIndx);
        //String percent = new DecimalFormat(".00%").format(charBeginIndx/mb.getCharTotalCount());
        Log.d(TAG,"charTotalCount="+mb.getCharTotalCount());
        //String percent = (float)(Math.round((charBeginIndx/mb.getCharTotalCount())*100))/100+"%";
        double percentNum = (double)charBeginIndx/mb.getCharTotalCount();
        float fp = new BigDecimal(percentNum).setScale(4,4).floatValue();
        String percent = fp*100+"%";
        Log.d(TAG,"pageNumIndx="+pageNumIndx+" charBeginIndx="+charBeginIndx+" percent="+percent);
        bookMark.setPercent(percent);
        //获取24小时制系统时间，大写HH表示24小时制，小写hh表示12小时制。
        String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
        bookMark.setTime(time);
        Log.d(TAG,"time="+time);
        mb.getBookMarkList().add(bookMark);
        mb.setCurrBookMarkIndx(mb.getBookMarkList().size() - 1);
        BookMark bookMarktwo = BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrBookMark();
        Log.d(TAG,"time="+bookMarktwo.getTime()+" percent="+bookMarktwo.getPercent());

    }
}
