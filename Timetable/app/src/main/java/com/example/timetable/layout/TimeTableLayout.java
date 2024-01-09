package com.example.timetable.layout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.timetable.view.RoundTextView;
import com.google.android.material.navigation.NavigationView;
import com.example.timetable.course.Course;
import com.example.timetable.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeTableLayout extends LinearLayout {

    //夏季时间
    private String[] time_summer = {"8:00-8:50","9:00-9:50","10:10-11:00","11:10-12:00","14:00-14:50","15:00-15:50","16:10-17:00","17:10-18:00","18:30-19:20","19:30-20:20"};
    //冬季时间
    private String[] time_winter = {"8:00-8:50","9:00-9:50","10:10-11:00","11:10-12:00","13:30-14:20","14:30-15:20","15:40-16:30","16:40-17:30","18:00-18:50","19:00-19:50"};
    //星期
    private String[] weekTitle = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    //最大星期数
    private int weeksNum = weekTitle.length;
    //最大节数
    private int maxSection = 10;

    //圆角半径
    private int radius = 8;
    //线宽
    private int tableLineWidth = 1;
    //数字字体大小
    private int numberSize = 14;
    //标题字体大小
    private int titleSize = 18;
    //课表信息字体大小
    private int courseSize = 12;


    //单元格高度
    private int cellHeight = 75;
    //星期标题高度
    private int titleHeight = 30;
    //最左边数字宽度
    private int numberWidth = 20;

    private Context mContext;
    private List<Course> courseList;
    private Map<String, Integer> colorMap = new HashMap<>();
    private Map<Integer, List<Course>> courseMap = new HashMap<>();

    //开学日期
    private Date startDate;
    private long weekNum;

    //菜单栏
    private ImageView mCategory;
    //周次信息
    private TextView mWeekTitle;
    private LinearLayout mMainLayout;
    private RelativeLayout mTitleLayout;
    private LinearLayout mDaysLayout;
    private NavigationView navigationView;  //右滑菜单
    private int currentX;
    private SharedPreferences sp;

    public void setSp(SharedPreferences sp) {
        this.sp = sp;
    }


    public TimeTableLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        prepareParams();
        addWeekTitle(this);  //添加周次标签

    }


    /**
     * 数据预处理 :和手机大小匹配
     */
    private void prepareParams() {
        tableLineWidth = dip2px(tableLineWidth);
        cellHeight = dip2px(cellHeight);
        titleHeight = dip2px(titleHeight);
        numberWidth = dip2px(numberWidth);
    }


    /**
     *加载数据
     * @param courses  课程信息
     * @param date     开学时间
     */
    public void loadData(List<Course> courses, Date date) {
        this.courseList = courses;
        this.startDate = date;
        this.weekNum = calcWeek(startDate);  //第几周
        if (this.weekNum>19){
            this.weekNum=19;
        }
        handleData(courseMap, courseList, weekNum);
        flushView(courseMap, weekNum);
    }

    /**
     * 处理数据
     * @param courseMap 处理结果
     * @param courseList 数据
     * @param weekNum 周次
     */
    private void handleData(Map<Integer, List<Course>> courseMap, List<Course> courseList, long weekNum) {
        courseMap.clear();
        //初始化courseMap
        for(int i=1;i<=7;i++){
            courseMap.put(i,new ArrayList<Course>());
        }
        //加入Map
        for (Course c : courseList) {
            List<Map<String,Integer>> listWeeks = c.getListWeeks();
            boolean flag = false;
            for(Map<String,Integer> m:listWeeks){
                if(m.get("start")<=weekNum && m.get("end")>=weekNum){
                    flag=true;
                }
            }
            if (!flag){
                continue;
            }
            if(c.getWeekType().equals("单")){
                if(weekNum%2==0){
                    continue;
                }
            }else if(c.getWeekType().equals("双")){
                if (weekNum%2!=0){
                    continue;
                }
            }
            courseMap.get(c.getDay()).add(c);
        }
    }



    /**
     * 设置菜单按钮的监听事件
     * @param listener
     */
    public void addListener(OnClickListener listener){
        mCategory.setOnClickListener(listener);
    }


    /**
     * 刷新课程视图
     * @param courseMap 处理好的课程数据
     * @param weekNum 周次
     */
    private void flushView(Map<Integer, List<Course>> courseMap, long weekNum) {

        //星期标签
        if(null != mDaysLayout)    this.removeView(mDaysLayout);
        addWeekLabel(this,weekNum);

        //初始化主布局   删除之前的
        if (null != mMainLayout) removeView(mMainLayout);
        mMainLayout = new LinearLayout(mContext);
        mMainLayout.setOrientation(HORIZONTAL);
        mMainLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mMainLayout);
        //周次标题
        mWeekTitle.setText("第 " + weekNum + " 周");
        //左侧节次标签
        addLeftNumber(mMainLayout);
        //课程信息
        if (null == courseMap || courseMap.isEmpty()) {//数据为空
            addVerticalTableLine(mMainLayout);
            TextView emptyLayoutTextView = createTextView("已结课，或未添加课程信息！", titleSize, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, getResources().getColor(R.color.textColor), Color.WHITE);
            mMainLayout.addView(emptyLayoutTextView);
        } else {//不为空
            for (int i = 1; i <= weeksNum; i++) {
                addVerticalTableLine(mMainLayout);
                addDayCourse(mMainLayout, courseMap, i);
            }
            //姓名 学号信息
            View headerView = navigationView.getHeaderView(0);
            TextView tv_name = headerView.findViewById(R.id.name);
            TextView tv_user = headerView.findViewById(R.id.username);
            tv_user.setText(sp.getString("username","空"));
            tv_name.setText(sp.getString("name","空"));
        }
        invalidate();
    }

    /**
     * 设置周次标题和菜单栏
     * @param pViewGroup 父组件
     */
    private void addWeekTitle(ViewGroup pViewGroup) {
        mTitleLayout = new RelativeLayout(mContext);
        mTitleLayout.setPadding(8, 15, 8, 15);
        mTitleLayout.setBackgroundColor(getResources().getColor(R.color.titleColor));
        //周次信息
        mWeekTitle = new TextView(mContext);
        mWeekTitle.setTextSize(titleSize);
        mWeekTitle.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mWeekTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        mTitleLayout.addView(mWeekTitle);
        //左侧菜单栏
        mCategory = new ImageView(mContext);
        mCategory.setImageResource(R.drawable.category);
        mCategory.setLayoutParams(new LayoutParams(dip2px(30), dip2px(30)));
        mTitleLayout.addView(mCategory);

        pViewGroup.addView(mTitleLayout);
        addHorizontalTableLine(pViewGroup);
    }

    /**
     * 设置星期和日期
     * @param pViewGroup  父组件
     * @param weekNum   周次
     */
    private void addWeekLabel(ViewGroup pViewGroup,long weekNum) {
        mDaysLayout = new LinearLayout(mContext);
        mDaysLayout.setOrientation(HORIZONTAL);
        mDaysLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, titleHeight*2));
        addView(mDaysLayout);
        //空白符
        TextView space = new TextView(mContext);
        space.setLayoutParams(new ViewGroup.LayoutParams(numberWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        space.setBackgroundColor(getResources().getColor(R.color.titleColor));
        mDaysLayout.addView(space);

        //星期
        try {
            // 本周第一天
            long date = sp.getLong("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-02-28 00:00:00").getTime())+1000*60*60*24*7*(weekNum-1);

            for (int i = 0; i < weeksNum; i++) {
                addVerticalTableLine(mDaysLayout);
                String month = new SimpleDateFormat("MM").format(new Date(date));
                String day = new SimpleDateFormat("dd").format(new Date(date));
                TextView title = createTextView(weekTitle[i]+"\n"+Integer.valueOf(month)+"."+Integer.valueOf(day), titleSize, 0, titleHeight*2, 1, getResources().getColor(R.color.textColor), getResources().getColor(R.color.titleColor));
                String month_today = new SimpleDateFormat("MM").format(new Date().getTime());
                String day_today = new SimpleDateFormat("dd").format(new Date().getTime());
                if (month.equals(month_today) && day.equals(day_today)){
                    title.setBackgroundColor(11111);
                }
                mDaysLayout.addView(title);
                date+=1000*60*60*24;

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * 添加左侧节次信息和上课时间
     * @param pViewGroup  父组件
     */
    private void addLeftNumber(ViewGroup pViewGroup) {
        LinearLayout leftLayout = new LinearLayout(mContext);
        leftLayout.setOrientation(VERTICAL);
        leftLayout.setLayoutParams(new LayoutParams(numberWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

        int time = sp.getInt("time",1);

        String[] t;
        if (time == 0){
            t = time_summer;
        }else{
            t = time_winter;
        }
        for (int i = 1; i <= maxSection; i++) {
            addHorizontalTableLine(leftLayout);
            LinearLayout time_date = new LinearLayout(mContext);
            time_date.setOrientation(VERTICAL);
            time_date.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cellHeight, 1));

            TextView number1 = createTextView(t[i-1].split("-")[0], 7, ViewGroup.LayoutParams.MATCH_PARENT, cellHeight/32*13, 0, getResources().getColor(R.color.textColor), Color.WHITE);
            TextView number2 = createTextView(String.valueOf(i), numberSize, ViewGroup.LayoutParams.MATCH_PARENT, cellHeight/32*8, 0, getResources().getColor(R.color.textColor), Color.WHITE);
            TextView number3 = createTextView(t[i-1].split("-")[1], 7, ViewGroup.LayoutParams.MATCH_PARENT, cellHeight/32*13, 0, getResources().getColor(R.color.textColor), Color.WHITE);
            time_date.addView(number1);
            time_date.addView(number2);
            time_date.addView(number3);

            leftLayout.addView(time_date);
        }
        pViewGroup.addView(leftLayout);
    }

    /**
     * 设置单天课程信息
     * @param pViewGroup  父组件
     * @param courseMap   处理好的信息
     * @param day         星期几
     */
    private void addDayCourse(ViewGroup pViewGroup, Map<Integer, List<Course>> courseMap, int day) {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        linearLayout.setOrientation(VERTICAL);
        List<Course> courses = getCourses(courseMap, day);
        if (null != courses) {   //展示一天的课
            for (int i = 0, size = courses.size(); i < size; i++) {
                Course course = courses.get(i);
                int s_section = course.getS_section();
                int e_section = course.getE_section();
                if (i == 0) addBlankCell(linearLayout, s_section - 1);
                else
                    addBlankCell(linearLayout, course.getS_section() - courses.get(i - 1).getE_section() - 1);
                addCourseCell(linearLayout, course);
                if (i == size - 1) addBlankCell(linearLayout, maxSection - e_section);
            }
        } else {
            addBlankCell(linearLayout, maxSection);
        }
        pViewGroup.addView(linearLayout);
    }

    /**
     * 获取单天课程信息
     * @param courseMap   处理好的信息
     * @param day   第几天
     * @return
     */
    public List<Course> getCourses(Map<Integer, List<Course>> courseMap, int day) {
        final List<Course> courses = courseMap.get(day);
        if (null != courses) {
            Collections.sort(courses, new Comparator<Course>() {
                @Override
                public int compare(Course o1, Course o2) {
                    return o1.getS_section() - o2.getS_section();
                }
            });
        }
        return courses;
    }

    /**
     * 添加课程单元格
     * @param pViewGroup 父组件
     * @param course 课程信息
     */
    private void addCourseCell(ViewGroup pViewGroup, Course course) {
        addHorizontalTableLine(pViewGroup);
        RoundTextView textView = new RoundTextView(mContext, radius, getColor(colorMap, course.getCourseName()));
        textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cellHeight *(course.getE_section()-course.getS_section()+1) + tableLineWidth*(course.getE_section()-course.getS_section())));
        textView.setTextSize(courseSize);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setText(String.format("%s\n@%s", course.getCourseName(),  course.getClassroom()));
        pViewGroup.addView(textView);
    }


    /**
     * 添加空白单元格
     *
     * @param pViewGroup 父组件
     * @param num        空白单元格数量
     */
    private void addBlankCell(ViewGroup pViewGroup, int num) {
        for (int i = 0; i < num; i++) {
            addHorizontalTableLine(pViewGroup);
            TextView blank = new TextView(mContext);
            blank.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, cellHeight));
            pViewGroup.addView(blank);
        }
    }

    /**
     * 添加垂直线
     *
     * @param pViewGroup 父组件
     */
    private void addVerticalTableLine(ViewGroup pViewGroup) {
        View view = new View(mContext);
        view.setLayoutParams(new ViewGroup.LayoutParams(tableLineWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setBackgroundColor(getResources().getColor(R.color.viewLine));
        pViewGroup.addView(view);
    }

    /**
     * 添加水平线
     *
     * @param pViewGroup 父组件
     */
    private void addHorizontalTableLine(ViewGroup pViewGroup) {
        View view = new View(mContext);
        view.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, tableLineWidth));
        view.setBackgroundColor(getResources().getColor(R.color.viewLine));
        pViewGroup.addView(view);
    }

    /**
     * 创建TextView
     *
     * @param content    文本内容
     * @param color  字体颜色
     * @param size   字体大小
     * @param width  宽度
     * @param height 高度
     * @param weight 权重
     * @return
     */
    private TextView createTextView(String content, int size, int width, int height, int weight, int color, int bkColor) {
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(new LayoutParams(width, height, weight));
        if(bkColor != -1)textView.setBackgroundColor(bkColor);
        textView.setTextColor(color);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(size);
        textView.setText(content);
        return textView;
    }

    private void toggleWeek(int flag){
        if(flag < 0){
            weekNum = weekNum - 1 <= 0 ? weekNum : weekNum - 1;
        }else{
            weekNum = weekNum + 1 > 19  ? weekNum : weekNum + 1;
        }
        handleData(courseMap, courseList, weekNum);
        flushView(courseMap, weekNum);
    }

    /**
     * 计算当前周次
     * @param date  时间戳
     * @return
     */
    private long calcWeek(Date date) {
        return (new Date().getTime() - date.getTime()) / (1000 * 3600 * 24 * 7) + 1;

    }

    private int getColor(Map<String, Integer> map, String name) {
        Integer tip = map.get(name);
        if (null != tip) {
            return tip;
        } else {
            int i = getResources().getColor(color[map.size() % color.length]);
            map.put(name, i);
            return i;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * @param dpValue
     * @return
     */
    private int dip2px(float dpValue) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    private int color[] = {
            R.color.one, R.color.two, R.color.three,
            R.color.four, R.color.five, R.color.six,
            R.color.seven, R.color.eight, R.color.nine,
            R.color.ten, R.color.eleven, R.color.twelve,
            R.color.thirteen, R.color.fourteen, R.color.fifteen
    };

    /**
     * 界面点击事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                currentX = (int) event.getX();
                Resources resources = this.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                float density = dm.density;
                int width = dm.widthPixels;
                int height = dm.heightPixels;
                if(currentX<=width/4*1){
                    toggleWeek(-1);
                }else if(currentX>=width/4*3){
                    toggleWeek(1);
                }
                break;
        }
        return true;
    }

    public void setNavigationView(NavigationView navigationView) {
        this.navigationView = navigationView;
    }

    public void setMaxSection(int maxSection) {
        this.maxSection = maxSection;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setTableLineWidth(int tableLineWidth) {
        this.tableLineWidth = tableLineWidth;
    }

    public void setNumberSize(int numberSize) {
        this.numberSize = numberSize;
    }

    public void setTitleSize(int titleSize) {
        this.titleSize = titleSize;
    }

    public void setCourseSize(int courseSize) {
        this.courseSize = courseSize;
    }


    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }

    public void setTitleHeight(int titleHeight) {
        this.titleHeight = titleHeight;
    }

    public void setNumberWidth(int numberWidth) {
        this.numberWidth = numberWidth;
    }
}
