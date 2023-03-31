package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 商品卡片
 */
public class CardMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private ImageView mPic;
    private TextView mTitle;
    private TextView mLabel;
    private TextView mDes;
    private int defaultPicResId;
    private ConsultingContent mConsultingContent;

    public CardMessageHolder(Context context, View convertView) {
        super(context, convertView);
        mPic = (ImageView) convertView.findViewById(R.id.sobot_goods_pic);
        mTitle = (TextView) convertView.findViewById(R.id.sobot_goods_title);
        mLabel = (TextView) convertView.findViewById(R.id.sobot_goods_label);
        mDes = (TextView) convertView.findViewById(R.id.sobot_goods_des);
        defaultPicResId = R.drawable.sobot_icon_consulting_default_pic;
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mConsultingContent = message.getConsultingContent();
        if (message.getConsultingContent() != null) {
            if (!TextUtils.isEmpty(CommonUtils.encode(message.getConsultingContent().getSobotGoodsImgUrl()))) {
                mPic.setVisibility(View.VISIBLE);
                mDes.setMaxLines(1);
                mDes.setEllipsize(TextUtils.TruncateAt.END);
                SobotBitmapUtil.display(context, CommonUtils.encode(message.getConsultingContent().getSobotGoodsImgUrl())
                        , mPic, defaultPicResId, defaultPicResId);
            } else {
                mPic.setVisibility(View.GONE);
            }

            mTitle.setText(message.getConsultingContent().getSobotGoodsTitle());
            mLabel.setText(message.getConsultingContent().getSobotGoodsLable());
            mDes.setText(message.getConsultingContent().getSobotGoodsDescribe());
            mLabel.setTextColor(ThemeUtils.getThemeColor(mContext));
            if (isRight) {
                try {
                    msgStatus.setClickable(true);
                    if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                        msgStatus.setVisibility(View.GONE);
                        msgProgressBar.setVisibility(View.GONE);
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                        msgStatus.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.GONE);
//                        msgStatus.setOnClickListener(new TextMessageHolder.ReSendTextLisenter(context, message
//                                .getId(), content, msgStatus, msgCallBack));
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                        msgProgressBar.setVisibility(View.VISIBLE);
                        msgStatus.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        sobot_msg_content_ll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_msg_content_ll && mConsultingContent != null) {
            if (SobotOption.hyperlinkListener != null) {
                SobotOption.hyperlinkListener.onUrlClick(mConsultingContent.getSobotGoodsFromUrl());
                return;
            }

            if (SobotOption.newHyperlinkListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, mConsultingContent.getSobotGoodsFromUrl());
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", mConsultingContent.getSobotGoodsFromUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
