package com.cooerwether.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooerweather.app.R;
import com.cooerwether.app.db.CoolWeatherDB;
import com.cooerwether.app.model.City;
import com.cooerwether.app.model.County;
import com.cooerwether.app.model.Province;
import com.cooerwether.app.util.HttpCallbackListener;
import com.cooerwether.app.util.HttpUtil;
import com.cooerwether.app.util.Utility;

public class ChooseAreaActivity extends Activity{
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEAL_CITY=1;
	public static final int LEVEAL_COUNTY=2;
	
	/*
	 * ʡ�б�
	 * */
	private List<Province> provinceList;
	
	/*
	 * ���б�
	 * */
	private List<City> cityList;
	
	/*
	 * ���б�
	 * */
	private List<County> countyList;
	
	/*
	 * ѡ�е�ʡ��
	 * */
	private Province selectedProvince;
	/*
	 * ѡ�еĳ���
	 * */
	private City selectedCity;
	/*
	 * ѡ�еļ���
	 * */
	private int currentLevel;
	
	/*
	 * �Ƿ��weatheracitivy����ת����
	 * */
	private boolean isFromWeatherActivity;
	
	private ListView listView;
	private TextView titleText;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> datalist = new ArrayList<String>();
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity){
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView =(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,datalist );
		listView.setAdapter(adapter);
		coolWeatherDB =CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince = provinceList.get(position);
					queryCities();
				}else if(currentLevel ==LEVEAL_CITY){
					selectedCity = cityList.get(position);
					queryCounties();
				}else if(currentLevel ==LEVEAL_COUNTY){
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		 queryProvinces();//����ʡ������
		
	}

	/*��ѯȫ������ʡ�ݣ����ȴ����ݿ��ѯ�������ٵ���������ѯ*/
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			datalist.clear();
			for (Province province : provinceList) {
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
	}


	/*��ѯѡ��ʡ�ݵ����г��У����ȴ����ݿ��ѯ�������ٵ���������ѯ*/
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			datalist.clear();
			for (City city : cityList) {
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEAL_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
		
	}
	/*��ѯѡ�г��е��أ����ȴ����ݿ��ѯ�������ٵ���������ѯ*/
	private void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			datalist.clear();
			for (County county : countyList) {
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEAL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
		
	}
	
	/*���ݴ���Ĵ��ź����ʹӷ�������ѯ����*/
	private void queryFromServer(final String code,final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address= "http://www.weather.com.cn/data/list3/city.xml";
		}
		Log.i("ChooseAreActivity_address", address);
		showProgressDialog();
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;

				if("province".equals(type)){
					result= Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if ("city".equals(type)){
					result= Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result= Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					//ͨ��runOnUiThread�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if ("city".equals(type)){
								queryCities();
							}else if ("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
				
			}
			@Override
			public void onError(Exception e) {
				Log.e("ChoooseAreaActivity", e.getMessage());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}


				});
			}
		});
		
	}
	
	/*��ʾ���ȶԻ���*/
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ�����...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/*�رս��ȶԻ���*/
	private void closeProgressDialog() {
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/*
	 * ����Back�������ݵ�ǰ�ļ����жϣ���ʱ�÷������б�ʡ���б������˳�
	 * */
	
	@Override
	public void onBackPressed() {
		if(currentLevel==LEVEAL_COUNTY){
			queryCities();
		}else if (currentLevel ==LEVEAL_CITY){
			queryProvinces();
		}else {
			if(isFromWeatherActivity){
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
		
	}
	
	
}
