package in.ac.iitb.treeplantationapp.Images;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.ac.iitb.treeplantationapp.R;

public class AdapterImages extends ArrayAdapter<String> {
    private Context mContext;
    public AdapterImages(Context context, ArrayList<String> objects) {
        super(context, R.layout.custom_view_images, objects);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        LayoutInflater customTreeListInflater = LayoutInflater.from(getContext());
        View customTreeListView = customTreeListInflater.inflate(R.layout.custom_view_images,parent, false);


        String imageUrl = getItem(position);
        ImageView treeImageCustomView = (ImageView) customTreeListView.findViewById(R.id.treeImageCustomView);
        Picasso.with(mContext).load(imageUrl).into(treeImageCustomView);


        return customTreeListView;

    }
}
