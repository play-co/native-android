package com.tealeaf;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.Window;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.os.Build;

import com.tealeaf.logger;
import com.tealeaf.TeaLeaf;
import com.tealeaf.TeaLeafOptions;
import com.tealeaf.NativeShim;

import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.view.MotionEvent;

public class TeaLeafFragment extends Fragment {

  Boolean initialized = false;

  static class InputEvents {
    public static final int MAX_EVENTS = 32;
    public static int[] ids = new int[MAX_EVENTS];
    public static int[] types = new int[MAX_EVENTS];
    public static int[] xs = new int[MAX_EVENTS];
    public static int[] ys = new int[MAX_EVENTS];

    private static int index = 0;

    public static Object lock = new Object();

    public static void push(int id, int type, int x, int y) {
      if (index < MAX_EVENTS) {
        ids[index] = id;
        types[index] = type;
        xs[index] = x;
        ys[index] = y;
        index++;
      }
    }

    public static void clear() {
      index = 0;
    }
  }

  private void handleInputEvents() {
    synchronized (InputEvents.lock) {
      if (InputEvents.index > 0 && initialized) {
        NativeShim.dispatchInputEvents(InputEvents.ids,
            InputEvents.types, InputEvents.xs, InputEvents.ys,
            InputEvents.index);
        InputEvents.clear();
      }
    }
  }

  class TealeafGLSurfaceView extends GLSurfaceView {

    int width;
    int height;

    public TealeafGLSurfaceView(Context context){
      super(context);
      setEGLContextClientVersion(2);

      // Set the Renderer for drawing on the GLSurfaceView
      setRenderer(new TealeafGLRenderer());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      int activePointer = event.getActionIndex();
      int id = event.getPointerId(activePointer);
      float x = Math.min(Math.max(0, event.getX(activePointer)), width);
      float y = Math.min(Math.max(0, event.getY(activePointer)), height);
      int type;
      int eventType = event.getAction();

      switch (eventType & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
          type = 1;
          break;
        case MotionEvent.ACTION_MOVE:
          type = 2;
          break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
        case MotionEvent.ACTION_CANCEL:
          type = 3;
          break;
        default:
          type = -1;
      }

      synchronized (InputEvents.lock) {
        InputEvents.push(id, type, (int) x, (int) y);
      }

      for (int i = 0; i < event.getPointerCount(); i++) {
        if (i == activePointer) {
          continue;
        }
        id = event.getPointerId(i);
        x = Math.min(Math.max(0, event.getX(i)), width);
        y = Math.min(Math.max(0, event.getY(i)), height);
        type = 2;
        synchronized (InputEvents.lock) {
          InputEvents.push(id, type, (int) x, (int) y);
        }
      }
      return true;
    }

    public class TealeafGLRenderer implements GLSurfaceView.Renderer {

      long lastMS = 0;
      public void onSurfaceCreated(GL10 unused, EGLConfig config) {
      }

      public void onDrawFrame(GL10 unused) {
        // Redraw background color
        if (initialized) {
          handleInputEvents();
          long now = System.currentTimeMillis();
          NativeShim.step((int) (now - lastMS));
          lastMS = now;
        }
      }

      public void onSurfaceChanged(GL10 unused, int width, int height) {
        TealeafGLSurfaceView.this.width = width;
        TealeafGLSurfaceView.this.height = height;

        Activity activity = getActivity();
        NativeShim shim = TeaLeaf.init(activity);
        initialized = shim != null;
        TeaLeafOptions opts = new TeaLeafOptions(activity);

        String sourceDir = opts.getSourceDir();
        NativeShim.coreInit(
            "http://localhost/",
            "http://localhost/",
            9200,
            9200,
            "devkit.native.launchClient",
            sourceDir,
            width,
            height,
            false,
            null, // Disable OpenGL splash
            "");
        lastMS = System.currentTimeMillis();
      }
    }
  }

  public void hideButtonBar() {
    // games are inherently full screen and immersive, hide OS UI bars
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return;
    }

    int uiFlag = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      uiFlag |= View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        uiFlag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
      }
    }

    getActivity().getWindow().getDecorView().setSystemUiVisibility(uiFlag);
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    hideButtonBar();
    return new TealeafGLSurfaceView(getActivity());
  }

  @Override
  public void onPause() {
    super.onPause();
    // Containing activity is paused.
    // TODO pause Tealeaf
  }

  @Override
  public void onResume() {
    super.onResume();
    hideButtonBar();
  }

  @Override
  public void onStop() {
    super.onStop();
    logger.log("{tealeaf} fragment onStop");
    // Containing activity has been stopped.
    // TODO pause Tealeaf
  }

  @Override
  public void onDestroyView() {
    logger.log("{tealeaf} fragment onDestroyView");
    // Containing activity is being removed. Cleanup.
    // TealeafGLSurfaceView view = (TealeafGLSurfaceView)getView();
    // if (initialized) {
    //   initialized = false;
    //   NativeShim.destroy();
    // }
  }
}
