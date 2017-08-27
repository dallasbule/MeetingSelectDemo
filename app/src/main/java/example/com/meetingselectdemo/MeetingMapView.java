package example.com.meetingselectdemo;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/10.
 */

public class MeetingMapView extends View {

    //用户绘制图形的画笔
    Paint paint = new Paint();

    //用于图形变换
    Matrix matrix = new Matrix();
    //用于每个小座位图的变换位移
    Matrix seatMatrix = new Matrix();
    //用于桌子的变换位移


    /**
     * 以下均为变量的定义
     *
     * @param context
     */

    //座位的水平间距
    private int horizontalSpacing;
    //座位的垂直间距
    private int vericalSpacing;
    //行数
    private int rowNum;
    //列数
    private int column;
    //会议室桌子的宽度
    private int tableWidth;
    //座位图距离上方的距离set
    //座位图距离上方的距离set
    private float spcingHeight;


    //座位已经选中
    private static final int SEAT_TYPE_SELECTED = 1;
    private int selectSeatId;
    //座位可选
    private static final int SEAT_TYPE_AVAILABLE = 0;
    private int availableSeatId;
    //不显示座位
    private static final int SEAT_NOT_SHOW = 2;
    //桌子
    private static final int CHAIR = 3;
    //可选时座位的图片
    Bitmap availableSeatBitmap;
    //选中时座位的图片
    Bitmap selectSeatBitmap;
    //桌子的图片
    Bitmap chairBitmap;
    //默认的座位图宽度
    private float defaultImgW = 50;
    //默认的座位图高度
    private float defaultImgH = 44;
    //整个座位图的高度
    private int totalSeatHeight;
    //整个座位图的宽度
    private int totalSeatWidth;

    private SeatChecker seatChecker;

    //人员列表数据
    private ArrayList<SeatBean> people = new ArrayList<>();
    //人员列表数据
    private ArrayList<Integer> seat = new ArrayList<>();
    //
    //    //已选中的人员
//    private ArrayList<String> selectPeople = new ArrayList<>();
    //存储选中的座位
//    ArrayList<Integer> selects = new ArrayList<>();
    //
    Map<Integer, SeatBean> peopleMap = new HashMap<Integer, SeatBean>();
    private int userId;

    //缩放比例
    float scaleX = 1;
    float scaleY = 1;

    /**
     * 标识是否正在缩放
     */
    boolean isScaling;
    //此处用来表示当前缩放的时候所触碰点的坐标
    float currentScaleX, currentScaleY;

    /**
     * 是否是第一次缩放
     */
    boolean firstScale = true;

    /**
     * 弹出框的列表
     */
    private ListView lv;

    //列表的adapter
    private MeetingParterAdapter mAdapter;

    //Y轴起始坐标
//    private int startY = getHeight() / 7;


    public MeetingMapView(Context context) {
        super(context);
    }

    public MeetingMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MeetingMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attr) {
        TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.SeatTableView);
        selectSeatId = typedArray.getResourceId(R.styleable.SeatTableView_seat_sold, R.drawable.seat_sold);
        availableSeatId = typedArray.getResourceId(R.styleable.SeatTableView_seat_available, R.drawable.seat_green);
        typedArray.recycle();

    }

    private void init() {
        availableSeatBitmap = BitmapFactory.decodeResource(getResources(), availableSeatId);
        selectSeatBitmap = BitmapFactory.decodeResource(getResources(), selectSeatId);

        horizontalSpacing = dip2Px(getContext(), 10);
        vericalSpacing = dip2Px(getContext(), 10);

        //缩放比列，主要用于资源图片的初始化大小，谨防图片过大导致
        scaleX = defaultImgW / availableSeatBitmap.getWidth();
        scaleY = defaultImgH / availableSeatBitmap.getHeight();
        //计算整个图的高
        totalSeatWidth = (int) (column * availableSeatBitmap.getWidth() * scaleX + (column - 1) * horizontalSpacing);
        totalSeatHeight = (int) (rowNum * availableSeatBitmap.getHeight() * scaleY + (rowNum - 1) * vericalSpacing);

        //桌子的高度
        spcingHeight = dip2Px(getContext(), 60);

//        matrix.postTranslate(horizontalSpacing, tableHeight + vericalSpacing * 3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (rowNum <= 0 || column <= 0) {
            return;
        }
        drawSeat(canvas);
