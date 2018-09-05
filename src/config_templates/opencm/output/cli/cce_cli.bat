@ECHO OFF
rem ############################################################################
rem
rem  Auto-generated windows batch script for generating environments and nodes
rem
rem ############################################################################

SET CCE_URL=http://cce_admin_server.opencm.org:8090
SET CCE_USERNAME=Administrator

rem --------------------------------------------------------------------
rem      Defining CCE Environments .......... 
rem --------------------------------------------------------------------
.\sagcc create landscape environments alias=Administration name=Administration description="Administration Environment" -s %CCE_URL% -u %CCE_USER%
.\sagcc create landscape environments alias=Development name=Development description="Development Environment" -s %CCE_URL% -u %CCE_USER%
.\sagcc create landscape environments alias=Test name=Test description="Test Environment" -s %CCE_URL% -u %CCE_USER%
.\sagcc create landscape environments alias=Pre-Production name=Pre-Production description="Pre-Production Environment" -s %CCE_URL% -u %CCE_USER%
.\sagcc create landscape environments alias=Production name=Production description="Production Environment" -s %CCE_URL% -u %CCE_USER%

rem ==========================================================================
rem                        Defining CCE Nodes  .......... 
rem ==========================================================================
rem ----------------------------------------------------
rem      [Administration] -> CCE_V101 Layer
rem ----------------------------------------------------
echo "Generating nodes for [Administration] -> CCE_V101 Layer .... 
.\sagcc create landscape nodes alias=ADM_CCE_V101_01 url=https://cce_admin_server.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Administration nodes nodeAlias=ADM_CCE_V101_01 -s %CCE_URL% -u %CCE_USER%

rem ----------------------------------------------------
rem      [Development] -> ESB_IS_V101 Layer
rem ----------------------------------------------------
echo "Generating nodes for [Development] -> ESB_IS_V101 Layer .... 
.\sagcc create landscape nodes alias=DEV_ESB_IS_V101_01 url=https://server_01.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Development nodes nodeAlias=DEV_ESB_IS_V101_01 -s %CCE_URL% -u %CCE_USER%

.\sagcc create landscape nodes alias=DEV_ESB_IS_V101_02 url=https://server_02.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Development nodes nodeAlias=DEV_ESB_IS_V101_02 -s %CCE_URL% -u %CCE_USER%

rem ----------------------------------------------------
rem      [Test] -> ESB_IS_V101 Layer
rem ----------------------------------------------------
echo "Generating nodes for [Test] -> ESB_IS_V101 Layer .... 
.\sagcc create landscape nodes alias=TEST_ESB_IS_V101_01 url=https://server_03.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Test nodes nodeAlias=TEST_ESB_IS_V101_01 -s %CCE_URL% -u %CCE_USER%

.\sagcc create landscape nodes alias=TEST_ESB_IS_V101_02 url=https://server_04.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Test nodes nodeAlias=TEST_ESB_IS_V101_02 -s %CCE_URL% -u %CCE_USER%

rem ----------------------------------------------------
rem      [Pre-Production] -> ESB_IS_V101 Layer
rem ----------------------------------------------------
echo "Generating nodes for [Pre-Production] -> ESB_IS_V101 Layer .... 
.\sagcc create landscape nodes alias=PREP_ESB_IS_V101_01 url=https://server_05.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Pre-Production nodes nodeAlias=PREP_ESB_IS_V101_01 -s %CCE_URL% -u %CCE_USER%

.\sagcc create landscape nodes alias=PREP_ESB_IS_V101_02 url=https://server_06.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Pre-Production nodes nodeAlias=PREP_ESB_IS_V101_02 -s %CCE_URL% -u %CCE_USER%

rem ----------------------------------------------------
rem      [Production] -> ESB_IS_V101 Layer
rem ----------------------------------------------------
echo "Generating nodes for [Production] -> ESB_IS_V101 Layer .... 
.\sagcc create landscape nodes alias=PROD_ESB_IS_V101_01 url=https://server_07.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Production nodes nodeAlias=PROD_ESB_IS_V101_01 -s %CCE_URL% -u %CCE_USER%

.\sagcc create landscape nodes alias=PROD_ESB_IS_V101_02 url=https://server_08.opencm.org:10193 -s %CCE_URL% -u %CCE_USER%
.\sagcc add landscape environments Production nodes nodeAlias=PROD_ESB_IS_V101_02 -s %CCE_URL% -u %CCE_USER%


echo "------------------------------------------------------ 
echo "--            Processing Completed                  -- 
echo "------------------------------------------------------ 
