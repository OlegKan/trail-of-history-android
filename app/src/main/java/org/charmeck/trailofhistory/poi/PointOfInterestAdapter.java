package org.charmeck.trailofhistory.poi;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.charmeck.trailofhistory.R;
import org.charmeck.trailofhistory.model.PointOfInterest;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Trey Robinson on 3/8/16.
 */
public class PointOfInterestAdapter extends RecyclerView.Adapter<PointOfInterestAdapter.ViewHolder>{

    private List<PointOfInterest> pointOfInterestList;
    private PointOfInterestItemClickListener listener;

    public interface PointOfInterestItemClickListener {
        void onPointOfInterestClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private PointOfInterestItemClickListener listener;

        int id;
        @Bind(R.id.poiName) TextView name;
        @Bind(R.id.poiDescription) TextView description;
        @Bind(R.id.poiLatitude) TextView latitude;
        @Bind(R.id.poiLongitude) TextView longitude;
        @OnClick(R.id.rowPointOfInterest)
        public void onClick(View view) {
            listener.onPointOfInterestClick(id);
        }

        public ViewHolder(View itemView, PointOfInterestItemClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
        }
    }

    public PointOfInterestAdapter(List<PointOfInterest> pointOfInterestList, PointOfInterestItemClickListener listener) {
        this.pointOfInterestList = pointOfInterestList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_point_of_interest, parent, false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PointOfInterest pointOfInterest = pointOfInterestList.get(position);

        holder.id = pointOfInterest.getId();
        holder.name.setText(pointOfInterest.getName());
        if (TextUtils.isEmpty(pointOfInterest.getDescription())) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(pointOfInterest.getDescription());
        }
        holder.latitude.setText(String.valueOf(pointOfInterest.getLatitude()));
        holder.longitude.setText(String.valueOf(pointOfInterest.getLongitude()));
    }

    @Override
    public int getItemCount() {
        return pointOfInterestList.size();
    }
}