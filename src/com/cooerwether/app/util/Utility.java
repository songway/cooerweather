package com.cooerwether.app.util;

import android.Manifest.permission;
import android.text.TextUtils;

import com.cooerwether.app.db.CoolWeatherDB;
import com.cooerwether.app.model.City;
import com.cooerwether.app.model.County;
import com.cooerwether.app.model.Province;

public class Utility {
	/*
	 * �����ʹ�����������ص�ʡ������
	 * */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
			String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvince = response.split(",");
			if(allProvince!=null&&allProvince.length>0){
				for (String p : allProvince) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					//�������������ݴ洢��Province��
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
			String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities = response.split(",");
			if(allCities!=null&&allCities.length>0){
				for (String c : allCities) {
					String[] array =c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//���������ݴ洢��City��
					coolWeatherDB.saveCity(city);
				}
			}
			
		}
		return false;
	}
	
	
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB ,
			String response, int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties = response.split(",");
			if(allCounties!=null&&allCounties.length>0){
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					//���������ݴ洢��county��
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
		
		
		
	}
	
}
