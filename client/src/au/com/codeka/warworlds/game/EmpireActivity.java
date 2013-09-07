package au.com.codeka.warworlds.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import au.com.codeka.common.model.BuildRequest;
import au.com.codeka.common.model.Colony;
import au.com.codeka.common.model.Empire;
import au.com.codeka.common.model.Fleet;
import au.com.codeka.common.model.Planet;
import au.com.codeka.common.model.Star;
import au.com.codeka.warworlds.R;
import au.com.codeka.warworlds.ServerGreeter;
import au.com.codeka.warworlds.ServerGreeter.ServerGreeting;
import au.com.codeka.warworlds.TabFragmentActivity;
import au.com.codeka.warworlds.WarWorldsActivity;
import au.com.codeka.warworlds.ctrl.BuildQueueList;
import au.com.codeka.warworlds.ctrl.ColonyList;
import au.com.codeka.warworlds.ctrl.EmpireRankList;
import au.com.codeka.warworlds.ctrl.FleetList;
import au.com.codeka.warworlds.model.BuildManager;
import au.com.codeka.warworlds.model.EmpireHelper;
import au.com.codeka.warworlds.model.EmpireManager;
import au.com.codeka.warworlds.model.MyEmpireManager;

/**
 * This dialog shows the status of the empire. You can see all your colonies, all your fleets, etc.
 */
