package au.com.codeka.warworlds.client.welcome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import au.com.codeka.warworlds.client.R;
import au.com.codeka.warworlds.client.util.GameSettings;

public class GameSettingsFragment extends PreferenceFragmentCompat
implements SharedPreferences.OnSharedPreferenceChangeListener {
  final static Map<GameSettings.ChatProfanityFilter, String> CHAT_PROFANITY_FILTER_DISPLAY =
      ImmutableMap.of(
          GameSettings.ChatProfanityFilter.AllowAll, "Do not filter",
          GameSettings.ChatProfanityFilter.AllowMild, "Filter only strong profanity",
          GameSettings.ChatProfanityFilter.AllowNone, "Filter all profanity"
      );

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public RecyclerView onCreateRecyclerView(
      LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
    RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
    // Add a margin to allow for the action bar.
    ((FrameLayout.LayoutParams) recyclerView.getLayoutParams()).topMargin =
        (int)(48 * getContext().getResources().getDisplayMetrics().density);
    return recyclerView;
  }

  @Override
  public void onCreatePreferences(Bundle bundle, String rootKey) {
    setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));

    PreferenceCategory category = new PreferenceCategory(getContext(), null);
    category.setTitle(R.string.pref_category_chat);
    getPreferenceManager().getPreferenceScreen().addPreference(category);

    ListPreference chatProfanityFilterPref = new ListPreference(getContext());
    chatProfanityFilterPref.setKey(GameSettings.Key.CHAT_PROFANITY_FILTER.toString());
    chatProfanityFilterPref.setPersistent(true);
    chatProfanityFilterPref.setTitle(R.string.pref_chat_profanity_filter);
    populateListPreference(
        chatProfanityFilterPref,
        GameSettings.ChatProfanityFilter.values(),
        CHAT_PROFANITY_FILTER_DISPLAY);
    category.addPreference(chatProfanityFilterPref);
  }

  @Override
  public void onStart() {
    super.onStart();
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(R.string.options);
      actionBar.show();
      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    getPreferenceManager().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
    refreshPreferenceSummaries();
  }

  @Override
  public void onStop() {
    super.onStop();
    getPreferenceManager().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    refreshPreferenceSummaries();
  }

  private <T extends Enum> void populateListPreference(
      ListPreference pref, T[] enumValues, Map<T, String> displayValues) {
    String[] entryValues = new String[enumValues.length];
    String[] entryDisplay = new String[enumValues.length];
    for (int i = 0; i < entryValues.length; i++) {
      entryValues[i] = enumValues[i].toString();
      entryDisplay[i] = displayValues.get(enumValues[i]);
    }
    pref.setEntryValues(entryValues);
    pref.setEntries(entryDisplay);
  }

  private void refreshPreferenceSummaries() {
    Preference p = findPreference(GameSettings.Key.CHAT_PROFANITY_FILTER.name());
    GameSettings.ChatProfanityFilter filter = GameSettings.i.getEnum(
        GameSettings.Key.CHAT_PROFANITY_FILTER, GameSettings.ChatProfanityFilter.class);
    p.setSummary(CHAT_PROFANITY_FILTER_DISPLAY.get(filter));
  }
}
