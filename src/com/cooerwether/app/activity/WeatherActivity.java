package com.cooerwether.app.activity;

import com.cooerweather.app.R;
import com.cooerwether.app.service.AutoUpdateService;
import com.cooerwether.app.util.HttpCallbackListener;
import com.cooerwether.app.util.HttpUtil;
import com.cooerwether.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	private LinearLayout weatherInfoLayout;
	
	/*显示城市名*/
	private TextView cityNameText;
	/*显示发布时间*/
	private TextView publishText;
	/*显示天气描述信息*/
	private TextView weatherDespText;
	/*显示气温1*/
	private TextView temp1Text;
	/*显示气温2*/
	private TextView temp2Text;
	/*显示当前日期*/
	private TextView currentDataText;
	/*切换城市*/
	private Button switchCity;
	/*更新天气*/
	private Button refreshWeather;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout =(LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText =(TextView) findViewById(R.id.city_name);
		publishText =(TextView) findViewById(R.id.publish_text);
		weatherDespText =(TextView) findViewById(R.id.weather_desp);
		temp1Text =(TextView) findViewById(R.id.temp1);
		temp2Text =(TextView) findViewById(R.id.temp2);
		currentDataText = (TextView) findViewById(R.id.current_date);
		String countyCode = getIntent().getStringExtra("county_code");
		
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
		
		if(!TextUtils.isEmpty(countyCode)){
			//有县级代号去查询天气
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			//没有县级直接显示本地天气
			showWeather();
		}
	}


	/*
	 * 查询县级代号对应天气
	 * */
	private void queryWeatherCode(String countyCode) {
		String address ="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}
	/*
	 * 查询天气代号对应天气
	 * */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address, "weatherCode");
	}
	/*
	 * 根据传入地址和类型去服务器查询天气代号或天气信息
	 * */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if("countyCode".equals(type)){
					//从服务器返回的数据中解析出的天气代号
					String[] array =response.split("\\|");
					if(array!=null&&array.length==2){
						String weatherCode =array[1];
						queryWeatherInfo(weatherCode);
					}
				}else if("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
				
			}
			@Override
			public void onError(Exception e) {
				publishText.setText("同步失败");
			}
		});
	}






	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText("今日"+prefs.getString("publish_time", "")+"发布");
		currentDataText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this,AutoUpdateService.class);
		startService(intent);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
}
