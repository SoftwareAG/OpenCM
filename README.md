# OpenCM
OpenCM is a Configuration Management Repository for webMethods components and is primarily an operational/runtime governance tool of DBP wM components. It can act as a "source of truth" (documentation repo) and provides the ability to audit & compare configuration property information in order to detect discrepancies. In addition, it is also used as an operations utility for quick and easy retrieval of property information through its user interface.   
 
## Description
OpenCM extracts configuration property information from runtime environments and stores the information in a local file-based repository. On top of the collected property information, automatic/continuous auditing of property information can then be performed to detect inconsistencies between different installations. OpenCM comes with a user interface for easy navigation and visualization of property information.

![Alt text](/github_images/UI-InstallationNode.jpg?raw=true "OpenCM User Interface")

OpenCM is an Integration Server package that is centrally installed on a separate administration IS. Main external dependency to OpenCM is SoftwareAG Platform Manager (on remote installations) which are used to perform property extractions (retrievals).

There are two types of comparisons, each one providing a separate type of difference report:

1. 2-Node Audits - Report directly in OpenCM UI
![Alt text](/github_images/UI-Report-01.jpg?raw=true "OpenCM HTML Report")

2. Layered Audits - Excel Report
![Alt text](/github_images/UI-Report-02.jpg?raw=true "OpenCM Excel Report")

## Requirements

The project was developed and tested on the following installation:

1. Integration Server 9.9, 9.12, 10.0, 10.1 (Windows)
2. Google Chrome Version 64.0
  
## Set-up

### Pre-requisites

* None

### OpenCM 
Download OpenCM by
```
git clone https://github.com/SoftwareAG/webMethods-IntegrationServer-OpenCM
```
 
**Install & Configure OpenCM** 

* Using the Integration Server Administrator, install the package 
* Using the Integration Server Administrator, define a Global Variable:
```
name: OPENCM_MASTER_PASSWORD
value: <your_password>
type: password
```

* Create a separate file path for the OpenCM data repository and other output files:
```
e.g. D:\SoftwareAG\opencm

```
* Under the above opencm directory, extract the sample/template configuration files found under the src. It will create the following sub-directories:
```
	<opencm_root>/cmdata
	<opencm_root>/config
	<opencm_root>/output
```

* Create soft links from the package directory (<opencm_package_dir>):
```
Windows: 
 Open up a CMD window and cd to <opencm_package_dir>/pub:
 	>> mklink /D cmdata "<opencm_root>\cmdata"	(e.g. >> mklink /D cmdata "\OpenCM\cmdata")
 	>> mklink /D output "<opencm_root>\output"

Linux: 
 Open up a shell window and cd to <opencm_package_dir>/pub:
 	>> ln -s <opencm_root>/cmdata .
 	>> ln -s <opencm_root>/output .
```

* Update configuration files to match your installation setup:
```
	<is_install_instance>/packages/OpenCM/config/default.properties
	<opencm_root>/config/opencm.properties
```

## How it works

### Quick Start
	* Open browser and point to http://<host>:<port>/OpenCM
	* Expand the tree to view the environment layout
	* Select/click on an individual installation
	* Expand tree by clicking on individual tree nodes, drag canvas, zoom by scrolling 
	* Select a configuration instance leaf to view individual configuration properties
	* To compare (1): select menu 2-node Audit, and then "Custom Audit - IS Extended Settings"
	* To view result: select menu 2-node Audit and select the "Open 2-Node Audit Report" menu item (a popup will appear with the result)
	* To view result: in the popup, click on the red highlighted items to view differences
	* To compare (2): select menu Layered Audits, then "Audit IS-ExtSettings"
	* To view result: select menu Layered Audits, then select the "IS-ExtSettings" excel item (and open)
	* To view result: All green rows are equal, scroll down to see red rows that indicate differences.

Please refer to the OpenCM User Guide for more detailed information
	
______________________
These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.	

Contact us at [TECHcommunity](mailto:technologycommunity@softwareag.com?subject=Github/SoftwareAG) if you have any questions.