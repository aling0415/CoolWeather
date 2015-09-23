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
	//������ʾ������
	private TextView cityNameText;
	//������ʾ��������
	private TextView publishText;
	//������ʾ����������Ϣ
	private TextView weatherDespText;
	//������ʾ����1
	private TextView temp1Text;
	//������ʾ����2
	private TextView temp2Text;
	//������ʾ��ǰ����
	private TextView currentDateText;
	//�л�����
	private Button switchCity;
	//��������
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ�����ؼ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.public_name);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text =(TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_data);
		String countryCode = getIntent().getStringExtra("country_code");
		if (!TextUtils.isEmpty(countryCode)) {
			//���ؼ�����ʱ��ȥ��ѯ����
			publishText.setText("ͬ���С���������");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		}else {
			//û���ؼ�����ʱ���ֱ����ʾ��������
			showWeather();
		}
		switchCity = (Button) findViewById(R.id.switch_city);
		switchCity.setOnClickListener(this);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		refreshWeather.setOnClickListener(this);
	}
	//��ѯ�ؼ���������Ӧ����������
	private void queryWeatherCode(String countryCode){
		String address = "http://www.weather.com.cn/data/list3/city"+countryCode+".xml";
		queryFormServer(address,"countryCode");
	}
	//��ѯ������������Ӧ������
	private void  queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFormServer(address,"weatherCode");
	}
	//���ݴ�����������ź����ͣ�ȥ��������ѯ����Ӧ������
	private void queryFormServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {
				// TODO Auto-generated method stub
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//�ӷ��������ص���Ϣ�н�������������
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
						publishText.setText("ͬ��ʧ��");
					}
				});
				
			}
		});
	}
	//��SharePreference�϶�ȡ��������Ϣ  ������ʾ�ڽ�����
	private void showWeather(){
		SharedPreferences shpfers = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(shpfers.getString("city_name", ""));
		temp1Text.setText(shpfers.getString("temp1", ""));
		temp2Text.setText(shpfers.getString("temp2", ""));
		weatherDespText.setText(shpfers.getString("weather_desp", ""));
		publishText.setText("����"+shpfers.getString("publish_time", "")+"����");
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
			publishText.setText("ͬ���С�������");
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
	//���ؼ��Ĺ����趨
//	 @Override
//	public void onBackPressed() {
//		Intent intent = new Intent(this,ChooseAreaActivity.class);
//		startActivity(intent);
//		finish();
//	}
	

}
