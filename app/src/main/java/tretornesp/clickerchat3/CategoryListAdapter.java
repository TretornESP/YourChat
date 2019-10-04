package tretornesp.clickerchat3;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {
    private ArrayList<Category> mDataset;
    private LayoutInflater mInflater;
    private ArrayList<ViewHolder> viewHolders;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public boolean active;
        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.category_name);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CategoryListAdapter(Context context, ArrayList<Category> myDataset) {
        viewHolders = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mDataset = myDataset;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public CategoryListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_view_category_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextView.setText(mDataset.get(position).getName());
        viewHolders.add(holder);
        holder.mTextView.setTextColor(holder.mTextView.getResources().getColor(R.color.black));
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < viewHolders.size(); i++) {
                    if (i != position) {
                        viewHolders.get(i).mTextView.setBackground(holder.mTextView.getResources().getDrawable(R.drawable.category_bg));
                        viewHolders.get(i).mTextView.setTextColor(holder.mTextView.getResources().getColor(R.color.black));
                        holder.active = false;
                    }
                }
                holder.mTextView.setBackground(holder.mTextView.getResources().getDrawable(R.drawable.category_bg_selected));
                holder.mTextView.setTextColor(holder.mTextView.getResources().getColor(R.color.white));
                ActiveCategory.activecategory = new Category(mDataset.get(position).getName());
                holder.active = true;
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}