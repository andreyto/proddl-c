﻿/*
 * 
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
 *
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Threading;
using System.IO;

using CommonTool;
using CommonTool.data;
using Microsoft.WindowsAzure.StorageClient;
using Microsoft.WindowsAzure.ServiceRuntime;
using System.Net;
using Microsoft.WindowsAzure.Diagnostics;

namespace PRODDLMaster
{
    class MasterHelper
    {
        private StorageService storageHelper;
        private DynamicDataServiceContext _dynamicDataContext;
        private String localStoragePath;

        private const String DYNAMIC_TABLE_MASTER_DRIVE_KEY_NAME = "MasterDriveIntialized";

        public void OnStop()
        {
            if (storageHelper != null)
            {
                storageHelper.unMountCloudDrive();
            }

            //Delete diagnositcs table
            storageHelper.deleteDiagnosticsTables(RoleEnvironment.GetConfigurationSettingValue("DiagnosticConnectionString"));

            //Clean up Dynamic Data table except azure drive information
            deleteDynamicData();
        }

        public void Run()
        {
            //local tracing file to an instance
            //Trace.Listeners.Clear();
            //TextWriterTraceListener twtl = new TextWriterTraceListener(RoleEnvironment.GetLocalResource("LocalStorage").RootPath);
            //twtl.Name = "TextLogger";
            //twtl.TraceOutputOptions = TraceOptions.ThreadId | TraceOptions.DateTime;
            //ConsoleTraceListener ctl = new ConsoleTraceListener(false);
            //ctl.TraceOutputOptions = TraceOptions.DateTime;
            //Trace.Listeners.Add(twtl);
            //Trace.Listeners.Add(ctl);
            //Trace.AutoFlush = true;

            DiagnosticMonitorConfiguration dmc = DiagnosticMonitor.GetDefaultInitialConfiguration();

            dmc.Logs.ScheduledTransferPeriod = TimeSpan.FromMinutes(3.0);
            dmc.Logs.ScheduledTransferLogLevelFilter = LogLevel.Verbose;

            DiagnosticMonitor.AllowInsecureRemoteConnections = true;
            DiagnosticMonitor.Start("DiagnosticConnectionString", dmc);

            Trace.WriteLine("This Trace line would not be printed by Diagnostics API");

            LocalResource localStorage = RoleEnvironment.GetLocalResource("LocalStorage");
            localStoragePath = Path.GetPathRoot(localStorage.RootPath);

            storageHelper = new StorageService(RoleEnvironment.GetConfigurationSettingValue("StorageConnectionString"));

            if (!this.IsMasterDriveExist())
            {
                String vhdFilePath = createDriveFromCMD();
                if (!String.IsNullOrEmpty(vhdFilePath))
                {
                    //String vhdFilePath = "C:\\master.vhd";
                    //if (File.Exists(vhdFilePath))
                    //{
                    if (storageHelper.uploadCloudDrive(vhdFilePath, "tools", RoleEnvironment.GetConfigurationSettingValue("VHDName")))
                    {
                        this.updateMasterDriveData();
                    }
                }
                else
                {
                    Trace.WriteLine("master.vhd does not exist");
                }
            }
            else
            {
                Trace.WriteLine("Azure drive file already exist");
            }

            String drivePath = storageHelper.getMountedDrivePath(RoleEnvironment.GetConfigurationSettingValue("VHDUri"));

            extractJRE();
            startJavaMainOperator(); //this call never returns, it's on JAVA's hand now

            while (true)
            {
                Thread.Sleep(10000);
            }
        }

        private void initializeTableContext()
        {
            try
            {
                _dynamicDataContext = new DynamicDataServiceContext(
                    storageHelper._account.TableEndpoint.ToString(), 
                    storageHelper._account.Credentials
                    );
                _dynamicDataContext.RetryPolicy = RetryPolicies.Retry(3, TimeSpan.FromSeconds(3));

                storageHelper._account.CreateCloudTableClient().CreateTableIfNotExist(_dynamicDataContext.getDynamicTableName());
            }
            catch (Exception ex)
            {
                Trace.TraceError("initializeTableClient() - " + ex.ToString());
            }
        }

        private Boolean IsMasterDriveExist()
        {
            if (_dynamicDataContext == null)
                initializeTableContext();

            DynamicDataModel driveData = _dynamicDataContext.getDynamicData(DYNAMIC_TABLE_MASTER_DRIVE_KEY_NAME);
            if (driveData != null && !String.IsNullOrEmpty(driveData.dataValue))
            {
                if (driveData.dataValue.Equals("true"))
                    return true;
            }
            return false;
        }

        private void updateMasterDriveData()
        {
            if (_dynamicDataContext == null)
                initializeTableContext();
            _dynamicDataContext.insertDynamicData(
                "dynamicdata_masterdrive", DYNAMIC_TABLE_MASTER_DRIVE_KEY_NAME, DYNAMIC_TABLE_MASTER_DRIVE_KEY_NAME, "true"
            );
        }

        private void deleteDynamicData()
        {
            if (_dynamicDataContext == null)
                initializeTableContext();
            _dynamicDataContext.deleteDynamicData("dynamicdata_catalogserver", "CatalogServerAddress");
            _dynamicDataContext.deleteDynamicData("dynamicdata_catalogserver", "CatalogServerPort");
        }

        private String createDriveFromCMD()
        {
            try
            {
                string vhdFilePath = localStoragePath + @RoleEnvironment.GetConfigurationSettingValue("VHDName");
                String scriptPath = createVHDScriptFile(localStoragePath, vhdFilePath);

                Process proc = new SharedTools().buildCloudProcess(
                    System.Environment.GetEnvironmentVariable("WINDIR") + "\\System32\\diskpart.exe",
                    "/s" + " " + scriptPath,
                    "createDriveFromCMD()");

                proc.Start();
                proc.BeginOutputReadLine();
                proc.BeginErrorReadLine();
                proc.WaitForExit();

                Trace.WriteLine("DONE: createDriveFromCMD()");
                return vhdFilePath;
            }
            catch (Exception ex)
            {
                Trace.TraceError("createDriveFromCMD() - " + ex.ToString());
            }
            return null;
        }

        private String createVHDScriptFile(String resourcePath, String vhdPath)
        {
            String scriptPath = Path.Combine(resourcePath, @"vhd.txt");
            TextWriter tw = new StreamWriter(scriptPath);
            tw.WriteLine(String.Format("create vdisk file={0} maximum=1024 type=fixed", vhdPath));
            tw.WriteLine(String.Format("select vdisk file={0}", vhdPath));
            tw.WriteLine("attach vdisk");
            tw.WriteLine("create partition primary");
            tw.WriteLine("format fs=ntfs label=vhd quick");
            tw.WriteLine("assign letter=v");
            tw.WriteLine("detach vdisk");
            tw.Close();

            return scriptPath;
        }

        private bool extractJRE()
        {
            try
            {
                new SharedTools().extractZipFile(
                    Path.Combine(Directory.GetCurrentDirectory(), @"tools\jre6x64.zip"), 
                    localStoragePath
                );

                Trace.WriteLine("DONE: extractJRE()");
                return true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: extractJRE() - " + ex.ToString());
                return false;
            }
        }

        private bool startJavaMainOperator()
        {
            try
            {
                Trace.WriteLine("START: startJavaMainOperator()");

                String jettyPort = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["HttpIn"].IPEndpoint.Port.ToString();
                IPEndPoint internalAddress = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["CatalogServer"].IPEndpoint;
                String roleRoot = Environment.GetEnvironmentVariable("RoleRoot");

                String jarPath = roleRoot + @"\approot\tools\proddl_core-1.0.jar";
                String jreHome = localStoragePath + @"jre";

                Process proc = new SharedTools().buildCloudProcess(
                    String.Format("\"{0}\\bin\\java.exe\"", jreHome),
                    String.Format("-jar {0} {1} {2} {3} {4} {5}",
                        jarPath, "true", localStoragePath, internalAddress.Address, internalAddress.Port, jettyPort),
                    "ProteinDockingMaster - Java Main Operator");

                proc.Start();
                proc.BeginOutputReadLine();
                proc.BeginErrorReadLine();
                proc.WaitForExit();

                Trace.WriteLine("DONE: startJavaMainOperator()");
                return true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: startJavaMainOperator() - " + ex.ToString());
                return false;
            }
        }
    }
}
