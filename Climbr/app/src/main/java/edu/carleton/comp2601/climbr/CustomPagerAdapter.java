package edu.carleton.comp2601.climbr;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by estitweg on 2017-03-04.
 */
public class CustomPagerAdapter extends PagerAdapter {


        Context mContext;
        LayoutInflater mLayoutInflater;

        public CustomPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return TabbedActivity.FindBelayerFragment.mResources.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
            imageView.setImageResource(TabbedActivity.FindBelayerFragment.mResources[position]);
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.i("CustomPageAdapter","profile picture was long clicked");
                    TabbedActivity.getInstance().tabLayout.getTabAt(2).select();
                    return true;
                }
            });

            TextView bio = (TextView)itemView.findViewById(R.id.bio);
            bio.setText(TabbedActivity.FindBelayerFragment.bioResources[position]);
            bio.setTextSize(16.0f);

            TextView name = (TextView)itemView.findViewById(R.id.name);
            name.setTextSize(30.0f);
            name.setText(TabbedActivity.FindBelayerFragment.nameResources[position]);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }



}