//        drawTable(canvas);
    }

    //第一次按下的坐标
    private int downX;
    private int downY;
    //离开的时候坐标
    private int endX;
    private int endY;
    private float spacingHeight2 = spcingHeight;
    private float spacingHeight3 = Math.abs(getHeight() - spcingHeight - totalSeatHeight);

    //对界面的操控，主要是平移
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        int x = (int) event.getX();
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                invalidate();
                break;
            //判断是为了到达边界不在移动
            case MotionEvent.ACTION_MOVE:
                int distanceX = Math.abs(x - downX);
                int distanceY = Math.abs(y - downY);
                int translateX = x - endX;
                int translateY = y - endY;
                if (getHeight() < spcingHeight + totalSeatHeight * getMatrixScaleY()) {
                    if (spcingHeight < Math.abs(getHeight() - spcingHeight - totalSeatHeight * getMatrixScaleY())) {
                        spacingHeight2 = Math.abs(getHeight() - spcingHeight - totalSeatHeight * getMatrixScaleY());
                        spacingHeight3 = 0;
                    }
                } else {
                    spacingHeight2 = spcingHeight;
                    spacingHeight3 = Math.abs(getHeight() - spcingHeight - totalSeatHeight * getMatrixScaleY());
                }
                if (Math.abs(getTranslateX()) < Math.abs((getWidth() - totalSeatWidth * getMatrixScaleX()) / 2)
                        && getTranslateY() >= 0 - spacingHeight2
                        && getTranslateY() <= spacingHeight3) {
                    if (distanceX > 10 || distanceY > 10) {
                        matrix.postTranslate(translateX, translateY);
                        invalidate();
                    }
                } else {
                    if (getTranslateX() < 0 && getTranslateY() < 0) {
                        if ((distanceX > 10 || distanceY > 10) && translateX >= 0 && translateY >= 0) {
                            matrix.postTranslate(translateX, translateY);
                            invalidate();
                        }
                    }
                    if (getTranslateX() < 0 && getTranslateY() > 0) {
                        if ((distanceX > 10 || distanceY > 10) && translateX >= 0 && translateY <= 0) {
                            matrix.postTranslate(translateX, translateY);
                            invalidate();
                        }
                    }
                    if (getTranslateX() > 0 && getTranslateY() < 0) {
                        if ((distanceX > 10 || distanceY > 10) && translateX <= 0 && translateY >= 0) {
                            matrix.postTranslate(translateX, translateY);
                            invalidate();
                        }
                    }
                    if (getTranslateX() > 0 && getTranslateY() > 0) {
                        if ((distanceX > 10 || distanceY > 10) && translateX <= 0 && translateY <= 0) {
                            matrix.postTranslate(translateX, translateY);
                            invalidate();
                        }
                    }
                }
                break;
        }
        endX = x;
        endY = y;

        return true;
    }
//        path.lineTo(centerX - tableWidth / 2, tableHeight + startY + getTranslateY());

