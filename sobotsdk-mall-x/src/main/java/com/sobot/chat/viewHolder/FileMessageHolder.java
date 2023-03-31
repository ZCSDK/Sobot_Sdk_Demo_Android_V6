package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotSectorProgressView;
import com.sobot.network.http.model.SobotProgress;
import com.sobot.network.http.upload.SobotUpload;
import com.sobot.network.http.upload.SobotUploadListener;
import com.sobot.network.http.upload.SobotUploadModelBase;
import com.sobot.network.http.upload.SobotUploadTask;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 文件消息
 */
public class FileMessageHolder extends MsgHolderBase implements View.OnClickListener {

    private SobotSectorProgressView sobot_file_icon;
    private TextView sobot_file_name;
    private TextView sobot_file_size;
    private RelativeLayout sobot_ll_file_container;

    private ZhiChiMessageBase mData;
    private String mTag;
    private int mResNetError;
    private int mResRemove;


    public FileMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_file_icon = convertView.findViewById(R.id.sobot_file_icon);
        sobot_file_name = convertView.findViewById(R.id.sobot_file_name);
        sobot_file_size = convertView.findViewById(R.id.sobot_file_size);
        sobot_ll_file_container = convertView.findViewById(R.id.sobot_ll_file_container);
        mResNetError = R.drawable.sobot_re_send_selector;
        mResRemove = R.drawable.sobot_icon_remove;
        sobot_ll_file_container.setOnClickListener(this);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mData = message;
        if (message.getAnswer() != null && message.getAnswer().getCacheFile() != null) {
            SobotCacheFile cacheFile = message.getAnswer().getCacheFile();
            sobot_file_name.setText(cacheFile.getFileName());
            sobot_file_size.setText(cacheFile.getFileSize());
            SobotBitmapUtil.display(mContext, ChatUtils.getFileIcon(mContext, cacheFile.getFileType()), sobot_file_icon);
            mTag = cacheFile.getMsgId();
            if (isRight) {
                if (SobotUpload.getInstance().hasTask(mTag)) {
                    SobotUploadTask uploadTask = SobotUpload.getInstance().getTask(mTag);
                    uploadTask.register(new ListUploadListener(mTag, this));

                    refreshUploadUi(uploadTask.progress);
                } else {
                    refreshUploadUi(null);
                }
            } else {
                refreshUploadUi(null);
            }
        }
        if (!isRight()) {
            refreshItem();//左侧消息刷新顶和踩布局
            checkShowTransferBtn();//检查转人工逻辑
            //关联问题显示逻辑
            if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
                resetAnswersList();
            } else {
                hideAnswers();
            }
            if (sobot_msg_content_ll != null) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) sobot_msg_content_ll.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.width = ScreenUtils.dip2px(mContext, 240);
                }
            }
        } else {
            if (msgStatus != null) {
                msgStatus.setOnClickListener(this);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (mData != null) {
            if (sobot_ll_file_container == v) {
                if (mData.getAnswer() != null && mData.getAnswer().getCacheFile() != null) {
                    // 打开详情页面
                    Intent intent = new Intent(mContext, SobotFileDetailActivity.class);
                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, mData.getAnswer().getCacheFile());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }

            if (msgStatus == v) {
                if (msgStatus.isSelected()) {
                    //下载失败
                    showReSendDialog(mContext, msgStatus, new ReSendListener() {

                        @Override
                        public void onReSend() {
                            SobotUploadTask uploadTask = SobotUpload.getInstance().getTask(mTag);
                            if (uploadTask != null) {
                                uploadTask.restart();
                            } else {
                                notifyFileTaskRemove();
                            }
                        }
                    });
                } else {
                    //取消
                    if (SobotUpload.getInstance().hasTask(mTag)) {
                        SobotUploadTask uploadTask = SobotUpload.getInstance().getTask(mTag);
                        uploadTask.remove();
                    }
                    notifyFileTaskRemove();
                }
            }
        }
    }

    private void notifyFileTaskRemove() {
        Intent intent = new Intent(ZhiChiConstants.SOBOT_BROCAST_REMOVE_FILE_TASK);
        intent.putExtra("sobot_msgId", mTag);
        CommonUtils.sendLocalBroadcast(mContext, intent);
    }

    private String getTag() {
        return mTag;
    }

    private void refreshUploadUi(SobotProgress progress) {
        if (progress == null) {
            if (msgStatus != null) {
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
            }
            // sobot_progress.setProgress(100);
            return;
        }
        if (msgStatus == null) {
            return;
        }
        switch (progress.status) {
            case SobotProgress.NONE:
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
                // sobot_progress.setProgress(progress.fraction * 100);
                break;
            case SobotProgress.ERROR:
                msgStatus.setVisibility(View.VISIBLE);
                msgStatus.setBackgroundResource(mResNetError);
                msgStatus.setSelected(true);
                //  sobot_progress.setProgress(100);
                msgProgressBar.setVisibility(View.GONE);
                break;
            case SobotProgress.FINISH:
                msgStatus.setVisibility(View.GONE);
                //   sobot_progress.setProgress(100);
                msgProgressBar.setVisibility(View.GONE);
                break;
            case SobotProgress.PAUSE:
            case SobotProgress.WAITING:
            case SobotProgress.LOADING:
                msgProgressBar.setVisibility(View.VISIBLE);
                msgStatus.setVisibility(View.GONE);
                msgStatus.setBackgroundResource(mResRemove);
                msgStatus.setSelected(false);
                //    sobot_progress.setProgress(progress.fraction * 100);
                break;
        }
    }

    private static class ListUploadListener extends SobotUploadListener {

        private FileMessageHolder holder;

        ListUploadListener(Object tag, FileMessageHolder holder) {
            super(tag);
            this.holder = holder;
        }

        @Override
        public void onStart(SobotProgress progress) {

        }

        @Override
        public void onProgress(SobotProgress progress) {
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
            }
        }

        @Override
        public void onError(SobotProgress progress) {
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
            }
        }

        @Override
        public void onFinish(SobotUploadModelBase result, SobotProgress progress) {
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
            }
        }

        @Override
        public void onRemove(SobotProgress progress) {

        }
    }
}