package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ivw.JsonParser;
import com.github.tvbox.osc.ui.activity.FastSearchActivity;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class IatDialog extends BaseDialog {
    private static final String TAG = "IatDialog";

    // 语音听写对象
    private SpeechRecognizer mIat;
    private Context context;

    private TextView mTvRes;

    public IatDialog(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_iat);

        mTvRes = findViewById(R.id.tv_iat);

        this.context = context;

        initIat();
    }

    @Override
    public void show() {
        super.show();
        startIat();
    }

    private void initIat() {
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
        if (mIat == null) {
            Log.e(TAG, "Iat init Error");
        } else {
            setIatParam();
        }
    }

    /****************    听写    ****************/

    int ret = 0;// 函数调用返回值

    private void startIat() {
        Log.d(TAG, "start iat");
        // 不显示听写对话框
        mTvRes.setText("");
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            Toast.makeText(context, "听写失败,错误码：" + ret, Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(context, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void setIatParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        String lag = "mandarin";
        // 设置引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        File extFile = context.getExternalFilesDir("msc");
        if (extFile != null) {
            mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                    extFile.getAbsolutePath() + "/iat.wav");
        }

    }

    StringBuilder sb = new StringBuilder();

    /**
     * 听写监听器。
     */
    private final RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
//            Toast.makeText(context, "请说～", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Toast.makeText(context, "听写错误：" + error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.d(TAG, "听写结束");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {

            String text = JsonParser.parseIatResult(results.getResultString());
            sb.append(text);

            mTvRes.append(text);

            if (isLast) {
                //TODO 最后的结果
                String res = sb.toString();

//                Toast.makeText(context, res, Toast.LENGTH_SHORT).show();

                if (res.startsWith("我要看")) {
                    res = res.substring(3);
                } else if (res.startsWith("播放")) {
                    res = res.substring(2);
                }

                startSearch(res);
                dismiss();
                ((Activity) context).finish();
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG, "当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_AUDIO_URL);
                Log.d(TAG, "session id =" + sid);
            }
        }
    };

    private void startSearch(String keyword) {
        Log.d(TAG, "[startSearch] " + keyword);
        Bundle bundle = new Bundle();
        bundle.putString("title", keyword);
//                refreshSearchHistory("keyword");
//                jumpActivity(FastSearchActivity.class, bundle);
        Intent intent = new Intent(context, FastSearchActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}