//    private void drawTable(Canvas canvas) {
//        //绘制桌子的paint初始化
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.GREEN);
//        paint.setAntiAlias(true);
//
//        float centerX = getWidth() / 2 + getTranslateX();
//        float tableWidth = totalSeatWidth * 0.5f;
//        if (tableWidth < this.tableWidth) {
//            tableWidth = this.tableWidth;
//        }
//
//        //开始绘制
//        Path path = new Path();
//        path.moveTo(centerX, startY + getTranslateY());
//        path.lineTo(centerX - tableWidth / 2, startY + getTranslateY());
//        path.lineTo(centerX + tableWidth / 2, tableHeight + startY + getTranslateY());
//        path.lineTo(centerX + tableWidth / 2, startY + getTranslateY());
//
//        canvas.drawPath(path, paint);
//
//    }

    //绘制座位图

    private void drawSeat(Canvas canvas) {
        //获取缩放的比列
        float zoom = getMatrixScaleX();
        //获取x轴平移距离
        float translateX = getTranslateX();
        //获取Y轴平移距离
        float translateY = getTranslateY();

        for (int i = 0; i < rowNum; i++) {
            //this.scaleY是座位资源图片的初始化缩放比例
            float top = i * availableSeatBitmap.getHeight() * this.scaleY * zoom + i * vericalSpacing * zoom + spcingHeight + translateY;

            float bottom = top + availableSeatBitmap.getHeight() * this.scaleY * zoom;
            //如果平移超过屏幕，则不继续绘制
            if (bottom < 0 || top > getHeight()) {
                continue;
            }
            for (int j = 0; j < column; j++) {
                float left = j * availableSeatBitmap.getWidth() * this.scaleX * zoom + j * horizontalSpacing * zoom + translateX + (getWidth() - totalSeatWidth * getMatrixScaleX()) / 2;
                float right = availableSeatBitmap.getWidth() * this.scaleX * zoom + left;
                if (right < 0 || left > getWidth()) {
                    continue;
                }
                //根据行列数搜索座位判定其状态
                int seatType = getSeatType(i, j);
                //平移获取新图
                seatMatrix.setTranslate(left, top);
                //按照比列缩放初始化每个座位图
                seatMatrix.postScale(this.scaleX * zoom, this.scaleY * zoom, left, top);
                //根据获取的状态来显示
                switch (seatType) {
                    case CHAIR:
                        break;
                    case SEAT_NOT_SHOW:
                        break;
                    case SEAT_TYPE_AVAILABLE:
                        canvas.drawBitmap(availableSeatBitmap, seatMatrix, paint);
                        break;
                    case SEAT_TYPE_SELECTED:
                        canvas.drawBitmap(selectSeatBitmap, seatMatrix, paint);
                        break;
                }
            }

        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public static int dip2Px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //此接口用于改变和存储座位状态
    public interface SeatChecker {
        /**
         * 是否可用座位
         *
         * @param row
         * @param column
         * @return
         */
        boolean isValidSeat(int row, int column);

        /**
         * 是否已经选择
         *
         * @param row
         * @param column
         * @return
         */
        boolean isSelect(int row, int column);

        void checked(int row, int column);

        void unCheck(int row, int column);

    }

    float[] m = new float[9];

    private float getTranslateX() {
        matrix.getValues(m);
        return m[Matrix.MTRANS_X];
    }

    private float getTranslateY() {
        matrix.getValues(m);
        return m[Matrix.MTRANS_Y];
    }

    //设置行数和列数
    public void setRowAndColumn(int rowNum, int column) {
        this.rowNum = rowNum;
        this.column = column;

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < column; j++) {
                seat.add(0);
            }
        }

        init();
        invalidate();
    }

    public void setSeatChecker(SeatChecker seatChecker) {
        this.seatChecker = seatChecker;
        invalidate();
    }

    /***
     * 以下是点击事件的
     */