public class EmpireActivity extends TabFragmentActivity
                            implements EmpireManager.EmpireFetchedHandler {
    private static Empire sCurrentEmpire;
    private static Map<String, Star> sStars;

    Context mContext = this;
    Bundle mExtras = null;
    boolean mFirstRefresh = true;
    boolean mFirstStarsRefresh = true;

    public enum EmpireActivityResult {
        NavigateToPlanet(1),
        NavigateToFleet(2);

        private int mValue;

        public static EmpireActivityResult fromValue(int value) {
            for (EmpireActivityResult res : values()) {
                if (res.mValue == value) {
                    return res;
                }
            }

            throw new IllegalArgumentException("value is not a valid EmpireActivityResult");
        }

        public int getValue() {
            return mValue;
        }

        EmpireActivityResult(int value) {
            mValue = value;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sCurrentEmpire = null;
        sStars = null;

        getTabManager().addTab(mContext, new TabInfo(this, "Overview", OverviewFragment.class, null));
        getTabManager().addTab(mContext, new TabInfo(this, "Colonies", ColoniesFragment.class, null));
        getTabManager().addTab(mContext, new TabInfo(this, "Build", BuildQueueFragment.class, null));
        getTabManager().addTab(mContext, new TabInfo(this, "Fleets", FleetsFragment.class, null));

        mExtras = getIntent().getExtras();
        if (mExtras != null) {
            String fleetKey = mExtras.getString("au.com.codeka.warworlds.FleetKey");
            if (fleetKey != null) {
                getTabHost().setCurrentTabByTag("Fleets");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ServerGreeter.waitForHello(this, new ServerGreeter.HelloCompleteHandler() {
            @Override
            public void onHelloComplete(boolean success, ServerGreeting greeting) {
                if (!success) {
                    startActivity(new Intent(mContext, WarWorldsActivity.class));
                    return;
                }

                Empire myEmpire = EmpireManager.i.getEmpire();
                EmpireManager.i.addEmpireUpdatedListener(myEmpire.key, EmpireActivity.this);
                EmpireManager.i.refreshEmpire();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        EmpireManager.i.removeEmpireUpdatedListener(this);
    }

    @Override
    public void onEmpireFetched(Empire empire) {
        Empire myEmpire = EmpireManager.i.getEmpire();
        if (myEmpire.key.equals(empire.key)) {
            sCurrentEmpire = (Empire) empire;
            getTabManager().reloadTab();
            mFirstRefresh = false;

            MyEmpireManager.i.requestStars(new MyEmpireManager.FetchStarsCompleteHandler() {
                @Override
                public void onComplete(List<Star> stars) {
                    TreeMap<String, Star> starMap = new TreeMap<String, Star>();
                    for (Star s : stars) {
                        starMap.put(s.key, s);
                    }
                    sStars = starMap;
                    getTabManager().reloadTab();
                    mFirstStarsRefresh = false;
                }
            });
        }
    }

    public static class BaseFragment extends Fragment {
        /**
         * Gets a view to display if we're still loading the empire details.
         */
        protected View getLoadingView(LayoutInflater inflator) {
            return inflator.inflate(R.layout.empire_loading_tab, null);
        }
    }

    public static class OverviewFragment extends BaseFragment {
        private View mView;
        private EmpireRankList mEmpireList;

        @Override
        public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
            if (sCurrentEmpire == null) {
                return getLoadingView(inflator);
            }

            mView = inflator.inflate(R.layout.empire_overview_tab, null);
            mEmpireList = (EmpireRankList) mView.findViewById(R.id.empire_rankings);

            Empire empire = EmpireManager.i.getEmpire();

            TextView empireName = (TextView) mView.findViewById(R.id.empire_name);
            ImageView empireIcon = (ImageView) mView.findViewById(R.id.empire_icon);
            TextView allianceName = (TextView) mView.findViewById(R.id.alliance_name);

            empireName.setText(empire.display_name);
            empireIcon.setImageBitmap(EmpireHelper.getShield(getActivity(), empire));
            if (empire.alliance != null) {
                allianceName.setText(empire.alliance.name);
            } else {
                allianceName.setText("");
            }

            final ProgressBar progress = (ProgressBar) mView.findViewById(R.id.progress_bar);
            progress.setVisibility(View.VISIBLE);
            mEmpireList.setVisibility(View.GONE);

            mEmpireList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    Empire empire = mEmpireList.getEmpireAt(position);
                    if (empire != null) {
                        Intent intent = new Intent(getActivity(), EnemyEmpireActivity.class);
                        intent.putExtra("au.com.codeka.warworlds.EmpireKey", empire.key);
                        getActivity().startActivity(intent);
                    }
                }
            });

            Empire myEmpire = EmpireManager.i.getEmpire();
            int minRank = 1;
            if (myEmpire.rank != null) {
              int myRank = myEmpire.rank.rank;
              minRank = myRank - 2;
            }
            if (minRank < 1) {
                minRank = 1;
            }
            EmpireManager.i.fetchEmpiresByRank(minRank, minRank + 4,
                    new EmpireManager.EmpiresFetchedHandler() {
                        @Override
                        public void onEmpiresFetched(List<Empire> empires) {
                            mEmpireList.setEmpires(empires, true);
                            mEmpireList.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                        }
                    });

            TextView empireSearch = (TextView) mView.findViewById(R.id.empire_search);
            empireSearch.setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        onEmpireSearch();
                        return true;
                    }
                    return false;
                }
            });

            final Button searchBtn = (Button) mView.findViewById(R.id.search_btn);
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEmpireSearch();
                }
            });

            return mView;
        }

        private void onEmpireSearch() {
            final TextView empireSearch = (TextView) mView.findViewById(R.id.empire_search);
            final ProgressBar progress = (ProgressBar) mView.findViewById(R.id.progress_bar);
            final ListView rankList = (ListView) mView.findViewById(R.id.empire_rankings);

            progress.setVisibility(View.VISIBLE);
            rankList.setVisibility(View.GONE);

            // hide the soft keyboard (if showing) while the search happens
            InputMethodManager imm = (InputMethodManager) mView.getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(empireSearch.getWindowToken(), 0);

            String nameSearch = empireSearch.getText().toString();
            EmpireManager.i.searchEmpires(getActivity(), nameSearch,
                    new EmpireManager.EmpiresFetchedHandler() {
                        @Override
                        public void onEmpiresFetched(List<Empire> empires) {
                            mEmpireList.setEmpires(empires, false);
                            rankList.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static class ColoniesFragment extends BaseFragment {
        @Override
        public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
            if (sStars == null || sCurrentEmpire == null) {
                return getLoadingView(inflator);
            }

            ArrayList<Colony> colonies = new ArrayList<Colony>();
            for (Star s : sStars.values()) {
                for (Colony c : s.colonies) {
                    if (c.empire_key != null && c.empire_key.equals(sCurrentEmpire.key)) {
                        colonies.add((Colony) c);
                    }
                }
            }

            View v = inflator.inflate(R.layout.empire_colonies_tab, null);
            ColonyList colonyList = (ColonyList) v.findViewById(R.id.colony_list);
            colonyList.refresh(colonies, sStars);

            colonyList.setOnColonyActionListener(new ColonyList.ColonyActionHandler() {
                @Override
                public void onViewColony(Star star, Colony colony) {
                    Planet planet = star.planets.get(colony.planet_index - 1);
                    // end this activity, go back to the starfield and navigate to the given colony

                    Intent intent = new Intent();
                    intent.putExtra("au.com.codeka.warworlds.Result", EmpireActivityResult.NavigateToPlanet.getValue());
                    intent.putExtra("au.com.codeka.warworlds.SectorX", star.sector_x);
                    intent.putExtra("au.com.codeka.warworlds.SectorY", star.sector_y);
                    intent.putExtra("au.com.codeka.warworlds.StarOffsetX", star.offset_x);
                    intent.putExtra("au.com.codeka.warworlds.StarOffsetY", star.offset_y);
                    intent.putExtra("au.com.codeka.warworlds.StarKey", star.key);
                    intent.putExtra("au.com.codeka.warworlds.PlanetIndex", planet.index);
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                }

                @Override
                public void onCollectTaxes() {
                    MyEmpireManager.i.collectTaxes();
                }
            });

            return v;
        }
    }

    public static class BuildQueueFragment extends BaseFragment {
        public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
            if (sCurrentEmpire == null) {
                return getLoadingView(inflator);
            }

            View v = inflator.inflate(R.layout.empire_buildqueue_tab, null);
            BuildQueueList buildQueueList = (BuildQueueList) v.findViewById(R.id.build_queue);
            buildQueueList.refresh(BuildManager.getInstance().getBuildRequests());
            buildQueueList.setBuildQueueActionListener(new BuildQueueList.BuildQueueActionListener() {
                @Override
                public void onAccelerateClick(Star star, BuildRequest buildRequest) {
                    BuildAccelerateDialog dialog = new BuildAccelerateDialog();
                    dialog.setBuildRequest(star, buildRequest);
                    dialog.show(getActivity().getSupportFragmentManager(), "");
                }

                @Override
                public void onStopClick(Star star, BuildRequest buildRequest) {
                    BuildStopConfirmDialog dialog = new BuildStopConfirmDialog();
                    dialog.setBuildRequest(star, buildRequest);
                    dialog.show(getActivity().getSupportFragmentManager(), "");
                }
            });

            return v;
        }
    }

    public static class FleetsFragment extends BaseFragment {
        @Override
        public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
            if (sStars == null || sCurrentEmpire == null) {
                return getLoadingView(inflator);
            }

            ArrayList<Fleet> fleets = new ArrayList<Fleet>();
            for (Star s : sStars.values()) {
                for (Fleet f : s.fleets) {
                    if (f.empire_key != null && f.empire_key.equals(sCurrentEmpire.key)) {
                        fleets.add(f);
                    }
                }
            }

            View v = inflator.inflate(R.layout.empire_fleets_tab, null);
            FleetList fleetList = (FleetList) v.findViewById(R.id.fleet_list);
            fleetList.refresh(fleets, sStars);

            EmpireActivity activity = (EmpireActivity) getActivity();
            if (activity.mFirstStarsRefresh && activity.mExtras != null) {
                String fleetKey = activity.mExtras.getString("au.com.codeka.warworlds.FleetKey");
                if (fleetKey != null) {
                    fleetList.selectFleet(fleetKey, true);
                }
            }

            fleetList.setOnFleetActionListener(new FleetList.OnFleetActionListener() {
                @Override
                public void onFleetView(Star star, Fleet fleet) {
                    Intent intent = new Intent();
                    intent.putExtra("au.com.codeka.warworlds.Result", EmpireActivityResult.NavigateToFleet.getValue());
                    intent.putExtra("au.com.codeka.warworlds.SectorX", star.sector_x);
                    intent.putExtra("au.com.codeka.warworlds.SectorY", star.sector_y);
                    intent.putExtra("au.com.codeka.warworlds.StarOffsetX", star.offset_x);
                    intent.putExtra("au.com.codeka.warworlds.StarOffsetY", star.offset_y);
                    intent.putExtra("au.com.codeka.warworlds.StarKey", star.key);
                    intent.putExtra("au.com.codeka.warworlds.FleetKey", fleet.key);
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                }

                @Override
                public void onFleetSplit(Star star, Fleet fleet) {
                    Bundle args = new Bundle();
                    args.putByteArray("au.com.codeka.warworlds.Fleet", fleet.toByteArray());

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FleetSplitDialog dialog = new FleetSplitDialog();
                    dialog.setFleet(fleet);
                    dialog.show(fm, "");
                }

                @Override
                public void onFleetMove(Star star, Fleet fleet) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FleetMoveDialog dialog = new FleetMoveDialog();
                    dialog.setFleet(fleet);
                    dialog.show(fm, "");
                }

                @Override
                public void onFleetMerge(Fleet fleet, List<Fleet> potentialFleets) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FleetMergeDialog dialog = new FleetMergeDialog();
                    dialog.setup(fleet, potentialFleets);
                    dialog.show(fm, "");
                }

                @Override
                public void onFleetStanceModified(Star star, Fleet fleet, Fleet.FLEET_STANCE newStance) {
                    MyEmpireManager.i.updateFleetStance(star, fleet, newStance);
                }
            });

            return v;
        }
    }
}
