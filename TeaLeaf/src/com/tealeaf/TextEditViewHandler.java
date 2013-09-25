package com.tealeaf;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.View.OnKeyListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import com.tealeaf.event.InputKeyboardKeyUpEvent;
import com.tealeaf.event.InputKeyboardSubmitEvent;
import com.tealeaf.event.InputKeyboardFocusNextEvent;
import android.view.View;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.app.Activity;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.content.Context;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.os.Handler;
import android.util.AttributeSet;
import com.tealeaf.util.ILogger;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.view.Display;
import android.widget.Toast;

/**
 * Allows JS to open up a keyboard that has an EditText attached
 * above the keyboard. Key strokes are propagated to JS so that the 
 * GL layer can be updated. It allows for different input types, length
 * constraints, and the ability to move back and forth between TextEditViews.
 */
public class TextEditViewHandler {

	private TeaLeaf activity;
	private View editTextHandler;
	private View editTextFrame;
	private TextEditView editText;
	private boolean isActive = false;
	private boolean registerTextChange = true;
	private InputName inputName = InputName.DEFAULT;
	private boolean hasForward = false;
	private int lastKnownHeight = -1;
	private boolean triggerFrameVisibility = false;

	public enum InputName {
		DEFAULT,
		NUMBER,
		PHONE,
		PASSWORD,
		CAPITAL
	}

