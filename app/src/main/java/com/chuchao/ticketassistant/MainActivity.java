
package com.chuchao.ticketassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView countdown;
    private TextView status;
    private EditText saleTime;
    private WebView webView;

    private long targetTime = 0;
    private boolean reminded = false;

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    private final Runnable timer = new Runnable() {
        @Override
        public void run() {
            updateCountdown();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("楚超抢票助手");
        title.setTextSize(28);
        title.setTextColor(Color.BLACK);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText(
            "目标数量：3张\n" +
            "票档：不限\n" +
            "购票平台：猫眼\n" +
            "验证码、订单及付款由本人确认"
        );
        info.setTextSize(17);
        info.setTextColor(Color.DKGRAY);
        info.setPadding(0, 20, 0, 20);
        layout.addView(info);

        TextView label = new TextView(this);
        label.setText("设置开售时间（北京时间）");
        label.setTextSize(17);
        label.setTextColor(Color.BLACK);
        layout.addView(label);

        saleTime = new EditText(this);
        saleTime.setSingleLine(true);
        saleTime.setTextSize(16);
        saleTime.setHint("例如：2026-11-08 10:00");
        layout.addView(saleTime);

        Button save = new Button(this);
        save.setText("保存开售时间");
        layout.addView(save);

        countdown = new TextView(this);
        countdown.setText("距离开售：尚未设置");
        countdown.setTextSize(21);
        countdown.setTextColor(Color.RED);
        countdown.setPadding(0, 20, 0, 20);
        layout.addView(countdown);

        status = new TextView(this);
        status.setText("状态：等待开售");
        status.setTextSize(16);
        status.setTextColor(Color.BLUE);
        layout.addView(status);

        Button open = new Button(this);
        open.setText("一键打开猫眼购票页面");
        layout.addView(open);

        Button back = new Button(this);
        back.setText("返回抢票助手");
        layout.addView(back);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        layout.addView(webView,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1
            )
        );

        webView.setVisibility(View.GONE);

        save.setOnClickListener(v -> {
            try {
                SimpleDateFormat format =
                    new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                    );
                format.setLenient(false);

                Date date = format.parse(
                    saleTime.getText().toString().trim()
                );

                if (date == null || date.getTime() <= System.currentTimeMillis()) {
                    Toast.makeText(this,
                        "请输入未来的开售时间",
                        Toast.LENGTH_LONG).show();
                    return;
                }

                targetTime = date.getTime();
                reminded = false;

                status.setText("状态：开售时间已保存");
                updateCountdown();

                Toast.makeText(this,
                    "开售时间设置成功",
                    Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(this,
                    "时间格式错误，请按示例输入",
                    Toast.LENGTH_LONG).show();
            }
        });

        open.setOnClickListener(v -> {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl("https://m.maoyan.com/");
            status.setText("状态：已打开猫眼，请选择楚超比赛");
        });

        back.setOnClickListener(v -> {
            webView.setVisibility(View.GONE);
            status.setText("状态：已返回抢票助手");
        });

        setContentView(layout);

        handler.post(timer);
    }

    private void updateCountdown() {

        if (targetTime == 0) {
            countdown.setText("距离开售：尚未设置");
            return;
        }

        long diff = targetTime - System.currentTimeMillis();

        if (diff <= 0) {
            countdown.setText("开售时间已到！");
            status.setText("状态：请立即进入猫眼购票");

            if (!reminded) {
                reminded = true;

                new AlertDialog.Builder(this)
                    .setTitle("楚超开售提醒")
                    .setMessage("预计开售时间已到，请进入猫眼购票！")
                    .setPositiveButton("立即购票", (dialog, which) -> {
                        webView.setVisibility(View.VISIBLE);
                        webView.loadUrl("https://m.maoyan.com/");
                    })
                    .setNegativeButton("稍后", null)
                    .show();
            }
            return;
        }

        long seconds = diff / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        countdown.setText(String.format(
            Locale.getDefault(),
            "距离开售：%d天 %02d:%02d:%02d",
            days, hours, minutes, secs
        ));
    }

    @Override
    public void onBackPressed() {
        if (webView != null &&
            webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(timer);
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
