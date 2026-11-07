
package com.chuchao.ticketassistant;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView title = new TextView(this);
        title.setText("楚超抢票助手");
        title.setTextSize(25);
        title.setTextColor(Color.BLACK);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("目标数量：3张\\n票档：不限\\n请通过官方页面购票。验证码、订单和付款由本人确认。");
        info.setTextSize(16);
        layout.addView(info);

        Button button = new Button(this);
        button.setText("打开猫眼购票页面");
        layout.addView(button);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        layout.addView(webView, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1
        ));

        button.setOnClickListener(v ->
            webView.loadUrl("https://m.maoyan.com/")
        );

        setContentView(layout);
        webView.loadUrl("https://m.maoyan.com/");
    }
}
