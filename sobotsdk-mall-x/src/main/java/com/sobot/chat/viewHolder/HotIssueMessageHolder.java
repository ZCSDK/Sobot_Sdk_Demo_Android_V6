package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.BusinessLineRespVo;
import com.sobot.chat.api.model.FaqDocRespVo;
import com.sobot.chat.api.model.GroupRespVo;
import com.sobot.chat.api.model.SobotFaqDetailModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.horizontalscroll.IssueViewPagerdAdapter;
import com.sobot.chat.widget.horizontalscroll.MyHorizontalScrollView;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 常见问题
 * Created by guoqf on 2021.06.25
 */
public class HotIssueMessageHolder extends MsgHolderBase {

    private Context mContext;
    //业务类
    private MyHorizontalScrollView fastMenu;
    private IssueViewPagerdAdapter fastMenuAdapter;

    private ImageView sobot_hot_pic;
    private HorizontalScrollView tab_hot_title;//问题分类
    private LinearLayout horizontalScrollView_ll,lin_question_list ;//分体分类
    private TextView sobot_tv_switch;
    private int blockIndex=0,groupIndex = 0;
    private int PAGE_NUM=5;
    private int curPageNum=0;

    private List<FaqDocRespVo> faqDocRespVoList = new ArrayList<>();


