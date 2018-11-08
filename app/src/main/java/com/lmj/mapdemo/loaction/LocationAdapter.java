package com.lmj.mapdemo.loaction;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.lmj.mapdemo.R;
import com.lmj.mapdemo.data.LocationEntity;
import com.lmj.mapdemo.data.LocationWrapper;

import java.util.ArrayList;
import java.util.List;


/**
 * author: lmj
 * date  : 2018/8/13.
 */

public class LocationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PoiSearch.OnPoiSearchListener {
    //上拉加载更多
    private static final int STATUS_LOADING_FINISH = 0;
    //正在加载中
    private static final int STATUS_LOADING = 1;
    private static final int STATUS_LOADING_FAIL = 2;
    //上拉加载更多状态-默认为0
    private static final int TYPE_ITEM = 0;  //普通Item View
    private static final int TYPE_FOOTER = 1;  //底部FootView

    private  FooterViewHolder mFooterViewHolder;
    private OnSelectListener mListener;
    private LocationEntity mPreEntity;//上次点击的地址
    private PoiSearch.Query mQuery;
    private boolean isSearch;
    private Activity mActivity;
    private LocationWrapper mWrapper;

    LocationAdapter(Activity activity, boolean isSearch) {
        mActivity = activity;
        this.isSearch = isSearch;
        mWrapper = new LocationWrapper();
    }

    void setPreEntity() {
        if (mWrapper.getDataList().size() > 0) {
            mPreEntity = mWrapper.getDataList().get(0);
        }
    }

    void setDataList(LocationWrapper wrapper){
        mWrapper = wrapper;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.viewholder_map_location, parent, false);
            return new ItemViewHolder(view);
        }
        View view = LayoutInflater.from(mActivity).inflate(R.layout.view_loadmore, parent, false);
        mFooterViewHolder = new FooterViewHolder(view);
        return mFooterViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder){
                ItemViewHolder viewHolder = (ItemViewHolder) holder;
                viewHolder.bind(position);
            }else {
                mFooterViewHolder.bind();
            }
    }

    @Override
    public int getItemCount() {
        return mWrapper.getDataList().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount()-1) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private void onLoadMoreData() {
        if (isSearch) {
            searchList(mWrapper.currentPage);
        } else {
            latSearchList(mWrapper.currentPage);
        }
    }

    private void latSearchList(int pageNum) {
        mQuery = new PoiSearch.Query("", "", mWrapper.getCityCode());
        LatLonPoint point = new LatLonPoint(mWrapper.getLatitude(), mWrapper.getLongitude());
        mQuery.setLocation(point);
        mQuery.setPageSize(15);
        mQuery.setPageNum(pageNum);
        PoiSearch poiSearch = new PoiSearch(mActivity, mQuery);
        poiSearch.setBound(new PoiSearch.SearchBound(point, 500));//设置周边搜索的中心点以及半径
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }


    //poi搜索
    private void searchList(int pageNum) {
        mQuery = new PoiSearch.Query(mWrapper.getKeyWord(), "", mWrapper.getCityCode());
        mQuery.setPageSize(15);
        mQuery.setPageNum(pageNum);
        PoiSearch poiSearch = new PoiSearch(mActivity, mQuery);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        mWrapper.atLastPage = true;
        if (rCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(mQuery)) {// 是否是同一条
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<LocationEntity> entities = new ArrayList<>();
                    for (PoiItem item : poiItems) {
                        mWrapper.atLastPage = false;
                        LocationEntity entity = new LocationEntity();
                        entity.title = item.getTitle();
                        entity.city = item.getCityName();
                        entity.cityCode = item.getCityCode();
                        entity.street = item.getAdName();
                        entity.address = item.getSnippet();
                        entity.latitude = item.getLatLonPoint().getLatitude();
                        entity.longitude = item.getLatLonPoint().getLongitude();
                        entity.province = item.getProvinceName();
                        entities.add(entity);
                    }
                    mWrapper.currentPage++;
                    mWrapper.getDataList().addAll(entities);
                    notifyDataSetChanged();
                    if (mQuery.getPageSize()<mWrapper.currentPage){
                        mWrapper.atLastPage = true;
                    }
                }
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }


    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        View bottomLine;
        ImageView select;

        ItemViewHolder(View view) {
            super(view);
            title = itemView.findViewById(R.id.item_title);
            content = itemView.findViewById(R.id.item_content);
            bottomLine = itemView.findViewById(R.id.item_bottom_line);
            select = itemView.findViewById(R.id.item_select);
        }

        void bind(int position) {
            if (mWrapper.getDataList().size() == 0) {
                return;
            }
            final LocationEntity entity = mWrapper.getDataList().get(position);
            if (position != mWrapper.getDataList().size() - 1) {
                bottomLine.setVisibility(View.VISIBLE);
            } else {
                bottomLine.setVisibility(View.GONE);
            }
            if (entity.selected) {
                select.setVisibility(View.VISIBLE);
            } else {
                select.setVisibility(View.INVISIBLE);
            }
            title.setText(entity.title);
            String contentStr = entity.province + entity.city + entity.street + entity.address;
            content.setText(contentStr);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPreEntity != null) {
                        mPreEntity.selected = !mPreEntity.selected;
                    }
                    entity.selected = !entity.selected;
                    mPreEntity = entity;
                    notifyDataSetChanged();
                    if (mListener != null) {
                        mListener.onSelect(entity);
                    }
                }
            });
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mLoadingLayout;
        LinearLayout mFailLayout;

        FooterViewHolder(View itemView) {
            super(itemView);
            mLoadingLayout = itemView.findViewById(R.id.item_loadmore_container_loading);
            mFailLayout = itemView.findViewById(R.id.item_loadmore_container_retry);
        }

        void bind() {
            if (!mWrapper.atLastPage) {
                changeStatus(STATUS_LOADING);
            } else {
                changeStatus(STATUS_LOADING_FINISH);
            }
        }

        void changeStatus(int status) {
            switch (status) {
                case STATUS_LOADING:
                    mLoadingLayout.setVisibility(View.VISIBLE);
                    mFailLayout.setVisibility(View.GONE);
                    onLoadMoreData();
                    break;
                case STATUS_LOADING_FAIL:
                    mLoadingLayout.setVisibility(View.GONE);
                    mFailLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    mLoadingLayout.setVisibility(View.GONE);
                    mFailLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    interface OnSelectListener {
        void onSelect(LocationEntity entity);
    }

    void setListener(OnSelectListener listener) {
        mListener = listener;
    }
}
