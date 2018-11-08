package com.lmj.mapdemo.loaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
 * date  : 2018/8/14.
 */

public class SearchLocationActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener{
    public static String CITY_CODE= "CITY_CODE";
    public static String SELECT_ENTITY= "SELECT_ENTITY";
    public static int SEARCH_REQUEST_CODE= 11010;
    //    搜索框
    EditText mSearchEdit;
    //    搜索框删除按钮
    ImageView mSearchDel;

    RecyclerView mRecyclerView;

    TextView mCancelBtn;

    private PoiSearch.Query mQuery;
    private String mCityCode ;
    private LocationAdapter mAdapter;
    private AppCompatActivity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_search);
        mActivity = this;
        mCityCode = getIntent().getStringExtra(CITY_CODE);
        init();
        setViews();
        setListeners();
    }

    protected void init() {
        mSearchEdit = findViewById(R.id.search_edit);
        mSearchDel = findViewById(R.id.search_del);
        mRecyclerView = findViewById(R.id.search_list);
        mCancelBtn = findViewById(R.id.search_confirm);

    }

    protected void setViews() {
        mAdapter = new LocationAdapter(mActivity,true);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(manager);
        mCancelBtn.setText(R.string.cancel);
    }

    protected void setListeners() {
        mAdapter.setListener(new LocationAdapter.OnSelectListener() {
            @Override
            public void onSelect(LocationEntity entity) {
                Intent intent = new Intent();
                intent.putExtra(SELECT_ENTITY,entity);
                mActivity.setResult(Activity.RESULT_OK,intent);
                mActivity.finish();
            }
        });
        mSearchEdit.setHint(R.string.location_search);
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable)) {
                    mSearchDel.setVisibility(View.VISIBLE);
                    searchList(mCityCode,editable.toString().trim());
                } else {
                    mSearchDel.setVisibility(View.GONE);
                }
            }
        });

        mSearchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    View view = mActivity.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        if (inputMethodManager!=null){
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                    }
                }
                return false;
            }
        });
        findViewById(R.id.search_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEdit.setText("");
            }
        });

        findViewById(R.id.search_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //poi搜索
    private void searchList(String cityCode, String keyWord) {
        if (TextUtils.isEmpty(keyWord)){
            if (mAdapter != null) {
                mAdapter.setDataList(new LocationWrapper());
                mRecyclerView.setAdapter(mAdapter);
            }
            return;
        }
        mQuery = new PoiSearch.Query(keyWord, "", cityCode);
        mQuery.setPageSize(30);
        mQuery.setPageNum(1);
        PoiSearch poiSearch = new PoiSearch(this, mQuery);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        if (rCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(mQuery)) {// 是否是同一条
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<LocationEntity> entities = new ArrayList<>();
                    for (PoiItem item : poiItems) {
                        LocationEntity entity = new LocationEntity();
                        entity.title = item.getTitle();
                        entity.city = item.getCityName();
                        entity.cityCode = item.getCityCode();
                        entity.street = item.getAdName();
                        entity.address =  item.getSnippet();
                        entity.latitude = item.getLatLonPoint().getLatitude();
                        entity.longitude = item.getLatLonPoint().getLongitude();
                        entity.province = item.getProvinceName();
                        entities.add(entity);
                    }

                    LocationWrapper wrapper = new LocationWrapper();
                    wrapper.setCityCode(mCityCode);
                    wrapper.setKeyWord(mSearchEdit.getText().toString().trim());
                  wrapper.currentPage = 2;
                    wrapper.getDataList().addAll(entities);
                    if (mAdapter != null) {
                        mAdapter.setDataList(wrapper);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
