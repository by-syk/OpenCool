package info.papdt.coolapk.api.apk;

import android.os.Build;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Map;

import info.papdt.coolapk.api.BaseApi;
import info.papdt.coolapk.model.ApkModel;
import info.papdt.coolapk.model.ApkListModel;
import info.papdt.coolapk.util.HttpParameters;
import static info.papdt.coolapk.api.Constants.*;
import static info.papdt.coolapk.util.Constants.*;

public class ApkApi extends BaseApi
{
	public static final Creator<ApkApi> CREATOR = new Creator<ApkApi>() {
		@Override
		public ApkApi create() {
			return new ApkApi();
		}
	};
	
	private ApkApi() {
		super();
	}
	
	public ApkListModel getApkList(String type, int page) {
		HttpParameters params = new HttpParameters();
		params.put("page", String.valueOf(page));
		params.put("listType", type);
		params.put("firstItemId", "");
		params.put("lastItemId", "");

		JSONObject result = requestJSON(HOST, APK_GET_LIST, params, null, HTTP_GET);
		
		if (result != null && result.optInt("status") > 0) {
			return new Gson().fromJson(result.toString(), ApkListModel.class);
		} else {
			return null;
		}
	}
	
	// appInfo: name -> versionCode
	public ApkListModel checkUpdate(Map<String, Integer> appInfo) {
		HttpParameters params = new HttpParameters();
		params.put("sdk", Build.VERSION.SDK);
		params.put("beta", "0");
		params.put("pkgs", buildPkgInfoStr(appInfo));
		
		JSONObject result = requestJSON(HOST, APK_CHECK_UPDATE, null, params, HTTP_POST);
		
		if (result != null && result.optInt("status") > 0) {
			return new Gson().fromJson(result.toString(), ApkListModel.class);
		} else {
			return null;
		}
	}
	
	private String buildPkgInfoStr(Map<String, Integer> appInfo) {
		StringBuilder sb = new StringBuilder();
		
		for (Map.Entry<String, Integer> entry : appInfo.entrySet()) {
			sb.append(entry.getKey()).append(":")
				.append(String.valueOf(entry.getValue())).append(", ");
		}
		
		return "[" + sb.toString().trim().substring(0, sb.length() - 2) + "]";
	}
}
