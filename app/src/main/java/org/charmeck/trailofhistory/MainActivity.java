package org.charmeck.trailofhistory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.charmeck.trailofhistory.dao.DAOFactory;
import org.charmeck.trailofhistory.dao.JsonPointOfInterestUtils;
import org.charmeck.trailofhistory.model.PointOfInterest;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String MODE_KEY = "org.charmeck.trailofhistory.MODE_KEY";

    private static final int MODE_LIST = 0;
    private static final int MODE_MAP = 1;

    private int mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // default mode
            mMode = 0;
        } else {
            mMode = savedInstanceState.getInt(MODE_KEY);
        }

        replaceContentFragment(mMode);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(MODE_KEY, mMode);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mode = menu.findItem(R.id.action_mode);
        if (mode != null) {
            switch (mMode) {
                case MODE_LIST:
                    mode.setIcon(R.mipmap.ic_google_maps_white_24dp);
                    mode.setTitle(R.string.map);
                    return true;
                case MODE_MAP:
                    mode.setIcon(R.mipmap.ic_format_list_bulleted_white_24dp);
                    mode.setTitle(R.string.list);
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:

                List<PointOfInterest> list = DAOFactory.getInstance(this).getPointOfInterestDAO().read();
                try {
                    JSONArray jsonArray = JsonPointOfInterestUtils.toJsonArray(list);
                    String message = jsonArray.toString(4);

                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.points_of_interest));
                    send.putExtra(Intent.EXTRA_TEXT, message);
                    startActivity(Intent.createChooser(send, getString(R.string.share_with)));
                } catch (JSONException e) {
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_mode:
                // switch modes
                mMode = (mMode == MODE_LIST) ? MODE_MAP : MODE_LIST;

                // update menu title and icon
                invalidateOptionsMenu();

                replaceContentFragment(mMode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceContentFragment(int mode) {
        switch (mode) {
            case MODE_LIST:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ListFragment.newInstance())
                        .commit();
                break;
            case MODE_MAP:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MapFragment.newInstance())
                        .commit();
                break;
            default:
                throw new IllegalArgumentException("unsupported mode : " + mode);
        }
    }
}