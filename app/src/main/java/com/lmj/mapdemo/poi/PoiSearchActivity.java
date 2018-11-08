package com.lmj.mapdemo.poi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.lmj.mapdemo.R;
import com.lmj.mapdemo.util.FileUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoiSearchActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener {

    private AMap aMap;
    private PoiSearch.Query mQuery;
    private MapView mMapView;
    public RecyclerView recyclerView;

    private EditText mPoiKey;
    private EditText mPoiCity;
    public BottomSheetBehavior behavior;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poisearch);
        mPoiCity = findViewById(R.id.poi_city);
        mPoiKey = findViewById(R.id.poi_key);
        mMapView = findViewById(R.id.poi_map);
        recyclerView = findViewById(R.id.poi_recycler);
        mMapView.onCreate(savedInstanceState);
        initMap();
        initList();
    }

    private void initMap(){
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.setMyLocationEnabled(true);
        aMap.animateCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel() - 3));
        aMap.setTrafficEnabled(false);// 显示实时交通状况
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.radiusFillColor(0x70f3ff);
        myLocationStyle.strokeColor(0xe3f9fd);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //设置自定义地图
        aMap.setCustomMapStylePath(FileUtil.CUSTOM_MAP_PATH);
        aMap.setMapCustomEnable(true);
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return false;
            }
        });
    }

    /**
     * 初始化底部弹出框
     */
    private void initList(){
        List<String> list = Arrays.asList(getResources().getStringArray(R.array.items));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MyTextAdapter simpleStringRecyclerViewAdapter = new MyTextAdapter(list);

        simpleStringRecyclerViewAdapter.setItemClickListener(new MyTextAdapter.MyItemClickerListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }else {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        recyclerView.setAdapter(simpleStringRecyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        behavior = BottomSheetBehavior.from(recyclerView);
        behavior.setHideable(true);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void searchButtonProcess(View view) {
        getLatByCity(mPoiCity.getText().toString().trim());
    }

    private void latSearchList(double latitude, double longitude) {
        mQuery = new PoiSearch.Query("", mPoiKey.getText().toString().trim());
        LatLonPoint point = new LatLonPoint(latitude, longitude);
        mQuery.setLocation(point);
        mQuery.setDistanceSort(true);
        mQuery.setPageSize(15);
        mQuery.setPageNum(1);  //自 5.2.1后修改成从1开始，填0和填1返回的结果一样

        PoiSearch poiSearch = new PoiSearch(this, mQuery);
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude,
               longitude), 1000));//设置周边搜索的中心点以及半径
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    private void getLatByCity(String cityName) {
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                //根据给定的经纬度和最大结果数返回逆地理编码的结果列表。
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                //根据给定的地理名称和查询城市，返回地理编码的结果列表。
                if (geocodeResult != null && geocodeResult.getGeocodeAddressList() != null && geocodeResult.getGeocodeAddressList().size() > 0) {
                    GeocodeAddress geocodeAddress = geocodeResult.getGeocodeAddressList().get(0);
                    double latitude = geocodeAddress.getLatLonPoint().getLatitude();
                    double longitude = geocodeAddress.getLatLonPoint().getLongitude();
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(latitude, longitude)));
                    latSearchList(latitude, longitude);
                }
            }
        });
        GeocodeQuery geocodeQuery = new GeocodeQuery(cityName, cityName);
        geocodeSearch.getFromLocationNameAsyn(geocodeQuery);
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        if (rCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(mQuery)) {// 是否是同一条
                    List<PoiItem> poiItems = poiResult.getPois();
                    ArrayList<MarkerOptions> markers = new ArrayList<>();
                    for (PoiItem item : poiItems) {
//                        View markView = View.inflate(this, R.layout.marker_new_normal_car, null);
//                        TextView markName = markView.findViewById(R.id.marker_name);
//                        markName.setText(item.getSnippet());
                        LatLng latLng = new LatLng(item.getLatLonPoint().getLatitude(),item.getLatLonPoint().getLongitude());
                        markers.add(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromAsset("icon_mark1.png")).draggable(false));
                    }
                    aMap.addMarkers(markers,false);
                }
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
