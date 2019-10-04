package tretornesp.clickerchat3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;

public class CategoryListPhotoAdapter extends RecyclerView.Adapter<CategoryListPhotoAdapter.ViewHolder> {
    private ArrayList<Category> mDataset;
    private ArrayList<String> mUrl;
    private LayoutInflater mInflater;
    private ArrayList<ViewHolder> viewHolders;
    private Context mContext;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public boolean active;
        public TextView mTextView;
        public ImageView mImageView;
        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.category_photo_name);
            mImageView = v.findViewById(R.id.category_photo_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CategoryListPhotoAdapter(Context context, ArrayList<CategoryAndURL> myDataset) {
        viewHolders = new ArrayList<>();
        mInflater = LayoutInflater.from(context);

        mDataset = new ArrayList<>();
        mUrl = new ArrayList<>();

        for (CategoryAndURL c: myDataset) {
            mDataset.add(c.getCategory());
            mUrl.add(c.getUrl());
        }

        mContext = context;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public CategoryListPhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_view_category_photo_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextView.setText(mDataset.get(position).getName());

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default", null, mContext.getPackageName());


        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
                // You can pass your own memory cache implementation
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(30)) //rounded corner bitmap
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        imageLoader.displayImage(mUrl.get(position), holder.mImageView, options );

        viewHolders.add(holder);
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                click(mDataset.get(position).getName());
            }
        });
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                click(mDataset.get(position).getName());
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(1000 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
    }

    private void click(String category) {
        ActiveCategory.navigateCategory = true;
        ActiveCategory.activecategory = new Category(category);
        Intent intent = new Intent(mContext, GroupList.class);
        intent.putExtra("global", "true");
        mContext.startActivity(intent);
    }

    public Bitmap resizeBitmapFitXY(int width, int height, Bitmap bitmap){
        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        float originalWidth = bitmap.getWidth(), originalHeight = bitmap.getHeight();
        Canvas canvas = new Canvas(background);
        float scale, xTranslation = 0.0f, yTranslation = 0.0f;
        if (originalWidth > originalHeight) {
            scale = height/originalHeight;
            xTranslation = (width - originalWidth * scale)/2.0f;
        }
        else {
            scale = width / originalWidth;
            yTranslation = (height - originalHeight * scale)/2.0f;
        }
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, transformation, paint);
        return background;
    }
}