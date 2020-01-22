# OpenCM
OpenCM is a Configuration Management Repository for webMethods components and is primarily an operational/runtime governance tool of DBP wM components. It can act as a "source of truth" (documentation repo) and provides the ability to audit & compare configuration property information in order to detect discrepancies. In addition, it is also used as an operations utility for quick and easy retrieval of property information through its user interface.   
 
## Description
OpenCM is an Integration Server package that is centrally installed on a separate administration IS. Main external dependency to OpenCM is SoftwareAG Platform Manager for all remote webMethods installations, which are used to perform property extractions (retrievals) and subsequent storing of the information into a local, file-based, repository. OpenCM comes with a user interface for easy navigation and visualization of property information.

![Alt text](/github_images/dnd-overview.png?raw=true "OpenCM User Interface")

The OpenMC user interface can visualize the integration platform servers and installations via a treestructure (see above), and by selecting an individual installation node, the configuration items are available for manual inspection as below.

![Alt text](/github_images/dnd-node.png?raw=true "OpenCM User Interface")

The above example shows the fix level version for a particular fix of an Integration Server. On top of the collected property information, automatic/continuous auditing of property information can then be performed to detect inconsistencies between different installations. 

Another view of all the webMethods installations can be viewed through the "Inventory" page, similar to the main tree structure, but this time in a searchable, tabular form:

![Alt text](/github_images/inventory.png?raw=true "OpenCM Inventory")

Comparing differences is referred to as "Auditing" and is performed through a wizard on the user interface. Once all the information is provided, a report table is generated:

![Alt text](/github_images/auditing.png?raw=true "OpenCM Auditing")

This example shows that there are differences in fix levels between the installations within the different environments. The report can also be saved off as an Excel sheet for offline viewing.

## Requirements

The project was developed and tested on the following installation:

1. Integration Server 9.9, 9.12, 10.0, 10.1, 10.3 (Windows)
2. Google Chrome Version 64.0
  
## Set-up

### Pre-requisites

* None

### OpenCM 
Download OpenCM by
```
git clone https://github.com/SoftwareAG/OpenCM
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

* Create a soft link from the package directory (<opencm_package_dir>):
```
Windows: 
 Open up a CMD window and cd to <opencm_package_dir>/pub:
 	>> mklink /D opencm "<opencm_root>\opencm"	(e.g. >> mklink /D opencm "\SoftwareAG\opencm")

Linux: 
 Open up a shell window and cd to <opencm_package_dir>/pub:
 	>> ln -s <opencm_root>/opencm .
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
	* To view Inventory: select menu Inventory, and then select the desired department from the tree
	* To run Audit: select menu Auditing, and then follow the wizard steps

Please refer to the OpenCM User Guide for more detailed information
	
______________________
These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.	
______________________
For more information you can Ask a Question in the [TECHcommunity Forums](http://tech.forums.softwareag.com/techjforum/forums/list.page?product=webmethods).

You can find additional information in the [Software AG TECHcommunity](http://techcommunity.softwareag.com/home/-/product/name/webmethods).
______________________
Contact us at [TECHcommunity](mailto:technologycommunity@softwareag.com?subject=Github/SoftwareAG) if you have any questions.
