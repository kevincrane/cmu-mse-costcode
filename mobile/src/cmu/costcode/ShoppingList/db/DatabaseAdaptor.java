package cmu.costcode.ShoppingList.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cmu.costcode.ShoppingList.objects.Customer;
import cmu.costcode.ShoppingList.objects.Item;
import cmu.costcode.ShoppingList.objects.ShoppingListItem;
import cmu.costcode.WIFIScanner.AccessPoint;

public class DatabaseAdaptor {
	
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "ShoppingList.db";
	
	private static final String TAG = "DatabaseAdaptor";
	private final Context ctx;
	private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

	
    /**
     * Create tables for ShoppingList database
     * @author kevin
     *
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String TEXT_TYPE = " TEXT";
		private static final String INT_TYPE = " INTEGER";
		private static final String COMMA_SEP = ",";
		private static final String SQL_CREATE_CUSTOMER =
		    "CREATE TABLE " + DbContract.CustomerEntry.TABLE_NAME + " (" +
		    DbContract.CustomerEntry.MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    DbContract.CustomerEntry.NAME_FIRST + TEXT_TYPE + COMMA_SEP +
		    DbContract.CustomerEntry.NAME_LAST + TEXT_TYPE + COMMA_SEP +
		    DbContract.CustomerEntry.ADDRESS + TEXT_TYPE + 
		    " )";
		
		private static final String SQL_CREATE_LISTITEM =
			    "CREATE TABLE " + DbContract.ListItemEntry.TABLE_NAME + " (" +
			    DbContract.ListItemEntry.ROW_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			    DbContract.ListItemEntry.MEMBER_ID + INT_TYPE + COMMA_SEP +
			    DbContract.ListItemEntry.ITEM_ID + INT_TYPE + COMMA_SEP +
			    DbContract.ListItemEntry.CHECKED + " BOOLEAN" + COMMA_SEP +
			    DbContract.ListItemEntry.POSITION + " INT_TYPE" +
			    " )";
		
		private static final String SQL_CREATE_ITEM =
			    "CREATE TABLE " + DbContract.ItemEntry.TABLE_NAME + " (" +
			    DbContract.ItemEntry.ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    DbContract.ItemEntry.DESCRIPTION + TEXT_TYPE + COMMA_SEP +
			    DbContract.ItemEntry.CATEGORY_NAME + TEXT_TYPE + 
			    " )";
		
		private static final String SQL_CREATE_CATEGORY =
			    "CREATE TABLE " + DbContract.CategoryEntry.TABLE_NAME + " (" +
			    DbContract.CategoryEntry.CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    DbContract.CategoryEntry.CAT_NAME + TEXT_TYPE +
			    " )";
		
		private static final String SQL_CREATE_AP =
			    "CREATE TABLE IF NOT EXISTS " + DbContract.AccessPointEntry.TABLE_NAME + " (" +
			    DbContract.AccessPointEntry.AP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    DbContract.AccessPointEntry.BSSID + " TEXT, " +
			    DbContract.AccessPointEntry.SSID + " TEXT, " +
			    DbContract.AccessPointEntry.POSX + " FLOAT, " +
			    DbContract.AccessPointEntry.POSY + " FLOAT, " +
			    DbContract.AccessPointEntry.DESC + " TEXT" +
			    " )";
		
		private static final String SQL_CREATE_VERSION =
			    "CREATE TABLE IF NOT EXISTS " + DbContract.InfoVersionEntry.TABLE_NAME + " (" +
			    DbContract.InfoVersionEntry.INFOVERSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    DbContract.InfoVersionEntry.INFO_NAME + " TEXT, " +
			    DbContract.InfoVersionEntry.INFO_VERSION + " TEXT, " +
			    DbContract.InfoVersionEntry.INFO_DESC + " TEXT" +
			    " )";
	
		//TODO: doesn't work
		private static final String SQL_DELETE_ENTRIES =
				"DROP TABLE IF EXISTS " +
				DbContract.CustomerEntry.TABLE_NAME + COMMA_SEP +
				DbContract.ListItemEntry.TABLE_NAME + COMMA_SEP +
				DbContract.ItemEntry.TABLE_NAME + COMMA_SEP +
				DbContract.CategoryEntry.TABLE_NAME + COMMA_SEP +
				DbContract.AccessPointEntry.TABLE_NAME + COMMA_SEP +
				DbContract.InfoVersionEntry.TABLE_NAME;
		
		/** Constructor*/
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		/**
		 * On creation of DB, generate all 4 ShoppingList tables
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_CUSTOMER);
			db.execSQL(SQL_CREATE_LISTITEM);
			db.execSQL(SQL_CREATE_ITEM);
			db.execSQL(SQL_CREATE_CATEGORY);
			db.execSQL(SQL_CREATE_AP);
			db.execSQL(SQL_CREATE_VERSION);
		}
	
		/**
		 * On upgrade of DB, discard all previous results, recreate original tables
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL(SQL_DELETE_ENTRIES);
			onCreate(db);
		}
    }


// ##### DATABASE ADAPTOR #####
    
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public DatabaseAdaptor(Context ctx) {
		this.ctx = ctx;
	}
	
	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public DatabaseAdaptor open() throws SQLException {
		dbHelper = new DatabaseHelper(ctx);
		if(db == null || !db.isOpen()) {
			db = dbHelper.getWritableDatabase();
		}
		return this;
	}
	
	/**
	 * Close DatabaseHelper, freeing its resources
	 */
	public void close() {
		dbHelper.close();
	}
	
	
// ##### CLASS METHODS #####
	
// 		##### DB GET METHODS #####
	
