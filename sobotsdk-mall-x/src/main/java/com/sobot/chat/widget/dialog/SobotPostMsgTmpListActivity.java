package com.sobot.chat.widget.dialog;

import android.content.Intent;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotPostMsgTmpListAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotLeaveMsgConfig;
import com.sobot.chat.api.model.SobotPostMsgTemplate;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;

/**
 * 选择留言模板
 * Created by jinxl on 2018/3/5.
 */
public class SobotPostMsgTmpListActivity extends SobotDialogBaseActivity implements  View.OnClickListener {
    private LinearLayout sobot_negativeButton;
    private GridView sobot_gv;

    private TextView sobot_tv_title;

    private ArrayList<SobotPostMsgTemplate> mDatas;


    private SobotPostMsgTmpListAdapter mListAdapter;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_post_msg_tmps;
    }

    @Override
    protected void initView() {
        sobot_negativeButton = (LinearLayout) findViewById(R.id.sobot_negativeButton);
        sobot_gv = (GridView) findViewById(R.id.sobot_gv);
        sobot_negativeButton.setOnClickListener(this);
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_choice_business);
        displayInNotch(this,sobot_gv);
    }

    @Override
    protected void initData() {
        mDatas = (ArrayList<SobotPostMsgTemplate>) getIntent().getSerializableExtra("sobotPostMsgTemplateList");
        if (mListAdapter == null) {
            mListAdapter = new SobotPostMsgTmpListAdapter(getContext(), mDatas, new SobotPostMsgTmpListAdapter.TemItemOnClick() {
                @Override
                public void onItemClick(SobotPostMsgTemplate item) {
                    zhiChiApi.getMsgTemplateConfig(getContext(), getIntent().getStringExtra("uid"), item.getTemplateId(), new StringResultCallBack<SobotLeaveMsgConfig>() {
                        @Override
                        public void onSuccess(SobotLeaveMsgConfig data) {
                            if (data != null) {
                                //选择留言模版成功 发送广播
                                Intent intent = new Intent();
                                intent.setAction(ZhiChiConstants.SOBOT_POST_MSG_TMP_BROCAST);
                                intent.putExtra("sobotLeaveMsgConfig", data);
                                intent.putExtra("uid", getIntent().getStringExtra("uid"));
                                intent.putExtra("mflag_exit_sdk", getIntent().getBooleanExtra("flag_exit_sdk", false));
                                intent.putExtra("mIsShowTicket", getIntent().getBooleanExtra("isShowTicket", false));
                                CommonUtils.sendLocalBroadcast(getContext(), intent);
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Exception e, String des) {

                        }
                    });
                }
            });
            sobot_gv.setAdapter(mListAdapter);
        }
    }


    @Override
    public void onClick(View v) {
        if (v == sobot_negativeButton) {
            finish();
        }
    }


}