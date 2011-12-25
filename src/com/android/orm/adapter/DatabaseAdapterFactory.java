package com.android.orm.adapter;

import java.util.Collection;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.orm.Persistable;
import com.android.orm.exception.DatabaseAdapterInstantiationException;
import com.android.orm.exception.PersistenceException;
import com.android.orm.util.PersistenceUtil;

/**
 * Generates a singleTone SqliteAdapter
 * 
 * @author Hamza Gumrah
 */
public abstract class DatabaseAdapterFactory {
	
	private static DatabaseAdapter adapter = null;
	
	private static DatabaseAdapterFactory self = new DatabaseAdapterFactory() {};
	
	/**
	 * shared metadata for all adapters. initiliazed at first adapter generation
	 */
	private static OrmRegistry registry = null;
	
	/**
	 * Use this method for initializing SqliteAdapter
	 * 
	 * @param ctx
	 * @param databaseName
	 * @param databaseVersion
	 * @param entityQualifiedNames
	 * @return generates an SqliteAdapter and returns it.
	 * @throws DatabaseAdapterInstantiationException
	 */
	public static DatabaseAdapter getAdapter(Context ctx, String databaseName, int databaseVersion, String... entityQualifiedNames) throws DatabaseAdapterInstantiationException {
		if (adapter == null) {
			synchronized (adapter) {
				if (adapter == null)
					try {
						registry = new OrmRegistry(entityQualifiedNames);
						adapter = self.newAdapter(ctx, databaseName, databaseVersion);
						adapter.open();
					}
					catch (Exception e) {
						throw new DatabaseAdapterInstantiationException(e.getMessage());
					}
			}
		}
		return adapter;
	}
	
	/**
	 * Use this method when you call adapter after initialization
	 * 
	 * @return initialized SqliteAdapter
	 */
	public static DatabaseAdapter getAdapter() {
		return adapter;
	}
	
	/**
	 * when multi thread operations needed, open another connection to database using a new adapter. it is essential to close additional adapters after usage.
	 * 
	 * @param ctx
	 * @param databaseName
	 * @param databaseVersion
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public DatabaseAdapter newAdapter(Context ctx, String databaseName, int databaseVersion) throws ClassNotFoundException, NoSuchMethodException {
		return new SqliteAdapter(ctx, databaseName, databaseVersion);
	}
	
	private class SqliteAdapter implements DatabaseAdapter {
		
		private final String TAG = "SqliteAdapter";
		
		private final Context context;
		
		private final SQLiteOpenHelper databaseHelper;
		
		private SQLiteDatabase dataBase;
		
		private final String DATABASE_NAME;
		
		private final int DATABASE_VERSION;
		
		/**
		 * @param ctx
		 * @param databaseName
		 * @param databaseVersion
		 * @param entityQualifiedNames
		 * @throws ClassNotFoundException if entity classes not found by ClassLoader
		 * @throws NoSuchMethodException
		 */
		public SqliteAdapter(Context ctx, String databaseName, int databaseVersion) throws ClassNotFoundException, NoSuchMethodException {
			this.context = ctx;
			this.DATABASE_NAME = databaseName;
			this.DATABASE_VERSION = databaseVersion;
			this.databaseHelper = new SqliteHelper();
			
		}
		
		@Override
		public void open() {
			this.dataBase = this.databaseHelper.getWritableDatabase();
		}
		
		@Override
		public void close() {
			this.databaseHelper.close();
		}
		
		@Override
		public <T extends Persistable> void persist(T entity) {
			try {
				ContentValues values = registry.getContentValues(entity);
				long systemId = dataBase.insert(PersistenceUtil.getEntityName(entity.getClass()), null, values);
				entity.setId(systemId);
			}
			catch (Exception e) {
				throw new PersistenceException(entity, e.getMessage());
			}
		}
		
		@Override
		public <T extends Persistable> T get(long systemId, Class<T> clazz) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setForeignKeySupport(boolean enabled) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void dropAll() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void update(Persistable entity) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void drop(String... entityNames) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void delete(Persistable entity) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Collection<Map<String, Object>> query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
			// TODO Auto-generated method stub
			return null;
		}
		
		/**
		 * nested class for handling database creations and upgrades.
		 * 
		 * @author Hamza
		 */
		private class SqliteHelper extends SQLiteOpenHelper {
			
			SqliteHelper() {
				super(context, DATABASE_NAME, null, DATABASE_VERSION);
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				dataBase.beginTransaction();
				try {
					for (String sql : registry.generateCreateStatements()) {
						Log.i(TAG, sql);
						dataBase.execSQL(sql);
					}
					dataBase.setTransactionSuccessful();
				}
				catch (Exception e) {
					Log.e(TAG, "Can not create tables , rolling back ... Error : " + e.getMessage());
				}
				finally {
					dataBase.endTransaction();
				}
				
			}
			
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				
			}
			
		}
		
	}
}
