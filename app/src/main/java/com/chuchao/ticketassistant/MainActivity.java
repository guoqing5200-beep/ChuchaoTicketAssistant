
package com.chuchao.ticketassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity {

    private TextView countdown;
    private TextView status;
    private EditText saleTime;
    private EditText urlInput;
    private WebView webView;

    private long targetTime = 0;
    private boolean reminded = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable timer = new Runnable() {
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
        layout.setPadding(20, 20, 20, 20);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("楚超抢票助手 4.0");
        title.setTextSize(26);
        title.setTextColor(Color.BLACK);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("目标比赛：宜昌东阳光 VS 武汉华工科技\n"
                + "目标数量：3张\n"
                + "票档：不限\n"
                + "平台：猫眼\n"
                + "支付：本人核对并确认");
        info.setTextSize(16);
        info.setTextColor(Color.DKGRAY);
        info.setPadding(0, 15, 0, 15);
        layout.addView(info);

        TextView timeLabel = new TextView(this);
        timeLabel.setText("开售时间（北京时间）");
        timeLabel.setTextColor(Color.BLACK);
        layout.addView(timeLabel);

        saleTime = new EditText(this);
        saleTime.setSingleLine(true);
        saleTime.setHint("yyyy-MM-dd HH:mm");
        layout.addView(saleTime);

        Button save = new Button(this);
        save.setText("保存开售时间");
        layout.addView(save);

        countdown = new TextView(this);
        countdown.setTextSize(21);
        countdown.setTextColor(Color.RED);
        countdown.setPadding(0, 12, 0, 12);
        layout.addView(countdown);

        status = new TextView(this);
        status.setText("状态：等待进入购票页面");
        status.setTextSize(15);
        status.setTextColor(Color.BLUE);
        layout.addView(status);

        TextView urlLabel = new TextView(this);
        urlLabel.setText("比赛购票链接（没有链接可留空）");
        urlLabel.setTextColor(Color.BLACK);
        urlLabel.setPadding(0, 12, 0, 0);
        layout.addView(urlLabel);

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setHint("粘贴猫眼比赛链接");
        layout.addView(urlInput);

        Button open = new Button(this);
        open.setText("进入猫眼购票页面");
        layout.addView(open);

        Button refresh = new Button(this);
        refresh.setText("刷新购票页面");
        layout.addView(refresh);

        Button back = new Button(this);
        back.setText("返回助手首页");
        layout.addView(back);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient());

        webView.setVisibility(View.GONE);

        layout.addView(webView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0, 1));

        save.setOnClickListener(v -> {
            try {
                SimpleDateFormat format =
                        new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm",
                                Locale.CHINA);

                format.setTimeZone(
                        TimeZone.getTimeZone("Asia/Shanghai"));
                format.setLenient(false);

                Date date = format.parse(
                        saleTime.getText().toString().trim());

                if (date == null ||
                        date.getTime() <= System.currentTimeMillis()) {
                    Toast.makeText(this,
                            "请输入未来的开售时间",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                targetTime = date.getTime();
                reminded = false;
                status.setText("状态：开售时间已保存");
                updateCountdown();

            } catch (Exception e) {
                Toast.makeText(this,
                        "时间格式不正确",
                        Toast.LENGTH_SHORT).show();
            }
        });

        open.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();

            if (url.isEmpty()) {
                url = "https://m.maoyan.com/";
            }

            if (!url.startsWith("https://")) {
                Toast.makeText(this,
                        "请使用有效的 HTTPS 猫眼链接",
                        Toast.LENGTH_LONG).show();
                return;
            }

            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
            status.setText("状态：正在打开购票页面");
        });

        refresh.setOnClickListener(v -> {
            if (webView.getVisibility() == View.VISIBLE) {
                webView.reload();
                status.setText("状态：正在刷新页面");
            }
        });

        back.setOnClickListener(v -> {
            webView.setVisibility(View.GONE);
            status.setText("状态：已返回助手首页");
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                status.setText("状态：购票页面已加载");
            }
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
            status.setText("状态：请进入猫眼检查售票情况");

            if (!reminded) {
                reminded = true;

                new AlertDialog.Builder(this)
                        .setTitle("楚超开售提醒")
                        .setMessage("预计开售时间已到，请检查猫眼购票页面。")
                        .setPositiveButton("立即进入",
                                (dialog, which) -> {
                                    webView.setVisibility(View.VISIBLE);
                                    webView.loadUrl(
                                            "https://m.maoyan.com/");
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
                Locale.CHINA,
                "距离开售：%d天 %02d:%02d:%02d",
                days, hours, minutes, secs));
    }

    @Override
    public void onBackPressed() {
        if (webView != null &&
                webView.getVisibility() == View.VISIBLE) {

            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                webView.setVisibility(View.GONE);
            }

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
