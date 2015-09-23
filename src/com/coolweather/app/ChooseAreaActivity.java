package com.coolweather.app;

import java.util.ArrayList;
import java.util.List;

import model.City;
import db.CoolWeatherDB;
import model.Country;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	
	//�Ƿ��weatherActivity��ת����
	private boolean isFromWeatherActivity;
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	//ʡ�б�������
	private List<Province> provinceList;
	//���б�����
	private List<City> cityList;
	//���б�����
	
	private List<Country> countryList;
	
	//ѡ�е�ʡ��
	 private Province selectedProvince;
	 //ѡ�еĳ���
	 private City selectedCity;
	 //��ǰѡ�еļ���
	 private int currentLevel;
	 
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false );
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//�Ѿ���ʾ�˳��У��Ҳ��Ǵ�weatherActivity��ת�����ľ���ʾ����
		if (prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity) {
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel==LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
					
				}else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				}else if (currentLevel == LEVEL_COUNTRY) {
					String countryCode = countryList.get(position).getCountryCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("country_code", countryCode);
					startActivity(intent);
					finish();
				}
				
			}
		});
		queryProvinces(); //����ʡ������
		
	}
	//��ѯȫ�����е�ʡ ���ȴ����ݿ��ѯ�����û�в鵽��ȥ��������ѯ��
	 private void queryProvinces(){
		 provinceList = coolWeatherDB.loadProvinces();
		 if (provinceList.size()>0) {
			dataList.clear();
			for (Province province: provinceList) {
				dataList.add(province.getProvinceName());
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}else {
			queryFromServer(null,"province");
			
		}	 
	 }
	//��ѯѡ��ʡ���е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ��������ѯ������
	 private void queryCities(){
		 cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		 if (cityList.size()>0) {
			dataList.clear();
			for (City city: cityList) {
				dataList.add(city.getCityName());
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
			
		}	 
	 }
	//��ѯ���е��أ����ȴ����ݿ��в�ѯ�������ѯ������ȥ�������ϲ�ѯ����
	 private void queryCounties(){
		 countryList = coolWeatherDB.loadCounties(selectedCity.getId());
		 if (countryList.size()>0) {
			dataList.clear();
			for (Country country: countryList) {
				dataList.add(country.getCountryName());
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTRY;
		}else {
			queryFromServer(selectedCity.getCityCode(),"country");
			
		}	 
	 }
	//���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ���ص�����
	 private void queryFromServer(final String code,final String type){
		 String address;
		 if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		 showProgressDialog();
		 HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvinceRespnose(coolWeatherDB, response);
				}else if ("city".equals(type)) {
					result = Utility.handleCitiesRespnose(coolWeatherDB, response, selectedProvince.getId());
				}else if ("country".equals(type)) {
					result = Utility.handleCountryRespnose(coolWeatherDB, response,selectedCity.getId());
				}
				if (result) {
					//ͨ��runOnUiThread()�����ص����߳��ϴ����߼�
					runOnUiThread(new  Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("country".equals(type)) {
								queryCounties();
							}
						}

						
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				//ͨ��runOnThread()�����ص����̴߳����߼�����
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	 }
	 
	 
	 
	 
	 
	private void showProgressDialog() {
		// ��ʾ���ȶԻ���
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���......");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		
	}
	 
	 
	private void closeProgressDialog() {
		// �رս��ȶԻ���
		if (progressDialog!=null) {
			progressDialog.dismiss();
		}
		
	}
	//����back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷��ص������б�ʡ�б�����ֱ���˳�
	public void onBackPressed(){
		if (currentLevel==LEVEL_COUNTRY) {
			queryCities();
		}else if (currentLevel==LEVEL_CITY) {
			queryProvinces();
		}else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
	
	

}
