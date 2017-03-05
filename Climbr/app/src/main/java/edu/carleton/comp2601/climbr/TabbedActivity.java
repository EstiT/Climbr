package edu.carleton.comp2601.climbr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.widget.ImageView;

import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Layer;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;


public class TabbedActivity extends AppCompatActivity implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    static TabbedActivity instance;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private final int NEARBY_GYMS = 0;
    private final int FIND_BELAYER = 1;
    private final int CONNECT = 2;
    private final int MY_TRAINER = 3;
    private final int PROFILE = 4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        instance = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(NEARBY_GYMS).setIcon(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_dialog_map, null));
        tabLayout.getTabAt(FIND_BELAYER).setIcon(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_group_white_48dp , null));
        tabLayout.getTabAt(CONNECT).setIcon(ResourcesCompat.getDrawable(getResources(),android.R.drawable.stat_notify_chat, null));
        tabLayout.getTabAt(MY_TRAINER).setIcon(ResourcesCompat.getDrawable(getResources(),R.mipmap.ic_alarm_on_white_48dp, null));
        tabLayout.getTabAt(PROFILE).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.climber, null));
//


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    public static TabbedActivity getInstance(){
        return instance;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class NearbyGymsFragment extends Fragment{
        MapView mMapView;
        private GoogleMap googleMap;

        public NearbyGymsFragment() {
            super();
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static NearbyGymsFragment newInstance() {
            NearbyGymsFragment fragment = new NearbyGymsFragment();
            //Bundle args = new Bundle();
            //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            //fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.location_fragment, container, false);
            mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);

            mMapView.onResume(); // needed to get the map to display immediately


            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;

                    if (!(ContextCompat.checkSelfPermission(TabbedActivity.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)) {
                        Log.i("MapsActivity", "Permission denied");
                        ActivityCompat.requestPermissions(TabbedActivity.getInstance(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                    }

                    if (ContextCompat.checkSelfPermission(TabbedActivity.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED){
                        Log.i("MapsActivity", "Permission is granted");
                        googleMap.setMyLocationEnabled(true);
                    }

                    try {

                        KmlLayer layer = new KmlLayer(googleMap, R.raw.climbing_gyms, TabbedActivity.getInstance().getApplicationContext());
                        layer.addLayerToMap();
                        /*
                        Log.i("2601", "layer: " + layer.getPlacemarks().toString());
                        Log.i("2601", "layer: " + layer.getContainers().toString());
                        for (KmlContainer containers : layer.getContainers()){
                        // Do something to container
                            Log.i("2601", "containers" +containers.getContainers());
                            for(KmlContainer container2: containers.getContainers()){
                                Log.i("2601", " placemarks" +container2.getPlacemarks());
                                for(KmlPlacemark p: container2.getPlacemarks()){
                                    Log.i("2601", " keys" +p.getPropertyKeys().toString());

                                    double lat = Double.parseDouble(((String)p.getProperty("Point")).substring(0,((String)p.getProperty("coordinates")).indexOf(",")));
                                    double lng = Double.parseDouble(((String)p.getProperty("coordinates")).substring(((String)p.getProperty("coordinates")).indexOf(",")));

                                    Log.i("coordinates: ", lat + " "+lng);
                                    googleMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng))
                                            .title(p.getProperty("name"))
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                    Log.i("2601", " name: " +p.getProperty("name"));

                                }
                            }

                        }*/
                        layer.setOnFeatureClickListener(new Layer.OnFeatureClickListener() {
                            @Override
                            public void onFeatureClick(Feature feature) {
                                Log.i("2601", "Feature" + feature.getProperties().toString());
                                Log.i("2601", "Feature name" + feature.getProperty("name"));
                            }
                        });

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    googleMap.setOnMarkerClickListener(TabbedActivity.getInstance());
                    googleMap.setOnInfoWindowClickListener(TabbedActivity.getInstance());
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    CameraUpdate center = CameraUpdateFactory.newLatLng(getLocation());
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(10.0f);
                    googleMap.moveCamera(center);
                    googleMap.animateCamera(zoom);

                    googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        @Override
                        public boolean onMyLocationButtonClick() {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLng(getLocation()));
                            return true;
                        }
                    });



                }


                private LatLng getLocation(){
                    LocationManager locationManager = (LocationManager) TabbedActivity.getInstance().getSystemService(Context.LOCATION_SERVICE);

                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, false);
                    Location location = null;
                    try {
                        location = locationManager.getLastKnownLocation(provider);
                    } catch (SecurityException e) {
                        Log.i("MapsActivity","Unable to get current location");
                        e.printStackTrace();
                    }

                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    return newLatLng;
                }
            });

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            mMapView.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            mMapView.onPause();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mMapView.onDestroy();
        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            mMapView.onLowMemory();
        }
    }



    public static class FindBelayerFragment extends Fragment {

        CustomPagerAdapter mCustomPagerAdapter;
        ViewPager mViewPager;
        static int[]  mResources = {
                R.drawable.profile,
                R.drawable.profile,
                R.drawable.profile,
                R.drawable.profile,
                R.drawable.profile,
                R.drawable.profile,
        };
        public FindBelayerFragment() {  }


        public static FindBelayerFragment newInstance() {
            FindBelayerFragment fragment = new FindBelayerFragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.find_belayer_fragment, container, false);


            TextView name = (TextView) rootView.findViewById(R.id.name);
            name.setTextSize(30.0f);

            mCustomPagerAdapter = new CustomPagerAdapter(TabbedActivity.getInstance());

            mViewPager = (ViewPager)rootView.findViewById(R.id.pager);
            mViewPager.setAdapter(mCustomPagerAdapter);



            return rootView;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){
                case NEARBY_GYMS:
                    return NearbyGymsFragment.newInstance();
                case FIND_BELAYER:
                    return FindBelayerFragment.newInstance();
//                case CONNECT:
//                    return ConnectFragment.newInstance();
//                case MY_TRAINER:
//                    return MyTrainerFragment.newInstance();
//                case PROFILE:
//                    return ProfileFragment.newInstance();
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 5;
        }


//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "Nearby Gyms";
//                case 1:
//                    return "Find Belayers";
//                case 2:
//                    return "Connect";
//                case 3:
//                    return "Personal Trainer";
//                case 4:
//                    return "Profile";
//            }
//            return null;
//        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("2601", marker.getTitle());
        Log.i("2601", marker.getId());

        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
    }


}