//    private int isHave(Integer seat) {
//        //使用二分查找法查找并返回所在数组位置，判定是否已经被选中
//        return Collections.binarySearch(selects, seat);
//    }

    //为每一个位置分配一个唯一的id用作数组下坐标
    private int getID(int row, int column) {
        return row * this.column + column;
    }

    //获取是否选取，即该座位是否被选取
    private int getSeatType(int row, int column) {

        if (seat.get(getID(row, column)) == 1) {
            return SEAT_TYPE_SELECTED;
        }
        if (seatChecker != null) {
            if (!seatChecker.isValidSeat(row, column)) {
                return SEAT_NOT_SHOW;
            }
            if (seatChecker.isSelect(row, column)) {
                return SEAT_TYPE_SELECTED;
            }
        }

        return SEAT_TYPE_AVAILABLE;
    }

    private void remove(int row, int column) {
        int id = getID(row, column);
        seat.set(id, 0);
    }


    /**
     * 方法内判断是为了将选中的座位顺序按照数组下标大小排列
     *
     * @param row
     * @param column
     */
    private void addChooseSeat(int row, int column, SeatBean people) {
        int id = getID(row, column);
        if (people.isSetSeat() == true) {
            remove(people.getRow(), people.getColumn());
            people.setColumn(column);
            people.setId(id);
            people.setRow(row);
        }else {
            people.setSetSeat(true);
            people.setColumn(column);
            people.setRow(row);
            people.setId(id);
        }
        seat.set(id, 1);
    }

    GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            int x = (int) e.getX();
            int y = (int) e.getY();
            String text = "";

            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < column; j++) {
                    /**获取屏幕每个座位的坐标范围
                     *
                     */
                    int top = (int) (i * availableSeatBitmap.getHeight() * scaleY * getMatrixScaleY()
                            + i * vericalSpacing * getMatrixScaleY() + spcingHeight + getTranslateY());

                    int bottom = (int) (top + availableSeatBitmap.getHeight() * scaleY * getMatrixScaleY());


                    int left = (int) (j * availableSeatBitmap.getWidth() * scaleX * getMatrixScaleX()
                            + j * horizontalSpacing * getMatrixScaleX() + getTranslateX() + (getWidth() - totalSeatWidth * getMatrixScaleX()) / 2);
                    int right = (int) (availableSeatBitmap.getWidth() * scaleX * getMatrixScaleX() + left);

                    if (seatChecker != null && seatChecker.isValidSeat(i, j)) {
                        if (x >= left && x <= right && y >= top && y <= bottom) {
                            int id = getID(i, j);
                            if (seat.get(id) == 1) {
                                for (int m = 0; m < people.size(); m++) {
                                    Log.e("********",people.get(m).getId()+"");
                                    if (getID(i, j) == people.get(m).getId()) {
                                        text = people.get(m).getUserName();
                                        break;
                                    }
                                }
                            } else {
                                text = "无人";
                            }
                            break;
                        }
                    }
                }
            }
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int x = (int) e.getX();
            int y = (int) e.getY();

            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < column; j++) {
                    /**获取屏幕每个座位的坐标范围
                     *
                     */
                    int top = (int) (i * availableSeatBitmap.getHeight() * scaleY * getMatrixScaleY()
                            + i * vericalSpacing * getMatrixScaleY() + spcingHeight + getTranslateY());

                    int bottom = (int) (top + availableSeatBitmap.getHeight() * scaleY * getMatrixScaleY());


                    int left = (int) (j * availableSeatBitmap.getWidth() * scaleX * getMatrixScaleX() + j * horizontalSpacing * getMatrixScaleX() + getTranslateX() + (getWidth() - totalSeatWidth * getMatrixScaleX()) / 2);
                    int right = (int) (availableSeatBitmap.getWidth() * scaleX * getMatrixScaleX() + left);

                    if (seatChecker != null && seatChecker.isValidSeat(i, j)) {
                        if (x >= left && x <= right && y >= top && y <= bottom) {

                            if (seat.get(getID(i, j)) == 1) {
                                for (int m = 0; m < people.size(); m++) {
                                    if (getID(i, j) == people.get(m).getId()) {
                                        people.get(m).setSetSeat(false);
                                    }
                                }
                                remove(i, j);
                                if (seatChecker != null) {
                                    seatChecker.unCheck(i, j);
                                }
                                invalidate();
                            } else {
                                showPeopleDialog(i, j);
                            }
                            float currentScale = getMatrixScaleY();
                            if (currentScaleY < 1.7) {
                                currentScaleX = x;
                                currentScaleY = y;
                                zoomAnimate(currentScale, 1.9f);
                                invalidate();
                            }
                            break;
                        }
                    }
                }
            }
            return super.onSingleTapConfirmed(e);
        }
    });

    /**
     * 人员列表弹出框
     */

    private void showPeopleDialog(final int rowNum, final int column) {
        View settingView = LayoutInflater.from(getContext()).inflate(R.layout.listview, null);
        lv = (ListView) settingView.findViewById(R.id.lv);
        // 实例化自定义的MyAdapter
        mAdapter = new MeetingParterAdapter(people, this.getContext());
        lv.setAdapter(mAdapter);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext(), AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("参会人员")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();

                    }
                }).create();
        dialog.setView(settingView, 0, 0, 0, 0);
        dialog.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // 改变CheckBox的状态
                Log.e("@@@@@@@@@@@@",arg2+"");
                addChooseSeat(rowNum, column, people.get(arg2));
                if (seatChecker != null) {
                    seatChecker.checked(rowNum, column);
                }
                mAdapter.notifyDataSetChanged();
                invalidate();
                dialog.dismiss();
            }

        });
