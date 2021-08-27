/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wpf.hegui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author Michael Yang（www.yangfuhai.com） update at 2013.08.07
 */
public class ACache {

	public static String sharePref = "wpf"; // 默认文件名

	SharedPreferences settings;

	public static ACache get(Context ctx) {
		return get(ctx, sharePref);
	}

	public static ACache get(Context ctx, String cacheName) {
		return new ACache(ctx, cacheName);
	}

	private ACache(Context ctx, String cacheName) {
		if (ctx != null) {
			settings = ctx.getSharedPreferences(cacheName, MODE_PRIVATE);
		}
	}

	// =======================================
	// ============ String数据 读写 ==============
	// =======================================
	/**
	 * 保存 String数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的String数据
	 */
	public void put(String key, String value) {
		if (settings == null) {
			return;
		}
		SharedPreferences.Editor localEditor = settings.edit();
		localEditor.putString(key, value);
		localEditor.commit();
	}

	public void put(String key, long value) {
		if (settings == null) {
			return;
		}
		SharedPreferences.Editor localEditor = settings.edit();
		localEditor.putString(key, String.valueOf(value));
		localEditor.commit();
	}

	/**
	 * 读取 String数据
	 * 
	 * @param key
	 * @return String 数据
	 */
	public String getAsString(String key) {
		if (settings == null) {
			return "";
		}
		return settings.getString(key, "");
	}

	// =======================================
	// ============= JSONObject 数据 读写 ==============
	// =======================================
	/**
	 * 保存 JSONObject数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的JSON数据
	 */
	public void put(String key, JSONObject value) {
		put(key, value.toString());
	}

	/**
	 * 读取JSONObject数据
	 * 
	 * @param key
	 * @return JSONObject数据
	 */
	public JSONObject getAsJSONObject(String key) {
		String JSONString = getAsString(key);
		try {
			if (TextUtils.isEmpty(JSONString)) {
				return null;
			}
			JSONObject obj = new JSONObject(JSONString);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// =======================================
	// ============ JSONArray 数据 读写 =============
	// =======================================
	/**
	 * 保存 JSONArray数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的JSONArray数据
	 */
	public void put(String key, JSONArray value) {
		put(key, value.toString());
	}

	/**
	 * 检测是否已经存储
	 *
	 * @param key
	 *            保存的key
	 */
	public boolean contains(String key) {
		if (settings == null) {
			return false;
		}
		return settings.contains(key);
	}


	/**
	 * 读取JSONArray数据
	 * 
	 * @param key
	 * @return JSONArray数据
	 */
	public JSONArray getAsJSONArray(String key) {
		String JSONString = getAsString(key);
		try {
			if (TextUtils.isEmpty(JSONString)) {
				return null;
			}
			JSONArray obj = new JSONArray(JSONString);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 移除某个key
	 * 
	 * @param key
	 * @return 是否移除成功
	 */
	public boolean remove(String key) {
		if (settings == null) {
			return false;
		}
		SharedPreferences.Editor localEditor = settings.edit();
		return localEditor.remove(key).commit();
	}

	/**
	 * 清除所有数据
	 */
	public boolean clear() {
		if (settings == null) {
			return false;
		}
		SharedPreferences.Editor localEditor = settings.edit();
		return localEditor.clear().commit();
	}

	/**
	 * 获取所有数据
	 */
	public Map<String, ?> getAll() {
		if (settings == null) {
			return null;
		}
		return settings.getAll();
	}

}
