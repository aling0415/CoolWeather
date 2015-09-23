package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelp extends SQLiteOpenHelper {
	
    //province�������
	public static final String CREAT_PROVINCE = "create table Province("+
    "id integer primary key autoincrement,"+"province_name text,"+"province_code text)";
	
	//city�������
	public static final String CREAT_CITY = "create table City("+
			"id integer primary key autoincrement,"+"city_name text,"+"city_code text,"+"province_id integer)";
	
	//country�������
	public static final String CREAT_COUNTRY = "create table Country("+
			"id integer primary key autoincrement,"+"country_name text,"+"country_code text,"+"city_id integer)";
	

	public CoolWeatherOpenHelp(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREAT_PROVINCE);   //����ʡ��
		db.execSQL(CREAT_CITY);      //�������б�  
		db.execSQL(CREAT_COUNTRY);    //����country��
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
