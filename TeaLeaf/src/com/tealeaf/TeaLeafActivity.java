package com.tealeaf;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.Display;
import android.widget.RelativeLayout;
import android.view.Window;
import android.view.View;
import android.os.Bundle;

import com.tealeaf.logger;
import com.tealeaf.TeaLeaf;
import com.tealeaf.TeaLeafOptions;
import com.tealeaf.NativeShim;

import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.view.MotionEvent;

import com.tealeaf.TeaLeafFragment;

public class TeaLeafActivity extends Activity {
  TeaLeafFragment tealeaf;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    // Must be called from the activity before `setContentView`
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    super.onCreate(savedInstanceState);
    RelativeLayout main = new RelativeLayout(this);
    main.setId(12333);
    setContentView(main);

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction();

    tealeaf = new TeaLeafFragment();
    ft.add(main.getId(), tealeaf, "tealeaf_fragment").commit();
  }
}
