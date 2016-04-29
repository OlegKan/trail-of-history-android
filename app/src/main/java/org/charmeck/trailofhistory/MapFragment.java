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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.charmeck.trailofhistory.dao.DAOFactory;
import org.charmeck.trailofhistory.model.PointOfInterest;
import org.charmeck.trailofhistory.util.PermissionHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String MAP_TYPE_KEY = "org.charmeck.trailofhistory.MAP_TYPE_KEY";

    private static final int REQUEST_EDIT = 1;

    private List<PointOfInterest> pointOfInterestList;
    private Map<String, PointOfInterest> pointOfInterestMap;

    private int mMapType;

    private GoogleMap mMap;

    private GoogleApiClient googleApiClient;

    @Bind(R.id.frameLayout) FrameLayout frameLayout;
    @Bind(R.id.fab) FloatingActionButton fab;

    @OnClick(R.id.fab)
    void onClick(View view) {
        // switch map types
        mMapType = (mMapType == GoogleMap.MAP_TYPE_NORMAL) ?
                GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL;

        setMapType(mMapType);

        int iconId;
        if (mMapType == GoogleMap.MAP_TYPE_NORMAL) {
            iconId = R.mipmap.ic_earth_white_24dp;
        } else {
            iconId = R.mipmap.ic_earth_off_white_24dp;
        }
        fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                iconId, getActivity().getTheme()));
    }

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            // default mode
            mMapType = GoogleMap.MAP_TYPE_NORMAL;
        } else {
            mMapType = savedInstanceState.getInt(MAP_TYPE_KEY);
        }

        pointOfInterestList = new ArrayList<>();
        pointOfInterestMap = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, rootView);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
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
                addMarkersToMap();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                buildGoogleApiClient();
                googleApiClient.connect();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        setMapType(mMapType);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            long previousClickTime;
            Marker previousMarker;

            @Override
            public boolean onMarkerClick(Marker marker) {
                if (previousMarker == null) {
                    Toast.makeText(getContext(), getString(R.string.tap_again_to_edit), Toast.LENGTH_LONG).show();
                    previousMarker = marker;
                    previousClickTime = System.currentTimeMillis();
                    return false;
                } else {
                    if (!previousMarker.equals(marker) && (System.currentTimeMillis() - previousClickTime) > 3000) {
                        previousMarker = null;
                        return false;
                    }
                }

                PointOfInterest pointOfInterest = pointOfInterestMap.get(marker.getId());
                if (pointOfInterest != null) {
                    Intent edit = new Intent(getContext(), EditPointOfInterestActivity.class);
                    edit.putExtra(EditPointOfInterestActivity.EXTRA_POINT_OF_INTEREST, pointOfInterest);
                    startActivityForResult(edit, REQUEST_EDIT);
                }

                previousMarker = null;

                return false;
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            PointOfInterest pointOfInterest;

            @Override
            public void onMarkerDragStart(Marker marker) {
                pointOfInterest = pointOfInterestMap.get(marker.getId());
            }

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (pointOfInterest != null) {
                    pointOfInterest.setLatitude(round(marker.getPosition().latitude, 7));
                    pointOfInterest.setLongitude(round(marker.getPosition().longitude, 7));

                    DAOFactory.getInstance(getContext()).getPointOfInterestDAO()
                            .update(pointOfInterest);
                }
            }

            private double round(double value, int places) {
                if (places < 0) throw new IllegalArgumentException();

                BigDecimal bd = new BigDecimal(value);
                bd = bd.setScale(places, RoundingMode.HALF_UP);
                return bd.doubleValue();
            }
        });

        initList();
        addMarkersToMap();

        //mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // check if location providers are available
        try {
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            if (providers.size() == 1 && providers.get(0).equals(LocationManager.PASSIVE_PROVIDER)) {
                Toast.makeText(getContext(),
                        R.string.location_service_not_available,
                        Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        @SuppressWarnings("ResourceType")
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        googleApiClient.disconnect();

        PointOfInterest pointOfInterest = new PointOfInterest();
        if (location != null) {
            pointOfInterest.setLatitude(location.getLatitude());
            pointOfInterest.setLongitude(location.getLongitude());
        }

        Intent edit = new Intent(getContext(), EditPointOfInterestActivity.class);
        edit.putExtra(EditPointOfInterestActivity.EXTRA_POINT_OF_INTEREST, pointOfInterest);
        startActivityForResult(edit, REQUEST_EDIT);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext() , R.string.location_service_not_available, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionHelper.REQUEST_CODE_LOCATION:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initList() {
        pointOfInterestList.clear();
        pointOfInterestList.addAll(DAOFactory.getInstance(getContext()).getPointOfInterestDAO().read());
    }

    private void addMarkersToMap() {
        mMap.clear();
        pointOfInterestMap.clear();

        for (int i = 0; i < pointOfInterestList.size(); i++) {
            PointOfInterest pointOfInterest = pointOfInterestList.get(i);
            LatLng position = new LatLng(pointOfInterest.getLatitude(), pointOfInterest.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .draggable(true)
                    .title(pointOfInterest.getName())
                    .snippet(pointOfInterest.getDescription())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mMap.addMarker(markerOptions);
            pointOfInterestMap.put("m"+i, pointOfInterest);
        }

        // move camera to the 1st element
        if (pointOfInterestList.size() > 0) {
            PointOfInterest pointOfInterest = pointOfInterestList.get(0);
            LatLng marker = new LatLng(pointOfInterest.getLatitude(), pointOfInterest.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
        }
    }

    private void setMapType(int type) {
        mMap.setMapType(type);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressWarnings("ResourceType")
    private void enableMyLocation() {
        if (PermissionHelper.hasAccessFineLocationPermission(getActivity())) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        } else {
            PermissionHelper.requestAccessFineLocationPermission(frameLayout, getActivity());
        }
    }

    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }
}
