package edu.carleton.comp2601.climbr;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
* Created by estitweg on 2017-03-04.
*/
public class CustomPagerAdapter extends PagerAdapter {


    Context mContext;
    LayoutInflater mLayoutInflater;
    TextView name;

    static CustomPagerAdapter instance;


    public CustomPagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        instance = this;
    }

    public static CustomPagerAdapter getInstance(){
        return instance;
    }

    @Override
    public int getCount() {
        return TabbedActivity.FindBelayerFragment.mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        //notifyDataSetChanged();
        final View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        name = (TextView)itemView.findViewById(R.id.name);
        final int pos = position;

        //Setting image
        //imageView.setImageResource(TabbedActivity.FindBelayerFragment.mResources[position]);//TODO
//        final String pureBase64Encoded = TabbedActivity.FindBelayerFragment.mResources.get(position);
//        final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
//        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        File file = TabbedActivity.FindBelayerFragment.mResources.get(position);
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
            Log.i("2601", "Read from file: " + pureBase64Encoded);
            br.close();
            final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            Log.i("2601 ", "setting image: " + decodedBitmap);
            imageView.setImageBitmap(decodedBitmap);
        }catch(Exception e){
            e.printStackTrace();
        }

        //imageView.setImageBitmap(TabbedActivity.FindBelayerFragment.mResources.get(position));

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i("CustomPageAdapter","profile picture was long clicked");
                TabbedActivity.recipient = TabbedActivity.FindBelayerFragment.nameResources.get(pos);
                TabbedActivity.ConnectFragment.getInstance().changeTitle("Messaging "+ TabbedActivity.recipient);
                //Log.i("2601", "name "+ (String)name.getText() + " \nnameresource" + TabbedActivity.FindBelayerFragment.nameResources[pos]);
                TabbedActivity.getInstance().tabLayout.getTabAt(2).select();
                return true;
            }
        });

        TextView bio = (TextView)itemView.findViewById(R.id.bio);
        bio.setText(TabbedActivity.FindBelayerFragment.bioResources.get(position));
        bio.setTextSize(16.0f);

        name.setTextSize(30.0f);
        name.setText(TabbedActivity.FindBelayerFragment.nameResources.get(position));

        //TODO error here sometimes: 04-07 23:02:13.778 10060-10725/edu.carleton.comp2601.climbr W/System.err: android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.

        TabbedActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                container.addView(itemView);

            }
        });

        return itemView;
    }

    public void refresh(){
        notifyDataSetChanged();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }



}
