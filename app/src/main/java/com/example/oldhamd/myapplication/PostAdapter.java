package com.example.oldhamd.myapplication;


import android.content.Intent;

import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final int view_item = 1;
    private final int view_prog = 0;
    private List<PostInfo> postList;

    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public PostAdapter(List<PostInfo> posts, RecyclerView recyclerView){
        postList = posts;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrolled(RecyclerView recyclerView1, int dx, int dy){
                    super.onScrolled(recyclerView1, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if(!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)){
                        if(onLoadMoreListener != null){
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return postList.get(position) != null ? view_item : view_prog;
    }

    @Override
    public void onBindViewHolder(PostViewHolder postHolder, int i){
        try {
            PostInfo pi = postList.get(i);
            postHolder.vTitle.setText(pi.title);
            postHolder.Url = pi.url;
            postHolder.Source = pi.source;
            Uri uri = Uri.parse(pi.thumb);
            ImageRequest request = ImageRequest.fromUri(uri);
            DraweeController controller = Fresco.newDraweeControllerBuilder().setImageRequest(request).setOldController(postHolder.vImg.getController()).build();
            postHolder.vImg.setController(controller);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.videoposts, viewGroup, false);

        return new PostViewHolder(itemView);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        protected SimpleDraweeView vImg;
        protected TextView vTitle;
        protected String Url;
        protected String Source;

        public PostViewHolder(View v){
            super(v);
            vImg = (SimpleDraweeView) v.findViewById(R.id.post_photo);
            vImg.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    Intent vid = new Intent(v.getContext(),VideoActivity.class);
                    vid.putExtra("url", Url);
                    vid.putExtra("source", Source);
                    v.getContext().startActivity(vid);

                }
            });
            vTitle = (TextView) v.findViewById(R.id.postTitle);
        }
    }
}
