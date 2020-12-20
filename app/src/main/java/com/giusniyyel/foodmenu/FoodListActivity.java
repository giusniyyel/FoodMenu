/*
 * Created by Daniel Campos
 * Last modified 19/12/20 06:33 PM
 * Copyright (C) 2020 GiusNiyyel Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.giusniyyel.foodmenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.giusniyyel.foodmenu.databinding.ActivityFoodListBinding;
import com.giusniyyel.foodmenu.databinding.FoodListContentBinding;
import com.giusniyyel.foodmenu.databinding.ItemListBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.giusniyyel.foodmenu.dummy.DummyContent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link FoodDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class FoodListActivity extends AppCompatActivity {

    private static final String PATH_FOOD = "food";
    private static final DatabaseReference DB_REFERENCE = FirebaseDatabase.getInstance().getReference(PATH_FOOD);

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    /**
     * This is a View Binding variable
     */
    private ActivityFoodListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityFoodListBinding.inflate(getLayoutInflater()); // This initiate the
        setContentView(mBinding.getRoot());

        onViewClicked();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));

        // To receive the data saved
        DB_REFERENCE.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                DummyContent.Food food = snapshot.getValue(DummyContent.Food.class);
                food.setId(snapshot.getKey());

                if (!DummyContent.ITEMS.contains(food)) {
                    DummyContent.addItem(food);
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                DummyContent.Food food = snapshot.getValue(DummyContent.Food.class);
                food.setId(snapshot.getKey());

                if (DummyContent.ITEMS.contains(food)) {
                    DummyContent.updateItem(food);
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                DummyContent.Food food = snapshot.getValue(DummyContent.Food.class);
                food.setId(snapshot.getKey());

                if (DummyContent.ITEMS.contains(food)) {
                    DummyContent.deleteItem(food);
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(FoodListActivity.this, "Moved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodListActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //This sets the buttons actions
    public void onViewClicked(){
        mBinding.btnSave.setOnClickListener(View -> {
            DummyContent.Food food = new DummyContent.Food(mBinding.etName.getText().toString().trim(),
                    mBinding.etPrice.getText().toString().trim());

            DB_REFERENCE.push().setValue(food);
            mBinding.etName.setText("");
            mBinding.etPrice.setText("");
        });
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final FoodListActivity mParentActivity;
        private final List<DummyContent.Food> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.Food item = (DummyContent.Food) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(FoodDetailFragment.ARG_ITEM_ID, item.getId());
                    FoodDetailFragment fragment = new FoodDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, FoodDetailActivity.class);
                    intent.putExtra(FoodDetailFragment.ARG_ITEM_ID, item.getId());

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(FoodListActivity parent,
                                      List<DummyContent.Food> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FoodListContentBinding mView = FoodListContentBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(mView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(String.format(Locale.getDefault(),"$%s", mValues.get(position).getPrice()));
            holder.mContentView.setText(mValues.get(position).getName());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);

            holder.btnDelete.setOnClickListener(view -> {
                DB_REFERENCE.child(mValues.get(position).getId()).removeValue();
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;
            final Button btnDelete;

            ViewHolder(FoodListContentBinding view) {
                super(view.getRoot());
                btnDelete = view.btnDelete;
                mIdView = view.idText;
                mContentView = view.content;
            }
        }
    }
}