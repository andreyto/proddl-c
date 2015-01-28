1. Setting up the environment
    a) download and install Azure SDK 1.8
    b) install Visual Studio 2010
    c) install Azure Tools for Visual Studio version 1.8 
    d) download and install eclipse (http://www.eclipse.org/downloads/)
    e) run eclipse and set up workspace
    f) install plug-ins from Eclipse Marketplace (Help->Eclipse MarketplaceÉ): mercurial, maven
        - search for mercurial and install 'MercurialEclipse' (requires Mercurial version 2.x or higher, http://tortoisehg.bitbucket.org/download/index.html)
        - search for maven and install 'Maven Integration for Eclipse'
        - restart eclipse after each install
    g) check out PRODDL-C hg project from bitbucket (including 3 major sub-projects: proddl_bootstrap, proddl_main, soyatec_java_SDK)
        - https://bitbucket.org/andreyto/proddl-c
    h) for easy access to Azure storage area, see 'Azure Storage Explorer' (http://azurestorageexplorer.codeplex.com/)
    *) Currently, Azure plugin for eclipse only supports Windows systems.
    
   
    1.1 soyatec_java_SDK - Java API for Microsoft Azure
        a) register Java API and library for Azure to local Maven repository
            - 'cpdetector_1.0.6.jar' & 'org.soyatec.windows.azure.java_2.0.2v20110627-1009.jar' located under 'soyatec_java_SDK/org.soyatec.windows.azure.lib/resource/azuresdk/'
            - mvn install:install-file -DgroupId=soyatec -DartifactId=azure-jdk -Dversion=2.0.2 -Dpackaging=jar -Dfile={jar file location}
            - mvn install:install-file -DgroupId=soyatec -DartifactId=cpdetector -Dversion=1.0.6 -Dpackaging=jar -Dfile={jar file location}
        
    1.2 Java API by Microsoft
        *) This module has been added to the main project, but it is not being used since it does not have Azure management API as of Feb 5 2013
        a) http://www.windowsazure.com/en-us/develop/java/
        b) https://github.com/WindowsAzure/azure-sdk-for-java
        
    1.3 proddl_main - main application in Java
        a) import 'proddl_main' project into eclipse by 'import->Maven->Existing Maven Project'
        b) rebuild maven repository in order to have local dependencies properly injected
            - use local installation of Maven rather than Eclipse embedded one in case of having missing dependency problem
            - Window > Preferences > Maven > Installation > Add 
        
    1.4 proddl_bootstrap - loader application for proddl_main in C#
        a) import the solution to VS
            - File > Open > Project/Solution > search and select 'proddl_bootstrap.sln' file from project checkout
        *) troubleshoot with missing reference for SevenZipSharp in CommonTool
            - remove 'SevenZipSharp' reference under CommonTool>References (it should have warning indicator on it)
            - right-click "Add Reference...">Browse>select "SevenZipSharp.dll" under 'proddl_bootstrap/CommonTool/tools'
            
    1.5 create X.509 v3 certificate for cloud role management
        - http://msdn.microsoft.com/en-us/library/windowsazure/gg432987.aspx
        - (Using IIS in Windows system) http://laultman.wordpress.com/2011/03/29/azure-deploying-x-509-certificates---visual-studio-2010/
        a) with VS
            - project > Publish > Sign in > choose your subscription > <Manage..> > New...
                + create or select existing certificate for authentication: VS will create a certificate for use
                + copy and paste service subscription ID to VS
                + you may change name of the subscription to any friendly names in VS
        
    1.6 Microsoft Azure Portal
        *) OLD PORTAL : https://windows.azure.com/default.aspx, NEW(Windows 8 themes) : https://manage.windowsazure.com
        a) create storage service
            - +NEW > DATA SERVICES > STORAGE > QUICK CREATE
        b) create cloud service
            - +NEW > COMPUTE > CLOUD SERVICE > QUICK OR CUSTOM CREATE            
        d) upload certificate to Azure cloud service
            d-1) get absolute path to certificate on local machine
                - to obtain the path within VS
                    + in solution explorer, proddl_bootstrap > Publish
                    + Sign In > <Manage...> in dropdown > select sign in information created in section 1.5 > Edit
                    + click 'Copy the full path' of step 2 in 'Edit Subscription' pop up
            d-2) (see Remarks r-3) go to Azure portal Settings > Management Certificates > UPLOAD
            
