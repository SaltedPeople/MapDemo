package com.lmj.mapdemo.loaction;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.lmj.mapdemo.R;
import com.lmj.mapdemo.data.LocationEntity;
import com.lmj.mapdemo.data.LocationWrapper;
import com.lmj.mapdemo.util.FileUtil;
import com.lmj.mapdemo.util.LocationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author: lmj
 * date  : 2018/8/13.
 */

public class MapLocationActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener {

    public static String SELECT_ENTITY = "SELECT_ENTITY";
    public static final int Location_REQUEST_CODE = 11011;

    private MapView mMapView;

    private AMap aMap;
    private PoiSearch.Query mQuery;
    private UiSettings mUiSettings;
    private LocationAdapter mAdapter;
    private LocationEntity mSelectEntity;
    private boolean isPositionClick;  //防止点击地址item造成的地图移动，从而引发地址列表变动
    private AppCompatActivity mActivity;
    private Marker mCurrentMarker;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location);
        mMapView = findViewById(R.id.location_map_view);
        mMapView.onCreate(savedInstanceState);
        mActivity = this;
        mSelectEntity = getIntent().getParcelableExtra(SELECT_ENTITY);
        init();
        setViews();
        setListeners();
    }

    protected void init() {
        initToolBar();
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();
        }
        if (mSelectEntity == null) {
            location();
        } else {
            moveMapCamera(mSelectEntity.latitude, mSelectEntity.longitude);
        }
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                addMarkerInScreenCenter();
            }
        });
        aMap.animateCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel() - 3));
        aMap.setTrafficEnabled(false);// 显示实时交通状况
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(false); // 是否显示默认的定位按钮

        //设置自定义地图
        aMap.setCustomMapStylePath(FileUtil.CUSTOM_MAP_PATH);
        aMap.setMapCustomEnable(true);
    }

    //定位
    private void location() {
        LocationUtil.getInstance().startLocation(new LocationUtil.LocationCallBack() {
            @Override
            public void onLocationSuccess(LocationEntity locationBean) {
                if (!TextUtils.isEmpty(locationBean.city) && !TextUtils.isEmpty(locationBean.street)) {
                    isPositionClick = false;
                    mSelectEntity = locationBean;
                    //把地图移动到定位地点
                    addMarkerCurrentPos(mSelectEntity.latitude,mSelectEntity.longitude);
                    moveMapCamera(locationBean.latitude, locationBean.longitude);
                } else {
                    Toast.makeText(mActivity, "定位失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLocationError() {
                Toast.makeText(mActivity, "定位失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.location_toolbar);
        TextView tvTitle = findViewById(R.id.title);
        TextView rightTip = findViewById(R.id.right_tip);
        if (toolbar != null) {
            toolbar.setTitle("");
            tvTitle.setText(R.string.location);
            rightTip.setText(R.string.confirm);
            RelativeLayout right = findViewById(R.id.right_tip_layout);
            right.setVisibility(View.VISIBLE);
            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(SELECT_ENTITY, mSelectEntity);
                    mActivity.setResult(RESULT_OK, intent);
                    mActivity.finish();
                }
            });
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.mipmap.icon_common_arrowleft);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeInputMethod();
                    finish();
                }
            });
        }
    }

    protected void setViews() {
        RecyclerView locationRecycler = findViewById(R.id.location_list);
        mAdapter = new LocationAdapter(mActivity, false);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        locationRecycler.setLayoutManager(manager);
        locationRecycler.setAdapter(mAdapter);
    }

    protected void setListeners() {
        mAdapter.setListener(new LocationAdapter.OnSelectListener() {
            @Override
            public void onSelect(LocationEntity entity) {
                isPositionClick = true;
                mSelectEntity = entity;
                moveMapCamera(entity.latitude, entity.longitude);
            }
        });
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (!isPositionClick && mSelectEntity != null) {
                    mSelectEntity.latitude = cameraPosition.target.latitude;
                    mSelectEntity.longitude = cameraPosition.target.longitude;
                    latSearchList();
                }
                isPositionClick = false;
            }
        });

        findViewById(R.id.location_current_position).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location();
            }
        });

        findViewById(R.id.location_search_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SearchLocationActivity.class);
                intent.putExtra(SearchLocationActivity.CITY_CODE, mSelectEntity.cityCode);
                startActivityForResult(intent, SearchLocationActivity.SEARCH_REQUEST_CODE);
            }
        });
    }

    private void latSearchList() {
        mQuery = new PoiSearch.Query("", "");
        LatLonPoint point = new LatLonPoint(mSelectEntity.latitude, mSelectEntity.longitude);
        mQuery.setLocation(point);
        mQuery.setDistanceSort(true);
        mQuery.setPageSize(15);
        mQuery.setPageNum(1);
        PoiSearch poiSearch = new PoiSearch(this, mQuery);
        poiSearch.setBound(new PoiSearch.SearchBound(point, 500));//设置周边搜索的中心点以及半径
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
                        entity.address = item.getSnippet();
                        entity.latitude = item.getLatLonPoint().getLatitude();
                        entity.longitude = item.getLatLonPoint().getLongitude();
                        entity.province = item.getProvinceName();
                        entities.add(entity);
                    }
                    if (entities.size() > 0) {
                        entities.get(0).selected = true;
                        mSelectEntity = entities.get(0);
                    }
                    LocationWrapper wrapper = new LocationWrapper();
                    wrapper.setCityCode(mSelectEntity.cityCode);
                    wrapper.setLatitude(mQuery.getLocation().getLatitude());
                    wrapper.setLongitude(mQuery.getLocation().getLongitude());
                    wrapper.currentPage = 2;
                    wrapper.getDataList().addAll(entities);
                    if (mAdapter != null) {
                        mAdapter.setDataList(wrapper);
                        mAdapter.setPreEntity();
                    }
                }
            }
        }
    }

    //把地图画面移动到定位地点
    private void moveMapCamera(double latitude, double longitude) {
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14));
    }

    /**
     * 在屏幕中心添加一个Marker
     */
    private void addMarkerInScreenCenter() {
        LatLng latLng = aMap.getCameraPosition().target;
        Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
        Marker screenMarker = aMap.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_icon_current)));
        //设置Marker在屏幕上,不跟随地图移动
        screenMarker.setPositionByPixels(screenPosition.x, screenPosition.y);
    }

    /**
     * 在当前位置添加一个Marker
     */
    private void addMarkerCurrentPos(double latitude,double longitude) {
        if (mCurrentMarker == null){
            mCurrentMarker = aMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.mipmap.icon_geo))));
        }else {
            mCurrentMarker.setPosition(new LatLng(latitude, longitude));
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SearchLocationActivity.SEARCH_REQUEST_CODE && data != null) {
            isPositionClick = true;
            mSelectEntity = data.getParcelableExtra(SearchLocationActivity.SELECT_ENTITY);
            moveMapCamera(mSelectEntity.latitude, mSelectEntity.longitude);
            latSearchList();
        }
    }

    private void closeInputMethod() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        }
    }
}
