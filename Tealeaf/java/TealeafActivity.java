package com.gameclosure.tealeafandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tealeaf.Devkit;
import com.tealeaf.DevkitFragment;

public class TealeafActivity extends Activity {

    private native void nativeInitialize(long tl0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tealeaf);

        // Hide the system bars
        // See: http://stackoverflow.com/questions/20583501/android-4-4-system-ui-flag-immersive-sticky-cannot-be-resolved-or-is-not-a-fie
        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        // --- //

        initializeDevkit();
    }

    public void initializeDevkit() {
        Devkit.setActivity(this);

        final Devkit devkit = Devkit.get();
        final TealeafActivity activity = this;
        devkit.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                activity.nativeInitialize(devkit.getTealeafEngine());
            }
        });

        transitionToGame();
    }

    public void transitionToGame() {
        Fragment fragment = new DevkitFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.contentFragment, fragment);
        transaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tealeaf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Devkit.get().popApp();
    }

    static {
        System.loadLibrary("tealeaf");
        System.loadLibrary("tealeafNative");
    }
}
