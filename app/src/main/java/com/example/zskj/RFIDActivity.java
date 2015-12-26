package com.example.zskj;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import rfid.ivrjacku1.IvrJackAdapter;
import rfid.ivrjacku1.IvrJackService;
import rfid.ivrjacku1.IvrJackStatus;

public class RFIDActivity extends Activity  implements IvrJackAdapter {

	private boolean bFirstLoad = true;
	private ImageView imgPlugout = null;
	private TextView txtStatus = null;
	private TextView txtTotal = null;
	private TextView txtDate = null;
	//
	private TextView lblEPC = null;
	private TextView lblTimes = null;
	private Button btnQuery = null;
	//
	private Button btnSetting = null;
	private Button clearScreen;
	private ListView epclist;
    //
    private ProgressDialogEx pd;
    private boolean bSuccess;
    private String cMsg;

    private boolean bCancel = false;
    private boolean bOpened = false;
    private MHandler handler = null;
    //
    private CustomListAdapter seqAdapter;
    private ArrayList<seqTag> seqArray = new ArrayList<seqTag>();
    private ArrayList<String> tagArray = new ArrayList<String>();
    private boolean bUpdateRequired = false;

    public static IvrJackService reader = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rfid_main);
		//
		imgPlugout = (ImageView)findViewById(R.id.imgPlugout);
		btnQuery = (Button)findViewById(R.id.btnQuery);
		btnSetting = (Button)findViewById(R.id.btnSetting);
		btnSetting.setVisibility(View.GONE);
		btnSetting.setOnClickListener(new View.OnClickListener()
	    {
			public void onClick(View paramView)
			{
				if (bOpened) {
					showToast("Please stop the inventory tag action.");
					return;
				}
				Intent intent1 = new Intent();
		    	//intent1.setClass(Demo.this, activity_Setting.class);
		    	//startActivity(intent1);
			}
	    });
		lblEPC = (TextView)findViewById(R.id.textView1);
		lblTimes = (TextView)findViewById(R.id.textView11);
		txtTotal = (TextView)findViewById(R.id.txtTotal);
		//
		txtStatus = (TextView)findViewById(R.id.txtStatus);
		txtDate = (TextView)findViewById(R.id.txtDate);
		clearScreen = (Button) findViewById(R.id.btnClear);
		clearScreen.setOnClickListener(new View.OnClickListener()
	    {
	      public void onClick(View paramView)
	      {
	    	  ListClear();
	      }
	    });

		//
		epclist = ((ListView)findViewById(R.id.tag_list));
		epclist.setCacheColorHint(Color.TRANSPARENT);
		epclist.setOnItemClickListener(new epclistItemClick());
	    seqAdapter = new CustomListAdapter(this, R.layout.customlistview, this.seqArray);
	    epclist.setAdapter(this.seqAdapter);
		//
		btnQuery.setOnClickListener(new btnQuery_Click());
		btnQuery.setVisibility(View.GONE);
		handler = new MHandler(this);
    	//
		reader = new IvrJackService();
		reader.open(this, this);
	}

	private void ListClear() {
		seqAdapter.setSelectItem(-1);
		this.seqArray.clear();
		this.txtTotal.setText(" 0");
		this.tagArray.clear();
		this.epclist.setAdapter(this.seqAdapter);
		this.bUpdateRequired = false;
	}

	private void ListRefresh(String paramString) {
		String[] sEPC = paramString.split(";");
		if (sEPC.length > 1) {
			String key = sEPC[0];
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", key);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
			this.finish();
		}
		/*for(String str: sEPC)
		{
			if (this.tagArray.contains(str)) {
				int i = Integer.parseInt(((seqTag) this.seqArray.get(this.tagArray
						.indexOf(str))).getCount());
				((seqTag) this.seqArray.get(this.tagArray.indexOf(str)))
						.setCount(Integer.toString(i + 1));
				if (!this.bUpdateRequired) {
					handler.sendEmptyMessageDelayed(100, 80L);
					this.bUpdateRequired = true;
				}
			} else {
				seqTag localseqTag = new seqTag();
				this.tagArray.add(str);
				localseqTag.setTag(str);
				localseqTag.setNum(Integer.toString(this.tagArray.size()));
				localseqTag.setCount("1");
				this.seqArray.add(localseqTag);
				if (!this.bUpdateRequired) {
					handler.sendEmptyMessageDelayed(100, 80L);
					this.bUpdateRequired = true;
				}
			}
		}
		handler.sendEmptyMessageDelayed(104, 1000L);*/
	}

	@Override
	public void onStart() {
		super.onStart();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  EEEE", Locale.ENGLISH);
    	Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		txtDate.setText(formatter.format(curDate));
	}

	@Override
    public void onDestroy() {
		if (reader != null) {
			reader.close();
			Log.i("HEX", "reader close");
		}
        super.onDestroy();
    }

	private void showToast(String msg) {
		showToast(msg, R.drawable.icon_info, true);
	}

	private void showToast(String msg, int resID, boolean bError) {
	    View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
	    Toast toast = new Toast(getApplicationContext());
	    toast.setGravity(Gravity.CENTER, 0, 0);
	    //if (bError)
	    //	toast.setDuration(Toast.LENGTH_LONG);
	    //else
	    	toast.setDuration(Toast.LENGTH_SHORT);
	    toast.setView(toastRoot);
	    TextView tv = (TextView)toastRoot.findViewById(R.id.toastbox_message);
	    tv.setText(msg);
	    if (resID > 0) {
	    	ImageView iv = (ImageView)toastRoot.findViewById(R.id.toastbox_icon);
	    	iv.setImageResource(resID);
	    }
	    toast.show();
	}

	private void showToast(String msg, int resID) {
	    showToast(msg, resID, false);
	}

	/**
     * 用Handler来更新UI
    */
    static class MHandler extends Handler {
        WeakReference<RFIDActivity> outerClass;

        MHandler(RFIDActivity activity) {
            outerClass = new WeakReference<RFIDActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
			RFIDActivity theClass = outerClass.get();
            switch (msg.what) {
	            case 1:
	    			theClass.pd.dismiss(); // 关闭ProgressDialog
	    			theClass.btnQuery.setEnabled(true);
	    			if (theClass.bCancel) break;
	    			if (theClass.bSuccess) {
	    				theClass.bOpened = !theClass.bOpened;
	    				if (!theClass.bOpened)
	    					theClass.btnQuery.setText(">>>>Start<<<<");
	    				else
	    					theClass.btnQuery.setText(">>>>Stop<<<<");
	    			} else {
	    				if (theClass.cMsg != null)
	    					theClass.showToast(theClass.cMsg);
	    			}
	    			break;

				case 100:
					theClass.seqAdapter.notifyDataSetChanged();
					//theClass.epclist.setSelection(theClass.epclist.getAdapter().getCount() - 1);
					System.out.println(theClass.tagArray.size());
					theClass.bUpdateRequired = false;
					break;

				case 104:
					theClass.txtTotal.setText("Total:" + theClass.tagArray.size());
					theClass.bUpdateRequired = false;
					break;
    		}
        }
    }

	//查询按钮事件
    private class btnQuery_Click implements View.OnClickListener
    {
		public void onClick(View v) 
		{
			btnQuery.setEnabled(false);
			if (!bOpened)
				pd = ProgressDialogEx.show(RFIDActivity.this, "Start read epc");
			else
				pd = ProgressDialogEx.show(RFIDActivity.this, "Stop read epc");
            new Thread(){  
                @Override  
                public void run() {  
                	int ret = 0;
                    try {
                    	cMsg = "Device communication error, make sure that is plugged."; 
                    	bSuccess = false;
						ret = reader.readEPC(!bOpened);
						if (ret == 0 && !bCancel) {
							bSuccess = true;
						}
						else if (ret == -1) {
							cMsg = "Device is running low battery, please charge!";
						}
					} catch (Exception e) {
						cMsg = "Unknown error."; 
                    	bSuccess = false;
					}
                    finally {

                    }
                    handler.sendEmptyMessage(1);
                }}.start();

		}	  
    }

    private class epclistItemClick implements OnItemClickListener 
    {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (bOpened) {
				showToast("Please stop the inventory tag action.");
				return;
			}
			seqAdapter.setSelectItem(arg2);  
			seqAdapter.notifyDataSetInvalidated(); 
			/*activity_TagMemory.sEPC = ((seqTag) seqArray.get(arg2)).getTag();
			Intent intent1 = new Intent();
	    	intent1.setClass(Demo.this, activity_TagMemory.class);
	    	startActivity(intent1);*/
		}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (bOpened) {
    			reader.stopReadEPC();
    		}
	   		finish();
	   		//System.exit(0);
 		   	return true;
 	   	}else{       
 	   		return super.onKeyDown(keyCode, event);
 	   	} 
    }

	@Override
	public void onConnect(String arg0) {
		imgPlugout.setVisibility(View.GONE);
		btnQuery.setVisibility(View.VISIBLE);					
		btnSetting.setVisibility(View.VISIBLE);
		txtTotal.setVisibility(View.VISIBLE);
		lblEPC.setVisibility(View.VISIBLE);
		lblTimes.setVisibility(View.VISIBLE);
		clearScreen.setVisibility(View.VISIBLE);
		epclist.setVisibility(View.VISIBLE);
		txtStatus.setText("Welcome");
		showToast("Recognized.", R.drawable.toastbox_auth_success);
	}

	@Override
	public void onDisconnect() {
		imgPlugout.setVisibility(View.VISIBLE);
		btnQuery.setVisibility(View.INVISIBLE);
		btnSetting.setVisibility(View.INVISIBLE);
		lblEPC.setVisibility(View.INVISIBLE);
		lblTimes.setVisibility(View.INVISIBLE);
		epclist.setVisibility(View.INVISIBLE);
		clearScreen.setVisibility(View.INVISIBLE);
		txtTotal.setVisibility(View.INVISIBLE);
		txtStatus.setText("You are not connected to the device!");
		btnQuery.setText(">>>>Start<<<<");
		bOpened = false;
		if (!bFirstLoad) {
			showToast("Plugout!", R.drawable.toastbox_remove);
		}
		bFirstLoad = false;
		bCancel = false;
	}

	@Override
	public void onInventory(String arg0) {
		ListRefresh(arg0);
	}

	@Override
	public void onStatusChange(IvrJackStatus arg0) {
		switch (arg0) {
			case ijsDetecting: 
				pd = ProgressDialogEx.show(RFIDActivity.this, "Detecting...");
				break;
				
			case ijsRecognized:
				pd.dismiss();
				break;
				
			case ijsUnRecognized:
				pd.dismiss();
				Toast.makeText(this, "Unrecognized!", Toast.LENGTH_SHORT).show();
				break;
		default:
			break;
		}
	}

}

