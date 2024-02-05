package com.github.tvbox.osc.ivw;

import static com.blankj.utilcode.util.StringUtils.getString;
import static com.google.common.io.Resources.getResource;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.activity.FastSearchActivity;
import com.github.tvbox.osc.ui.dialog.IatDialog;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * @author AoBing
 * @date 2024/2/2
 */
public class IvwHelper {
    private static final String TAG = "IvwHelper";
    private static boolean mscInitialize = false;
    private Context context;
    private VoiceWakeuper mIvw;


    private int curThresh = 1450;

    private String keep_alive = "1";
    private String ivwNetMode = "0";

    // 唤醒结果内容
    private String resultString;

    private IatDialog iatDialog;

    private static final IvwHelper ourInstance = new IvwHelper();

    public static IvwHelper getInstance() {
        return ourInstance;
    }

    private IvwHelper() {
    }

    public boolean isListening() {
        return mIvw.isListening();
    }

    public void init(Context context) {
        if (context == null) {
            return;
        }
        if (mscInitialize) {
            Log.e(TAG, "NOT need init");
            return;
        }
        this.context = context;
        initializeMsc();
        iatDialog = new IatDialog(context.getApplicationContext());
    }

    private void initializeMsc(){
        if (mscInitialize) return;
        // 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用“,”分隔。
        // 设置你申请的应用appid
        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        StringBuffer param = new StringBuffer();
        param.append("appid=" + context.getString(R.string.app_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(context, param.toString());


        mIvw = VoiceWakeuper.createWakeuper(context, null);

        if (mIvw != null) {
            mscInitialize = true;
            Toast.makeText(context, "初始化成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    public int getCurThresh() {

        return curThresh;
    }

    public void startIvw(int curThresh) {
        this.curThresh = curThresh;
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            resultString = "";

            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());

            File path = context.getExternalFilesDir("msc");
            if (path != null) {
                // 设置唤醒录音保存路径，保存最近一分钟的音频
                mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH,
                        path.getAbsolutePath() + "/ivw.wav");
            }
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            /*	mIvw.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");*/

            Log.d(TAG, "[startIvw] curThresh:" + curThresh);
//            Toast.makeText(context, "start ivw", Toast.LENGTH_LONG).show();
            mIvw.startListening(mWakeuperListener);
        } else {
            Log.e(TAG, "[startIvw] ivm is NULL");
        }
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d(TAG, "onResult");
            String key = "";
            if (!"1".equalsIgnoreCase(keep_alive)) {
//                setRadioEnable(true);
            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
                key = object.optString("keyword");

                Intent intent = new Intent(context, IatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            Log.d(TAG, "result:" + resultString);
//            Toast.makeText(context, key, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(SpeechError error) {
            Log.e(TAG, error.getPlainDescription(true));
            Toast.makeText(context, error.getPlainDescription(true), Toast.LENGTH_LONG).show();
//            setRadioEnable(true);
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    public void stop() {
        if (mIvw == null) {
            return;
        }
        mIvw.stopListening();
    }

    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.app_id) + ".jet");
        Log.d(TAG, "resPath: " + resPath);
        return resPath;
    }




}
