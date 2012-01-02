package com.android.orm.adapter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;
import com.android.orm.exception.BatchPersistenceException;
import com.android.orm.exception.DatabaseAdapterInstantiationException;
import com.android.orm.exception.PersistenceException;
import com.android.orm.exception.RecordNotFoundException;
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
						//send Set to avoid duplicate values
						Set<String> entityNames = new HashSet<String>();
						for(String name:entityQualifiedNames)
							entityNames.add(name);
						registry = new OrmRegistry(entityNames);
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
	
	static final Map<String, EntityMetaData> getRegistryData() {
		if (registry == null)
			throw new IllegalArgumentException("Registry information is not sufficient");
		return registry.getRegistryData();
	}
	
	static final EntityMetaData getEntityMetaData(String entityName) {
		if (registry == null)
			throw new IllegalArgumentException("Registry information is not sufficient");
		return registry.getEntityMetaData(entityName);
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
			this.databaseHelper = new SqliteDatabaseHelper();
			
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
				ContentValues values = SqliteHelper.getContentValues(entity);
				long id = dataBase.insert(PersistenceUtil.getEntityName(entity.getClass()), null, values);
				entity.setId(id);
			}
			catch (Exception e) {
				throw new PersistenceException(entity, e.getMessage());
			}
		}
		
		@Override
		public <T extends Persistable> void persist(Collection<T> entityCollection) {
			if(entityCollection==null || entityCollection.size()==0)
				return;
			BatchPersistenceException ex = null;
			this.dataBase.beginTransaction();
			try {
				for (T entity : entityCollection) {
					ContentValues values = SqliteHelper.getContentValues(entity);
					entity.setId(dataBase.insert(PersistenceUtil.getEntityName(entity.getClass()),null,values));
				}
				this.dataBase.setTransactionSuccessful();
			}
			catch (Exception e) {
				Log.e(TAG, "Rolling back batch insert, ERROR : "+e.getMessage());
				for(T entity:entityCollection)
					entity.setId(OrmConstants.NOT_PERSISTED_ID);
				ex = new BatchPersistenceException(e.getMessage());
			}
			finally{
				this.dataBase.endTransaction();
				if(ex!=null)
					throw ex;
			}
		}
		
		@Override
		public <T extends Persistable> void persist(Set<T> entityCollection) {
			if(entityCollection==null || entityCollection.size()==0)
				return;
			this.dataBase.beginTransaction();
			BatchPersistenceException ex = null;
			try {
				for (T entity : entityCollection) {
					ContentValues values = SqliteHelper.getContentValues(entity);
					entity.setId(dataBase.insert(PersistenceUtil.getEntityName(entity.getClass()),null,values));
				}
				this.dataBase.setTransactionSuccessful();
			}
			catch (Exception e) {
				Log.e(TAG, "Rolling back batch insert, ERROR : "+e.getMessage());
				for(T entity:entityCollection)
					entity.setId(OrmConstants.NOT_PERSISTED_ID);
				ex = new BatchPersistenceException(e.getMessage());
			}
			finally{
				this.dataBase.endTransaction();
				if(ex!=null)
					throw ex;
			}
		}
		@Override
		public <T extends Persistable> T get(long systemId, Class<T> clazz) {
			return null;
		}
		
		@Override
		public void setForeignKeySupport(boolean enabled) {
			if (enabled)
				this.dataBase.execSQL("PRAGMA foreign_keys = ON;");
			else
				this.dataBase.execSQL("PRAGMA foreign_keys = OFF;");
		}
		
		@Override
		public void update(Persistable entity) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void delete(Persistable entity) {
			if (PersistenceUtil.isPersisted(entity))
				throw new PersistenceException(entity, "Can not delete entity which was not saved to database ");
			String entityName = PersistenceUtil.getEntityName(entity.getClass());
			int rowNumber = dataBase.delete(entityName, OrmConstants.PRIMARY_KEY_COLUMN_NAME + " = " + entity.getId(), null);
			if (rowNumber == 0)
				throw new RecordNotFoundException(entity);
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
		private class SqliteDatabaseHelper extends SQLiteOpenHelper {
			
			SqliteDatabaseHelper() {
				super(context, DATABASE_NAME, null, DATABASE_VERSION);
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				dataBase.beginTransaction();
				try {
					for (String sql : SqliteHelper.generateCreateStatements()) {
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
				Log.w(TAG, "Upgrading database from version "+oldVersion+ " to "+newVersion+" current implementation will drop all tables ...");
				for(String sql:SqliteHelper.generateDropStatements()){
					Log.i(TAG, sql);
					dataBase.execSQL(sql);
				}
				this.onCreate(db);	
			}
			
		}
		
	}
}
