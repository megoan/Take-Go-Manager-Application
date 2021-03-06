package com.example.shmuel.myapplication.controller.cars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.shmuel.myapplication.controller.Clients.ClientEditActivity;
import com.example.shmuel.myapplication.controller.InputWarningDialog;
import com.example.shmuel.myapplication.controller.MainActivity;
import com.example.shmuel.myapplication.R;
import com.example.shmuel.myapplication.controller.TabFragments;
import com.example.shmuel.myapplication.controller.branches.BranchesFragment;
import com.example.shmuel.myapplication.controller.carmodels.CarModelsFragment;
import com.example.shmuel.myapplication.model.backend.BackEndFunc;
import com.example.shmuel.myapplication.model.backend.DataSourceType;
import com.example.shmuel.myapplication.model.backend.FactoryMethod;
import com.example.shmuel.myapplication.model.backend.SelectedDataSource;
import com.example.shmuel.myapplication.model.backend.Updates;
import com.example.shmuel.myapplication.model.datasource.ListDataSource;
import com.example.shmuel.myapplication.model.datasource.MySqlDataSource;
import com.example.shmuel.myapplication.model.entities.MyAddress;
import com.example.shmuel.myapplication.model.entities.Branch;
import com.example.shmuel.myapplication.model.entities.Car;
import com.example.shmuel.myapplication.model.entities.CarModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by shmuel on 23/10/2017.
 */

public class CarRecyclerViewAdapter extends RecyclerView.Adapter<CarRecyclerViewAdapter.ViewHolder> implements Filterable{
    BackEndFunc backEndFunc= FactoryMethod.getBackEndFunc(SelectedDataSource.dataSourceType);
    BackEndFunc backEndForSql=FactoryMethod.getBackEndFunc(DataSourceType.DATA_INTERNET);
    public ArrayList<Car> objects;
    private Context mContext;
    public ActionMode actionMode;
    private int selectedPosition=-1;
    MyFilter myFilter;
    private ProgressDialog progDailog;

    public void removeitem(int position)
    {
        objects.remove(position);
    }

    public CarRecyclerViewAdapter(ArrayList<Car> objects, Context context) {
        this.objects=objects;
        this.mContext=context;
    }

    @Override
    public CarRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_card_layout, parent, false);
        return new CarRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CarRecyclerViewAdapter.ViewHolder holder, final int position) {
        final Car car = objects.get(position);
        if(selectedPosition==position){
            if(((MainActivity)mContext).is_in_action_mode==true){
                holder.itemView.setBackgroundColor(Color.parseColor("#a3a3a3"));
                if(!car.isInUse())
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_not_in_use, mContext.getTheme()));
                    } else {
                        holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_not_in_use));
                    }
                }
                else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_in_use, mContext.getTheme()));
                    } else {
                        holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_in_use));
                    }
                }
            }
        }
        else
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
            if(!car.isInUse())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_not_in_use, mContext.getTheme()));
                } else {
                    holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_not_in_use));
                }
            }
            else
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_in_use, mContext.getTheme()));
                } else {
                    holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_in_use));
                }
            }
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MyActionModeCallbackCar callback=new MyActionModeCallbackCar();
                actionMode=((Activity)mContext).startActionMode(callback);
                actionMode.setTitle("delete car");
                selectedPosition=position;
                ((MainActivity)mContext).is_in_action_mode=true;
                notifyDataSetChanged();
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedPosition);
                if(((MainActivity)mContext).is_in_action_mode==false){
                    Intent intent=new Intent(mContext,CarActivity.class);
                    Car car1=objects.get(position);
                    Branch branch=backEndFunc.getBranch(car1.getBranchNum());
                    CarModel carModel=backEndFunc.getCarModel(car1.getCarModel());
                    intent.putExtra("carModel",carModel.getCompanyName()+" "+carModel.getCarModelName());
                    intent.putExtra("branch",branch.getMyAddress().getAddressName());
                    intent.putExtra("mileage",car1.getMileage());
                    intent.putExtra("id",car1.getCarNum());
                    intent.putExtra("rating",car1.getRating());
                    intent.putExtra("numberOfRatings",car1.getNumOfRatings());
                    intent.putExtra("oneDayCost",car1.getOneDayCost());
                    intent.putExtra("oneMileCost",car1.getOneKilometerCost());
                    intent.putExtra("year",car1.getYear());
                    intent.putExtra("inUse",car1.isInUse());
                    intent.putExtra("imgUrl",car1.getImgURL());
                    intent.putExtra("position",position);
                    intent.putExtra("carmodelID",carModel.getCarModelCode());
                    intent.putExtra("branchID",branch.getBranchNum());
                    ((Activity)mContext).startActivity(intent);
                }
                if (actionMode!=null) {
                    actionMode.finish();
                }
                selectedPosition=-1;
            }
        });


        MyAddress carMyAddress =backEndFunc.getBranch(car.getBranchNum()).getMyAddress();
        CarModel carModel=backEndFunc.getCarModel(car.getCarModel());

        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(mContext)
                .load(car.getImgURL())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.default_car_image)
                .placeholder(R.drawable.default_car_image)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false; // important to return false so the error placeholder can be placed
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imageView);

        holder.companyName.setText(carModel.getCompanyName()+" "+carModel.getCarModelName());
        holder.branch.setText("Branch: "+ carMyAddress.getAddressName());
        holder.carYear.setText(String.valueOf(car.getYear()));
