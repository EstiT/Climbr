package edu.carleton.comp2601.climbr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Layer;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


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
    public final int NEARBY_GYMS = 0;
    public final int FIND_BELAYER = 1;
    public final int CONNECT = 2;
    public final int MY_TRAINER = 3;
    public final int PROFILE = 4;

    public static JSONObject data;
    static String myUsername;
    static String recipient= "";

    public static HashMap<String,Boolean> gyms = new HashMap<String, Boolean>();

    TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        instance = this;
        final Intent intent = getIntent();
        myUsername = intent.getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        //clear scroll flags
        params.setScrollFlags(0);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        //set the icons for each tab
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(NEARBY_GYMS).setIcon(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_dialog_map, null));
        tabLayout.getTabAt(FIND_BELAYER).setIcon(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_group_white_48dp , null));
        tabLayout.getTabAt(CONNECT).setIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_chat_white, null));
        tabLayout.getTabAt(MY_TRAINER).setIcon(ResourcesCompat.getDrawable(getResources(),R.mipmap.ic_alarm_on_white_48dp, null));
        tabLayout.getTabAt(PROFILE).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.climber, null));

        //if the tabbed activity is loaded from the user onboard, bring them to the profile tab
        if(intent.hasExtra("from")){
            if(intent.getStringExtra("from").equals("UserOnboard")){
                tabLayout.getTabAt(TabbedActivity.getInstance().PROFILE).select();
            }
        }

    }

    @Override
    public void onDestroy(){
        //super.onDestroy();
        //send disconnect request
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Serializable> map = new HashMap<String, Serializable>();
                map.put("username", myUsername);
                LoginActivity.getInstance().c.sendRequest("DISCONNECT_REQUEST", map);
            }
        });
        t.start();
        super.onDestroy();
    }

    public Bitmap getBitmapFromString(String jsonString) {
/*
* This Function converts the String back to Bitmap
* */
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
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
            }
            catch (Exception e) {
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
                        //KmlLayer layer = new KmlLayer(googleMap, R.raw.climbing_gyms, TabbedActivity.getInstance().getApplicationContext());
                        //layer.addLayerToMap();

                        ArrayList<Marker> markers = new ArrayList<Marker>();

                        KmlLayer layer = new KmlLayer(googleMap, R.raw.climbing_gyms, TabbedActivity.getInstance().getApplicationContext());
                        layer.addLayerToMap();

                        /*
                        for (KmlPlacemark p : layer.getPlacemarks()) {
                            Log.i("2601", "Placemark:  "+ p.toString());
                        }
                        for (KmlContainer c : layer.getContainers()) {
                            for (KmlContainer c1 : c.getContainers()) {
                                for (KmlPlacemark p : c1.getPlacemarks()) {
                                    Log.i(this.getClass().getName(), p.toString());
                                    String name = p.getProperty("name");
                                    LatLng pt = (LatLng) p.getGeometry().getGeometryObject();
                                    markers.add(mMap.addMarker(new MarkerOptions().position(pt)
                                            .title(name).snippet("Information for: " + name)));
                                }
                            }
                        }*/

                        //add climber icon to display local climbing gyms
                        BitmapDescriptor b = BitmapDescriptorFactory.fromResource(R.drawable.climbericon);
                        for (KmlContainer c : layer.getContainers()) {
                            for (KmlContainer c1 : c.getContainers()) {
                                for (KmlPlacemark p : c1.getPlacemarks()) {
                                    //Log.i(this.getClass().getName(), p.toString());
                                    String name = p.getProperty("name");
                                    LatLng pt = (LatLng) p.getGeometry().getGeometryObject();
                                    if(!gyms.containsKey(name)){
                                        gyms.put(name,false);
                                        markers.add(mMap.addMarker(new MarkerOptions().position(pt)
                                                .title(name)
                                                .snippet("Tap to add to favourites")
                                                .icon(b)));
                                    }
                                }
                            }
                        }

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
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    googleMap.setOnMarkerClickListener(TabbedActivity.getInstance());
                    googleMap.setOnInfoWindowClickListener(TabbedActivity.getInstance());
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
                    }
                    catch (SecurityException e) {
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
        static ArrayList<File> mResources = new ArrayList<File>();
        static ArrayList<String> bioResources = new ArrayList<String>();
        static ArrayList<String> nameResources= new ArrayList<String>();;

        public FindBelayerFragment() {
        }


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

            //set custom pager adapter as mViewPagers adapter
            mCustomPagerAdapter = new CustomPagerAdapter(TabbedActivity.getInstance());

            mViewPager = (ViewPager)rootView.findViewById(R.id.pager);
            mViewPager.setAdapter(mCustomPagerAdapter);

            //send Profiles request to populate resources for view pager
            HashMap<String, Serializable> map = new HashMap<String, Serializable>();
            LoginActivity.getInstance().c.sendRequest("PROFILES", map);

            return rootView;
        }
    }

    public static class ConnectFragment extends Fragment {

        TextView title, messages;
        EditText msgText;
        static ConnectFragment instance;


        public ConnectFragment() {
        }

        public static ConnectFragment newInstance() {
            ConnectFragment fragment = new ConnectFragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            instance = this;
        }

        public static ConnectFragment getInstance(){
            return instance;
        }

        public void addMsg(final String m){
            //append new message to the users messages
            TabbedActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messages.setText(messages.getText() + "\n" + m);
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.connect_fragment, container, false);

            title = (TextView) rootView.findViewById(R.id.title);
            messages = (TextView) rootView.findViewById(R.id.messages);
            msgText = (EditText) rootView.findViewById(R.id.msgText);

            /*
            //potentially make message send when enter is pressed
            msgText.setOnKeyListener(new View.OnKeyListener(){
                public boolean onKey(View v, int keyCode, KeyEvent event){
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        Log.i("2601","key down");
                        switch (keyCode){
                            case KeyEvent.KEYCODE_DPAD_CENTER:
                            case KeyEvent.KEYCODE_ENTER:
                                Log.i("2601","enter");
                                TabbedActivity.getInstance().sendClicked(v);
                                return true;
                            default:
                                break;
                        }
                    }
                    return false;
                }
            });
            */

            return rootView;
        }

        public void changeTitle(String s){
            title.setText(s);
        }

        public String getMessageText(){
            return msgText.getText().toString();
        }


    }
    public void sendClicked(View v){
        Log.i("2601","Send clicked");
        //send the text typed to the recipient
        sendMessage(ConnectFragment.getInstance().getMessageText());
    }
    public void msgTextClicked(View v){
        //set the chat tab icon back to not having a notification
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tabLayout.getTabAt(CONNECT).setIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_chat_white, null));
            }
        });

    }
    public void sendMessage(final String msg){
        //send message request
        HashMap<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("username", myUsername);
        map.put("recipient", recipient);
        map.put("message",msg);
        LoginActivity.getInstance().c.sendRequest("MESSAGE", map);
        //clear the text field
        ConnectFragment.instance.msgText.setText("");
    }

    public static class ProfileFragment extends Fragment {


        public ProfileFragment() {
        }

        public static ProfileFragment newInstance() {
            ProfileFragment fragment = new ProfileFragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

            Button edit = (Button) rootView.findViewById(R.id.profileEditButton);
            TextView bio = (TextView)rootView.findViewById(R.id.profileBio);
            TextView pullups = (TextView)rootView.findViewById(R.id.profilePullups);
            TextView grade = (TextView)rootView.findViewById(R.id.profileGrade);
            TextView age = (TextView)rootView.findViewById(R.id.profileAge);
            ImageView dp = (ImageView)rootView.findViewById(R.id.profileImage);

            if(LoginActivity.getInstance().c.hasSetInfo){
                //preset all of the text fields
                bio.setText(LoginActivity.getInstance().c.bio);
                pullups.setText(LoginActivity.getInstance().c.maxPullups);
                grade.setText(LoginActivity.getInstance().c.maxGrade);
                age.setText(LoginActivity.getInstance().c.age);
                bio.setText(LoginActivity.getInstance().c.bio);

                //set the image, get it from file
                File file = LoginActivity.getInstance().c.myImage;
                try {
                    FileReader fr = new FileReader(file.getAbsoluteFile());
                    BufferedReader br = new BufferedReader(fr);
                    StringBuilder text = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    String pureBase64Encoded = text.toString();
                    br.close();
                    final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    dp.setImageBitmap(decodedBitmap);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //send get profile request when edit button clicked
                    HashMap<String, Serializable> map = new HashMap<String, Serializable>();
                    map.put("username", myUsername);
                    LoginActivity.getInstance().c.sendRequest("GET_PROFILE_REQUEST",map);
                }
            });
            return rootView;
        }
    }

    public static class MyTrainerFragment extends Fragment {
        Chronometer timer;
        boolean running;
        long time;
        String level;
        public static MyTrainerFragment instance;
        TextView instructions;
        Spinner spinner;
        int currentStep;
        Button nextButton;
        HashMap<String, Routine> routines;

        public MyTrainerFragment() {
        }

        public static MyTrainerFragment newInstance() {
            MyTrainerFragment fragment = new MyTrainerFragment();
            return fragment;
        }

        public static MyTrainerFragment getInstance(){
            return instance;
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        public void addRoutines(){
            //create all of the routines for the trainer
            ArrayList<Step> steps = new ArrayList<Step>();
            steps.add(new Step("Do 5 pullups.", false));
            steps.add(new Step("Plank for 20 seconds", true));
            steps.add(new Step("10 situps", false));
            steps.add(new Step("Hang for 10 seconds", true));

            Routine r1 = new Routine(steps);
            routines.put("Beginner", r1);

            ArrayList<Step> steps2 = new ArrayList<Step>();
            steps2.add(new Step("Do 10 pullups.", false));
            steps2.add(new Step("Plank for 1 minute", true));
            steps2.add(new Step("30 situps", false));
            steps2.add(new Step("Hang for 20 seconds", true));

            Routine r2 = new Routine(steps2);
            routines.put("Intermediate", r2);

            ArrayList<Step> steps3 = new ArrayList<Step>();
            steps3.add(new Step("Do 20 pullups.", false));
            steps3.add(new Step("Plank for 5 minutes", true));
            steps3.add(new Step("50 situps", false));
            steps3.add(new Step("Hang for 45 seconds", true));

            Routine r3 = new Routine(steps3);
            routines.put("Advanced", r3);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            instance = this;
            routines = new HashMap<String, Routine>();
            addRoutines();

            currentStep = 0;

            View rootView = inflater.inflate(R.layout.my_trainer_fragment, container, false);

            spinner = (Spinner) rootView.findViewById(R.id.dropDown);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(TabbedActivity.getInstance(),
                    R.array.difficulties, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);

            instructions = (TextView) rootView.findViewById(R.id.instructions);
            nextButton = (Button) rootView.findViewById(R.id.nextButton);

            //set the spinner text, default is beginner
            if(spinner.isSelected()){
                level = spinner.getSelectedItem().toString();
            }
            else{
                level = "Beginner";
            }

            //set up timer
            timer = (Chronometer)rootView.findViewById(R.id.chronometer);
            timer.setBase(SystemClock.elapsedRealtime());
            time = 0;
            running = false;
            //tap on timer to start/ stop
            timer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (running){
                        timer.stop();
                        running = false;
                        time = timer.getBase() - SystemClock.elapsedRealtime();
                    }
                    else{
                        timer.setBase(SystemClock.elapsedRealtime() + time);
                        timer.start();
                        running = true;
                    }
                }
            });
            //long click on timer to reset
            timer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    timer.setBase(SystemClock.elapsedRealtime());
                    return false;
                }
            });

            return rootView;
        }

        public void updateView(Step s){
            //display the workout instructions and timer if is a timed activity
            instructions.setText(s.instruction);
            if(s.timed){
                timer.setVisibility(View.VISIBLE);
            }
            else{
                timer.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void nextClicked(View v){
        Routine r = MyTrainerFragment.getInstance().routines.get(MyTrainerFragment.getInstance().spinner.getSelectedItem().toString());

        if(MyTrainerFragment.getInstance().nextButton.getText().equals("Done")){
            MyTrainerFragment.getInstance().nextButton.setText("Start");
            MyTrainerFragment.getInstance().instructions.setText("");
        }
        //show next or notify done routine

        if(MyTrainerFragment.getInstance().currentStep == r.steps.size()){
            Toast t = Toast.makeText(this, "You finished the " +MyTrainerFragment.getInstance().spinner.getSelectedItem().toString()+ " training routine!", Toast.LENGTH_LONG);
            t.show();
            MyTrainerFragment.getInstance().currentStep = 0;
            MyTrainerFragment.getInstance().instructions.setText("");
            MyTrainerFragment.getInstance().timer.setVisibility(View.VISIBLE);

        }
        else{
            if(MyTrainerFragment.getInstance().currentStep == r.steps.size()-1) {
                MyTrainerFragment.getInstance().nextButton.setText("Done");
            }
            else{
                MyTrainerFragment.getInstance().nextButton.setText("Next");
            }
            Step s = r.steps.get(MyTrainerFragment.getInstance().currentStep);
            Log.i("COMP 2601", "step: " + s.instruction);
            MyTrainerFragment.getInstance().updateView(s);
            MyTrainerFragment.getInstance().currentStep++;
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
            //return the correct fragments for each tab
            switch (position){
                case NEARBY_GYMS:
                    return NearbyGymsFragment.newInstance();
                case FIND_BELAYER:
                    return FindBelayerFragment.newInstance();
                case CONNECT:
                    return ConnectFragment.newInstance();
                case MY_TRAINER:
                    return MyTrainerFragment.newInstance();
                case PROFILE:
                    return ProfileFragment.newInstance();
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 5;
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("2601", "marker title:"+marker.getTitle() + " marker id:" + marker.getId());
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        //marker.hideInfoWindow();
        if(gyms.get(marker.getTitle())){
            //is a favourite
            //remove from favourites
            BitmapDescriptor b = BitmapDescriptorFactory.fromResource(R.drawable.climbericon);
            marker.setIcon(b);
            marker.setSnippet("Tap to add to favourites");
            gyms.put(marker.getTitle(),false);
        }
        else {
            //Add to favourites
            BitmapDescriptor b = BitmapDescriptorFactory.fromResource(R.drawable.climbericongold);
            marker.setIcon(b);
            marker.setSnippet("Tap to remove from favourites");
            gyms.put(marker.getTitle(),true);
        }
    }




}
