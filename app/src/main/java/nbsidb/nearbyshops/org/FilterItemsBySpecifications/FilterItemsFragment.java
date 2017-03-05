package nbsidb.nearbyshops.org.FilterItemsBySpecifications;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import nbsidb.nearbyshops.org.DaggerComponentBuilder;
import nbsidb.nearbyshops.org.ModelItemSpecification.EndPoints.ItemSpecNameEndPoint;
import nbsidb.nearbyshops.org.ModelItemSpecification.EndPoints.ItemSpecValueEndPoint;
import nbsidb.nearbyshops.org.ModelItemSpecification.ItemSpecificationItem;
import nbsidb.nearbyshops.org.ModelItemSpecification.ItemSpecificationName;
import nbsidb.nearbyshops.org.ModelItemSpecification.ItemSpecificationValue;
import nbsidb.nearbyshops.org.R;
import nbsidb.nearbyshops.org.RetrofitRESTContract.ItemSpecItemService;
import nbsidb.nearbyshops.org.RetrofitRESTContract.ItemSpecNameService;
import nbsidb.nearbyshops.org.RetrofitRESTContract.ItemSpecValueService;
import nbsidb.nearbyshops.org.Utility.UtilityLogin;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class FilterItemsFragment extends Fragment implements AdapterItemSpecName.NotificationsFromAdapterName, AdapterItemSpecValue.NotificationsFromAdapter {

    @Inject ItemSpecNameService itemSpecNameService;

    @Bind(R.id.recycler_view_names) RecyclerView recyclerViewName;

    AdapterItemSpecName adapterName;

    public List<ItemSpecificationName> datasetName = new ArrayList<>();

    GridLayoutManager layoutManagerName;


    Set<Integer> checkboxesList = new HashSet<>();


    @Inject ItemSpecItemService itemSpecItemService;



    @Inject
    ItemSpecValueService itemSpecValueService;

    @Bind(R.id.recycler_view_values) RecyclerView recyclerViewValues;

    AdapterItemSpecValue adapterValues;

    public List<ItemSpecificationValue> datasetValues = new ArrayList<>();

    GridLayoutManager layoutManagerValues;



    boolean isDestroyed;

    private int limit_name = 10;
    int offset_name = 0;
    int item_count_name = 0;


    private int limit_values = 10;
    int offset_values = 0;
    int item_count_values = 0;





    public FilterItemsFragment() {
        DaggerComponentBuilder.getInstance()
                .getNetComponent()
                .Inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        setRetainInstance(true);

        View rootView = inflater.inflate(R.layout.fragment_filter_items, container, false);
        ButterKnife.bind(this,rootView);


        setupRecyclerViewName();
        setupRecyclerViewValues();

        if(savedInstanceState==null)
        {

            boolean resetGetRowCountName = false;
            if(item_count_name == 0)
            {
                resetGetRowCountName=true;
            }

            makeNetworkCallName(true,true,resetGetRowCountName);

        }






        return rootView;
    }




    void setupRecyclerViewName()
    {

        adapterName = new AdapterItemSpecName(datasetName,this,getActivity());

        recyclerViewName.setAdapter(adapterName);

        layoutManagerName = new GridLayoutManager(getActivity(),1);
        recyclerViewName.setLayoutManager(layoutManagerName);

//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.HORIZONTAL_LIST));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        layoutManagerName.setSpanCount(1);


        recyclerViewName.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(layoutManagerName.findLastVisibleItemPosition()==datasetName.size()-1)
                {

                    if(offset_name + limit_name > layoutManagerName.findLastVisibleItemPosition()+1)
                    {
                        return;
                    }

                    // trigger fetch next page

                    if((offset_name + limit_name) <= item_count_name)
                    {
                        offset_name = offset_name + limit_name;
                        makeNetworkCallName(false,false,false);
                    }

                }
            }
        });

    }




    void setupRecyclerViewValues()
    {

        adapterValues = new AdapterItemSpecValue(datasetValues,this,getActivity(),this);

        recyclerViewValues.setAdapter(adapterValues);

        layoutManagerValues = new GridLayoutManager(getActivity(),1);
        recyclerViewValues.setLayoutManager(layoutManagerValues);

//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.HORIZONTAL_LIST));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        layoutManager.setSpanCount(metrics.widthPixels/400);


//        int spanCount = (int) (metrics.widthPixels/(230 * metrics.density));
//
//        if(spanCount==0){
//            spanCount = 1;
//        }

        layoutManagerValues.setSpanCount(1);


        recyclerViewValues.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(layoutManagerValues.findLastVisibleItemPosition()==datasetValues.size()-1)
                {

                    if(offset_values + limit_values > layoutManagerValues.findLastVisibleItemPosition()+1)
                    {
                        return;
                    }

                    // trigger fetch next page

                    if((offset_values+limit_values)<=item_count_values)
                    {
                        offset_values = offset_values + limit_values;
                        makeNetworkCallValues(false,false,false);
                    }

                }
            }
        });

    }



    boolean getRowCountName = true;

    int selectedItemNameID = 0;


    void makeNetworkCallName(final boolean clearDataset, boolean resetOffset, final boolean resetGetRowCount)
    {

        if(resetGetRowCount)
        {
            getRowCountName = true;
        }

        if(resetOffset)
        {
            offset_name = 0;
        }


        Call<ItemSpecNameEndPoint> call = itemSpecNameService.getItemSpecName(
                null,null,limit_name,offset_name,getRowCountName
        );


        call.enqueue(new Callback<ItemSpecNameEndPoint>() {
            @Override
            public void onResponse(Call<ItemSpecNameEndPoint> call, Response<ItemSpecNameEndPoint> response) {

                if(isDestroyed)
                {
                    return;
                }


                if(clearDataset)
                {
                    datasetName.clear();
                }


                if(response.code()==200)
                {
                    datasetName.addAll(response.body().getResults());

                    if(getRowCountName)
                    {
                        item_count_name = response.body().getItemCount();
                    }

                    getRowCountName=false;
                    adapterName.notifyDataSetChanged();
                }
                else
                {
                    showToastMessage("Failed Code : " + String.valueOf(response.code()));
                }


            }



            @Override
            public void onFailure(Call<ItemSpecNameEndPoint> call, Throwable t) {

                if(isDestroyed)
                {
                    return;
                }

                showToastMessage("Failed !");
            }
        });
    }










    void initializeValuesList()
    {

        boolean resetGetRowCountValue = false;
        if(item_count_values == 0)
        {
            resetGetRowCountValue=true;
        }

        makeNetworkCallValues(true,true,resetGetRowCountValue);
    }




    boolean getRowCountValues = true;


    void makeNetworkCallValues(final boolean clearDataset, boolean resetOffset, final boolean resetGetRowCount)
    {

        if(resetGetRowCount)
        {
            getRowCountValues = true;
        }

        if(resetOffset)
        {
            offset_values = 0;
        }

//        itemSpecNameID = getActivity().getIntent().getIntExtra(ITEM_SPEC_NAME_INTENT_KEY,0);

        Call<ItemSpecValueEndPoint> call = itemSpecValueService.getItemSpecName(
                selectedItemNameID,null,null,limit_values,offset_values,getRowCountValues
        );


        call.enqueue(new Callback<ItemSpecValueEndPoint>() {
            @Override
            public void onResponse(Call<ItemSpecValueEndPoint> call, Response<ItemSpecValueEndPoint> response) {

                if(isDestroyed)
                {
                    return;
                }


                if(clearDataset)
                {
                    datasetValues.clear();
                }


                if(response.code()==200)
                {
                    datasetValues.addAll(response.body().getResults());

                    if(getRowCountValues)
                    {
                        item_count_values = response.body().getItemCount();
                    }

                    getRowCountValues=false;
                    adapterValues.notifyDataSetChanged();
                }
                else
                {
                    showToastMessage("Failed Code : " + String.valueOf(response.code()));
                }
            }



            @Override
            public void onFailure(Call<ItemSpecValueEndPoint> call, Throwable t) {

                if(isDestroyed)
                {
                    return;
                }

                showToastMessage("Failed !");
            }
        });
    }














    void showToastMessage(String message)
    {
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }








    @Override
    public void listItemClick(ItemSpecificationName itemSpecName, int position) {

        selectedItemNameID = itemSpecName.getId();
        initializeValuesList();

        int itemID = getActivity().getIntent().getIntExtra(ITEM_ID_INTENT_KEY,0);

        Call<List<ItemSpecificationItem>> call = itemSpecItemService.getItemSpecName(
        itemSpecName.getId(), itemID
        );


        call.enqueue(new Callback<List<ItemSpecificationItem>>() {

            @Override
            public void onResponse(Call<List<ItemSpecificationItem>> call, Response<List<ItemSpecificationItem>> response) {

                if(response.code()==200 && response.body()!=null)
                {

                    checkboxesList.clear();

                    for(ItemSpecificationItem item: response.body())
                    {
                        checkboxesList.add(item.getItemSpecValueID());
                    }


                    adapterValues.notifyDataSetChanged();
                }

            }

            @Override
            public void onFailure(Call<List<ItemSpecificationItem>> call, Throwable t) {

            }
        });


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void listItemClick(ItemSpecificationValue itemSpecValue, int position) {

    }


    public static final String ITEM_ID_INTENT_KEY = "item_id_intent_key";


    @Override
    public void deleteItemSpecItem(int itemSpecValueID) {

        int itemID = getActivity().getIntent().getIntExtra(ITEM_ID_INTENT_KEY,0);

        Call<ResponseBody> call = itemSpecItemService.deleteItemSpecItem(UtilityLogin.getAuthorizationHeaders(getActivity()),itemSpecValueID,itemID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code()==200)
                {
                    showToastMessage("Delete Successful !");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }




    @Override
    public void insertItemSpecItem(int ItemSpecValueID) {

        ItemSpecificationItem itemSpecificationItem = new ItemSpecificationItem();

        itemSpecificationItem.setItemID(getActivity().getIntent().getIntExtra(ITEM_ID_INTENT_KEY,0));
        itemSpecificationItem.setItemSpecValueID(ItemSpecValueID);


        Call<ResponseBody> call = itemSpecItemService.saveItemSpecName(
                UtilityLogin.getAuthorizationHeaders(getActivity()),
                itemSpecificationItem
        );


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(response.code()==201)
                {
                    showToastMessage("Insert Successful !");
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });


    }
}