//        holder.companyName.setText(carModel.getCompanyName()+" "+carModel.getCarModelName());
        holder.dailyPrice.setText("USD "+String.valueOf(car.getOneDayCost()));
        holder.milePrice.setText("USD "+String.valueOf(car.getOneKilometerCost()));
        holder.ratingBar.setRating((float) car.getRating());
        holder.rating.setText(String.valueOf(car.getRating()));
        holder.numberOfRatings.setText("("+String.valueOf(car.getNumOfRatings())+")");

       if(!car.isInUse())
       {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_not_in_use, mContext.getTheme()));
           } else {
               holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_not_in_use));
           }
       }
       else
       {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_in_use, mContext.getTheme()));
           } else {
               holder.inUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_in_use));
           }
       }


    }

    @Override
    public int getItemCount() {
        if(objects==null)return 0;
        return objects.size();
    }

    @Override
    public Filter getFilter() {
        if (myFilter == null)
            myFilter = new MyFilter();

        return myFilter;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyName;
        TextView carYear;
        TextView dailyPrice;
        TextView milePrice;
        ImageView imageView;
        RatingBar ratingBar;
        TextView rating;
        TextView numberOfRatings;
        ImageButton inUse;
        TextView branch;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView=(ImageView)itemView.findViewById(R.id.carModelCardImage2);
            companyName=(TextView)itemView.findViewById(R.id.carModelCardCompany);
            branch=(TextView)itemView.findViewById(R.id.carCardBranch);
            carYear=(TextView)itemView.findViewById(R.id.carCardYear);
            dailyPrice=(TextView)itemView.findViewById(R.id.carCardDayPrice);
            milePrice=(TextView)itemView.findViewById(R.id.carCardMilePrice);
            ratingBar=(RatingBar)itemView.findViewById(R.id.carCardratingBar);
            rating=(TextView)itemView.findViewById(R.id.carCardRating);
            numberOfRatings=(TextView)itemView.findViewById(R.id.carCardNumberOfRatings);
            inUse=(ImageButton)itemView.findViewById(R.id.carCardInUdeButton);
            progressBar=(ProgressBar)itemView.findViewById(R.id.downloadProgressBar);
        }
    }
    public class MyActionModeCallbackCar implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId())
            {
                case R.id.delete_item:{
                    if(selectedPosition>-1 && objects.get(selectedPosition).isInUse()){
                    Toast.makeText(mContext,
                            "cannot delete car, car is in use!!!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                        builder.setTitle("Delete Car");

                        builder.setMessage("are you sure?");

                        builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                int objectsLengh=objects.size();
                                //Car car=new Car(objects.get(selectedPosition));
                                new BackGroundDeleteCar().execute();
                                /*backEndFunc.deleteCar(car.getCarNum());
                                if (objectsLengh==objects.size()) {
                                    objects.remove(selectedPosition);
                                }
                                backEndFunc.removeCarFromBranch(car.getCarNum(),car.getBranchNum());
                                BranchesFragment.mAdapter.objects=backEndFunc.getAllBranches();
                                BranchesFragment.mAdapter.notifyDataSetChanged();
                                notifyDataSetChanged();
                                Toast.makeText(mContext,
                                        "car deleted", Toast.LENGTH_SHORT).show();

                                selectedPosition=-1;
                                notifyItemChanged(selectedPosition);
                                actionMode.finish();*/
                            }
                        });

                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            }
            return true;
        }



        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectedPosition=-1;
            notifyItemChanged(selectedPosition);
            ((MainActivity)mContext).is_in_action_mode=false;
            notifyDataSetChanged();
        }

    }

    private class MyFilter extends Filter {
        FilterResults results;
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            results = new FilterResults();
            if (charSequence == null || charSequence.length() == 0) {
                results.values = ListDataSource.carList;
                results.count = ListDataSource.carList.size();
            }
            else
            {
                ArrayList<Car> filteredCars = new ArrayList<Car>();
                for (Car c : backEndFunc.getAllCars()) {
                    CarModel carModel=backEndFunc.getCarModel(c.getCarModel());
                    Branch branch=backEndFunc.getBranch(c.getBranchNum());
                    String s=(carModel.getCompanyName()+" "+carModel.getCarModelName()+" "+ branch.getMyAddress().getAddressName()).toLowerCase();
                    if (s.contains( charSequence.toString().toLowerCase() )|| charSequence.toString().toLowerCase().contains(carModel.getCompanyName().toLowerCase())) {
                        // if `contains` == true then add it
                        // to our filtered list
                        filteredCars.add(c);
                    }
                }
                results.values = filteredCars;
                results.count = filteredCars.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            objects=new ArrayList<Car>((ArrayList<Car>)results.values);
            CarsTabFragment.cars=new  ArrayList<Car>((ArrayList<Car>)results.values);
            notifyDataSetChanged();
        }
    }



    public class BackGroundDeleteCar extends AsyncTask<Void,Void,Updates> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(mContext);
            progDailog.setMessage("Updating...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.show();
        }

        @Override
        protected Updates doInBackground(Void... voids) {
            Car car=objects.get(selectedPosition);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            final StorageReference imageRef;
            imageRef = storageRef.child("car"+"/"+car.getCarNum()+".jpg");
            Updates updates=backEndForSql.deleteCar(car.getCarNum(),car.getBranchNum());
            switch (updates){
                case ERROR:{
                    MySqlDataSource.carList=backEndForSql.getAllCars();
                    return Updates.ERROR;
                }
                case CARMODEL_AND_BRANCH:
                {
                    MySqlDataSource.carModelList=backEndForSql.getAllCarModels();
                    break;
                }
            }

            imageRef.delete();
            MySqlDataSource.carList=backEndForSql.getAllCars();
            MySqlDataSource.branchList=backEndForSql.getAllBranches();

            return updates;
        }


        @Override
        protected void onPostExecute(Updates updates) {
            selectedPosition=-1;
            notifyItemChanged(selectedPosition);
            notifyDataSetChanged();

            super.onPostExecute(updates);
            if(updates==Updates.ERROR)
            {
                InputWarningDialog.showWarningDialog("Server error","sorry, car wasn't deleted! \nplease try again soon!",mContext);
                CarsTabFragment.mAdapter.objects=MySqlDataSource.carList;
                CarsTabFragment.cars=MySqlDataSource.carList;
                CarsTabFragment.mAdapter.notifyDataSetChanged();
                return;
            }
            if(updates==Updates.CARMODEL_AND_BRANCH)
            {
                CarModelsFragment.mAdapter.objects=MySqlDataSource.carModelList;
                CarModelsFragment.carModels=MySqlDataSource.carModelList;
                CarModelsFragment.mAdapter.notifyDataSetChanged();

            }
            BranchesFragment.mAdapter.objects=MySqlDataSource.branchList;
            BranchesFragment.branches=MySqlDataSource.branchList;
            BranchesFragment.mAdapter.notifyDataSetChanged();

            CarsTabFragment.mAdapter.objects=MySqlDataSource.carList;
            CarsTabFragment.cars=MySqlDataSource.carList;
            CarsTabFragment.mAdapter.notifyDataSetChanged();
            //TabFragments.carsTab.updateView2(position);
            Toast.makeText(mContext,
                    "car deleted", Toast.LENGTH_SHORT).show();
            actionMode.finish();
            progDailog.dismiss();
        }
    }
}