//        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
//        WindowManager m = (WindowManager) getContext()
//                .getSystemService(Context.WINDOW_SERVICE);
//        Display d = m.getDefaultDisplay();
//        params.height = (int) (d.getHeight() * 0.7);
//        params.width = d.getWidth();
//        dialog.getWindow().setAttributes(params);
//        new AlertDialog.Builder(getContext())
//                .setTitle("请选择人员")
//                .setSingleChoiceItems(people.toArray(new String[people.size()]), 0, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        userId = i;
//                    }
//                })
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        addChooseSeat(rowNum, column, userId);
//                        if (seatChecker != null) {
//                            seatChecker.checked(rowNum, column);
//                        }
//                        invalidate();
//                        dialogInterface.dismiss();
//                    }
//                })
//                .setNegativeButton("取消", null)
//                .show();
    }

    public void setData(String[] people) {
        for (int i = 0; i < people.length; i++) {
            SeatBean selectbean = new SeatBean();
            selectbean.setUserName(people[i]);
            selectbean.setSetSeat(false);
            selectbean.setUserId(-1);
            this.people.add(selectbean);
        }
        for (int i = 0; i < people.length; i++) {
            Log.e("*(****", this.people.get(i).getUserName());
        }
    }

    private float getMatrixScaleY() {
        matrix.getValues(m);
        return m[4];
    }

    private float getMatrixScaleX() {
        matrix.getValues(m);
        return m[Matrix.MSCALE_X];
    }

    class ScaleAnimation implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float zoom = (float) valueAnimator.getAnimatedValue();
            setZoom(zoom);
        }
    }

    /**
     * 此方法用于缩放时候通过获取的当前缩放比列来设置动画
     */
    private void setZoom(float zoom) {
        float z = zoom / getMatrixScaleX();
        matrix.postScale(z, z, currentScaleX, currentScaleY);
        invalidate();
    }

    /**
     * 缩放的动画效果
     *
     * @param cur
     * @param tar
     */
    private void zoomAnimate(float cur, float tar) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(cur, tar);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        ScaleAnimation zoomAnim = new ScaleAnimation();
        valueAnimator.addUpdateListener(zoomAnim);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    /**
     * 缩放手势的监听
     */

    ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            isScaling = true;
            float scaleFactor = detector.getScaleFactor();
            if (getMatrixScaleY() * scaleFactor > 3) {
                scaleFactor = 3 / getMatrixScaleY();
            }
            if (firstScale) {
                currentScaleX = detector.getCurrentSpanX();
                currentScaleY = detector.getCurrentSpanY();
                firstScale = false;
            }

            if (getMatrixScaleY() * scaleFactor < 0.5) {
                scaleFactor = 0.5f / getMatrixScaleY();
            }
            matrix.postScale(scaleFactor, scaleFactor, currentScaleX, currentScaleY);
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
            firstScale = true;
        }
    });
}