    public HotIssueMessageHolder(Context context, View convertView ) {
        super(context, convertView);
        mContext = context;
        fastMenu=  convertView.findViewById(R.id.sobot_fast_menu);
        tab_hot_title=  convertView.findViewById(R.id.tab_hot_title);
        horizontalScrollView_ll =  convertView.findViewById(R.id.horizontalScrollView_ll);
        sobot_hot_pic=  convertView.findViewById(R.id.sobot_hot_pic);
        lin_question_list =  convertView.findViewById(R.id.lin_question_list);
        sobot_tv_switch =  convertView.findViewById(R.id.sobot_tv_switch_list);
        sobot_tv_switch.setVisibility(View.GONE);
        sobot_tv_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 换一组
                if (faqDocRespVoList != null && faqDocRespVoList.size() > 0) {
                    curPageNum = curPageNum + 1;
                    int total =faqDocRespVoList.size();
                    int maxNum = (total % PAGE_NUM == 0) ? (total / PAGE_NUM) : (total / PAGE_NUM + 1);
                    curPageNum = (curPageNum >= maxNum) ? 0 : curPageNum;
//                    LogUtils.d("=====sobot_tv_switch=onClick==showList====");
                    showList();
                }
            }
        });
    }

    @Override
    public void bindData(Context context, final ZhiChiMessageBase message) {
        SobotFaqDetailModel bean = message.getFaqDetailModel();
        PAGE_NUM = bean.getGuidePageCount();
        //图片
        //ShowType : 展示类型:1-问题列表,2-分组加问题列表,3-业务加分组加问题列表
        if(bean.getShowType()==1){
            if (!TextUtils.isEmpty(bean.getImgUrl())) {
                ViewGroup.LayoutParams params = sobot_hot_pic.getLayoutParams();
                params.width = (int) ScreenUtils.dpToPixel(mContext, 74);
                params.height = (int) ScreenUtils.dpToPixel(mContext, 233);
                sobot_hot_pic.setLayoutParams(params);
                sobot_hot_pic.setVisibility(View.VISIBLE);

                SobotBitmapUtil.display(mContext, CommonUtils.encode(bean.getImgUrl()), sobot_hot_pic);
            } else {
                sobot_hot_pic.setVisibility(View.GONE);
            }
            //只显示列表
            curPageNum = 0;
            faqDocRespVoList = bean.getFaqDocRespVos();
            setList(faqDocRespVoList);
        }else if(bean.getShowType()==2){
            List<GroupRespVo> groupRespVoList= bean.getGroupRespVos();

            if (!TextUtils.isEmpty(bean.getImgUrl())) {
                ViewGroup.LayoutParams params = sobot_hot_pic.getLayoutParams();
                params.width = (int) ScreenUtils.dpToPixel(mContext, 74);
                params.height = (int) ScreenUtils.dpToPixel(mContext, 272);
                sobot_hot_pic.setLayoutParams(params);
                sobot_hot_pic.setVisibility(View.VISIBLE);

                SobotBitmapUtil.display(mContext, CommonUtils.encode(bean.getImgUrl()), sobot_hot_pic);
            } else {
                sobot_hot_pic.setVisibility(View.GONE);
            }
            //显示分组和列表
            showTab(groupRespVoList);
        }else if(bean.getShowType()==3){
            //显示豆腐块、分组、列表
            List<BusinessLineRespVo> businessLineRespVoList = bean.getBusinessLineRespVos();
            showBlock(businessLineRespVoList);
        }

    }

    /**
     * 换一换，分页显示文图列表
     */
    private void showList(){

        lin_question_list.removeAllViews();
        if (faqDocRespVoList != null && faqDocRespVoList.size() > 0) {
            int startNum = 0;
            int endNum = faqDocRespVoList.size();
            if (endNum>PAGE_NUM) {//有分组且不是全部
                startNum = curPageNum * PAGE_NUM;
//                endNum = Math.min(startNum + PAGE_NUM, faqList.size());
                endNum = (curPageNum+1)*PAGE_NUM;
            }
            for (int i = startNum; i < endNum&& i<faqDocRespVoList.size(); i++) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_fad,null);
                TextView answer = view.findViewById(R.id.sobot_tv_name);
                final FaqDocRespVo info = faqDocRespVoList.get(i);
                answer.setText(info.getQuestionName());
                answer.setLines(1);
                answer.setEllipsize(TextUtils.TruncateAt.END);
                answer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //questionType 问题类型：0-单轮，1-多轮，2-内部知识库文章，3-内部知识库普通问题
                        msgCallBack.clickIssueItem(info,"");
                    }
                });
                lin_question_list.addView(view,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            if(endNum-startNum<PAGE_NUM && faqDocRespVoList.size() > PAGE_NUM){
                for (int i = endNum-startNum; i < PAGE_NUM; i++) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_fad,null);
                    TextView answer = view.findViewById(R.id.sobot_tv_name);
                    answer.setText("");
                    lin_question_list.addView(view,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            }
        }
    }

    /**
     * 创建问题列表
     * @param faqList
     * @return
     */
    private void setList(List<FaqDocRespVo>  faqList){
        faqDocRespVoList = faqList;
        lin_question_list.removeAllViews();
        if(faqList != null && faqList.size()>PAGE_NUM){
            //显示换一换
            sobot_tv_switch.setVisibility(View.VISIBLE);
        }else{
            //隐藏换一换
            sobot_tv_switch.setVisibility(View.GONE);
        }
        curPageNum = 0;
        if (faqList != null && faqList.size() > 0) {
            int startNum = 0;
            int endNum = faqList.size();
            if (endNum>PAGE_NUM) {//有分组且不是全部
                startNum = curPageNum * PAGE_NUM;
                endNum = (curPageNum+1)*PAGE_NUM;
            }
            for (int i = startNum; i < endNum&& i<faqList.size(); i++) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_fad,null);
                TextView answer = view.findViewById(R.id.sobot_tv_name);
                final FaqDocRespVo info = faqList.get(i);
                answer.setText(info.getQuestionName());
                answer.setLines(1);
                answer.setEllipsize(TextUtils.TruncateAt.END);
                answer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //questionType 问题类型：0-单轮，1-多轮，2-内部知识库文章，3-内部知识库普通问题
                        msgCallBack.clickIssueItem(info,"");
                    }
                });
                lin_question_list.addView(view);
            }
        }

    }
    private void showTab(final List<GroupRespVo> groupRespVoList){
        if (groupRespVoList != null&& groupRespVoList.size()>0) {
            groupIndex=0;
            tab_hot_title.setVisibility(View.VISIBLE);
            horizontalScrollView_ll.removeAllViews();
            for (int i = 0; i < groupRespVoList.size(); i++) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_tab, null);
                if (view != null) {
                    TextView titleTv = view.findViewById(R.id.sobot_tab_item_name);
                    titleTv.setText(groupRespVoList.get(i).getGroupName());
                    horizontalScrollView_ll.addView(view);
                    final int position = i;
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            groupIndex = position;
                            List<FaqDocRespVo> datas = groupRespVoList.get(position).getFaqDocRespVos();
                            if (datas != null) {
                                setList(datas);
                                updateIndicator(position);
                            }
                        }
                    });
                }
            }
            tab_hot_title.scrollTo(0,0);
            List<FaqDocRespVo> datas = groupRespVoList.get(groupIndex).getFaqDocRespVos();
            if (datas != null) {
                setList(datas);
                updateIndicator(groupIndex);
            }

        }else {
            tab_hot_title.setVisibility(View.GONE);
        }
    }

    private void updateIndicator(int index) {
        if (horizontalScrollView_ll.getChildCount() > 0) {
            for (int i = 0; i < horizontalScrollView_ll.getChildCount(); i++) {
                View view = horizontalScrollView_ll.getChildAt(i);
                TextView titleTv = view.findViewById(R.id.sobot_tab_item_name);
                View line = view.findViewById(R.id.sobot_tab_line);
                if (index == i) {
                    titleTv.setTextColor(ThemeUtils.getThemeColor(mContext));
                    line.setBackgroundColor(ThemeUtils.getThemeColor(mContext));
                    line.setVisibility(View.VISIBLE);
                } else {
                    titleTv.setTextColor(mContext.getResources().getColor(R.color.sobot_common_wenzi_black));
                    line.setVisibility(View.INVISIBLE);
                }
            }
        }

    }

    private void showBlock(final List<BusinessLineRespVo> businessLineRespVoList){
            if(businessLineRespVoList!=null&& businessLineRespVoList.size()>0){
                fastMenuAdapter = new IssueViewPagerdAdapter(mContext,businessLineRespVoList);
                fastMenu.setOnItemClickListener(new MyHorizontalScrollView.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        blockIndex = pos;
                        if( businessLineRespVoList.get(blockIndex).getHasGroup() != 2) {
                            //图片
                            if (!TextUtils.isEmpty(businessLineRespVoList.get(blockIndex).getImgUrl())) {
                                ViewGroup.LayoutParams params = sobot_hot_pic.getLayoutParams();
//                            params.width = (int) ScreenUtils.dpToPixel(mContext,80);
//                            params.width = (int) ScreenUtils.dpToPixel(mContext,70);
                                params.width = (int) ScreenUtils.dpToPixel(mContext, 74);
                                if (businessLineRespVoList.get(blockIndex).getHasGroup() == 0) {
                                    //有tab，设置高度，为(44+10)+(150+10)+28=242
//                                params.height = (int)ScreenUtils.dpToPixel(mContext,294);
                                    params.height = (int) ScreenUtils.dpToPixel(mContext, 272);
                                } else {
                                    //无tab,设置高度为(150+10)+28=188
//                                params.height = (int)ScreenUtils.dpToPixel(mContext,252);
                                    params.height = (int) ScreenUtils.dpToPixel(mContext, 233);
                                }
                                sobot_hot_pic.setLayoutParams(params);
                                sobot_hot_pic.setVisibility(View.VISIBLE);

                                SobotBitmapUtil.display(mContext, CommonUtils.encode(businessLineRespVoList.get(blockIndex).getImgUrl()), sobot_hot_pic);
                            } else {
                                sobot_hot_pic.setVisibility(View.GONE);
                            }
                        }
                        //是否有分组：0-有，1-无 2-链接
                        if(businessLineRespVoList.get(blockIndex).getHasGroup() == 0) {
                            tab_hot_title.setVisibility(View.VISIBLE);
                            curPageNum=0;
                            showTab(businessLineRespVoList.get(blockIndex).getGroupRespVos());
                        }else if(businessLineRespVoList.get(blockIndex).getHasGroup() == 1) {
                            tab_hot_title.setVisibility(View.GONE);
                            groupIndex=0;
                            curPageNum=0;
                            faqDocRespVoList = businessLineRespVoList.get(blockIndex).getFaqDocRespVos();
                            setList(faqDocRespVoList);
                        }else if(businessLineRespVoList.get(blockIndex).getHasGroup() == 2){
                            //打开网页
                            Intent intent = new Intent(mContext, WebViewActivity.class);
                            intent.putExtra("url", businessLineRespVoList.get(blockIndex).getBusinessLineUrl());
                            mContext.startActivity(intent);
                        }
                    }
                });
                fastMenu.initDatas(fastMenuAdapter);
                if(blockIndex==0){
                    //图片
                    if (!TextUtils.isEmpty(businessLineRespVoList.get(blockIndex).getImgUrl())) {
                        ViewGroup.LayoutParams params = sobot_hot_pic.getLayoutParams();
                        params.width = (int)ScreenUtils.dpToPixel(mContext,74);
                        if(businessLineRespVoList.get(blockIndex).getHasGroup() == 0){
                            //有tab，设置高度，为(44+10)+(150+10)+28=242
                            params.height = (int)ScreenUtils.dpToPixel(mContext,272);
                        }else{
                            //无tab,设置高度为(150+10)+28=188
                            params.height = (int)ScreenUtils.dpToPixel(mContext,233);
                        }
                        sobot_hot_pic.setLayoutParams(params);
                        sobot_hot_pic.setVisibility(View.VISIBLE);
                        SobotBitmapUtil.display(mContext, CommonUtils.encode(businessLineRespVoList.get(blockIndex).getImgUrl()), sobot_hot_pic);
                    }else{
                        sobot_hot_pic.setVisibility(View.GONE);
                    }
                }
                if(businessLineRespVoList.get(blockIndex).getHasGroup() == 0) {
                    showTab(businessLineRespVoList.get(blockIndex).getGroupRespVos());
                }else if(businessLineRespVoList.get(blockIndex).getHasGroup() == 1) {
                    if(tab_hot_title.getVisibility()==View.VISIBLE){
                        tab_hot_title.setVisibility(View.GONE);
                    }
                    groupIndex=0;
                    curPageNum=0;
                    faqDocRespVoList = businessLineRespVoList.get(blockIndex).getFaqDocRespVos();
                    setList(faqDocRespVoList);

                }else if(businessLineRespVoList.get(blockIndex).getHasGroup() == 2){
                    //打开网页
                    Intent intent = new Intent(mContext, WebViewActivity.class);
                    intent.putExtra("url", businessLineRespVoList.get(blockIndex).getBusinessLineUrl());
                    mContext.startActivity(intent);
                }
            }
    }
}