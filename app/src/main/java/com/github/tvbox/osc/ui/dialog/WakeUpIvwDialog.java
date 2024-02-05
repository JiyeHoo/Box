package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ivw.IvwHelper;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WakeUpIvwDialog extends BaseDialog {

    private final static int MAX = 3000;
    private final static int MIN = 0;

    private int curThresh = 1450;

    private String threshStr = "门限值: ";
    private TextView tvThresh;
    private SeekBar seekbarThresh;

    private LinearLayout llIvw;

    private TextView tvIvwSet;

    private Context context;

    public WakeUpIvwDialog(@NonNull @NotNull Context context) {
        super(context, R.style.CustomDialogStyleDim);
        setContentView(R.layout.dialog_wake_up);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();

        curThresh = IvwHelper.getInstance().getCurThresh();

//        IvwHelper.getInstance().startIvw();
    }

    private void initView() {
        seekbarThresh = findViewById(R.id.seekBar_thresh);
        seekbarThresh.setMax(MAX - MIN);
        seekbarThresh.setProgress(curThresh);
        tvThresh = findViewById(R.id.tvThresh);
        tvThresh.setText(threshStr + curThresh);
        TextView title = findViewById(R.id.title);
        title.setText("唤醒设置");
        seekbarThresh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                curThresh = seekbarThresh.getProgress() + MIN;
                tvThresh.setText(threshStr + curThresh);
            }
        });

        llIvw = findViewById(R.id.llIvw);
        llIvw.setOnClickListener(this::startOrClose);
        tvIvwSet = findViewById(R.id.tv_ivw_set);

        if (IvwHelper.getInstance().isListening()) {
            tvIvwSet.setText("关闭");
            setRadioEnable(false);
        }
    }

    private void startOrClose(View view) {

        if (IvwHelper.getInstance().isListening()) {
            // todo close
            IvwHelper.getInstance().stop();
            setRadioEnable(true);
        } else {
            // open
            IvwHelper.getInstance().startIvw(curThresh);
            setRadioEnable(false);
        }

    }

    private void setRadioEnable(final boolean enabled) {
        seekbarThresh.setEnabled(enabled);
        tvIvwSet.setText(enabled ? "开启" : "关闭");

        if (enabled) {
            seekbarThresh.setProgressTintList(ColorStateList.valueOf(Color.WHITE));
            seekbarThresh.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
            seekbarThresh.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        } else {
            seekbarThresh.setProgressTintList(ColorStateList.valueOf(Color.BLACK));
            seekbarThresh.setThumbTintList(ColorStateList.valueOf(Color.BLACK));
            seekbarThresh.setProgressBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        }
    }


}