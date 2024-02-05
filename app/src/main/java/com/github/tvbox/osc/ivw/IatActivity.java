package com.github.tvbox.osc.ivw;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.dialog.IatDialog;

public class IatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iat);
        hideSysBar();
        IatDialog dialog = new IatDialog(this);
        dialog.show();
    }

    private void hideSysBar() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        //    uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        //    uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }
}