/*
 * Copyright J. Craig Venter Institute, 2011
 *
 * The creation of this program was supported by the U.S. National
 * Science Foundation grant 1048199 and the Microsoft allocation
 * in the MS Azure cloud.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pdl.services.storage;

import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.table.*;
import org.soyatec.windowsazure.table.internal.CloudTableQuery;
import pdl.common.Configuration;

import java.net.URI;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/8/11
 * Time: 8:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableOperator {
    private Configuration conf;

    public TableStorageClient tableStorageClient;

    public TableOperator() {
        try {
            conf = Configuration.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TableOperator( Configuration conf ) {
        this.conf = conf;
    }

    private void initTableClient( String storageType ) {
        try {

            if( "diagnostics".equals( storageType ) )
                tableStorageClient = TableStorageClient.create(
                        URI.create( (String)conf.getProperty( "TABLE_HOST_NAME" ) ),
                        Boolean.parseBoolean( (String)conf.getProperty( "PATH_STYLE_URIS" ) ),
                        (String)conf.getProperty( "DIAGNOSTICS_ACCOUNT_NAME" ),
                        (String)conf.getProperty( "DIAGNOSTICS_ACCOUNT_PKEY" ) );
            else
                tableStorageClient = TableStorageClient.create(
                        URI.create( (String)conf.getProperty( "TABLE_HOST_NAME" ) ),
                        Boolean.parseBoolean( (String)conf.getProperty( "PATH_STYLE_URIS" ) ),
                        (String)conf.getProperty( "AZURE_ACCOUNT_NAME" ),
                        (String)conf.getProperty( "AZURE_ACCOUNT_PKEY" ) );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public void initDiagnosticsTableClient( ) {
        initTableClient( "diagnostics" );
    }

    public ITable createTable( String tableName ) {
        ITable table = null;
        try {
            if( tableStorageClient == null )
                initTableClient( null );

            table = tableStorageClient.getTableReference( tableName );
            if ( null == table ) {
                throw new NullPointerException( String.format(  "TableStorageClient returned null ITable '%s'.", tableName ));
            }
            if( !table.isTableExist() ) {
                table.createTable();
                if (!table.isTableExist())
                    throw new Exception( "Table - " + tableName + " is not created." );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return table;
    }

    public boolean insertSingleEntity( String tableName, ITableServiceEntity entity ) {
        boolean rtnVal = false;
        try {

            if( tableStorageClient == null )
                initTableClient( null );

            ITable table = tableStorageClient.getTableReference( tableName );
            if( !table.isTableExist() )
                table = createTable( tableName );

            table.getTableServiceContext().insertEntity( entity );
            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean insertMultipleEntities( String tableName, List<ITableServiceEntity> entityList ) {
        boolean rtnVal = false;
        try {

            if( tableStorageClient == null )
                initTableClient( null );

            ITable table = tableStorageClient.getTableReference( tableName );
            if( !table.isTableExist() )
                table = createTable( tableName );

            TableServiceContext context = table.getTableServiceContext();
            context.setModelClass( entityList.get( 0 ).getClass() );
            context.startBatch();

            for( ITableServiceEntity entity : entityList )
                context.updateEntity(entity);

            context.executeBatch();

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public AbstractTableServiceEntity queryEntityBySearchKey(
            String tableName, String searchColumn,
            String searchKey, Class model) {
        AbstractTableServiceEntity entity = null;

        try {
            List<ITableServiceEntity> entityList = this.queryListBySearchKey(
                    tableName, searchColumn, searchKey, null, null, model
            );

            if( entityList != null && entityList.size() > 0 ) {
                if( entityList.size() > 1 )
                    throw new Exception(
                            String.format(
                                    "More than 1 record has been found for {0}({1}) in {2}" ,
                                    searchKey, searchColumn, tableName )
                    );

                entity = (AbstractTableServiceEntity)entityList.get( 0 );
            }
        } catch ( StorageException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return entity;
    }

    public List<ITableServiceEntity> queryListBySearchKey(
            String tableName, String searchColumn, Object searchKey,
            String order, String orderColumn, Class model) throws StorageException, Exception {
        List<ITableServiceEntity> entityList = null;
        ITable table;

        try {
            if( tableStorageClient == null )
                initTableClient( null );

            CloudTableQuery sql = CloudTableQuery.select();
            if( searchColumn != null && searchKey != null )
                sql.eq( searchColumn, searchKey );
            /*if( order != null && orderColumn != null ) {
                if( order.equals( "asc" ) )
                    sql.orderAsc( orderColumn );
                else if( order.equals( "desc" ) )
                    sql.orderDesc( orderColumn );
            }*/

            table = tableStorageClient.getTableReference( tableName );

            if( table.isTableExist() )
                entityList = table.getTableServiceContext().retrieveEntities( sql.toAzureQuery(), model );

        } catch ( StorageException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw ex;
        }

        return entityList;
    }

    public AbstractTableServiceEntity queryEntityByCondition(
            String tableName, String condition, Class model) {
        AbstractTableServiceEntity entity = null;

        List<ITableServiceEntity> entityList;

        try {
            entityList = this.queryListByCondition( tableName, condition, model );

            if( entityList != null && entityList.size() >= 1 )
                entity = (AbstractTableServiceEntity)entityList.get( 0 );

        } catch ( StorageException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return entity;
    }

    public List<ITableServiceEntity> queryListByCondition(
            String tableName, String condition, Class model) {
        List<ITableServiceEntity> entityList = null;
        ITable table;

        try {
            if( tableStorageClient == null )
                initTableClient( null );

            CloudTableQuery sql = CloudTableQuery.select();
            sql.where( condition );

            table = tableStorageClient.getTableReference( tableName );

            if( table.isTableExist() )
                entityList = table.getTableServiceContext().retrieveEntities( sql.toAzureQuery(), model );

        } catch ( StorageException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return entityList;
    }

    public void deleteEntity( String tableName, ITableServiceEntity entity ) {
        ITable table;

        try {
            if( tableName.startsWith( "WAD" ) ) //Azure Diagnostics Table storage
                initTableClient( "diagnostics" );
            else if( tableStorageClient == null )
                initTableClient( null );

            table = tableStorageClient.getTableReference( tableName );

            if( table != null && table.isTableExist() )
                table.getTableServiceContext().deleteEntityIfNotModified( entity );

        } catch ( StorageException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public boolean updateSingleEntity( String tableName, ITableServiceEntity entity ) {
        boolean rtnVal = false;

        try {
            if( tableStorageClient == null )
                initTableClient( null );

            ITable table = tableStorageClient.getTableReference( tableName );
            if( !table.isTableExist() )
                throw new Exception( "updateEntity - Table does not exist: " + tableName );

            entity.setValues( null );
            table.getTableServiceContext().updateEntity( entity );

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rtnVal;
    }

    public <B extends ITableServiceEntity> void updateMultipleEntities( String tableName, List<B> entityList ) {
        try {
            if( tableStorageClient == null )
                initTableClient( null );

            ITable table = tableStorageClient.getTableReference( tableName );
            if( !table.isTableExist() )
                throw new Exception( "updateEntity - Table does not exist: " + tableName );

            TableServiceContext context = table.getTableServiceContext();
            context.setModelClass( entityList.get( 0 ).getClass() );
            context.startBatch();

            for( B entity : entityList ) {
                entity.setValues( null );
                context.updateEntity( entity );
            }

            context.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
