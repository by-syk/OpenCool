package net.typeblog.coolapk.api;

import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import net.typeblog.coolapk.util.GlobalContext;

import net.typeblog.coolapk.util.HttpParameters;
import net.typeblog.coolapk.util.HttpUtility;
import static net.typeblog.coolapk.BuildConfig.DEBUG;
import static net.typeblog.coolapk.api.Constants.*;
import static net.typeblog.coolapk.util.Constants.*;

public class BaseApi
{
	public static interface Creator<T> {
		T create();
	}
	
	private static final String TAG = BaseApi.class.getSimpleName();
	private static final HashMap<Class, Object> sInstances = new HashMap<Class, Object>();
	
	private SharedPreferences mPrefs;
	private String mClientId, mClientSecret;
	private String mCookie = "";
	private boolean mRegistering = false;
	
	public static <T extends BaseApi> T getInstance(Class<T> apiClass) {
		T ret = (T) sInstances.get(apiClass);
		
		if (ret == null) {
			try {
				ret = ((Creator<T>) apiClass.getDeclaredField("CREATOR").get(null)).create();
				sInstances.put(apiClass, ret);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		return ret;
	}
	
	protected BaseApi() {
		mPrefs = GlobalContext.getSharedPreferences("access");
		mClientId = mPrefs.getString("id", "0");
		mClientSecret = mPrefs.getString("secret", "");
	}
	
	protected String request(String host, String location, HttpParameters urlParams, HttpParameters postParams, String method) {
		if (urlParams == null)
			urlParams = new HttpParameters();
			
		if (DEBUG) {
			Log.d(TAG, "registering = " + mRegistering);
			Log.d(TAG, "id = " + mClientId + " secret = " + mClientSecret);
		}
			
		if (!mRegistering && (mClientId.equals("0") || mClientSecret.equals(""))) {
			
			if (DEBUG)
				Log.d(TAG, "registering device");
			
			registerDevice();
		}
			
		urlParams.put("appId", APP_ID);
		urlParams.put("clientId", mClientId);
		urlParams.put("clientSecret", mClientSecret);
		urlParams.put("clientTime", String.valueOf(System.currentTimeMillis() / 1000));
		urlParams.put("jsonRequest", "1");
		
		try {
			return HttpUtility.httpRequest(host, location, urlParams, postParams, method, mCookie);
		} catch (Exception e) {
			return null;
		}
	}
	
	protected JSONObject requestJSON(String host, String location, HttpParameters urlParams, HttpParameters postParams, String method) {
		String str = request(host, location, urlParams, postParams, method);
		
		if (DEBUG)
			Log.d(TAG, "str to convert: "  + str);
		
		if (str == null) {
			return null;
		} else {
			try {
				return new JSONObject(str);
			} catch (JSONException e) {
				
				if (DEBUG) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
				
				return null;
			}
		}
	}
	
	private void registerDevice() {
		mRegistering = true;
		
		HttpParameters params = new HttpParameters();
		params.put("manufacturer", Build.MANUFACTURER);
		params.put("model", Build.MODEL);
		params.put("brand", Build.BRAND);
		params.put("sdk", Build.VERSION.SDK);
		params.put("rom", Build.VERSION.RELEASE);
		params.put("version", APP_VER);
		params.put("deviceId", "error");
		
		JSONObject json = requestJSON(HOST, DEVICE_REGISTER, null, params, HTTP_POST);
		
		if (json != null && json.optInt("status") == 1) {
			JSONObject data = json.optJSONObject("data");
			
			if (data != null) {
				mClientId = data.optString("clientId");
				mClientSecret = data.optString("clientSecret");
				mPrefs.edit().putString("id", mClientId)
							 .putString("secret", mClientSecret)
							 .commit();
			}
		}
		
		mRegistering = false;
	}
	
	protected void setCookie(String cookie) {
		mCookie = cookie;
	}
	
}
