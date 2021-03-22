# CA MAT Performance Benchmarking by Broadcom


This plugin helps you integrate a performance testing stage in your CI/CD pipeline for proactive issue detection and analysis of mainframe applications.   


Table of Contents
=================
* [Summary](#summary)
* [Prerequisites](#prerequisites)
* [Supported Systems](#supported-systems)
* [Installing](#installing)
  * [Automated Component Installation](#automated-component-installation)
  * [Component Compatibility](#component-compatibility)
* [Configure the Plugin](#configure-the-plugin)
  * [Define the CA MAT Analyze Profile](#define-the-ca-mat-analyze-profile)
  * [Define the CA MAT Detect Profile](#define-the-ca-mat-detect-profile)
  * [Define the z/OSMF Profile](#define-the-zosmf-profile)
  * [Define the Endevor Profile](#define-the-ca-endevor-profile)
  * [Define the Monitoring Scope](#define-the-monitoring-scope)
* [Configure Email Notifications](#configure-email-notifications)
* [Using the Plugin](#using-the-plugin)
  * [Include Performance Benchmarking in the Pipeline](#include-performance-benchmarking-in-the-pipeline)
    * [Define the Compilation Step Details](#define-the-compilation-step-details)
    * [Specify the Job for Performance Benchmarking](#specify-the-job-for-performance-benchmarking)
    * [Define Recipients of Performance Benchmarking Reports](#define-recipients-of-performance-benchmarking-reports)
    * [Pipeline Script](#pipeline-script)
  * [Execute the Pipeline](#execute-the-pipeline)
  * [Review Performance Benchmarking Results](#review-performance-benchmarking-results)
    * [Pipeline Build Log](#pipeline-build-log)
    * [Performance Benchmarking Reports](#performance-benchmarking-reports)
* [References](#references)


# Summary
CA MAT Performance Benchmarking by Broadcom helps you integrate a performance testing stage in your CI/CD pipeline for proactive issue detection and analysis of mainframe applications. The plugin allows you to implement the Performance on Commit strategy, which is aimed at proactive discovery of performance issues in mainframe applications at early development and testing stages. The CA MAT Performance Benchmarking plugin by Broadcom employs the performance measurement and analytical tools powered by CA Mainframe Application Tuner (CA MAT) to provide performance benchmarking of your application in the build stage of your CI/CD pipeline. Include the performance benchmarking step after the compilation step in your CI/CD pipeline to automatically check your mainframe application performance on every code commit and immediately obtain the performance benchmarking results.  

If you use CA Endevor® for managing your source code, the CA MAT Performance Benchmarking plugin by Broadcom can ensure the compilation of the updated source code directly from a specific sandbox that is associated with your application.  

For a complete overview of the architecture of mainframe-specific CI/CD pipelines, see *Performance Testing with CA MAT in DevOps CI/CD Pipelines* in the [CA MAT documentation](http://techdocs.broadcom.com/mat).

# Prerequisites
Before you install the CA MAT Performance Benchmarking plugin by Broadcom, verify that you meet the following prerequisites:  

- Jenkins is installed and configured on your machine.  
- The minimum Java version supported by Jenkins is installed on your Jenkins machine.  
- CA Mainframe Application Tuner (CA MAT) version 12.0.02 (GA) or later is installed.  
- CA MAT customization is successfully completed.  
- CA MAT database integration is enabled and configured.  
- CA MAT REST API is installed and configured.  
- PMA customization is successfully completed.  
- z/OSMF is installed and configured.  

Optionally, CA Endevor® SCM users can automate the compilation process of updated source code from the specific sandbox directly from the Jenkins pipeline. To leverage this automation, meet the following prerequisites:  

- CA Endevor® version 18.1 or later installed and configured.  
- CA Endevor® Web Services installed and configured.  

For detailed information, refer to the respective sections of the [CA Mainframe Application Tuner (CA MAT)](http://techdocs.broadcom.com/mat) and [CA Endevor®](http://techdocs.broadcom.com/endevor) documentation.  

# Supported Systems
This version of the plugin supports the following operating systems of your Jenkins machine:  
- Windows (All versions supported by Jenkins).  

Linux-based operating systems are likely to be supported but have not been tested.  

# Installing
Install the CA MAT Performance Benchmarking plugin by Broadcom using the Jenkins plugin management functionality. For more information, see [Managing Plugins](https://www.jenkins.io/doc/book/managing/plugins/) in the Jenkins documentation.
  
## Automated Component Installation
Along with the CA MAT Performance Benchmarking plugin by Broadcom, the plugin installation procedure automatically installs on your Jenkins machine the latest versions of the following components:  

- Zowe CLI  
- CA MAT Detect plug-in for Zowe CLI  
- CA MAT Analyze plug-in for Zowe CLI  
- CA Endevor SCM plug-in for Zowe CLI  

The above listed components are required for the operation of the CA MAT Performance Benchmarking plugin by Broadcom.

## Component Compatibility
If some of the required components are already installed on your Jenkins machine, verify that the current versions of the installed components comply with the minimum compatible versions that the CA MAT Performance Benchmarking plugin by Broadcom requires:  

- Zowe CLI - version 6.25.0  
- CA MAT Detect plug-in for Zowe CLI - version 1.0.1  
- CA MAT Analyze plug-in for Zowe CLI  - version 1.0.2  
- CA Endevor SCM plug-in for Zowe CLI  - version 5.7.2  

**Note**: If you have Zowe CLI already installed on your Jenkins machine, you need to configure the Jenkins machine to start the Jenkins service with the user that has access to the Zowe command line (usually, the admin). The setup depends upon the operating system of your Jenkins machine. For example, in Windows, go to the Services application, edit the properties of the Jenkins service, and define the user in the This account field. After you reassign a user, restart the Jenkins service.  

After the successful installation of the CA MAT Performance Benchmarking plugin by Broadcom, the **Performance Benchmarking** option appears in the main Jenkins menu. By clicking the Performance Benchmarking option, you access the Configure Performance Benchmarking page. When you enter the Performance Benchmarking page, the plugin re-verifies whether you have all the necessary components installed. 


# Configure the Plugin
Before you start using the CA MAT Performance Benchmarking plugin by Broadcom in your Jenkins pipelines, perform the initial configuration of the plugin:   

- Define the CA MAT Analyze profile  
- Define the CA MAT Detect profile  
- Define the z/OSMF profile  
- (For CA Endevor® users) Define the Endevor profile  
- Define the monitoring scope  

You configure the plugin settings in the Configure Performance Benchmarking window. To access the configuration window, click the **Performance Benchmarking** option in the main Jenkins menu. 

## Define the CA MAT Analyze Profile
The CA MAT Analyze profile enables the communication of the plugin with CA Mainframe Application Tuner (CA MAT) that is running on mainframe and ensures the analysis of the detected performance issue. To define the CA MAT Analyze profile, use the details of the CA MAT REST API server. You can create multiple profiles and switch between them by assigning one default profile.  

**Follow these steps:**   
1. In the Configure Performance Benchmarking window, click **Define the CA MAT Analyze Profile**.  
The **Define the CA MAT Analyze Profile** page opens. The page contains the list of all available CA MAT Analyze profiles.

2. In the **Entries** section, click **Add** and select **CA MAT Analyze Profile**.  
A new entry with the CA MAT Analyze profile fields appears.

3. Provide values for the following fields:  
   * **Profile Name**  
   Specify the name of your CA MAT Analyze profile that you create, for example, MATPROF1.  
   * **Protocol**  
   Select the protocol type that you defined for your CA MAT REST API server: http or https.  
   * **Host Name**  
   Specify the name or IP address that you defined for your CA MAT REST API server, for example, 127.0.0.0 or localhost. 
   * **Port Number**  
   Provide the port number that you defined for your CA MAT REST API server, for example, 8080.  
   *	**Username**  
   Specify your mainframe username.  
   *	**Password**  
   Specify your mainframe username.  
   *	**Zowe Discoverable**  
   Select this option only if you have the Zowe API Mediation Layer properties configured in your CA MAT REST API server settings.  
   *	**Default Profile**  
   Select this option to set the profile as default.

4. Click **Save** to apply all changes.  
A notification about the save and update process appears in the message section above all profile entries.

The CA MAT Analyze profile is defined. 

To update an existing profile, provide new values in the respective fields and click **Save**. To delete a profile, click **Delete**, then click **Save** to apply the changes.  

Ensure that you have one default CA MAT Analyze profile. If you delete a default profile or do not mark any profile as default, the plugin automatically assigns the first profile as the default one when you click save **Save**.

## Define the CA MAT Detect Profile
The CA MAT Detect profile enables the communication of the plugin with the Performance Management Assistant component (PMA) of CA MAT that is running on mainframe and ensures the automatic performance issue detection. To define the CA MAT Detect profile, use your PMA customization settings and your mainframe account details. You can create multiple profiles and switch between them by assigning one default profile.  

**Follow these steps:**   
1. In the Configure Performance Benchmarking window, click **Define the CA MAT Detect Profile**.  
The **Define the CA MAT Detect Profile** page opens. The page contains the list of all available CA MAT Detect profiles.

2. In the **Entries** section, click **Add** and select **CA MAT Detect Profile**.  
A new entry with the CA MAT Detect profile fields appears.

3. Provide values for the following fields:  
   * **Profile Name**  
   Specify the name of your CA MAT Detect profile that you create, for example, PMAPROF1.  
   *	**Job Account Number**  
   Specify your z/OS TSO/E account information. Only numbers are allowed. For example, 123456789.  
   *	**Job Class**  
   Specify your z/OS class information. Allowed values: alphanumeric characters (A-Z, 0-9). For example, A.  
   *	**Message Class**  
   Specify the MSGCLASS parameter value to be assigned to the output class in the job log. The specified MSGCLASS value is used in all JCLs that PMA runs while you execute the commands. Values: alphanumeric characters (A-Z, 0-9). For example, A.  
   Default: A.  
   *	**Load Library**  
   Specify PMA loadlib information. To provide the loadlib name, refer to your PMA installation details (*&HLQ*.CEETLOAD).  
   *	**PMA HLQ**  
   Specify the PMA high-level qualifier to access the KSDSALT, KSDSJOB, and KSDSEXC VSAM files to collect the necessary data. For example, PMA.V12.  
   *	**Default Profile**  
   Select this option to set the profile as default.

4. Click **Save** to apply all changes.  
A notification about the save and update process appears in the message section above all profile entries.

The CA MAT Detect profile is defined. 

To update an existing profile, provide new values in the respective fields and click **Save**. To delete a profile, click **Delete**, then click **Save** to apply the changes.  

Ensure that you have one default CA MAT Detect profile. If you delete a default profile or do not mark any profile as default, the plugin automatically assigns the first profile as the default one when you click **Save**.

## Define the z/OSMF Profile
The z/OSMF profile enables the communication of the plugin with your instance of the IBM z/OS Management Facility. To define the z/OSMF profile, use your z/OSMF server details. You can create multiple profiles and switch between them by assigning one default profile.  

**Follow these steps:**   
1. In the Configure Performance Benchmarking window, click **Define the z/OSMF Profile**.  
The **Define the CA MAT z/OSMF Profile** page opens. The page contains the list of all available z/OSMF profiles.

2. In the **Entries** section, click **Add** and select **z/OSMF Profile**.  
A new entry with the z/OSMF profile fields appears.

3. Provide values for the following fields:  
   * **Profile Name**  
   Specify the name of your z/OSMF profile that you create, for example, ZOSPROF1.  
   *	**Host Name**  
   Specify your z/OSMF server host name, for example, myhost.com.  
   *	**Port Number**  
   Specify your z/OSMF server port number, for example, 443.  
   Default: 443.  
   *	**Username**  
   Provide your mainframe (z/OSMF) user name, which can be the same as your TSO login.  
   *	**Password**  
   Provide your mainframe (z/OSMF) user name, which can be the same as your TSO password.  
   *	**Reject Unauthorized**  
   Select this option to reject self-signed certificates.  
   *	**Base Path**  
   Specify the base path for your API mediation layer instance. Specify this option to prepend the base path to all z/OSMF resources when making REST requests.  
   Leave blank if you are not using an API mediation layer.  
   *	**Encoding**  
  (Optional) Specify encoding for download and upload of z/OS data sets and USS files. If not specified, the default encoding is 1047.  
   *	**Response Timeout**  
   (Optional) Specify the maximum amount of time in seconds the z/OSMF Files TSO servlet should run before returning a response. Any request exceeding this amount of time will be terminated and return an error. Allowed values: 5 - 600.  
   *	**Default Profile**  
   Select this option to set the profile as default.  

4. Click **Save** to apply all changes.  
A notification about the save and update process appears in the message section above all profile entries.  

The z/OSMF profile is defined. 

To update an existing profile, provide new values in the respective fields and click **Save**. To delete a profile, click **Delete**, then click **Save** to apply the changes.  

Ensure that you have one default z/OSMF profile. If you delete a default profile or do not mark any profile as default, the plugin automatically assigns the first profile as the default one when you click **Save**.  

## Define the CA Endevor Profile
The CA Endevor profile enables the communication of the plugin with CA Endevor® and enables you to remotely interact with your source code. Define this profile if you use CA Endevor® for managing your source code. To define the CA Endevor profile, use the details of your CA Endevor Web services session and credentials. You can create multiple profiles and switch between them by assigning one default profile.  

**Follow these steps:**   
1. In the Configure Performance Benchmarking window, click **Define the CA Endevor Profile**.  
The **Define the CA Endevor Profile** page opens. The page contains the list of all available CA Endevor profiles.

2. In the **Entries** section, click **Add** and select **CA Endevor Profile**.  
A new entry with the CA Endevor profile fields appears.

3. Provide values for the following fields:  
   * **Profile Name**  
   Specify the name of your CA Endevor profile that you create, for example, ENDVPROF.  
   * **Protocol**  
   Select the protocol type that you defined for your CA Endevor® REST API server: http or https.  
   * **Host Name**  
   Specify the host name for your CA Endevor® Web services server, for example, endevorhost.com. 
   * **Port Number**  
   Specify your CA Endevor® Web services server port number, for example, 8080.  
   *	**Username**  
   Specify your username for Endevor session.  
   *	**Password**  
   Specify the password for your Endevor session.  
   *	**Base Path**  
   Specify the base path for connecting to CA Endevor® REST API.  
   Default: EndevorService/rest
   *	**Reject Unauthorized**  
   Select this option to reject self-signed certificates.  
   *	**Default Profile**  
   Select this option to set the profile as default.

4. Click **Save** to apply all changes.  
A notification about the save and update process appears in the message section above all profile entries.

The CA Endevor profile is defined. 

To update an existing profile, provide new values in the respective fields and click **Save**. To delete a profile, click **Delete**, then click **Save** to apply the changes.  

Ensure that you have one default CA Endevor profile. If you delete a default profile or do not mark any profile as default, the plugin automatically assigns the first profile as the default one when you click save **Save**.


## Define the Monitoring Scope
The PMA component of CA MAT performs a constant monitoring of all jobs on mainframe. To narrow down the monitoring activity only to the group of jobs that you want to focus on, you must specify the monitoring scope.  

**Note:** Define the monitoring scope after you have successfully defined the communication profiles.

To define the monitoring scope, you include specific jobs or groups of jobs and exclude specific programs or groups of programs. You can create multiple entries for job inclusions and program exclusions. The resulting scope becomes a subject of permanent performance monitoring by the PMA component of CA MAT that is running on mainframe. From the resulting monitoring scope, you further include particular jobs for performance benchmarking while configuring specific pipelines and analyze the performance of the jobs under test.

**Follow these steps:**   
1. In the Configure Performance Benchmarking window, click **Define the Monitoring Scope**.  
The **Define the Monitoring Scope** page opens.  
**Note:** When you access or update the Define the Monitoring Scope page, the plugin communicates with mainframe to retrieve the current monitoring scope details. The retrieval process may take time depending on your connection and mainframe response speed. The progress bar above the **Entries** section indicates the current status of data retrieval. Please wait for the process to complete before you update the monitoring scope details.

2. In the **Entries** section, click **Add**, and select one of the scope definition entry types:  
*	**Job Inclusion**  
   Specify the mandatory parameters of the job that you want to specifically include in the monitoring scope:  
      * **Job Name**  
      Provide the name of the job that you want to include in the monitoring scope, for example, ABCJOB. The maximum length is 8 characters.  
      You can use wildcard characters * and _ to define an inclusion pattern for the job names.  
      Examples:  
        -	Specify **TEST*** to include all job names that start with TEST.  
        -	Specify **\_TEST** to include all job names that end with TEST.  
        -	Specify **\_TEST*** to include all job names that contain TEST.    
              
    Click **Advanced** to optionally specify additional parameters of the included job:  
      *	**Step Name**  
      Specify the step name of the job.  
      The maximum length is 8 characters. The Step Name field supports the same use of wildcards as the Job name.  
      *	**Proc Step**  
      Specify the procedure step of the job.  
      The maximum length is 8 characters. The Proc Step field supports the same use of wildcards as the Job name.  
      *	**Description**  
      Provide a description of the included job, for example, *Test job included*.  
      The maximum length is 24 characters.  
      The description that you provide appears in the inclusion entry in PMA.      
   
*	**Program Exclusion**  
   Specify the mandatory parameters of the program that you want to specifically exclude from the monitoring scope:  
      *	**Program Name**  
      Provide the name of the program that you want to exclude from the monitoring scope, for example, ABCPGM. The maximum length is 8 characters.  
      You can use wildcard characters * and _ to define an exclusion pattern for the program names.  
      Examples:  
          - Specify **TEST*** to exclude all program names that start with TEST.  
          -	Specify **\_TEST** to exclude all program names that end with TEST.  
          -	Specify **\_TEST*** to exclude all program names that contain TEST.  
      
    Click **Advanced** to specify optional parameters of the excluded program:  
      * **Description**  
      Provide a description of the excluded program, for example, *Backup program excluded*. The maximum length is 24 characters.
The description that you provide appears in the exclusion entry in PMA.   

3. Confirm the changes using one of the following options:  
   * Click **Save** to save all changes and exit.  
   *	Click **Apply** to save all changes and stay on the page.  

You have defined the general scope of jobs to be monitored. You can define multiple entries for included jobs and excluded programs.  

You can modify the monitoring scope at any time. To update an existing entry, provide new values in the respective fields and save the page. To delete an entry, click **Delete**, then save the page. 

From the resulting scope, you add the specific job that you want to test for performance during the configuration of a particular pipeline. For more information, see [Using the Plugin](#using-the-plugin).  


# Configure Email Notifications
The CA MAT Performance Benchmarking plugin by Broadcom enables you to configure email notifications for the following user types:  
* **Pipeline user**  
Receives email notifications with the performance analysis report for the tested job after a successful pipeline execution.    
* **Jenkins administrator**  
  *	Manages the emailing facility using the SMTP server settings.  
  *	If a pipeline user email is provided in a pipeline, Jenkins administrator automatically receives the performance benchmarking email report in a copy.


To enable the emailing facility for the CA MAT Performance Benchmarking plugin by Broadcom, configure the details of your SMTP server and define an administrator email.  

**Follow these steps:**  
1. In the main Jenkins menu, click **Manage Jenkins**, then click **Configure system**.  
The Jenkins system configuration window opens.  

2. Navigate to the **Configure email notifications for CA MAT Performance Benchmarking by Broadcom** section and provide values for the following fields:  
   * **Administrator Email**  
   Specify the email address of the Jenkins administrator.  
   You can provide several comma-separated email addresses.  
   *	**SMTP Server**  
   Specify SMTP server to be used for sending email notifications, for example, smtp.example.com.  
   *	**SMTP Port**  
   Specify the port number on the SMTP server, for example, 25.  
   *	**SMTP Authentication**  
   (Optional) Select this option to define SMTP authentication for the server.  
   If selected, specify values for the following additional fields that appear:  
     *	**Username**  
     Provide the username for your SMTP server.  
     *	**Password**  
     Provide the password for your SMTP server.  
  
3. Click **Save** to apply all changes.
           
You have configured the CA MAT Performance Benchmarking plugin by Broadcom to send email notifications and defined the administrator email address.  
  
To configure sending performance benchmarking reports to specific pipeline users, you define the recipient emails in the **Post-build Actions** stage while configuring a particular pipeline. For more information, see [Using the Plugin](#using-the-plugin).
 

# Using the Plugin  
The CA MAT Performance Benchmarking plugin by Broadcom enables you to integrate a comprehensive performance analysis of your mainframe job in the build stage of your Jenkins pipeline.  

To integrate performance benchmarking using the Jenkins UI, you create and configure a **Freestyle** pipeline. Alternatively, you can use the **Pipeline** type to manually insert a script that performs the same performance benchmarking functionality.  

The analysis starts when you commit a source code change in your SCM, which triggers the Jenkins pipeline. Within the build stage of the pipeline, you specify the compilation step details that are associated with the SCM where you update your source code, and specify the mainframe job for performance benchmarking. During the pipeline execution, the CA MAT Performance Benchmarking plugin by Broadcom measures the performance of your updated mainframe job, analyzes the results, and sends email notifications with the performance benchmarking reports to the intended recipients.  

You get the performance analysis data of the updated job right after the successful pipeline execution, which helps you quickly detect whether your latest code changes have affected the normal performance level of the tested job.  


## Include Performance Benchmarking in the Pipeline
You include the performance benchmarking tools in the Build stage of a Jenkins pipeline. CA MAT Performance Benchmarking by Broadcom monitors the performance of your changed application after the compilation of your updated source code. To perform the compilation step prior to performance benchmarking, you can include in the pipeline a compiler of your choice that is specific to your source code and SCM, or use the built-in Autogen step that compiles the job from a specific sandbox of CA Endevor®.  

The following sequence of the pipeline steps within the Build stage ensures the proper performance benchmarking of your application upon each code change:  
1. **Compilation**  
This step ensures the compilation of executables from the latest version of the source code in your SCM.  
You have the following options to include the latest executable in your pipeline:  
   *	Use a compiler of your choice that is integrated with your SCM.  
   To include the compilation step in your Jenkins pipeline, refer to the documentation of the chosen compiler.  
   *	Add the built-in **Autogen** step that enables you to use the source code from your CA Endevor® sandbox.  
   If you use CA Endevor®, you can include in the Build stage the Autogen step that is provided by the CA MAT Performance Benchmarking plugin by Broadcom. The Autogen step collects the source code from the specified CA Endevor® sandbox and compiles the job from this source after each update.  
    
2. **Performance Benchmarking**  
In this step, the CA MAT Performance Benchmarking plugin by Broadcom runs the test job on mainframe, measures the performance KPIs of the executable compiled from the changed source code, and evaluates the results.  

To integrate performance benchmarking in your Jenkins pipeline using the CA MAT Performance Benchmarking plugin by Broadcom, create a new Jenkins pipeline or edit an existing pipeline and perform the following actions:  

1. Define the compilation step details  
2. Specify the job for performance benchmarking  
3. Define the recipients of the performance benchmarking reports


### Define the Compilation Step Details
You define the compilation step details in the Build stage of your Freestyle Jenkins pipeline. This step compiles the executable from the updated source code in your SCM that is associated with the job under performance analysis. 

The configuration of the compilation step depends on the SCM and the compiler that you use. Refer to the documentation of your specific compiler and SCM on how to include the compilation step in the Build stage of a Jenkins pipeline.

For users of CA Endevor®, the CA MAT Performance Benchmarking plugin by Broadcom provides the Autogen step integrated in the Jenkins pipeline, which automates the compilation of the updated Endevor elements. The communication of Jenkins with CA Endevor® runs through the Endevor plugin for Zowe CLI, which is automatically installed along with CA MAT Performance Benchmarking by Broadcom. To set up the compilation from CA Endevor®, you need to provide the details of the CA Endevor® sandbox that is associated with the job under performance test. When you update the source code elements in CA Endevor®, the autogeneration process starts from the specified sandbox. 

**Follow these steps:**
1. In the pipeline configuration window, navigate to the **Build** stage.  

2. Click **Add build step** and select the **Autoge** step.  
The Autogen parameter section opens.  
Provide values for the following parameters:  
   *	**Element**  
   Specify the name of the element in your CA Endevor® sandbox that you want to include in the autogeneration process, for example, RUNCOB01. The maximum length is 8 characters.  
   You can specify several comma-separated elements within the sandbox, for example, RUNCOB01, RUNCOB02.  
   You can use wildcard characters * and % to define name patterns for the element names.  
   Examples:  
          - Specify * to include all elements from the sandbox in the autogeneration process.  
          - Specify **RUNCOB*** to include all element names that start with RUNCOB.  
          - Specify **%%%COB** to include element names that contain the substring COB starting after position three.        
   *	**Environment**  
   Provide the environment for the CA Endevor® sandbox that you want to include in the autogeneration process, for example, DEV.  
   *	**System**  
   Provide the system name for the environment in your CA Endevor® sandbox that you want to include in the autogeneration process, for example, APCTTC0.  
   *	**Subsystem**  
   Provide the subsystem name for the environment in your CA Endevor® sandbox that you want to include in the autogeneration process, for example, TESTCICD.  
   *	**Stage**  
   Specify the environment stage in your CA Endevor® that you want to include in the autogeneration process, for example, 1.  
   *	**Instance**  
   Specify the instance name of the Endevor environment that is associated with CA Endevor® Web services, for example, WEBSMFNE. For more information, refer to the [CA Endevor®](http://techdocs.broadcom.com/endevor) documentation.  
  
3. (Optional) Select option **Override signout** to work with elements that might be currently used by another user.  
Select the Override signout option only if you have the permission to override the signout of another user. 
    
4. Confirm the Autogen step configuration using one of the following options:  
   *	Click **Save** to save all changes and exit.  
   *	Click **Apply** to save all changes and stay on the page.  
 
You have defined the Autogen step within the Build stage of your Jenkins pipeline.  

With the Autogen step configured, the pipeline triggers the autogeneration process upon each execution, compiles the executable from the selected elements, and prepares the changed application for the Performance benchmarking step that follows.

  **Note**: For certain source code configurations, you might need to define more than one Autogen step to include elements from different sandboxes of your CA Endevor®. For example, when the job under test is located within sandbox A and executes a program that resides in sandbox B, you might need to perform the autogen process for both sandboxes A and B.  


### Specify the Job for Performance Benchmarking 
You specify the job for the Performance benchmarking step in the Build stage of a Jenkins pipeline. The Performance benchmarking step must follow the compilation step. In the Performance benchmarking step, you define the specific job to be tested that has been compiled in the preceding step. The job must fall within the general monitoring scope that you defined in the configuration of the CA MAT Performance Benchmarking plugin by Broadcom. Once the job executable is generated in the compilation step, the plugin submits the specified tested job on mainframe and runs the performance benchmarking using the PMA component of CA MAT.  

  **Note**: Before you specify the job for the Performance benchmarking step, verify that you have configured the preceding compilation step for this job within the Build stage. The proper configuration of the compilation and performance benchmarking steps ensures that you obtain the analysis results related to the latest source code change.  

**Follow these steps:**  
1. In the pipeline configuration window, navigate to the **Build** stage.  

2. Click **Add build step** and select the **Performance Benchmarking** step.  
The Performance Benchmarking section opens.  

3. In the **Test Job** field, specify the mainframe data set name that contains the job for performance analysis, for example, TEST.POC.JCLLIB(TESTCICD).  
The maximum length is 44 characters.  

4. Confirm the Performance benchmarking step configuration using one of the following options:  
   *	Click **Save** to save all changes and exit.  
   *	Click **Apply** to save all changes and stay on the page.  
 
You have defined the tested job for the Performance benchmarking step within the Build stage of your Jenkins pipeline.  


### Define Recipients of Performance Benchmarking Reports
Specify the emails of the pipeline users intended to receive email notifications with the performance benchmarking report for the tested job.  

  **Note:** To use the emailing option, ensure that you have configured the SMTP server details in the *Configure email notifications for CA MAT Performance Benchmarking by Broadcom* section of the Jenkins system configuration.  

**Follow these steps:**  
1. In the pipeline configuration window, navigate to the **Post-build Actions** stage.  

2. Click **Add post-build action** and select the **Performance Benchmarking Report** option.  
The Performance Benchmarking Report section opens.  

3. In the **Recipients** field, provide the email address of the pipeline users to receive performance benchmarking reports.  
You can provide several comma-separated email addresses.  

4. Confirm the recipient list using one of the following options:  
   * Click **Save** to save all changes and exit.  
   *	Click **Apply** to save all changes and stay on the page.  
 
You have defined the recipients of emails with performance benchmarking reports for the tested job. If the tested job is included in the monitoring scope, the plugin sends a performance analysis report after each successful execution of the Performance benchmarking step.  

### Pipeline Script
You can configure your Jenkins pipeline with another project type called **Pipeline**. When you use the Pipeline type, you do not define the steps of the build stage through Jenkins UI. Instead, you manually provide the pipeline script using the predefined classes of the CA MAT Performance Benchmarking functionality that enable you to configure the same steps:  
- Compilation  
- Performance benchmarking  
- Sending email notifications  

The following example of the script employs the predefined classes to perform the CA MAT Performance Benchmarking functionality on the job TEST.POC.JCLLIB(TESTCICD) compiling all elements that reside in the TESTCICD CA Endevor® sandbox, and sends email notifications to the specified recipient and the Jenkins administrator in copy:  

```
pipeline {
    agent {
		label 'master'
	}
	stages {
		stage('Autogen compilation job') {
            steps {
                step([$class: 'Autogen', element: '*', environment: 'dev', 
                system: 'APCTTC0', subsystem: 'TESTCICD', stage: '1',
                instance: 'WEBSMFNE', signout: true])
            }
	    }
	    stage('Performance Benchmarking') {
            steps {
                step([$class: 'PerformanceAnalysisBuilder', 
                testjob: 'TEST.POC.JCLLIB(TESTCICD)'])
            }
	    }
    }
    post {
		// Send email notification
		always {
		        step([$class: 'EmailPostBuildAction',
		        recipients: 'user1@example.com'])
		}
    }
}
```  


## Execute the Pipeline
Trigger the pipeline execution using either of the following options:  

* **Automated execution**  
Change the source code of the tested job in your SCM or in the IDE synchronized with the SCM.  
For automated execution, you need to configure a mainframe-specific pipeline that connects the local developer IDE, SCM, and Jenkins into a common automated solution. Refer to *Performance Testing with CA MAT in DevOps CI/CD Pipelines* in the [CA MAT documentation](http://techdocs.broadcom.com/mat) for the details of the complete architecture of mainframe-specific CI/CD pipelines.  
 
* **Manual execution**  
In the pipeline details menu, click **Build Now**.  

If the pipeline execution results in an error, check the Jenkins log for details.  

With the compilation and performance benchmarking steps properly configured, upon each pipeline execution the CA MAT Performance Benchmarking plugin by Broadcom automatically performs the following actions:  

1. **Compile the source code**  
The plugin triggers the compiler that you specified, or uses the CA Endevor® sandbox details that you provided for the Autogen step.  

2. **Run the job under test**  
The plugin submits the compiled tested job to run on mainframe and verifies whether the job falls within the defined monitoring scope.  

3. **Get performance KPIs**  
The plugin obtains the performance metric values of the tested job from mainframe.  

4. **Check for performance alerts**  
The plugin obtains from mainframe the information whether the current run of the tested job has raised a performance alert.  

5. **(If alerts found) Measure the alerted application**  
CA Mainframe Application Tuner is automatically triggered to measure the alerted job on mainframe.  

6. **Send an email with performance analysis results for your review**  
The plugin evaluates and summarizes the obtained results and sends you an email with the performance benchmarking report.  


## Review Performance Benchmarking Results
After the pipeline execution successfully completes, you can review the performance analysis metrics that the plugin measured and analyzed during the job run. The plugin evaluates the performance of mainframe jobs based on the following KPIs:  

*	CPU time  
*	Service Units (SRVU)  
*	Execute Channel Program count (EXCP)  
*	Elapsed time

During each job run, the plugin measures the current values of the key performance metrics of the job, compares the measurement results of the current run with the previous and average values (if available), calculates the deviations of each parameter, and analyzes the results. If the deviation of any metric exceeds 75% 2 times in a row, the plugin creates a performance alert. The performance alert triggers an immediate measurement of the job by CA Mainframe Application Tuner, which provides you with details for a quick, yet comprehensive analysis, and enables you to easily identify the cause of the performance degradation. 

You can access the performance analysis results in the following locations:  
* Pipeline build log  
* Performance benchmarking reports


### Pipeline Build Log
You can view a simplified performance benchmarking report for a job in the Jenkins pipeline build log. To access the log, click the build number in the **Build History** section of the pipeline, then click **Console Output**.  

The log displays the job details and measurement results for the key performance metrics as follows:

```
Running job TEST.POC.JCLLIB(TESTCICD)...
Job TESTCICD completed
Running performance analysis...
Performance analysis is finished
 ALERT -> THE CURRENT CPU TIME IS HIGHER THAN THE AVERAGE CPU TIME
 ALERT -> THE CURRENT EXCP COUNT IS HIGHER THAN THE AVERAGE EXCP COUNT
 THE CHANGE IN PERCENTAGE BETWEEN THE CURRENT AND THE PREVIOUS RUN VALUES ARE:- ELAPSED:+383% CPU:+316% EXCP:+326% SRVU:+313%
 THE CHANGE IN PERCENTAGE BETWEEN THE CURRENT RUN AND THE AVERAGE VALUES ARE:- ELAPSED: -48% CPU: +25% EXCP: +4% SRVU: -37% 
 
 JOBNAME  STEPNAME PROCSTEP PGM/JCL  PGM/APPL 
 TESTCICD  PMATEST   PMATEST    PMASAMP
         

          VALID AVERAGE                         A V E R A G E   V A L U E S 
           CALCULATIONS               >--------------------------------------------< 
                   18                  00:01:44.91 00:00:00.20      49053      25308 
   -------------------- ---- --------  ----------- ----------- ---------- ----------
    DATE        TIME    COND              ELAPSED         CPU
    YYYY.MM.DD HH:MM:SS CODE SYSTEM    HH:MM:SS.HH HH:MM:SS.HH    EXCP       SRVU
    ---------- -------- ---- ------    ----------- ----------- ---------- ----------
    2021.01.15 03:45:31    0 CA31      00:00:54.00 00:00:00.25      51218      15790
    2021.01.15 03:43:31    0 CA31      00:00:11.79 00:00:00.06      12062       3865
    
    
Running alert analysis...
Alert analysis is finished
No ALERTS generated today, "2021-01-15", for jobname TESTCICD

```  

In this example, the plugin compares the KPIs of the current run of job TESTCICD with the previous run and with the average values that are based on 18 previous calculations. The current run of the job reveals significant deviations of all KPIs from the previous run, and less than 75% deviations of CPU time and EXCP count as compared to the average values. No performance alerts have been raised.  

The output log also provides information about all performance alerts raised today for the tested job.  

### Performance Benchmarking Reports
With the configured SMTP server and with the recipient emails for performance benchmarking reports defined for the specific pipeline, the plugin sends email notifications with performance analysis results of the tested job after a successful execution of the Performance benchmarking step.  

The email notification contains the subject **PMA Analyser Report** and the **Overall test status** heading within the message body followed by one of the possible results. Depending on the performance benchmarking results, you can receive 4 types of email notifications:  

* **Initial Test**  
The report indicates the first performance test of the job. The job performance assessment has been made based on 1 calculation. The email contains only the job details and the performance metrics of the current run that are identical to the average values. No user action is expected.  
  
* **Within Normal Range**  
The report indicates that the plugin has compared the current performance metrics of the tested job with the previous and average results, and the deviation does not exceed 75%. The email contains the deviation percentage of the KPIs (CPU time, elapsed time, EXCP, SRVU), the performance metrics from the current and previous runs, and the average values. No user action is expected.
   
 * **Warning**  
The report indicates that the plugin has detected abnormally high values of one or more key performance metrics, with the deviation exceeding 75%, but no performance alert has been raised so far. The report specifically indicates the KPIs that cause the warning. The email contains the deviation percentage of the KPIs (CPU time, elapsed time, EXCP, SRVU), the performance metrics from the current and previous runs, and the average values.  
The notification also explains the possible reasons of the accidental performance issue. For example, the deviation might have been caused by the overall system performance. The email prompts you to measure your job with the CA MAT Analyze plug-in for Zowe CLI, and then rerun the pipeline.  
  
* **Alert**  
The report indicates that the plugin has raised a performance alert for the tested job, and CA MAT has immediately measured the job. This situation means that the latest code changes have introduced a significant issue to your job performance. The report specifically indicates the KPIs that cause the alert. The email contains the deviation percentage of the KPIs (CPU time, elapsed time, EXCP, SRVU), the performance metrics from the current and previous runs, and the average values.  
The notification provides examples of commands for the CA MAT Analyze plug-in for Zowe CLI that you can use in your command line interface or IDE terminal to analyze the source code and address the performance degradation. The command samples already contain all the necessary parameters. You can just copy a command from the email and paste it in your command line for execution.

   
# References
* [CA Mainframe Application Tuner (CA MAT) documentation](http://techdocs.broadcom.com/mat)
