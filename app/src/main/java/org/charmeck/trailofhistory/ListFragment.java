/*
 * Copyright (C) 2016 Oleg Kan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.charmeck.trailofhistory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.charmeck.trailofhistory.dao.DAOFactory;
import org.charmeck.trailofhistory.model.PointOfInterest;
import org.charmeck.trailofhistory.poi.PointOfInterestAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ListFragment extends Fragment implements
        PointOfInterestAdapter.PointOfInterestItemClickListener {

    private static final int REQUEST_EDIT = 1;

    private PointOfInterestAdapter poiAdapter;
    private List<PointOfInterest> pointOfInterestList;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance() {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        pointOfInterestList = new ArrayList<>();
        initList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        poiAdapter = new PointOfInterestAdapter(pointOfInterestList, this);
        recyclerView.setAdapter(poiAdapter);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_EDIT:
                initList();
                poiAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent edit = new Intent(getContext(), EditPointOfInterestActivity.class);
                edit.putExtra(EditPointOfInterestActivity.EXTRA_POINT_OF_INTEREST, new PointOfInterest());
                startActivityForResult(edit, REQUEST_EDIT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPointOfInterestClick(int position) {

        PointOfInterest pointOfInterest = DAOFactory.getInstance(getContext()).getPointOfInterestDAO()
                .read(position);

        Intent edit = new Intent(getContext(), EditPointOfInterestActivity.class);
        edit.putExtra(EditPointOfInterestActivity.EXTRA_POINT_OF_INTEREST, pointOfInterest);
        startActivityForResult(edit, REQUEST_EDIT);
    }

    private void initList() {
        pointOfInterestList.clear();
        pointOfInterestList.addAll(DAOFactory.getInstance(getContext()).getPointOfInterestDAO().read());
    }

}
