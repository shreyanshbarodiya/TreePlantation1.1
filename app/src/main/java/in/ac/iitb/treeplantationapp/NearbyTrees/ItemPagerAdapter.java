package in.ac.iitb.treeplantationapp.NearbyTrees;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.ac.iitb.treeplantationapp.R;


public class ItemPagerAdapter extends android.support.v4.view.PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<String> mUrls;


    public ItemPagerAdapter(Context context, ArrayList<String> mUrls) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mUrls = mUrls;
    }

    @Override
    public int getCount() {
        if(mUrls.size()>0){
            return mUrls.size();
        }else{
            return 1;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        if(mUrls != null && position < mUrls.size()){
            Picasso.with(mContext).load(mUrls.get(position)).into(imageView);
        }else{
            imageView.setImageResource(R.drawable.tree);
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
