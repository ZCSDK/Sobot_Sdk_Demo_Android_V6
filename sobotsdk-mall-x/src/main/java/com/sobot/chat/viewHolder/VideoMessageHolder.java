package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.network.http.model.SobotProgress;
import com.sobot.network.http.upload.SobotUpload;
import com.sobot.network.http.upload.SobotUploadListener;
import com.sobot.network.http.upload.SobotUploadModelBase;
import com.sobot.network.http.upload.SobotUploadTask;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 视频
 */
public class VideoMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private ImageView st_tv_play;
    private SobotRCImageView st_iv_pic;
    private int sobot_bg_default_pic;

    private ZhiChiMessageBase mData;
    private String mTag;

    public VideoMessageHolder(Context context, View convertView) {
        super(context, convertView);
        st_tv_play = (ImageView) convertView.findViewById(R.id.st_tv_play);
        st_iv_pic = (SobotRCImageView) convertView.findViewById(R.id.st_iv_pic);
        sobot_bg_default_pic = R.drawable.sobot_bg_default_pic;
        answersList = (LinearLayout) convertView
                .findViewById(R.id.sobot_answersList);
        st_tv_play.setOnClickListener(this);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mData = message;
        if (message.getAnswer() != null && message.getAnswer().getCacheFile() != null) {
            SobotCacheFile cacheFile = message.getAnswer().getCacheFile();
            SobotBitmapUtil.display(mContext, cacheFile.getSnapshot(), st_iv_pic, sobot_bg_default_pic, sobot_bg_default_pic);
            mTag = cacheFile.getMsgId();
            if (isRight()) {
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
                if (sobot_msg_content_ll != null) {
                    //图片、视频、文件、小程序根据关联问题数量动态判断气泡内间距
                    sobot_msg_content_ll.setPadding((int) mContext.getResources().getDimension(R.dimen.sobot_msg_left_right_padding_edge), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_top_bottom_padding_edge), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_left_right_padding_edge), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_top_bottom_padding_edge));
                }
            } else {
                hideAnswers();
                if (sobot_msg_content_ll != null) {
                    //图片、视频、文件、小程序根据关联问题数量动态判断气泡内间距
                    sobot_msg_content_ll.setPadding(0, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mData != null) {
            if (st_tv_play == v) {
                if (mData.getAnswer() != null && mData.getAnswer().getCacheFile() != null) {
                    SobotCacheFile cacheFile = mData.getAnswer().getCacheFile();
                    // 播放视频
                    Intent intent = SobotVideoActivity.newIntent(mContext, cacheFile);
                    mContext.startActivity(intent);
                }
            }

            if (msgStatus == v) {
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
            }
            if(msgProgressBar!=null) {
                msgProgressBar.setProgress(100);
                msgProgressBar.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
            }
            st_tv_play.setVisibility(View.VISIBLE);
            return;
        }
        if (msgStatus == null) {
            return;
        }
        switch (progress.status) {
            case SobotProgress.NONE:
                msgStatus.setVisibility(View.GONE);
                st_tv_play.setVisibility(View.VISIBLE);
                msgProgressBar.setProgress((int) (progress.fraction * 100));
                break;
            case SobotProgress.ERROR:
                st_tv_play.setVisibility(View.VISIBLE);
                msgStatus.setVisibility(View.VISIBLE);
                msgProgressBar.setProgress(100);
                msgProgressBar.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
                break;
            case SobotProgress.FINISH:
                st_tv_play.setVisibility(View.VISIBLE);
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setProgress(100);
                msgProgressBar.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
                break;
            case SobotProgress.PAUSE:
            case SobotProgress.WAITING:
            case SobotProgress.LOADING:
                st_tv_play.setVisibility(View.GONE);
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setProgress((int) (progress.fraction * 100));
                msgProgressBar.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    private static class ListUploadListener extends SobotUploadListener {

        private VideoMessageHolder holder;

        ListUploadListener(Object tag, VideoMessageHolder holder) {
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
