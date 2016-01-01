package com.example.zskj;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
	private final static int SCANNIN_GREQUEST_CODE = 1;
	DBHelper dbHelper;
	/**
	 */
	private TextView mTextView ;
	private TableLayout mTableLayout;
	/**
	 * ��ʾɨ���ĵ�ͼƬ
	 */
	private ImageView mImageView;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextView = (TextView) findViewById(R.id.result);
		mTableLayout = (TableLayout) findViewById(R.id.tb_Results);
		//mImageView = (ImageView) findViewById(R.id.qrcode_bitmap);
		
		Button mButton = (Button) findViewById(R.id.button_QRScan);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MipcaActivityCapture.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}
		});

		Button btn_RFID = (Button) findViewById(R.id.button_RFIDScan);
		btn_RFID.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, RFIDActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}
		});

		try {
			OutputStream ostr = new FileOutputStream("/data/data/com.example.zskj/databases/stage.db");
			InputStream istr = getAssets().open("stage.db");
			int bufLen = 2048;
			byte[] buf = new byte[bufLen];
			int len=0;
			int pos=0;
			while((len=istr.read(buf,0,bufLen))>0) {
				ostr.write(buf,0,len);
				pos += len;
			}
			istr.close();
			ostr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		dbHelper = new DBHelper(this);
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				String key = bundle.getString("result");

				android.database.sqlite.SQLiteDatabase db = openOrCreateDatabase("stage.db", Context.MODE_PRIVATE, null);
				try {
//					Cursor c = db.rawQuery("select * from DEPT where DEPTNO=?", new String[] {key});
					Cursor c = db.rawQuery("select * from 继电保护装置 where 设备编码=?", new String[] {key});
					if(c.moveToNext()) {
						fillResultListView(c);
					}
					else {
						mTextView.setText("未找到该设备信息, 设备编码: " + key);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally {
					db.close();
				}
				mTableLayout.bringToFront();
				//mTextView.setText(key);
				//mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
			}
			break;
		}
    }

	void fillResultListView(Cursor c) {
		//TableRow tr = new TableRow();
		for (int i = 0; i < c.getColumnCount(); i++) {
			TableRow tr = (TableRow) mTableLayout.getChildAt(i);
			if (tr == null) {
				tr = new TableRow(this);
				TextView tr1 = new TextView(this);
				TextView tr2 = new TextView(this);
				tr.addView(tr1);
				tr.addView(tr2);
				mTableLayout.addView(tr);
			}
			((TextView)tr.getChildAt(0)).setText(c.getColumnName(i));
			((TextView)tr.getChildAt(1)).setText(c.getString(i));
		}
	}

}
