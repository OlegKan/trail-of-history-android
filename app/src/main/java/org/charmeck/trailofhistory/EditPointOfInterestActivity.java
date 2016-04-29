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

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.charmeck.trailofhistory.dao.DAOFactory;
import org.charmeck.trailofhistory.model.PointOfInterest;
import org.charmeck.trailofhistory.util.PermissionHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditPointOfInterestActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, GetCoordinatesDialog.OnCoordinatesChangedListener {

    public static final String EXTRA_POINT_OF_INTEREST = "org.charmeck.trailofhistory.EXTRA_POINT_OF_INTEREST";

    @Bind(R.id.name) EditText nameEditText;
    @Bind(R.id.description) EditText descriptionEditText;
    @Bind(R.id.latitude) EditText latitudeEditText;
    @Bind(R.id.longitude) EditText longitudeEditText;

    @OnClick(R.id.get_coordinates)
    public void onClick(View view) {
        if (PermissionHelper.hasAccessFineLocationPermission(this)) {
            showGetCoordinatesDialog();
        } else {
            PermissionHelper.requestAccessFineLocationPermission(longitudeEditText, this);
        }
    }

    private PointOfInterest pointOfInterest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_point_of_interest);
        ButterKnife.bind(this);

        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if (extras != null) {
            pointOfInterest = (PointOfInterest) extras.getSerializable(EXTRA_POINT_OF_INTEREST);
        }

        initUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (pointOfInterest.getId() == -1) {
            menu.removeItem(R.id.action_delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:

                if (!validateUi()) {
                    return false;
                }

                updatePointOfInterest();

                if (pointOfInterest.getId() == -1) {
                    DAOFactory.getInstance(this).getPointOfInterestDAO()
                            .create(pointOfInterest);
                } else {
                    DAOFactory.getInstance(this).getPointOfInterestDAO()
                            .update(pointOfInterest);
                }

                setResult(RESULT_OK);
                finish();
                return true;
            case R.id.action_delete:
                DAOFactory.getInstance(this).getPointOfInterestDAO()
                        .delete(pointOfInterest);

                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        updatePointOfInterest();
        savedInstanceState.putSerializable(EXTRA_POINT_OF_INTEREST, pointOfInterest);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionHelper.REQUEST_CODE_LOCATION:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // nothing to do
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onCoordinatesChanged(double latitude, double longitude) {
        pointOfInterest.setLatitude(latitude);
        pointOfInterest.setLongitude(longitude);

        initUi();
    }

    private void initUi() {
        if (pointOfInterest != null) {
            nameEditText.setText(pointOfInterest.getName());
            descriptionEditText.setText(pointOfInterest.getDescription());
            latitudeEditText.setText(String.valueOf(pointOfInterest.getLatitude()));
            longitudeEditText.setText(String.valueOf(pointOfInterest.getLongitude()));

            nameEditText.setSelection(nameEditText.length());
        }
    }

    private boolean validateUi() {
        return validateEditText(nameEditText) &
                //validateEditText(descriptionEditText) &
                validateEditText(latitudeEditText) &
                validateEditText(longitudeEditText);
    }

    private boolean validateEditText(EditText editText) {
        boolean result = true;
        if (TextUtils.isEmpty(editText.getText().toString())) {
            result = false;
            editText.setError(getString(R.string.empty_error));
        }

        return result;
    }

    private void updatePointOfInterest() {
        pointOfInterest.setName(nameEditText.getText().toString());
        pointOfInterest.setDescription(descriptionEditText.getText().toString());
        try {
            pointOfInterest.setLatitude(Double.valueOf(latitudeEditText.getText().toString()));
        } catch (NumberFormatException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        try {
            pointOfInterest.setLongitude(Double.valueOf(longitudeEditText.getText().toString()));
        } catch (NumberFormatException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showGetCoordinatesDialog() {
        updatePointOfInterest();
        DialogFragment dialog = GetCoordinatesDialog.newInstance();
        dialog.show(getSupportFragmentManager(), GetCoordinatesDialog.class.getName());
    }
}