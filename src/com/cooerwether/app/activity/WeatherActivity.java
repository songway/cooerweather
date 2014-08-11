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
	
	/*��ʾ������*/
	private TextView cityNameText;
	/*��ʾ����ʱ��*/
	private TextView publishText;
	/*��ʾ����������Ϣ*/
	private TextView weatherDespText;
	/*��ʾ����1*/
	private TextView temp1Text;
	/*��ʾ����2*/
	private TextView temp2Text;
	/*��ʾ��ǰ����*/
	private TextView currentDataText;
	/*�л�����*/
	private Button switchCity;
	/*��������*/
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
			//���ؼ�����ȥ��ѯ����
			publishText.setText("ͬ����");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			//û���ؼ�ֱ����ʾ��������
			showWeather();
		}
	}


	/*
	 * ��ѯ�ؼ����Ŷ�Ӧ����
	 * */
	private void queryWeatherCode(String countyCode) {
		String address ="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}
	/*
	 * ��ѯ�������Ŷ�Ӧ����
	 * */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address, "weatherCode");
	}
	/*
	 * ���ݴ����ַ������ȥ��������ѯ�������Ż�������Ϣ
	 * */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if("countyCode".equals(type)){
					//�ӷ��������ص������н���������������
					String[] array =response.split("\\|");
					if(array!=null&&array.length==2){
						String weatherCode =array[1];
						queryWeatherInfo(weatherCode);
					}
				}else if("weatherCode".equals(type)){
					//������������ص�������Ϣ
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
				publishText.setText("ͬ��ʧ��");
			}
		});
	}






	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText("����"+prefs.getString("publish_time", "")+"����");
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
			publishText.setText("ͬ����...");
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
