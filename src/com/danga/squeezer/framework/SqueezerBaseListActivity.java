package com.danga.squeezer.framework;


import java.util.List;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.danga.squeezer.R;
import com.danga.squeezer.SqueezerActivity;
import com.danga.squeezer.SqueezerHomeActivity;

/**
 * <p>
 * A generic base class for an activity to list items of a particular SqueezeServer data type. The data type
 * is defined by the generic type argument, and must be an extension of {@link SqueezerItem}.
 * You must provide an {@link SqueezerItemView} to provide the view logic used by this activity. This is done
 * by implementing {@link SqueezerItemListActivity#createItemView()}.
 * <p>
 * When the activity is first created ({@link #onCreate(Bundle)}), an empty {@link SqueezerItemListAdapter} is
 * created using the provided {@link SqueezerItemView}. See {@link SqueezerItemListActivity}, too see details
 * of ordering and receiving of list items from SqueezeServer, and handling of item selection.
 * 
 * @param <T>	Denotes the class of the items this class should list
 * @author Kurt Aaholst
 */
public abstract class SqueezerBaseListActivity<T extends SqueezerItem> extends SqueezerItemListActivity {
	protected static final int DIALOG_FILTER = 0;
	protected static final int DIALOG_ORDER = 1;
	
	private SqueezerItemListAdapter<T> itemListAdapter;
	private ListView listView;
	private TextView loadingLabel;
	private SqueezerItemView<T> itemView;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_list);
		listView = (ListView) findViewById(R.id.item_list);
		loadingLabel = (TextView) findViewById(R.id.loading_label);
		itemView = createItemView();
		
    	listView.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			T item = getItemListAdapter().getItem(position);
    			if (item != null && item.getId() != null) {
    	   			try {
    					onItemSelected(position, item);
    	            } catch (RemoteException e) {
    	                Log.e(getTag(), "Error from default action for '" + item + "': " + e);
    	            }
    			}
    		}
    	});
    	
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
				AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
				getItemListAdapter().setupContextMenu(menu, adapterMenuInfo.position);
			}
		});
		
		listView.setOnScrollListener(this);
		
		prepareActivity(getIntent().getExtras());
	}

	
	/**
	 * Implement the action to be taken when an item is selected.
	 * @param index Position in the list of the selected item.
	 * @param item The selected item. This may be null if 
	 * @throws RemoteException
	 */
	abstract protected void onItemSelected(int index, T item) throws RemoteException;
	
	/**
	 * @return A new view logic to be used by this activity
	 */
	abstract protected SqueezerItemView<T> createItemView();
	
	/**
	 * Initial setup of this activity. 
	 * @param extras Optionally use this information to setup the activity. (may be null)
	 */
	public void prepareActivity(Bundle extras) {
	}

	@Override
	public final boolean onContextItemSelected(MenuItem menuItem) {

		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) menuItem.getMenuInfo();
		final T selectedItem = getItemListAdapter().getItem(menuInfo.position);

		if (getService() != null) {
			try {
				return itemView.doItemContext(menuItem, menuInfo.position, selectedItem);
			} catch (RemoteException e) {
                Log.e(getTag(), "Error context menu action '"+ menuInfo + "' for '" + selectedItem + "': " + e);
			}
		}
		return false;
	}

	@Override
	protected void onServiceConnected() throws RemoteException {
		registerCallback();
		orderItems();
	}

	@Override
    public void onPause() {
        if (getService() != null) {
        	try {
				unregisterCallback();
			} catch (RemoteException e) {
                Log.e(getTag(), "Error unregistering list callback: " + e);
			}
        }
        super.onPause();
    }
	


	/**
	 * @return The current listadapter, or null if not set
	 */
	public SqueezerItemAdapter<T> getItemListAdapter() {
		return itemListAdapter;
	}

	/**
	 * Order items from the start, and prepare an adapter to receive them
	 * @throws RemoteException 
	 */
	public void orderItems() {
		reorderItems();
		listView.setVisibility(View.GONE);
		loadingLabel.setVisibility(View.VISIBLE);
		clearItemListAdapter();
	}
	
	public void onItemsReceived(final int count, final int start, final List<T> items) {
		getUIThreadHandler().post(new Runnable() {
			public void run() {
				listView.setVisibility(View.VISIBLE);
				loadingLabel.setVisibility(View.GONE);
				getItemListAdapter().update(count, start, items);
			}
		});
	}
	
	/**
	 * Set the adapter to handle the display of the items, see also {@link #setListAdapter(android.widget.ListAdapter)}
	 * @param listAdapter
	 */
	private void clearItemListAdapter() {
		itemListAdapter = new SqueezerItemListAdapter<T>(itemView);
		listView.setAdapter(itemListAdapter);
	}
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.itemlistmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.menu_item_home:
        	SqueezerHomeActivity.show(this);
			return true;
        case R.id.menu_item_main:
        	SqueezerActivity.show(this);
			return true;
        }
        return super.onMenuItemSelected(featureId, menuItem);
	}
	
}