package com.rhg.qf.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.rhg.qf.R;
import com.rhg.qf.adapter.MainAdapter;
import com.rhg.qf.adapter.viewHolder.HotFoodViewHolder;
import com.rhg.qf.bean.CommonListModel;
import com.rhg.qf.bean.HotFoodUrlBean;
import com.rhg.qf.bean.IBaseModel;
import com.rhg.qf.bean.InflateModel;
import com.rhg.qf.constants.AppConstants;
import com.rhg.qf.impl.OnItemClickListener;
import com.rhg.qf.mvp.presenter.HotFoodPresenter;
import com.rhg.qf.mvp.presenter.HotFoodSearchPresenter;
import com.rhg.qf.ui.activity.GoodsDetailActivity;
import com.rhg.qf.utils.SizeUtil;
import com.rhg.qf.widget.RecycleViewDivider;

import butterknife.Bind;

/**
 * desc:热销单品——综合
 * author：remember
 * time：2016/6/19 16:55
 * email：1013773046@qq.com
 */
public abstract class AbstractHotFoodFragment extends BaseFragment implements OnItemClickListener<CommonListModel<HotFoodUrlBean.HotFoodBean>> {
    @Bind(R.id.common_recycle)
    RecyclerView commonRecycle;
    @Bind(R.id.common_refresh)
    ProgressBar commonRefresh;
    @Bind(R.id.common_swipe)
    SwipeRefreshLayout commonSwipe;

    CommonListModel<HotFoodUrlBean.HotFoodBean> hotFoodBeanList = new CommonListModel<>();
    MainAdapter<CommonListModel<HotFoodUrlBean.HotFoodBean>> hotFoodAdapter;
    HotFoodPresenter hotFoodPresenter;
    HotFoodSearchPresenter hotFoodSearchPresenter;
    int hotFoodType;
    String foodName;

    public AbstractHotFoodFragment() {
        hotFoodPresenter = new HotFoodPresenter(this);
        hotFoodType = getHotFoodType();
    }

    protected abstract int getHotFoodType();

    @Override
    public void receiveData(Bundle arguments) {
        foodName = arguments.getString(AppConstants.KEY_PRODUCT_NAME, "");
    }

    @Override
    public int getLayoutResId() {
        return R.layout.common_swipe_recycle_layout;
    }

    @Override
    public void loadData() {
        commonRefresh.setVisibility(View.VISIBLE);
        if (hotFoodSearchPresenter == null)
            hotFoodSearchPresenter = new HotFoodSearchPresenter(this);
        hotFoodSearchPresenter.getSearchHotFood(AppConstants.SEARCH_HOTFOOD, foodName, hotFoodType + 1);

//        hotFoodPresenter.getHotFoods(AppConstants.SEARCH_HOTFOOD, hotFoodType, foodName);
    }

    @Override
    protected void initData() {
        commonRecycle.setHasFixedSize(true);
        commonRecycle.setLayoutManager(new LinearLayoutManager(getContext()));
        commonRecycle.addItemDecoration(new RecycleViewDivider(getContext(),
                LinearLayoutManager.HORIZONTAL, SizeUtil.dip2px(2),
                ContextCompat.getColor(getContext(), R.color.white_light)));
        hotFoodAdapter = new MainAdapter<>(
                new InflateModel(new Class[]{HotFoodViewHolder.class, View.class}, new Object[]{R.layout.item_hot_sell}),
                hotFoodBeanList,
                this);
        commonRecycle.setAdapter(hotFoodAdapter);
        commonSwipe.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getContext(), R.color.colorBlueNormal));
        commonSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                hotFoodSearchPresenter.getSearchHotFood(AppConstants.SEARCH_HOTFOOD, foodName, hotFoodType + 1);
            }
        });

    }


    @Override
    protected void initView(View view) {
    }

    @Override
    protected void showFailed() {
    }

    @Override
    public void showSuccess(Object o) {
        if (o instanceof IBaseModel) {
            hotFoodBeanList.setRecommendShopBeanEntity(((IBaseModel) o).getEntity());
            hotFoodAdapter.notifyDataSetChanged();
        }
        if (commonRefresh.getVisibility() == View.VISIBLE)
            commonRefresh.setVisibility(View.GONE);
        if (commonSwipe.isRefreshing())
            commonSwipe.setRefreshing(false);
    }

    @Override
    public void onItemClick(View view, int position, CommonListModel<HotFoodUrlBean.HotFoodBean> model) {
        Intent intent = new Intent(getContext(), GoodsDetailActivity.class);
        HotFoodUrlBean.HotFoodBean item = model.getEntity().get(position);
        intent.putExtra(AppConstants.KEY_PRODUCT_ID, item.getID());
        intent.putExtra(AppConstants.KEY_MERCHANT_ID, item.getRId());
        intent.putExtra(AppConstants.KEY_MERCHANT_NAME, item.getRName());
        //noinspection unchecked
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
    }

    @Override
    public void onItemLongClick(View view, int position, CommonListModel<HotFoodUrlBean.HotFoodBean> item) {

    }
}
