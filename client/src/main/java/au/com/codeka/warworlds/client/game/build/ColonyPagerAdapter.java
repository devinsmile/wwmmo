package au.com.codeka.warworlds.client.game.build;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import au.com.codeka.warworlds.client.util.RomanNumeralFormatter;
import au.com.codeka.warworlds.common.Log;
import au.com.codeka.warworlds.common.proto.Colony;
import au.com.codeka.warworlds.common.proto.Planet;
import au.com.codeka.warworlds.common.proto.Star;
import java.util.List;

/**
 * A {@link PagerAdapter} used by the {@link BuildLayout} to allow you to quickly swipe between your
 * colonies.
 */
public class ColonyPagerAdapter extends PagerAdapter {
  private static final Log log = new Log("ColonyPagerAdapter");
  private final Context context;
  private final BuildLayout buildLayout;

  private final SparseArray<ColonyView> views = new SparseArray<>();

  private Star star;
  private List<Colony> colonies;

  public ColonyPagerAdapter(
      Context context,
      Star star,
      List<Colony> colonies,
      BuildLayout buildLayout) {
    this.context = context;
    this.buildLayout = buildLayout;
    this.star = star;
    this.colonies = colonies;
  }

  public void refresh(Star star, List<Colony> colonies) {
    this.star = star;

    // TODO: what if the current list of colonies has changed?
    this.colonies = colonies;
    notifyDataSetChanged();

    for (int i = 0; i < views.size(); i++) {
      int position = views.keyAt(i);
      ColonyView view = views.valueAt(i);
      view.refresh(star, colonies.get(position));
    }
  }

  @Override
  public Object instantiateItem(ViewGroup parent, int position) {
    log.info("instantiating item %d", position);
    Colony colony = colonies.get(position);
    ColonyView view = new ColonyView(context, star, colony, buildLayout);
    view.setLayoutParams(new ViewPager.LayoutParams());
    parent.addView(view);
    views.put(position, view);
    return view;
  }

  @Override
  public void destroyItem(ViewGroup collection, int position, Object view) {
    views.remove(position);
    collection.removeView((View) view);
  }

  @Override
  public int getCount() {
    return colonies.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    int planetIndex = 0;
    for (Planet planet : star.planets) {
      if (planet.colony != null && planet.colony.id.equals(colonies.get(position).id)) {
        planetIndex = planet.index;
      }
    }
    return String.format("%s %s", star.name, RomanNumeralFormatter.format(planetIndex + 1));
  }
}