	/**
	 * Return a new Customer object, created from DB data in 'customers' table with 'memberId'
	 * @param memberId
	 * @return
	 */
	public Customer dbGetCustomer(int memberId) {
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.CustomerEntry.TABLE_NAME, 
				new String[] {
					DbContract.CustomerEntry.MEMBER_ID,
					DbContract.CustomerEntry.NAME_FIRST,
					DbContract.CustomerEntry.NAME_LAST,
					DbContract.CustomerEntry.ADDRESS
				}, DbContract.CustomerEntry.MEMBER_ID + "=" + memberId, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of Customers with memberId " + memberId 
					+ ". Found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No Customer with memberId " + memberId + " found.");
			return null;
		}
		
		// Create new Customer object, return it
		String firstName = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.CustomerEntry.NAME_FIRST)
				);
		String lastName = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.CustomerEntry.NAME_LAST)
				);
		String address = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.CustomerEntry.ADDRESS)
				);
		Customer cust = new Customer(memberId, firstName, lastName, address);
		
		return cust;
	}
	
	
	/**
	 * Return an ShoppingListItem corresponding to MemberId
	 * @param memberId
	 * @return
	 */
	public Map<String, ArrayList<ShoppingListItem>> dbGetShoppingListItems(int memberId) {
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.ListItemEntry.TABLE_NAME, 
				new String[] {
					DbContract.ListItemEntry.ITEM_ID,
					DbContract.ListItemEntry.CHECKED,
					DbContract.ListItemEntry.POSITION
				}, DbContract.ListItemEntry.MEMBER_ID + "=" + memberId, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of ShoppingListItems for memberId " + memberId 
					+ ". Found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No ListItems with memberId " + memberId + " found.");
			return null;
		}
		

		// Create Customer's list of ListItems mapped to category
		Map<String, ArrayList<ShoppingListItem>> shoppingList = 
				new HashMap<String, ArrayList<ShoppingListItem>>();
		
		// Iterate through all ShoppingListItems
		for(int i=0; i<mCursor.getCount(); i++) {
			// Read ShoppingListItem properties
			int itemId = mCursor.getInt(
				    mCursor.getColumnIndexOrThrow(DbContract.ListItemEntry.ITEM_ID)
					);
			boolean checked = mCursor.getInt(
					mCursor.getColumnIndexOrThrow(DbContract.ListItemEntry.CHECKED)
					) > 0;
			int position = mCursor.getInt(
				    mCursor.getColumnIndexOrThrow(DbContract.ListItemEntry.POSITION)
					);
			
			// Get Item object from DB
			Item item = dbGetItemById(itemId);
			String category = item.getCategory();
			
			// Get list of items for current ListItem category
			ArrayList<ShoppingListItem> listItems;
			if(shoppingList.containsKey(category)) {
				listItems = shoppingList.get(category);
			} else {
				listItems = new ArrayList<ShoppingListItem>();
			}
			listItems.add(new ShoppingListItem(itemId, checked, position, item));
			
			// Add this ShoppingListItem to shoppingList
			shoppingList.put(category, listItems);
			mCursor.moveToNext();
		}
		
		Log.i(TAG, "Reading from DB: ShoppingList with " + shoppingList.size() + " categories");
		return shoppingList;
	}
	
	
	/**
	 * Returns an Item object by its itemId
	 * @param listId
	 * @return
	 */
	public Item dbGetItemById(int itemId) {
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.ItemEntry.TABLE_NAME, 
				new String[] {
					DbContract.ItemEntry.ITEM_ID,
					DbContract.ItemEntry.DESCRIPTION,
					DbContract.ItemEntry.CATEGORY_NAME
				}, DbContract.ItemEntry.ITEM_ID + "=" + itemId, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of Items with itemId " + itemId 
					+ ". Found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No Items with itemId " + itemId + " found.");
			return null;
		}
		
		// Read Item description and category from DB
		String description = mCursor.getString(
				mCursor.getColumnIndexOrThrow(DbContract.ItemEntry.DESCRIPTION)
				);
		String category = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.ItemEntry.CATEGORY_NAME)
				);
		
		// Create Item object and return it
		Log.i(TAG, "Reading from DB: Item (id " + itemId + ") in category '" 
				+ category + "': '" + description + "'");
		return new Item(itemId, description, category);
	}
	
	
	
	/**
	 * Return list of access point
	 * @return 
	 */
	public List<AccessPoint> dbGetAccessPoint() {
		db.execSQL(DatabaseHelper.SQL_CREATE_AP);
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.AccessPointEntry.TABLE_NAME, 
				new String[] {
					DbContract.AccessPointEntry.BSSID,
					DbContract.AccessPointEntry.SSID,
					DbContract.AccessPointEntry.POSX,
					DbContract.AccessPointEntry.POSY,
				}, null, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of AccessPoint. Found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No AccessPoint found.");
			return null;
		}
		

		// Create the list of AccessPoint
		List<AccessPoint> apList = 
				new ArrayList<AccessPoint>(mCursor.getCount());
		
		// Iterate through all AccessPoint list
		for(int i=0; i<mCursor.getCount(); i++) {
			// Read AccessPoint properties
			AccessPoint ap = new AccessPoint();
			ap.setBssid(mCursor.getString(
					mCursor.getColumnIndexOrThrow(DbContract.AccessPointEntry.BSSID)
					)
			);
			ap.setSsid(mCursor.getString(
					mCursor.getColumnIndexOrThrow(DbContract.AccessPointEntry.SSID)
					)
			);
			ap.setPosX(mCursor.getFloat(
					mCursor.getColumnIndexOrThrow(DbContract.AccessPointEntry.POSX)
					)
			);
			ap.setPosY(mCursor.getFloat(
					mCursor.getColumnIndexOrThrow(DbContract.AccessPointEntry.POSY)
					)
			);
			apList.add(ap);
			
			mCursor.moveToNext();
		}
		
		Log.i(TAG, "Reading from DB: APlist with " + apList.size() + " APs");
		return apList;
	}
	
	
	
