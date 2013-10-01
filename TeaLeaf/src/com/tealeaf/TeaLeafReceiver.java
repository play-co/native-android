package com.tealeaf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class TeaLeafReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        
        if (action.equals("com.android.vending.INSTALL_REFERRER")) {
            
            // log referrer
            try {
                Bundle bundle = intent.getExtras();
                String referrer = URLDecoder.decode(bundle.getString("referrer"), "UTF-8");
				Settings.build(context);
				Settings settings = Settings.getInstance();
				settings.setString("installReferrer.referrer", referrer);
			} catch (UnsupportedEncodingException e) {
				logger.log(e);
			} 
		}
    }

}
