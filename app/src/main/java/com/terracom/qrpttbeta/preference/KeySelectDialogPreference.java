package com.terracom.qrpttbeta.preference;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.terracom.qrpttbeta.R;

public class KeySelectDialogPreference extends DialogPreference implements OnKeyListener {
	
	private TextView valueTextView;
	private int keyCode;
	
	public KeySelectDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		
		builder.setOnKeyListener(this);
		builder.setNeutralButton(R.string.reset_key, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				keyCode = 0;
				valueTextView.setText("No Key");
                persistInt(keyCode);
			}
		});
	}
	
	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout layout = new LinearLayout(getContext());
	    layout.setOrientation(LinearLayout.VERTICAL);
	    layout.setPadding(6,6,6,6);
		
		TextView promptTextView = new TextView(getContext());
		promptTextView.setText(R.string.pressKey);
	    promptTextView.setGravity(Gravity.CENTER_HORIZONTAL);
		
		valueTextView = new TextView(getContext());
		valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	    valueTextView.setGravity(Gravity.CENTER_HORIZONTAL);
	    valueTextView.setPadding(0, 12, 0, 12);
		
	    TextView alertTextView = new TextView(getContext());
	    alertTextView.setText(R.string.pressKeyInfo);
	    alertTextView.setGravity(Gravity.CENTER_HORIZONTAL);
	    alertTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
	    
		layout.addView(promptTextView, params);
		layout.addView(valueTextView, params);
		layout.addView(alertTextView);
		
		return layout;
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		if(restorePersistedValue)  {
			keyCode = getPersistedInt(0);
		} else {
			keyCode = (Integer)defaultValue;
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult) {
			persistInt(keyCode);
		}
		
		super.onDialogClosed(positiveResult);
	}
	
	@TargetApi(12)
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		if(keyCode == 0) {
			valueTextView.setText("No Key");
		} else {
			if(android.os.Build.VERSION.SDK_INT >= 12) {
				valueTextView.setText(KeyEvent.keyCodeToString(keyCode));
			} else {
				valueTextView.setText("Key code: "+keyCode);
			}
		}
	}

	@TargetApi(12)
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if(keyCode != KeyEvent.KEYCODE_BACK) {
			this.keyCode = keyCode;
			
			if(android.os.Build.VERSION.SDK_INT >= 12) {
				valueTextView.setText(KeyEvent.keyCodeToString(keyCode));
			} else {
				valueTextView.setText("Key code: "+keyCode);
			}
		} else {
			dialog.dismiss();
		}
		return true;
	}

}