	public TextEditViewHandler(TeaLeaf tealeaf) {
		this.activity = tealeaf;

		LayoutInflater inflater = activity.getLayoutInflater();
		editTextHandler = inflater.inflate(R.layout.edit_text_handler, null);
		editTextHandler.setOnClickListener(this.getScreenCaptureListener());

		// setup screen listener
		final FrameLayout group = this.activity.getGroup();
		editTextFrame = editTextHandler.findViewById(R.id.handler_wrapper);

		// TODO: we could use the observer in TeaLeaf.java to avoid duplicate code here...
		group.getViewTreeObserver()
			 .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			 public void onGlobalLayout() {
			 	Rect r = new Rect();		
				group.getWindowVisibleDisplayFrame(r);

				Display display = activity.getWindow()
				 						  .getWindowManager()
				 						  .getDefaultDisplay();

				int originalHeight = display.getHeight();
				final int visibleHeight = r.bottom - r.top;
				int heightDiff = originalHeight - visibleHeight;

				// if keyboard appeared the height will change
				// triggerFrameVisibility means a keyboard was activated
				if ((lastKnownHeight != visibleHeight || activity.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) && triggerFrameVisibility) {
					updateEditFramePosition(visibleHeight);
					triggerFrameVisibility = false;
				}

				lastKnownHeight = visibleHeight;
			}
		});

		// setup EditText
		editText = (TextEditView) editTextHandler.findViewById(R.id.handler_text);
		editText.setTextEditViewHandler(this);
		editText.addTextChangedListener(new TextWatcher() {
			private String beforeText = "";

			@Override
			public void afterTextChanged(Editable s) {
				// propagate text changes to JS to update views
				if (registerTextChange) {
					logger.log("KeyUp textChange in TextEditView");
					EventQueue.pushEvent(new InputKeyboardKeyUpEvent(s.toString(), beforeText, editText.getSelectionStart()));
				} else {
					registerTextChange = true;
				}
			} 

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				beforeText = s.toString();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			
			}
		});

		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					EventQueue.pushEvent(new InputKeyboardSubmitEvent(0, editText.getText().toString()));
					closeKeyboard();
				} else if (actionId == EditorInfo.IME_ACTION_NEXT) {
					EventQueue.pushEvent(new InputKeyboardFocusNextEvent(true));
				}

				return false;
			}
		});

		// setup forward and back keys
		View backButton = editTextHandler.findViewById(R.id.back_button);
		View forwardButton = editTextHandler.findViewById(R.id.forward_button);
		View doneButton = editTextHandler.findViewById(R.id.done_button);

		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventQueue.pushEvent(new InputKeyboardSubmitEvent(0, editText.getText().toString()));
				closeKeyboard();
			}
		});
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventQueue.pushEvent(new InputKeyboardFocusNextEvent(false));
			}
		});
		forwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventQueue.pushEvent(new InputKeyboardFocusNextEvent(true));
			}
		});

		// attach above GL layer
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
																		  RelativeLayout.LayoutParams.FILL_PARENT);

		activity.addContentView(editTextHandler, rlp);
	}

	public void updateEditFramePosition(int visibleHeight) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editTextFrame.getLayoutParams();
		params.setMargins(0, visibleHeight - editTextFrame.getMeasuredHeight(), 0, 0);
		editTextFrame.setLayoutParams(params);

		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				editTextHandler.setVisibility(View.VISIBLE);
				editTextHandler.requestLayout();
			}	
		}, 100);
	}

	/**
	 * Used to determine when the keyboard has gone away.
	 */
	public void onBackPressed() {
		deactivate();
	}

	/**
	 * Perform actions necessary when TextEditView pops up.
	 */
	public void activate(String text, String hint, boolean hasBackward, boolean hasForward, String inputType, String inputReturnButton, int maxLength, int cursorPos) {

		if (inputReturnButton.equals("done")) {
			editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		} else if (inputReturnButton.equals("next")) {
			editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		} else if (inputReturnButton.equals("search")) {
			editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		} else if (inputReturnButton.equals("send")) {
			editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
		} else if (inputReturnButton.equals("go")) {
			editText.setImeOptions(EditorInfo.IME_ACTION_GO);
		} else {
			int action = hasForward ? EditorInfo.IME_ACTION_NEXT : EditorInfo.IME_ACTION_DONE;
			if (inputReturnButton.equals("default")) {
				editText.setImeOptions(action);
			} else {
				editText.setImeActionLabel(inputReturnButton, action);
			}
		}

		if (!isActive) {
			isActive = true;
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editTextFrame.getLayoutParams();
			triggerFrameVisibility = true;

			// In order to show keyboard directly after making EditText visible we must show keyboard
			// independent of EditText and then requestFocus.
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

			editText.requestFocus();
		}

		// EditText options
		int type;
		inputName = InputName.valueOf(inputType.toUpperCase().trim());

		switch (inputName) {
			case NUMBER:
				type = InputType.TYPE_CLASS_NUMBER;
				break;
			case PHONE:
				type = InputType.TYPE_CLASS_PHONE;
				break;
			case PASSWORD:
				type = InputType.TYPE_TEXT_VARIATION_PASSWORD;
				break;
			case CAPITAL:
				type = InputType.TYPE_TEXT_FLAG_CAP_WORDS;
				break;
			default:
				type = InputType.TYPE_CLASS_TEXT;
				break;
		}

		//for auto correct use this flag -> InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
		editText.setInputType(type | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		if (maxLength == -1) {
			editText.setFilters(new InputFilter[] {});
		} else {
			editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });
		}

		registerTextChange = false;

		editText.setSingleLine();
		editText.setFocusable(true);
		editText.setFocusableInTouchMode(true);
		editText.setText(text);
		editText.setHint(hint);
		editText.setSelection(cursorPos < 0 || cursorPos > editText.length() ? editText.getText().length() : cursorPos);

		// Button options
		View backButton = editTextHandler.findViewById(R.id.back_button);
		View forwardButton = editTextHandler.findViewById(R.id.forward_button);
		View doneButton = editTextHandler.findViewById(R.id.done_button);

		if (!hasForward && !hasBackward) {
			backButton.setVisibility(View.GONE);
			forwardButton.setVisibility(View.GONE);
			doneButton.setVisibility(View.VISIBLE);
		} else {
			backButton.setVisibility(View.VISIBLE);
			forwardButton.setVisibility(View.VISIBLE);
			doneButton.setVisibility(View.GONE);
			backButton.setEnabled(hasBackward);
			forwardButton.setEnabled(hasForward);
		}

		this.hasForward = hasForward;
	}

	/**
	 * Perform actions necessary when TextEditView is closed.
	 */
	public void deactivate() {
		if (isActive) {
			isActive = false;
			editTextHandler.setVisibility(View.INVISIBLE);

			// return to top of screen
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editTextFrame.getLayoutParams();
			params.setMargins(0, 0, 0, 0);
			editTextFrame.setLayoutParams(params);
		}
	}

	public void closeKeyboard() {
		logger.log("TextEditView closeKeyboard");
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);	

		this.deactivate();	
	}

	public OnClickListener getScreenCaptureListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isActive) {
					closeKeyboard();
				}
			}
		};	
	}

	public static class TextEditView extends EditText {

		private TextEditViewHandler handler;

		public TextEditView(Context context) { super(context); }
		public TextEditView(Context context, AttributeSet attrs) { super(context, attrs); }
		public TextEditView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

		@Override
		public boolean onKeyPreIme(int keyCode, KeyEvent event) {
			// listen for soft keyboard back button.
			// you cannot use standard onBackPressed for this key press.
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				if (handler != null) {
					handler.deactivate();
				}
			}

			return super.onKeyPreIme(keyCode, event);
		}

		/**
		 * Set instance of TextEditViewHandler to perform actions.
		 */
		public void setTextEditViewHandler(TextEditViewHandler handler) {
			this.handler = handler;	
		}
	}
}
