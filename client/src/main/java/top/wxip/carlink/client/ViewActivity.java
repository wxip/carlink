package top.wxip.carlink.client;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;

public class ViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        final SurfaceView displayView = (SurfaceView) findViewById(R.id.display);
        SurfaceViewUtil.initSurfaceView(displayView, this);
    }
}