2. Deploy application to Azure service
    2.1 using eclipse and VS from project files
        2.1.1 proddl_main
            a) run install target of proddl_main
                - proddl_main pom.xml->Run As->Maven install
                - inspect for any errors during install
            b) make sure 'proddl_core-1.0.jar' is installed to proddl_bootstrap project under 'proddl_bootstrap/CommonTool/tools'
            
        2.1.2 proddl_boostrap
            a) In Solution explorer, mouse-right click on proddl_bootstrap->Publish
            b) Publish Azure Publish Application windows
                - Sign in
                    select the certificate created in section 1.5
                - Settings
                    + Cloud Service will be automatically loaded
                    + choose Environment: Production or Staging
                    + Build Configuration: Release or Debug
                    + enable remote desktop for cloud instance
                        - check 'Enable Remote Desktop for all roles'
                            + put user name and password
                            + set expiration date
                        - remote desktop credential will be imported to configuration file automatically by VS
                - Summary
                    + Target profile
                        - Empty configuration is selected as default
                
    2.2 using Azure application package and empty configuration file
        a) get application .cspkg file and configuration file .cscfg
        b) go to Azure portal > select available cloud service > Dashboard > choose environment > Upload
        c) put deployment name in the text box
        d) search .cspkg and .cscfg files
        e) check "Deploy even if one or more roles contain a single instasnce"
        f) submit

3. Configure settings after deploying the application to Azure service
    3.1 download empty configuration file from Azure portal
        a) select cloud service
        b) select CONFIGURE from top menu
        c) click DOWNLOAD at the bottom of CONFIGURE page
    3.2 edit downloaded empty configuration (ServiceConfiguration.cscfg.xml)
        3.2.1 common settings for both roles
            - StorageConnectionString for both PRODDLJobRunner and PRODDLMaster (see Remarks r-1)
            - DeploymentName: this can be empty
            - ConfigurationFinalized: 0 or 1, cloud node will sleep indefinitely unless this value is 1
        3.2.2 PRODDLMaster
            - SubscriptionId: it can be obtained through Azure portal
            - VHDName: virtual hard disk name to be stored in blob storage
            - VHDSize: size of VHD in Megabytes
        3.2.3 change number of startup roles by changing values for 'Instances'
    3.3 upload the updated configuration file using Azure portal site
        - check "Apply the configuration even if one or more roles contain a single instance.", if there is only one instance of each node kind
        

4. Submit 'cert' job to create certificates for java application (see Remarks r-5)
    4.1 create windows certificate (http://www.windowsazure4j.org/learn/labs/Management/index.html, see Remarks r-1)
    4.2 upload .cer and .pfx to the service
        a) $ curl {address}/pdl/r/file/upload -u admin:pdlAdmin -F file={path to .cer} -X POST
        b) $ curl {address}/pdl/r/file/upload -u admin:pdlAdmin -F file={path to .pfx} -X POST
        c) save file IDs that are returned in json format (ex. {"id":"bc8b6458-3698-0c1d-ff34-1004ce377f62"})
    4.3 submit 'cert' job
        curl {address}/pdl/r/job/cert -u admin:pdlAdmin -d '{"pfx_fid":"{pfx fileId}", "cer_fid":"{cer fileId}", "c_password":"{certificate password"}' -H "Content-Type: application/json" -X POST

5. Upload tools or libraries (see Remarks r-5)
    5.1 upload files to the service through the REST API
        a) $ curl {address}/pdl/r/file/upload/?type=tool -u admin:pdlAdmin -F file={path to .cer} -X POST

            
5. Howto create/run unit test
    5.1 class level test
        5.1.1 add proddl.properties to 'proddl_cloud/src/main/resources/' and 'proddl_web/src/test/resources'
            a) proddl.properties should at least have key value pairs for:
                DeploymentName={deployment name}
                DeploymentId={deployment id for current cloud service}
                SubscriptionId={subscription id for cloud service}
                StoragePath={local storage area for general use}
                DataStorePath={local storage area for files and tasks}
                StorageAccountName={storage space name}
                StorageAccountPkey={primary access key}
                CloudRoleWorkerName=PRODDLJobRunner
                WebserverPort={port number for jetty}
        5.1.2 create test classes using annotations from junit 4 under '/src/test/' of each module where unit testing is needed
        5.1.3 run 'test' target in proddl_main (all tests with @Test will run except methods or classes with @Ignore annotation)
    5.2 jetty web application test
        5.2.1 create properties file described in 5.1.1.a into 'proddl_web/src/test/resources'
        5.2.2 create Run configuration in IDE with Maven jetty run target
            *) install proddl_cloud at 'Before build' for maven:jetty run
    5.3 local test for java application
        5.3.1 locate proddl_core-1.0.jar on a local system
        5.3.2 create .properties files for master and worker in difference directories (..\master, ..\worker)
        5.3.3 open 2 cmd windows
        5.3.4 run command for each role
            > java -cp {directory path to .properties file};{path to proddl_core-1.0.jar} pdl.operator.ServiceOperator
            

<Remarks>
r-1) ** Connection string for PRODDLJobRunner must use secured connection ('HTTPS') while PRODDLMaster must useunsecured protocol ('HTTP') in order to properly mount a cloud drive.
r-2) Subscription ID for Azure instance account has to be present in configuration file of PRODDLMaster
r-3) manual certificate upload process can be skipped if you are using VS for application deployment
r-4) In case of downtime of the web page, reference 'CreatingJavaCertificate.html' under source area
r-5) Admin users ONLY
