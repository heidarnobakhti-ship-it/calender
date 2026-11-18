package com.example.aban1405;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private int selectedMonth;
    private int[] counts;
    private DayCell[] cells;
    private LinearLayout root;
    private final String[] monthNames = {"آبان", "آذر", "دی", "بهمن", "اسفند"};
    private final int[] monthDays = {30,30,30,30,29};
    // Saturday-first offsets for 1 Aban through 1 Esfand 1405.
    private final int[] firstDayOffsets = {6,1,3,5,0};
    private final String[] persianDigits = {"۰","۱","۲","۳","۴","۵","۶","۷","۸","۹"};

    private String fa(int n) {
        String s = String.valueOf(n), out = "";
        for (int i=0;i<s.length();i++) out += persianDigits[s.charAt(i)-'0'];
        return out;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable bg(int color, int radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        if (strokeColor != 0) d.setStroke(dp(1), strokeColor);
        return d;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private void setupRoot() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(14));
        root.setBackgroundColor(Color.rgb(243,247,246));
        setContentView(root);
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(16,44,50));
        getWindow().setNavigationBarColor(Color.rgb(16,44,50));
        prefs = getSharedPreferences("aban_marks", MODE_PRIVATE);
        selectedMonth = prefs.getInt("last_month", -1);
        if (selectedMonth >= 0 && selectedMonth < 5) showCalendar();
        else showMonthPicker();
    }

    private void showMonthPicker() {
        setupRoot();
        root.addView(text("تقویم سال ۱۴۰۵", 27, Color.rgb(20,53,58), true),
                new LinearLayout.LayoutParams(-1, dp(58)));
        root.addView(text("ماه موردنظر را انتخاب کنید", 15, Color.rgb(91,116,117), false),
                new LinearLayout.LayoutParams(-1, dp(42)));

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams listP = new LinearLayout.LayoutParams(-1, 0, 1f);
        listP.topMargin = dp(12); listP.bottomMargin = dp(12);
        root.addView(list, listP);

        for (int i=0; i<monthNames.length; i++) {
            final int m = i;
            LinearLayout item = new LinearLayout(this);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(18), 0, dp(18), 0);
            item.setBackground(bg(Color.WHITE, 16, Color.rgb(222,233,230)));
            TextView name = text(monthNames[i], 21, Color.rgb(28,57,60), true);
            name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            item.addView(name, new LinearLayout.LayoutParams(0, -1, 1f));
            TextView range = text(fa(monthDays[i]) + " روز", 13, Color.rgb(91,116,117), false);
            item.addView(range, new LinearLayout.LayoutParams(dp(76), -1));
            TextView arrow = text("‹", 30, Color.rgb(22,124,128), true);
            item.addView(arrow, new LinearLayout.LayoutParams(dp(28), -1));
            item.setOnClickListener(v -> {
                selectedMonth = m;
                prefs.edit().putInt("last_month", m).apply();
                showCalendar();
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(66));
            p.setMargins(0, dp(7), 0, dp(7));
            list.addView(item, p);
        }

        root.addView(text("با انتخاب ماه، تقویم همان ماه باز می‌شود.", 12,
                Color.rgb(120,140,139), false), new LinearLayout.LayoutParams(-1, dp(36)));
    }

    private void showCalendar() {
        counts = new int[monthDays[selectedMonth] + 1];
        cells = new DayCell[monthDays[selectedMonth] + 1];
        for (int d=1; d<counts.length; d++)
            counts[d] = prefs.getInt("m"+selectedMonth+"_day_"+d, 0);

        setupRoot();
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(52)));

        Button change = new Button(this);
        change.setText("انتخاب ماه");
        change.setTextSize(12); change.setAllCaps(false);
        change.setTextColor(Color.WHITE);
        change.setBackground(bg(Color.rgb(22,124,128), 12, 0));
        top.addView(change, new LinearLayout.LayoutParams(dp(112), dp(44)));
        change.setOnClickListener(v -> showMonthPicker());

        TextView title = text(monthNames[selectedMonth] + " ۱۴۰۵", 25,
                Color.rgb(20,53,58), true);
        top.addView(title, new LinearLayout.LayoutParams(0, -1, 1f));

        root.addView(text("برای اضافه‌کردن خط، روی عدد روز بزنید", 13,
                Color.rgb(91,116,117), false), new LinearLayout.LayoutParams(-1, dp(38)));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(7);
        grid.setRowCount(7);
        grid.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, 0, 1f);
        gp.topMargin = dp(8); gp.bottomMargin = dp(10);
        root.addView(grid, gp);

        String[] weekdays = {"شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه"};
        for (String day : weekdays) {
            TextView w = text(day, 10, Color.rgb(65,91,92), true);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width=0; p.height=dp(40); p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
            p.setMargins(dp(1),dp(1),dp(1),dp(4));
            grid.addView(w,p);
        }
        for (int i=0; i<firstDayOffsets[selectedMonth]; i++) {
            View blank = new View(this);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width=0; p.height=0; p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
            grid.addView(blank,p);
        }
        for (int day=1; day<=monthDays[selectedMonth]; day++) {
            final int d = day;
            DayCell cell = new DayCell(this, day, counts[day]);
            cells[day] = cell;
            cell.setBackground(bg(Color.WHITE, 12, Color.rgb(222,233,230)));
            cell.setOnClickListener(v -> {
                counts[d]++;
                prefs.edit().putInt("m"+selectedMonth+"_day_"+d, counts[d]).apply();
                cells[d].setCount(counts[d]);
            });
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width=0; p.height=0;
            p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
            p.rowSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
            p.setMargins(dp(2),dp(3),dp(2),dp(3));
            grid.addView(cell,p);
        }

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, new LinearLayout.LayoutParams(-1, dp(58)));
        Button reset = new Button(this);
        reset.setText("پاک‌کردن خط‌های این ماه");
        reset.setTextColor(Color.WHITE); reset.setAllCaps(false);
        reset.setBackground(bg(Color.rgb(22,124,128), 14, 0));
        reset.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
            .setTitle("پاک‌کردن علامت‌ها")
            .setMessage("تمام خط‌های ثبت‌شده در ماه " + monthNames[selectedMonth] + " پاک شوند؟")
            .setNegativeButton("انصراف", (a,b1)->{})
            .setPositiveButton("پاک شود", (a,b1)->{
                SharedPreferences.Editor e = prefs.edit();
                for(int i=1;i<counts.length;i++){ counts[i]=0; e.remove("m"+selectedMonth+"_day_"+i); cells[i].setCount(0); }
                e.apply();
            }).show());
        footer.addView(reset, new LinearLayout.LayoutParams(dp(240), dp(48)));
    }

    @Override public void onBackPressed() {
        if (selectedMonth >= 0) showMonthPicker();
        else super.onBackPressed();
    }

    class DayCell extends View {
        private final int day;
        private int count;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        DayCell(Activity c, int day, int count) { super(c); this.day=day; this.count=count; setClickable(true); }
        void setCount(int n) { count=n; invalidate(); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float cx=getWidth()/2f, cy=getHeight()/2f;
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            paint.setTextSize(Math.min(dp(22), getHeight()*0.40f));
            paint.setColor(Color.rgb(28,57,60));
            String s=fa(day);
            Paint.FontMetrics fm=paint.getFontMetrics();
            float baseline=cy-(fm.ascent+fm.descent)/2f;
            c.drawText(s,cx,baseline,paint);
            if(count>0) {
                paint.setColor(Color.rgb(211,59,65));
                paint.setStrokeWidth(dp(2.1f));
                paint.setStrokeCap(Paint.Cap.ROUND);
                float half=Math.max(dp(8),paint.measureText(s)*0.56f);
                int visible=Math.min(count, 8);
                for(int i=0;i<visible;i++) {
                    float y=baseline - dp(1) + (i-(visible-1)/2f)*dp(3.0f);
                    c.drawLine(cx-half,y,cx+half,y,paint);
                }
                if(count>8) {
                    paint.setTextSize(dp(9)); paint.setTextAlign(Paint.Align.CENTER);
                    c.drawText("+"+fa(count-8),cx,getHeight()-dp(3),paint);
                }
            }
        }
    }
}