//		##### DB CREATE METHODS #####
	
	/**
	 * Create a new row in the Customers db
	 * @param firstName
	 * @param lastName
	 * @param address
	 * @return new memberId
	 */
	public int dbCreateCustomer(String firstName, String lastName, String address) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.CustomerEntry.NAME_FIRST, firstName);
		values.put(DbContract.CustomerEntry.NAME_LAST, lastName);
		values.put(DbContract.CustomerEntry.ADDRESS, address);
		
		// Insert the new row, returning the primary key value of the new row
		return (int)db.insert(DbContract.CustomerEntry.TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new row in the ShoppingListItem db; returns memberId
	 * @param itemId
	 * @param memberId
	 * @param checked
	 * @param position
	 * @return
	 */
	public int dbCreateShoppingListItem(int itemId, int memberId, boolean checked, int position) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ListItemEntry.ITEM_ID, itemId);
		values.put(DbContract.ListItemEntry.MEMBER_ID, memberId);
		values.put(DbContract.ListItemEntry.CHECKED, checked);
		values.put(DbContract.ListItemEntry.POSITION, position);
		
		// Insert the new row, returning the primary key value of the new row (memberId)
		return (int)db.insert(DbContract.ListItemEntry.TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new row in the Item db; returns new itemId
	 * @param description
	 * @param category
	 * @return
	 */
	public int dbCreateItem(String description, String category) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ItemEntry.DESCRIPTION, description);
		values.put(DbContract.ItemEntry.CATEGORY_NAME, category);
		
		// Insert the new row, returning the primary key value of the new row (itemId)
		return (int)db.insert(DbContract.ItemEntry.TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new row in the AccessPoint db; returns BSSID of AccessPoint
	 * @param description
	 * @param category
	 * @return
	 */
	public void dbCreateAccessPoint(AccessPoint ap) {
		db.execSQL(DatabaseHelper.SQL_CREATE_AP);
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.AccessPointEntry.BSSID, ap.getBssid());
		values.put(DbContract.AccessPointEntry.SSID, ap.getSsid());
		values.put(DbContract.AccessPointEntry.POSX, ap.getPosX());
		values.put(DbContract.AccessPointEntry.POSY, ap.getPosY());
		values.put(DbContract.AccessPointEntry.DESC, ap.getDescription());
		
		// Insert the new row, returning the primary key value of the new row (itemId)
		db.insert(DbContract.AccessPointEntry.TABLE_NAME, null, values);
	}
	
	
