package aquajmt.mapua.com.shopapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import aquajmt.mapua.com.shopapp.R;
import aquajmt.mapua.com.shopapp.activities.OrderInfoActivity;
import aquajmt.mapua.com.shopapp.adapters.OrderArrayAdapter;
import aquajmt.mapua.com.shopapp.api.models.OrderInfo;
import aquajmt.mapua.com.shopapp.utils.SharedPref;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Bryan on 7/28/2017.
 */

public class DashboardNotificationsFragment extends Fragment implements AbsListView.OnScrollListener {

    private static final int PAGE_SIZE = 30;
    private boolean isOrderListInitialized;
    private int currentPage = 1;

    private Listener listener;
    private OrderArrayAdapter orderArrayAdapter;

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.lst_orders)
    ListView lstOrders;

    @BindView(R.id.container_no_orders)
    View containerNoOrders;

    @BindView(R.id.container_error_loading)
    View containerErrorLoading;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement "
                    + DashboardNotificationsFragment.class.getSimpleName() + "."
                    + Listener.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_orders, container, false);
        ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList(true);
            }
        });

        ArrayList<OrderInfo> orders = new ArrayList<>();
        orderArrayAdapter = new OrderArrayAdapter(getContext(), orders);
        lstOrders.setAdapter(orderArrayAdapter);
        lstOrders.setOnScrollListener(this);

        lstOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                OrderInfo order = orderArrayAdapter.getItem(pos);
                SharedPref.currentOrder = order;
                Intent intent = new Intent(getActivity(), OrderInfoActivity.class);

                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        orderArrayAdapter.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshList(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view != null && view.getChildAt(view.getChildCount() - 1) != null) {
            if (view.getLastVisiblePosition() == view.getAdapter().getCount() - 1
                    && view.getChildAt(view.getChildCount() - 1).getBottom() <= view.getHeight()) {
            }
        }
    }

    private void refreshList(final boolean hideRefreshThingAfterLoad) {
        listener.retrieveShopOrders(2, currentPage, PAGE_SIZE, new Receiver() {
            @Override
            public void retrieveShopOrders(List<OrderInfo> orderPartials) {
                if (hideRefreshThingAfterLoad) swipeRefreshLayout.setRefreshing(false);

                if (!isOrderListInitialized) {
                    isOrderListInitialized = true;
                    lstOrders.setVisibility(View.VISIBLE);
                    containerErrorLoading.setVisibility(View.GONE);
                }

                if (orderPartials.isEmpty()) {
                    lstOrders.setVisibility(View.GONE);
                    containerNoOrders.setVisibility(View.VISIBLE);
                } else {
                    orderArrayAdapter.addAll(orderPartials);
                    lstOrders.setVisibility(View.VISIBLE);
                    containerNoOrders.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError() {
                if (hideRefreshThingAfterLoad) swipeRefreshLayout.setRefreshing(false);

                if (!isOrderListInitialized || orderArrayAdapter.isEmpty()) {
                    lstOrders.setVisibility(View.GONE);
                    containerErrorLoading.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public interface Listener {
        void retrieveShopOrders(int type, int page, int pageSize, Receiver receiver);
    }

    public interface Receiver {
        void retrieveShopOrders(List<OrderInfo> orderPartials);
        void onError();
    }
}
