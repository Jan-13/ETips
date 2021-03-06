package com.meizhuo.etips.activities;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.meizhuo.etips.common.ETipsContants;
import com.meizhuo.etips.model.ElectricityInfo;
import com.meizhuo.etips.net.utils.LifeServiceAPI;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 查询电费
 * @author Jayin Ton
 *
 */
public class LifeElectricityActivity extends BaseUIActivity {
	private EditText et_falt, et_room;
	private TextView tv_tip, tv_flat, tv_room, tv_hasUse, tv_left,
			tv_recordTime;
	private RelativeLayout relayout;
	private ProgressBar pb;
	private String roomNum, flagNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acty_life_electricity);
		initData();
		initLayout();
	}
	
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.acty_life_electricity, menu);
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() ==  R.id.query){
			query();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void initLayout() {
		et_falt = (EditText) this
				.findViewById(R.id.acty_life_electricity_et_input_flatNum);
		et_room = (EditText) this
				.findViewById(R.id.acty_life_electricity_et_input_roomNum);
		tv_tip = (TextView) this
				.findViewById(R.id.acty_life_electricity_tv_tip);
		tv_flat = (TextView) this
				.findViewById(R.id.acty_life_electricity_flatNum);
		tv_room = (TextView) this
				.findViewById(R.id.acty_life_electricity_roomNum);
		tv_hasUse = (TextView) this
				.findViewById(R.id.acty_life_electricity_hasUse);
		tv_left = (TextView) this.findViewById(R.id.acty_life_electricity_left);
		tv_recordTime = (TextView) this
				.findViewById(R.id.acty_life_electricity_recordTime);
		pb = (ProgressBar) this
				.findViewById(R.id.acty_life_electricity_progressBar);
		relayout = (RelativeLayout) this
				.findViewById(R.id.acty_life_electricity_re_electricityInfo);

	}

	@Override
	protected void initData() {

	}
	
	private void query(){
		flagNum = et_falt.getText().toString().trim();
		if (flagNum == null || flagNum.equals("")) {
			Toast.makeText(LifeElectricityActivity.this, "公寓名称不能为空！",
					Toast.LENGTH_SHORT).show();
			return;
		}
		roomNum = et_room.getText().toString().trim();
		if (roomNum == null || roomNum.equals("")) {
			Toast.makeText(LifeElectricityActivity.this, "房间编号不能为空！",
					Toast.LENGTH_SHORT).show();
			return;
		}
		LifeElcHandler handler = new LifeElcHandler();
		new LifeElcThread(handler).start();
	}

	class LifeElcHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ETipsContants.Start:
				tv_tip.setVisibility(View.GONE);
				pb.setVisibility(View.VISIBLE);
				relayout.setVisibility(View.GONE);
				break;

			case ETipsContants.Finish:
				ElectricityInfo elc = (ElectricityInfo) msg.obj;
				tv_tip.setVisibility(View.GONE);
				pb.setVisibility(View.GONE);
				relayout.setVisibility(View.VISIBLE);

				tv_flat.setText(elc.apartID);
				tv_room.setText(elc.meterRoom);
				tv_hasUse.setText(elc.hasUseElc);
				tv_left.setText(elc.ElcLeft);
				tv_recordTime.setText(elc.RecordTime);
				break;
			case ETipsContants.Fail:
				tv_tip.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
				relayout.setVisibility(View.GONE);
				tv_tip.setText((String) msg.obj);
				break;
			}
		}
	}

	class LifeElcThread extends Thread {
		Handler handler;

		public LifeElcThread(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			handler.sendEmptyMessage(ETipsContants.Start);
			LifeServiceAPI api = new LifeServiceAPI();
			Message msg = handler.obtainMessage();
			try {
				ElectricityInfo elc = api.getElectricityInfo(flagNum, roomNum);
				if (elc != null && !elc.hasUseElc.equals("")) {
					msg.what = ETipsContants.Finish;
					msg.obj = elc;
				} else {
					msg.what = ETipsContants.Fail;
					msg.obj = "查询失败，原因可能是：1.请检查你的输入 and 网络环境 2.你宿舍不属于智能用电宿舍楼";
				}
				handler.sendMessage(msg);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				msg.what = ETipsContants.Fail;
				msg.obj = "网络连接错误";
				handler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
				msg.what = ETipsContants.Fail;
				msg.obj = "网络连接错误 ";
				handler.sendMessage(msg);
			}

		}
	}

}
