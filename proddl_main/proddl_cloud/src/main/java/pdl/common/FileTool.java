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

package pdl.common;

import org.soyatec.windowsazure.table.ITableServiceEntity;
import pdl.cloud.StorageServices;
import pdl.cloud.model.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 3/20/12
 * Time: 1:46 PM
 */
public class FileTool {
    Configuration conf;
    StorageServices services;
    String uploadDirectoryPath;
    String fileTableName;

    public FileTool() {
        services = new StorageServices();
        this.getUploadDirectoryPath();
    }

    private void getUploadDirectoryPath() {
        if(conf==null)
            conf = Configuration.getInstance();

        fileTableName = conf.getStringProperty("TABLE_NAME_FILES");

        String storagePath = conf.getStringProperty("DATASTORE_PATH");

        uploadDirectoryPath = ToolPool.buildFilePath(storagePath, StaticValues.DIRECTORY_FILE_AREA);
        File uploadDir = new File(uploadDirectoryPath);
        if(!uploadDir.exists())
            uploadDir.mkdir();
    }

    public FileInfo createFileRecord(String username) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(fileInfo.getIuuid()+StaticValues.FILE_EXTENSION);
        fileInfo.setUserId(username);
        fileInfo.setStatus(StaticValues.FILE_STATUS_RESERVED);

        int hashedDirectory = Math.abs(fileInfo.getIuuid().hashCode()) % conf.getIntegerProperty("MAX_FILE_COUNT_PER_DIRECTORY");
        String dirPath = uploadDirectoryPath + hashedDirectory;
        ToolPool.createDirectoryIfNotExist(dirPath);
        fileInfo.setPath(String.valueOf(hashedDirectory));

        return fileInfo;
    }

    public FileInfo getFileInfoById(String fileId) throws Exception{
        FileInfo fileInfo = null;

        ITableServiceEntity entity = services.queryEntityBySearchKey(fileTableName, StaticValues.COLUMN_ROW_KEY, fileId, FileInfo.class);
        if(entity!=null)
            fileInfo = (FileInfo)entity;

        return fileInfo;
    }

    public String createFile(String type, InputStream fileIn, String username) throws Exception{
        String rtnVal = null;
        try {
            FileInfo fileInfo = this.createFileRecord(username);

            String newFilePath = ToolPool.buildFilePath(uploadDirectoryPath, fileInfo.getPath(), fileInfo.getName());
            FileOutputStream fileOut = new FileOutputStream(newFilePath);

            int readBytes = 0;
            int readBlockSize = 4 * 1024 * 1024;
            byte[] buffer = new byte[readBlockSize];
            while ((readBytes = fileIn.read(buffer, 0, readBlockSize)) != -1) {
                fileOut.write(buffer, 0, readBytes);
            }

            fileOut.close();
            fileIn.close();

            //TODO It might need to allow files to be uploaded to other blob containers than jobFiles
            if (type!=null && type.equals("blob")) {
                fileInfo.setContainer(conf.getStringProperty("BLOB_CONTAINER_FILES"));
                boolean uploaded = services.uploadFileToBlob(fileInfo, newFilePath, false);

                if(!uploaded) {
                    File file = new File(newFilePath);
                    if(file.exists())
                        file.delete();
                    throw new Exception("FileTool:Failed to upload file to Blob storage.");
                }
            }

            fileInfo.setStatus(StaticValues.FILE_STATUS_COMMITTED);
            boolean recordInserted = this.insertFileRecord(fileInfo);
            if(recordInserted)
                rtnVal = fileInfo.getIuuid();
        } catch(Exception ex) {
            throw ex;
        }
        return rtnVal;
    }

    public boolean insertFileRecord(FileInfo fileinfo) throws Exception {
        boolean rtnVal = false;
        try {
            rtnVal = services.insertSingleEnttity(fileTableName, fileinfo);
        } catch (Exception ex) {
            throw ex;
        }
        return rtnVal;
    }

    public boolean commitFileRecord(String fileId) throws Exception {
        boolean rtnVal = false;
        try {
            FileInfo fileInfo = getFileInfoById(fileId);
            if(fileInfo!=null) {
                fileInfo.setStatus(StaticValues.FILE_STATUS_COMMITTED);
                rtnVal = services.updateEntity(fileTableName, fileInfo);
            } else
                throw new Exception("File does not exist with ID:" + fileId);
        } catch (Exception ex) {
            throw ex;
        }
        return rtnVal;
    }

    public boolean deleteFileRecord(String fileId) throws Exception {
        boolean rtnVal = false;
        try {

        } catch (Exception ex) {
            throw ex;
        }
        return rtnVal;
    }

    public boolean copy(String from, String to) throws Exception {
        boolean rtnVal = false;
        try {
            File fromFile = new File(from);
            if(fromFile.exists() && fromFile.canRead()) {
                FileInputStream in = new FileInputStream(from);
                FileOutputStream out = new FileOutputStream(to);

                int readBytes = 0;
                int readBlockSize = 4 * 1024 * 1024;
                byte[] buffer = new byte[readBlockSize];
                while ((readBytes = in.read(buffer, 0, readBlockSize)) != -1) {
                    out.write(buffer, 0, readBytes);
                }

                out.close();
                in.close();

                rtnVal = true;
            }
        } catch(Exception ex) {
            throw ex;
        }
        return rtnVal;
    }

    public boolean copyFromDatastore(String path, String to) throws Exception {
        return this.copy(ToolPool.buildFilePath(uploadDirectoryPath, path), to);
    }

    public String getFilePath(String fileId) throws Exception {
        String filePath;
        FileInfo fileInfo = this.getFileInfoById(fileId);
        if(fileInfo==null)
            throw new Exception("File record does not exist!");
        else
            filePath = ToolPool.buildFilePath(uploadDirectoryPath, fileInfo.getPath(), fileInfo.getName());

        return filePath;
    }

    public boolean delete(String fileId, String username) throws Exception {
        boolean rtnVal = false;
        try {
            FileInfo info = (FileInfo)services.queryEntityBySearchKey(fileTableName, StaticValues.COLUMN_ROW_KEY, fileId, FileInfo.class);

            if(info!=null) {
                if(!username.equals(info.getUserId()))
                    throw new Exception("The file belongs to another user");

                services.deleteEntity(fileTableName, info);

                String filePath = uploadDirectoryPath + info.getName();
                File file = new File(filePath);
                if(file.exists())
                    file.delete();

                rtnVal = services.deleteBlob(conf.getStringProperty("BLOB_CONTAINER_FILES"), info.getName());
            }
        } catch(Exception ex) {
            throw ex;
        }

        return rtnVal;
    }
}
