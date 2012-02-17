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

package pdl.operator.app;

import pdl.services.StorageServices;
import pdl.utils.ZipHandler;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/10/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractApplicationOperator implements IApplicationOperator {
    protected String storagePath;
    protected String packagePath;
    protected String packageName;
    protected String packageFile;
    protected String packageFilePath;
    protected String param;
    protected String flagFile;

    public AbstractApplicationOperator(String storagePath, String packageName, String flagFile, String param) {
        this.storagePath = storagePath;
        this.packageName = packageName;
        this.packageFile = packageName + ".zip";
        this.packagePath = storagePath + packageName;
        this.packageFilePath = storagePath + packageFile;
        this.flagFile = flagFile;
        this.param = param;
    }

    public void run(StorageServices services) throws Exception {
        if(download(services))
            if( unzip(flagFile))
                start( param );
    }

    public boolean download(StorageServices services) throws Exception {
        boolean rtnVal = false;

        if( services.downloadToolsByName(packageFile, storagePath)) {
            rtnVal = true;
        }

        return rtnVal;
    }

    public abstract boolean start( String param );

    public boolean unzip(String flagFile) throws Exception {
        boolean rtnVal = false;

        ZipHandler zipOperator = new ZipHandler();
        if( zipOperator.unZip( packageFilePath, storagePath ) ) {
            File pachageLocation = new File( packagePath );
            if( pachageLocation.isDirectory()
                    && pachageLocation.exists()
                    && ( new File( packagePath + File.separator + flagFile ) ).exists() )
                rtnVal = true;
        }

        return rtnVal;
    }

    public boolean stop() {
        return true;
    }
}
