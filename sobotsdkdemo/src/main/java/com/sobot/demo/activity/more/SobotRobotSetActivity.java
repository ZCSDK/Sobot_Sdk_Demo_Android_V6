package com.sobot.demo.activity.more;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sobot.demo.R;
import com.sobot.demo.SobotSPUtil;

/**
 * Created by Administrator on 2017/11/21.
 */

public class SobotRobotSetActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText sobot_robot_code;//指定接入的机器人编号
    private EditText sobot_robot_alias;//指定接入的机器人编号对应的别名
    private RelativeLayout sobot_tv_left;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sobot_robot_set_activity);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        findvViews();
    }

    private void findvViews(){
        sobot_tv_left = (RelativeLayout) findViewById(R.id.sobot_demo_tv_left);
        sobot_tv_left .setOnClickListener(this);
        TextView sobot_text_title = (TextView) findViewById(R.id.sobot_demo_tv_title);
        sobot_text_title.setText("对接机器人设置");
        sobot_robot_code = (EditText) findViewById(R.id.sobot_robot_code);
        sobot_robot_alias = (EditText) findViewById(R.id.sobot_robot_alias);

        getSobotStartSet();
    }

    private void getSobotStartSet() {
        String sobot_value_robot_code = SobotSPUtil.getStringData(this, "sobot_demo_robot_code", "");
        if (!TextUtils.isEmpty(sobot_value_robot_code)) {
            sobot_robot_code.setText(sobot_value_robot_code);
        }
        String sobot_value_robot_alias = SobotSPUtil.getStringData(this, "sobot_demo_robot_alias", "");
        if (!TextUtils.isEmpty(sobot_value_robot_alias)) {
            sobot_robot_alias.setText(sobot_value_robot_alias);
        }
    }

    private void saveSobotStartSet() {
        SobotSPUtil.saveStringData(this, "sobot_demo_robot_code", sobot_robot_code.getText().toString());
        SobotSPUtil.saveStringData(this, "sobot_demo_robot_alias", sobot_robot_alias.getText().toString());
    }

    @Override
    public void onBackPressed() {
        saveSobotStartSet();
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_tv_left){
            saveSobotStartSet();
            finish();
        }
    }
}