//		##### UPDATE DB METHODS #####
	
	/**
	 * Sets checked status of a ListItem to 'checked'
	 * @param memberId
	 * @param itemId
	 * @param checked
	 * @return
	 */
	public int dbSetItemChecked(int memberId, int itemId, boolean checked) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ListItemEntry.CHECKED, checked);

		// Which row to update, based on the ID
		String selection = DbContract.ListItemEntry.MEMBER_ID + "=" + memberId
				+ " AND " + DbContract.ListItemEntry.ITEM_ID + "=" + itemId;
		
		return (int)db.update(DbContract.ListItemEntry.TABLE_NAME, values, selection, null);
	}
	
	/**
	 * Delete a row from ListItem db for selected member, 
	 * @param memberId
	 * @param itemId
	 * @return
	 */
	public int dbDeleteItemRow(int memberId, int itemId) {
		// Which row to update, based on the ID
		String selection = DbContract.ListItemEntry.MEMBER_ID + "=" + memberId
				+ " AND " + DbContract.ListItemEntry.ITEM_ID + "=" + itemId;
		return (int) db.delete(DbContract.ListItemEntry.TABLE_NAME, selection, null);
	}
	
	/**
	 * Update a row in the Item db with new category/description
	 * @param itemId
	 * @param category
	 * @param description
	 * @return
	 */
	public int dbUpdateItem(int itemId, String category, String description) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ItemEntry.CATEGORY_NAME, category);
		values.put(DbContract.ItemEntry.DESCRIPTION, description);

		// Which row to update, based on the ID
		String selection = DbContract.ItemEntry.ITEM_ID + "=" + itemId;
		
		return (int)db.update(DbContract.ItemEntry.TABLE_NAME, values, selection, null);
	}
	
	/**
	 * Delete all of data in AccessPoint Table
	 * @return The number of deleted rows
	 */
	public int dbDeleteAccessPoint() {
		return (int) db.delete(DbContract.AccessPointEntry.TABLE_NAME, null, null);
	}

	/**
	 * Get Version information
	 * @param The information name
	 * @return The version of information
	 */
	public String dbGetVersion(String infoName) {
		db.execSQL(DatabaseHelper.SQL_CREATE_VERSION);
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(DbContract.InfoVersionEntry.TABLE_NAME, 
				new String[] {
					DbContract.InfoVersionEntry.INFO_VERSION,
				}, DbContract.InfoVersionEntry.INFO_NAME + "= '" + infoName + "'", null,
				null, null, null, null);
		
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read floorplan version. Found " + mCursor.getCount() + " matches.");
			// Create new Customer object, return it
			return mCursor.getString(mCursor.getColumnIndexOrThrow(DbContract.InfoVersionEntry.INFO_VERSION));
		} else {
			// No matches found
			Log.d(TAG, "No floorplan version found.");
			return null;
		}
		
		
	}

	/**
	 * Insert or update version information
	 * @param infoName The name of information
	 * @param version The version of information
	 * @param description Description about the information
	 * @return The number of affected rows
	 */
	public int dbSetVersion(String infoName, String version, String description) {
		ContentValues values = new ContentValues(3);
		values.put(DbContract.InfoVersionEntry.INFO_NAME, infoName);
		values.put(DbContract.InfoVersionEntry.INFO_VERSION, version);
		values.put(DbContract.InfoVersionEntry.INFO_DESC, description);
		
		// Insert if no data
		if(dbGetVersion(infoName) == null) {
			// insert
			return (int)db.insert(DbContract.InfoVersionEntry.TABLE_NAME, null, values);
		}
		// else update data
		else {
			// Which row to update, based on the infoName
			String selection = DbContract.InfoVersionEntry.INFO_NAME + "= '" + infoName + "'";
			
			return (int)db.update(DbContract.InfoVersionEntry.TABLE_NAME, values, selection, null);
		}
	}
}
