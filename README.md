# OpenCM
OpenCM is a Configuration Management Repository for webMethods components and is primarily an operational/runtime governance tool of integration platform wM components. OpenCM provides an inventory of installation components and can thereafter (when defined) support the ability to audit & compare configuration property information in order to detect discrepancies. 

In addition, OpenCM can act the "source of truth" for any configuration properties within the landscape. By defining what properties should be, it is then possible to generate compliance reports to detect which runtime installations that are not configured according to the source of truth.

## Description
OpenCM is an Integration Server package that is centrally installed on an (administration) IS. Main external dependency to OpenCM is SoftwareAG Platform Manager for all remote webMethods installations, which are used to perform property extractions (retrievals) and subsequent storing of the information into a local, file-based, repository. OpenCM comes with a user interface for easy navigation and visualization of property information.

### Installation Inventory
![Alt text](/github_images/inventory.png?raw=true "OpenCM User Interface")

The OpenMC user interface can visualize the integration platform servers and installations via a treestructure (see above), and by selecting portions of the tree. Once selected, a list of installations defined will then be shown in the center table.

### Configuration Repository
The configuration properties of an installation can be inspected by clicking on an installation in the inventory table. 

![Alt text](/github_images/Repository.png?raw=true "OpenCM User Interface")

### Runtime Audits
Comparing differences between multiple installations is referred to as "Runtime Auditing" and is performed by describing which installations to audit, and what properties to compare:

![Alt text](/github_images/RuntimeAudit.png?raw=true "OpenCM Auditing")

This example shows that there are differences in extended settings between the two installations selected. The result table can also be saved off as an Excel sheet for offline viewing.

### Governance Audits
In addition to Runtime Audits, it is also possible to perform "Governance Audits", which is to ensure that configuration properties are according to a so-called "source of truth". I.e., a particular configuration property must be of a certain value.

![Alt text](/github_images/GovernanceAudit.png?raw=true "OpenCM Auditing")

The definition of a "governance rule" is similar to runtime audits, with the addition of a property value that is considered what it must be.

## Requirements

The project was developed and tested on Integration Servers 9.x and 10.x. Browser verifications include MS Edge, Firefox, Chrome and MS Explorer.
 
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
* It is then recommended to create a separate file path for the OpenCM configurations and data repository files:
```
e.g. D:\SoftwareAG\opencm

```
* Once created, change the global variable:
```
	OPENCM_ROOT_DIR = D:\SoftwareAG\opencm
```

## How it works
Please refer to the OpenCM User Guide for more detailed information
	
______________________
These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.	
______________________
For more information you can Ask a Question in the [TECHcommunity Forums](http://tech.forums.softwareag.com/techjforum/forums/list.page?product=webmethods).

You can find additional information in the [Software AG TECHcommunity](http://techcommunity.softwareag.com/home/-/product/name/webmethods).
______________________
Contact us at [TECHcommunity](mailto:technologycommunity@softwareag.com?subject=Github/SoftwareAG) if you have any questions.
