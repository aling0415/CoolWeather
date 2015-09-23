package com.coolweather.app;

import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
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

public class WeatherActivity extends Activity  implements OnClickListener{
	private LinearLayout weatherInfoLayout;
	//用于显示城市名
	private TextView cityNameText;
	//用于显示发布日期
	private TextView publishText;
	//用于显示天气描述信息
	private TextView weatherDespText;
	//用于显示气温1
	private TextView temp1Text;
	//用于显示气温2
	private TextView temp2Text;
	//用于显示当前日期
	private TextView currentDateText;
	//切换城市
	private Button switchCity;
	//更新天气
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.public_name);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text =(TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_data);
		String countryCode = getIntent().getStringExtra("country_code");
		if (!TextUtils.isEmpty(countryCode)) {
			//有县级代号时就去查询天气
			publishText.setText("同步中。。。。。");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		}else {
			//没有县级代号时候就直接显示本地天气
			showWeather();
		}
		switchCity = (Button) findViewById(R.id.switch_city);
		switchCity.setOnClickListener(this);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		refreshWeather.setOnClickListener(this);
	}
	//查询县级代号所对应的天气代号
	private void queryWeatherCode(String countryCode){
		String address = "http://www.weather.com.cn/data/list3/city"+countryCode+".xml";
		queryFormServer(address,"countryCode");
	}
	//查询天气代号所对应的天气
	private void  queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFormServer(address,"weatherCode");
	}
	//根据传入的天气代号和类型，去服务器查询所对应的天气
	private void queryFormServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {
				// TODO Auto-generated method stub
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//从服务器返回的信息中解析出天气代号
						String[] array = response.split("\\|");
						if (array!=null&&array.length==2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}

			
				}else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread( new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败");
					}
				});
				
			}
		});
	}
	//从SharePreference上读取到天气信息  并且显示在界面上
	private void showWeather(){
		SharedPreferences shpfers = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(shpfers.getString("city_name", ""));
		temp1Text.setText(shpfers.getString("temp1", ""));
		temp2Text.setText(shpfers.getString("temp2", ""));
		weatherDespText.setText(shpfers.getString("weather_desp", ""));
		publishText.setText("今天"+shpfers.getString("publish_time", "")+"发布");
		currentDateText.setText(shpfers.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent  = new Intent(this,AutoUpdateService.class);
		startService(intent);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中。。。。");
			SharedPreferences prefes = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefes.getString("weatherCode", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;

		default:
			break;
		}
	}
	//返回键的功能设定
//	 @Override
//	public void onBackPressed() {
//		Intent intent = new Intent(this,ChooseAreaActivity.class);
//		startActivity(intent);
//		finish();
//	}
	

}
