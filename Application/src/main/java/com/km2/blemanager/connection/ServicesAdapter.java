package com.km2.blemanager.connection;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.km2.blemanager.BleCharacteristic;
import com.km2.blemanager.BleService;
import com.km2.blemanager.R;
import com.km2.blemanager.widgets.CommentAnimator;
import com.km2.blemanager.utils.TransitionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.km2.blemanager.utils.AnimUtils.getFastOutSlowInInterpolator;

public class ServicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EXPAND = 0x1;
    private static final int COLLAPSE = 0x2;
    private static final int COMMENT_LIKE = 0x3;
    private static final int REPLY = 0x4;

    private final List<BleService> mBleServices = new ArrayList<>();
    private final Transition expandCollapse;

    private RecyclerView mServiceList;

    private CommentAnimator serviceAnimator;
    private int expandedServicePosition = RecyclerView.NO_POSITION;
    private Callback mCallback;

    public interface Callback {

        void readCharacteristic(BleCharacteristic bleCharacteristic);

        void writeCharacteristic(BleCharacteristic bleCharacteristic, String s);

        void startNotifications(BleCharacteristic bleCharacteristic);
    }

    public ServicesAdapter(@NonNull Context context, long expandDuration, List<BleService> bleServices) {
        mBleServices.addAll(bleServices);
        expandCollapse = new AutoTransition();
        expandCollapse.setDuration(expandDuration);
        expandCollapse.setInterpolator(getFastOutSlowInInterpolator(context));
        TransitionUtils.TransitionListenerAdapter transitionListenerAdapter = new TransitionUtils.TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                mServiceList.setOnTouchListener(touchEater);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                serviceAnimator.setAnimateMoves(true);
                mServiceList.setOnTouchListener(null);
            }
        };
        expandCollapse.addListener(transitionListenerAdapter);
    }

    private View.OnTouchListener touchEater = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    };

    @Override
    public int getItemCount() {
        int count = 0;
        if (!mBleServices.isEmpty()) {
            count += mBleServices.size();
        }
        return count;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createServiceHolder(parent);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindService((ServiceViewHolder) holder, getBleService(position));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> partialChangePayloads) {
        if (holder instanceof ServiceViewHolder) {
            bindPartialBleServiceChange(
                    (ServiceViewHolder) holder, position, partialChangePayloads);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    private BleService getBleService(int adapterPosition) {
        return mBleServices.get(adapterPosition); // description
    }

    private ServiceViewHolder createServiceHolder(ViewGroup parent) {
        final ServiceViewHolder holder = new ServiceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_service, parent, false));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                TransitionManager.beginDelayedTransition(mServiceList, expandCollapse);
                serviceAnimator.setAnimateMoves(false);

                // collapse any currently expanded items
                if (RecyclerView.NO_POSITION != expandedServicePosition) {
                    notifyItemChanged(expandedServicePosition, COLLAPSE);
                }

                // expand this item (if it wasn't already)
                if (expandedServicePosition != position) {
                    expandedServicePosition = position;
                    notifyItemChanged(position, EXPAND);
                    holder.itemView.requestFocus();
                } else {
                    expandedServicePosition = RecyclerView.NO_POSITION;
                }
            }
        });

        return holder;
    }

    private void bindService(ServiceViewHolder holder, BleService bleService) {
        final int position = holder.getAdapterPosition();
        final boolean isExpanded = position == expandedServicePosition;

        holder.mName.setText("Name: " + bleService.getName());
        holder.mUUID.setText("UUID: " + bleService.getUUID());

        for (BleCharacteristic bleCharacteristic : bleService.getBleCharacteristics()) {

            setUpCharacteristicsView(holder, bleCharacteristic);


        }
        setExpanded(holder, isExpanded);
    }

    private void setUpCharacteristicsView(ServiceViewHolder holder, final BleCharacteristic bleCharacteristic) {
        View view = LayoutInflater.from(holder.mCharacteristicContainer.getContext()).inflate(R.layout.list_item_characterstic, holder.mCharacteristicContainer, false);
        TextView name = (TextView) view.findViewById(R.id.characteristic_name);
        TextView address = (TextView) view.findViewById(R.id.characteristic_address);
        name.setText("Name: " + bleCharacteristic.getName());
        address.setText("UUID: " + bleCharacteristic.getUUID());

        Button read = (Button) view.findViewById(R.id.read);
        Button write = (Button) view.findViewById(R.id.write);
        Button notification = (Button) view.findViewById(R.id.notification);

        if (bleCharacteristic.isReadable()) {
            read.setVisibility(View.VISIBLE);
            read.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getCallback().readCharacteristic(bleCharacteristic);
                }
            });
        }
        if (bleCharacteristic.isWritable()) {
            write.setVisibility(View.VISIBLE);
            write.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getCallback().writeCharacteristic(bleCharacteristic, "00C00020E0");
                }
            });
        }

        if (bleCharacteristic.isNotifiable()) {
            notification.setVisibility(View.VISIBLE);
            notification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getCallback().startNotifications(bleCharacteristic);
                }
            });
        }
        holder.mCharacteristicContainer.addView(view);
        view.setTag(bleCharacteristic);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setExpanded(ServiceViewHolder holder, boolean isExpanded) {
        holder.itemView.setActivated(isExpanded);
        holder.mCharacteristicContainer.setVisibility((isExpanded) ? View.VISIBLE : View.GONE);
    }

    private void bindPartialBleServiceChange(ServiceViewHolder holder, int position, List<Object> partialChangePayloads) {
        // for certain changes we don't need to rebind data, just update some view state
        if ((partialChangePayloads.contains(EXPAND) || partialChangePayloads.contains(COLLAPSE)) || partialChangePayloads.contains(REPLY)) {
            setExpanded(holder, position == expandedServicePosition);
        } else if (partialChangePayloads.contains(COMMENT_LIKE)) {
        } else {
            onBindViewHolder(holder, position);
        }
    }


    public void setRecyclerView(RecyclerView gattServicesList) {
        mServiceList = gattServicesList;
    }

    public void setAnimator(CommentAnimator commentAnimator) {
        serviceAnimator = commentAnimator;
    }


    static class ServiceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.service_name)
        TextView mName;
        @BindView(R.id.service_uuid)
        TextView mUUID;

        @BindView(R.id.characteristics_container)
        LinearLayout mCharacteristicContainer;

        ServiceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public Callback getCallback() {
        if (mCallback == null) {
            throw new IllegalStateException("Callback is null");
        }
        return mCallback;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }
}