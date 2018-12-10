package org.opencm.audit.util;

import java.util.LinkedList;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import com.google.common.base.Splitter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Font;

import org.opencm.inventory.Inventory;
import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;
import org.opencm.audit.configuration.FormatRule;
import org.opencm.audit.env.AssertionConfig;
import org.opencm.audit.env.AssertionGroup;
import org.opencm.audit.env.AssertionProperty;
import org.opencm.audit.env.AssertionEnvironment;
import org.opencm.audit.env.AssertionValue;
import org.opencm.repository.util.RepoUtils;


public class ExcelWriter {

	private final int VALUE_CELL_MAX_LENGTH				= 40;
	
	Configuration opencmConfig; 
	
	public ExcelWriter(Configuration opencmConfig) {
		this.opencmConfig = opencmConfig; 
	}
	
	public void writeAssertionGroups(Inventory opencmNodes, HashMap<String,AssertionGroup> assGroups, String envPropsName, AssertionConfig assConfig) { 
		String excelFilename = envPropsName + ".xlsx";
		LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"ExcelWriter - writeAssertionGroups -> Number of groups: " + assGroups.size() + " into " + excelFilename);
		
		java.io.File excelFile = new java.io.File(this.opencmConfig.getOutput_dir() + File.separator + Configuration.OPENCM_RESULTS_DIR_EXCEL + File.separator + excelFilename);
		
		LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  - writeAssertionGroups -> Creating Workbook .... ");
		Workbook wb = new XSSFWorkbook(); 
		try {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  - writeAssertionGroups -> Creating Styles .... ");
		    Map<String, CellStyle> styles = createStyles(wb);
			
	    	// ----------------------------------------------------
		    // Sort the Assertion Groups alphabetically
	    	// ----------------------------------------------------
		    ArrayList<String> arrayAGs = new ArrayList<String>();
		    Iterator<String> itGroups = assGroups.keySet().iterator();
		    while (itGroups.hasNext()) {
		    	arrayAGs.add(itGroups.next());
		    }
	        java.util.Collections.sort(arrayAGs);
	        
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  - writeAssertionGroups -> Groups are sorted .... ");
	        
	    	// ----------------------------------------------------
	        // Determine environments
	    	// ----------------------------------------------------
	        LinkedList<String> envs = new LinkedList<String>();
			Iterator<String> groupIt = assGroups.keySet().iterator();
			while (groupIt.hasNext()) {
			    String layerKey = groupIt.next();
			    AssertionGroup layer = assGroups.get(layerKey);
			    Iterator<String> propIt = layer.getAssertionProperties().keySet().iterator();
				while (propIt.hasNext()) {
				    String propKey = propIt.next();
				    AssertionProperty prop = layer.getAssertionProperties().get(propKey);
				    Iterator<String> envIt = prop.getAssertionEnvironments().keySet().iterator();
					while (envIt.hasNext()) {
					    String envKey = envIt.next();
					    if (!envs.contains(envKey)) {
					    	envs.add(envKey);
					    }
					}
				}
			}
	        
			
	    	// ----------------------------------------------------
	        // Process all the individual Assertion Groups
	    	// ----------------------------------------------------
		    for (int arrAG = 0; arrAG < arrayAGs.size(); arrAG++) {
		    	AssertionGroup ag = assGroups.get(arrayAGs.get(arrAG));
				if (!ag.hasAssertionProperties()) {
					continue;
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"ExcelWriter - writeAssertionGroups -> " + ag.getAssertionGroup() + " (" + ag.getAssertionProperties().size() + " properties)");
				
		    	// ----------------------------------------------------
		        // Create a sheet for each assertion group
		    	// ----------------------------------------------------
		        Sheet sheet = wb.createSheet(ag.getAssertionGroup());
		        
		        // turn off gridlines
		        sheet.setDisplayGridlines(false);
		        sheet.setPrintGridlines(false);
		        sheet.setFitToPage(true);
		        sheet.setHorizontallyCenter(true);
		        PrintSetup printSetup = sheet.getPrintSetup();
		        printSetup.setLandscape(true);
		        
		        // The header row: centered text in 18pt font
		        Row headerRow = sheet.createRow(0);
		        headerRow.setHeightInPoints(20);
		        Cell titleCell = headerRow.createCell(0);
		        titleCell.setCellValue(assConfig.getReportName() + " - Assertion Group: " + ag.getAssertionGroup());
		        titleCell.setCellStyle(styles.get("title"));
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$N$1"));

		        //header with property and environment titles
		        Row header2Row = sheet.createRow(1);
		        
		        
		        // Create Title 2 Row with Environment names:
		        for (int t = 0; t < envs.size() + 1; t++) {
		            //set default column widths, the width is measured in units of 1/256th of a character width
		        	if (t == 0) {
			            sheet.setColumnWidth(0, 20 * 256); //the column is 20 characters wide
			            sheet.setColumnWidth(1, 2 * 256);
		        	} else {
			            sheet.setColumnWidth(t * 2, 20 * 256); 
			            sheet.setColumnWidth(t * 2 + 1, 20 * 256); 
		        	}
		        	if (!assConfig.getIncludeDefaultValues() || (t > 0)) {
			            sheet.addMergedRegion(new CellRangeAddress(1, 1, t * 2, t * 2 + 1));
		        	}
		            Cell title2Cell = header2Row.createCell(t * 2);
		            if (t == 0) {
		            	title2Cell.setCellValue("Property");
			        	if (assConfig.getIncludeDefaultValues()) {
			        		Cell defCell = header2Row.createCell(1);
			            	defCell.setCellValue("Default Value");
				            defCell.setCellStyle(styles.get("header"));
			        	}
		            } else {
		            	title2Cell.setCellValue(envs.get(t-1));
		            }
		            title2Cell.setCellStyle(styles.get("header"));
		        }
		        
		    	// ----------------------------------------------------
		        // Populate the assertion Result: Property by property
		    	// ----------------------------------------------------
			    // Sort the Properties alphabetically
		        HashMap<String,AssertionProperty> aps = ag.getAssertionProperties();
			    Iterator<String> itProps = aps.keySet().iterator();
			    ArrayList<String> arrayAPs = new ArrayList<String>();
			    while (itProps.hasNext()) {
			    	arrayAPs.add(itProps.next());
			    }
		        java.util.Collections.sort(arrayAPs);
		        
		        int rowIndex = 2;			// Start row for properties data
		        
			    for (int arrAP = 0; arrAP < arrayAPs.size(); arrAP++) {
					AssertionProperty ap = ag.getAssertionProperties().get(arrayAPs.get(arrAP));
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," - processing property [" + arrAP + "/" + arrayAPs.size() + "] -> " + ap.getPropertyName());
					boolean isEqual = ap.isEqual();
					
					int propertyStartRowIdx = rowIndex;
					CellAddress firstAddress = null;
					CellAddress lastAddress = null;
					
					// Create necessary rows for this property
					// Number of rows based on the first environment and its property values
		        	AssertionEnvironment ae = ap.getAssertionEnvironments().get(envs.get(0));
		        	
					LinkedList<AssertionValue> avs = ae.getValues();
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Number of Values for property: " + ap.getPropertyName() + " :: "+ avs.size());
					for (int v = 0; v < avs.size(); v++) {
						// Create the Property Name Cells
						LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Creating Row ... " + rowIndex);
				        Row propertyRow = sheet.createRow(rowIndex++);
			            Cell nodeCell = propertyRow.createCell(0);
			            Cell valueCell = propertyRow.createCell(1);
						
			            if (v == 0) {
					        nodeCell.setCellStyle(styles.get("property"));
			            	if (assConfig.getIncludeDefaultValues()) {
			            		valueCell.setCellStyle(styles.get("defaultValue"));
			            	} else {
			            		valueCell.setCellStyle(styles.get("property"));
			            	}
					        // Write property name in initial row - first column 
							nodeCell.setCellValue(ap.getPropertyName());
							
			            	// Adjust column width if necessary
			            	if (ap.getPropertyName().length() > (sheet.getColumnWidth(nodeCell.getColumnIndex()) / 256)) {
								sheet.setColumnWidth(nodeCell.getColumnIndex(), (ap.getPropertyName().length() * 256));
			            	}
			            	
			            	if (assConfig.getIncludeDefaultValues()) {
						        // Write default value next to property name 
			            		if (ap.getDefaultValue() != null) {
									valueCell.setCellValue(ap.getDefaultValue());
			            		} else {
									valueCell.setCellValue(RepoUtils.ASSERTION_DEFAULT_VALUE_MISSING);
			            		}
			            	}
			            	
			            }
			            
		            }
					
			        // Merge the Property name Cells (if spanning multiple rows)
			        if ((rowIndex - propertyStartRowIdx) > 1) {  // Merge the Property cells in column A
		            	if (assConfig.getIncludeDefaultValues()) {
					        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$" + (propertyStartRowIdx + 1) + ":$A$" + rowIndex));
					        sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (propertyStartRowIdx + 1) + ":$B$" + rowIndex));
		            	} else {
					        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$" + (propertyStartRowIdx + 1) + ":$B$" + rowIndex));
		            	}
			        }
			        
		        	// Loop through environments columns
			        for (int colIdx = 1; colIdx < envs.size() + 1; colIdx++) {
			        	int currentRowIdx = propertyStartRowIdx;
			        	ae = ap.getAssertionEnvironments().get(envs.get(colIdx-1));
						LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  Setting values for Environment: " + ae.getEnvironment());
			        	
			        	avs = ae.getValues();
						LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," AGAIN ... Number of Values for property: " + ap.getPropertyName() + " :: "+ avs.size());
			        	
			        	// Write Cell info for all values
						for (int i = 0; i < avs.size(); i++) {
							AssertionValue av = avs.get(i);
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Component: " + av.getComponent() + " Instance :: "+ av.getInstance());
							
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Getting Row ... " + currentRowIdx);
					        Row propertyRow = sheet.getRow(currentRowIdx++);
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"    Row " + (currentRowIdx-1) + " retrieved...");
				            Cell nodeCell = propertyRow.createCell(colIdx * 2);
				            Cell valueCell = propertyRow.createCell((colIdx * 2) + 1);
				            
				            // Record addresses for range definitions
				            if ((colIdx == 1) && (i == 0)) {
								LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  Setting First Address: [" + nodeCell.getAddress().getRow() + "," + nodeCell.getAddress().getColumn() + "]");
				            	firstAddress = nodeCell.getAddress();
				            } else if ((colIdx == envs.size()) && (i == avs.size()-1)) {
								LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  Setting Last Address: [" + valueCell.getAddress().getRow() + "," + valueCell.getAddress().getColumn() + "]");
				            	lastAddress = valueCell.getAddress();
				            }
							
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"    Cells Created on row " + (currentRowIdx-1) + " retrieved...");
							if ((av.getMissingInfo() == null) || (av.getMissingInfo().equals(RepoUtils.ASSERTION_MISSING_DATA))) {
								LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"    Setting cell values... on row " + (currentRowIdx-1) + " retrieved...");
								if (av.componentIsFixed() || av.getComponent() == null) {
							        // Write only node name in first column 
					            	nodeCell.setCellValue(av.getNode());
								} else {
							        // Write node name and component name (e.g. IS instance name) and its instance name... (can be different on UM)
					            	nodeCell.setCellValue(av.getNode() + "\n(" + av.getComponent() + ")");
					            	propertyRow.setHeightInPoints((3 * sheet.getDefaultRowHeightInPoints()));
								}

						        // Write property value in second column 
								if (av.getMissingInfo() != null) {
									valueCell.setCellValue(av.getMissingInfo());
								} else {
									// Certain values are very long (e.g. extended settings..). Need to break it up for better visualization.
									if (av.getValue().length() > VALUE_CELL_MAX_LENGTH) {
										StringBuffer sb = new StringBuffer();
										Iterable<String> valueParts = Splitter.fixedLength(VALUE_CELL_MAX_LENGTH).split(av.getValue());
										valueParts.forEach(valuePart -> {
											if (sb.length() > 0) {
												sb.append("\n");
											}
											sb.append(valuePart);
										});
										valueCell.setCellValue(sb.toString());
									} else {
										valueCell.setCellValue(av.getValue());
									}
								}
								
								// Apply Default Formatting (coloring via properties)
						        nodeCell.setCellStyle(styles.get("nodeRegular"));
						        valueCell.setCellStyle(styles.get("valueRegular"));
								
								// Hide Equals
						        if (assConfig.hideEquals() && isEqual) {
						        	propertyRow.setZeroHeight(true);
						        }
						        
							} else {
								// Either undefined or missing data
								if (av.getMissingInfo().equals(RepoUtils.ASSERTION_UNDEFINED_NODE)) {
							        nodeCell.setCellStyle(styles.get("undefinedData"));
							        valueCell.setCellStyle(styles.get("undefinedData"));
								}
							}
							
			            }
			        }
			        
	            	// Apply conditional formatting for property if using EQUALS or DIFFS
				    if ((assConfig.getFormatting() != null) &&  (firstAddress != null) && (lastAddress != null)) {
						LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  Setting Range: [" + firstAddress.getRow() + "," + lastAddress.getRow() + "," + firstAddress.getColumn() + "," + lastAddress.getColumn() + "]");
						CellRangeAddress propertyRange = new CellRangeAddress(firstAddress.getRow(), lastAddress.getRow(), firstAddress.getColumn(), lastAddress.getColumn());
				    	
				    	LinkedList<FormatRule> frs = assConfig.getFormatting();
		            	SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
		            	
		                CellRangeAddress[] regions = {propertyRange};
		                
				    	for (int i = 0; i < frs.size(); i++) {
				    		FormatRule fr = frs.get(i);
				    		ConditionalFormattingRule rule = null;
				    		
				    		if (fr.getRule().equals(FormatRule.FORMAT_RULE_EQUALS) && isEqual) {
				            	rule = sheetCF.createConditionalFormattingRule("true");
				    		} else if (fr.getRule().equals(FormatRule.FORMAT_RULE_DIFFERENCES) && !isEqual) {
				            	rule = sheetCF.createConditionalFormattingRule("true");
				    		}
				    		
				    		if (rule == null) {
				            	continue;
				    		}
				    			
			                ConditionalFormattingRule [] cfRules = {rule};
			                PatternFormatting patternFmt = rule.createPatternFormatting();
			                Color clr = new XSSFColor(java.awt.Color.decode(fr.getBGColor()));
			                patternFmt.setFillBackgroundColor(clr);
			                
			                sheetCF.addConditionalFormatting(regions, cfRules);
				    	}
				    }
			        
			    } // End Property Row
			    
            	// Apply conditional formatting for custom rules and custom range
			    if (assConfig.getFormatting() != null) {
			    	LinkedList<FormatRule> frs = assConfig.getFormatting();
	            	SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
	                
			    	for (int i = 0; i < frs.size(); i++) {
			    		FormatRule fr = frs.get(i);
			    		if (fr.getRule().equals(FormatRule.FORMAT_RULE_EQUALS) || fr.getRule().equals(FormatRule.FORMAT_RULE_DIFFERENCES)) {
			            	continue;
			    		}

			    		ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule(fr.getRule());
		                ConditionalFormattingRule [] cfRules = {rule};
		                
						CellRangeAddress sheetRange = CellRangeAddress.valueOf(fr.getRange());
		                CellRangeAddress[] regions = {sheetRange};
		                
		                PatternFormatting patternFmt = rule.createPatternFormatting();
		                Color clr = new XSSFColor(java.awt.Color.decode(fr.getBGColor()));
		                patternFmt.setFillBackgroundColor(clr);
		                
		                sheetCF.addConditionalFormatting(regions, cfRules);
			    	}
			    }
			    
			    // Finally, adjust column widths for environments (location and value):
			    if (assConfig.getIncludeDefaultValues()) {
	            	sheet.autoSizeColumn(1);
			    }
		        for (int t = 0; t < envs.size() + 1; t++) {
		        	if (t != 0) {
		            	sheet.autoSizeColumn(t * 2);
		            	sheet.autoSizeColumn(t * 2 + 1);
		        	}
		        }
			    sheet.createFreezePane(0, 2);

		    }

            // Write the output to a file
		    FileOutputStream out = new FileOutputStream(excelFile);
		    if (wb.getNumberOfSheets() > 0) {
			    wb.write(out);
		    } else {
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"writeAssertionGroups: No worksheets to write. No Excel will be generated... ");
		    }
		    wb.close();
		} catch (Exception ex) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"writeAssertionGroups " + ex.toString());
		}

	}
		        
    private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<>();

        short borderColor = IndexedColors.GREY_50_PERCENT.getIndex();

        CellStyle style;
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)18);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(titleFont);
        styles.put("title", style);

        Font title2Font = wb.createFont();
        title2Font.setFontHeightInPoints((short)12);
        title2Font.setColor(IndexedColors.WHITE.getIndex());
        title2Font.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        style.setFont(title2Font);
        styles.put("header", style);

        Font propertyFont = wb.createFont();
        propertyFont.setFontHeightInPoints((short)11);
        propertyFont.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.WHITE.getIndex());
        style.setFont(propertyFont);
        styles.put("property", style);

        Font nodeFontDefault = wb.createFont();
        nodeFontDefault.setFontHeightInPoints((short)9);
        nodeFontDefault.setBold(false);
        nodeFontDefault.setColor(IndexedColors.BLUE_GREY.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setFont(nodeFontDefault);
        style.setWrapText(true);
        styles.put("defaultValue", style);
        
        Font nodeFont = wb.createFont();
        nodeFont.setFontHeightInPoints((short)10);
        nodeFont.setBold(false);
        nodeFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setFont(nodeFont);
        style.setWrapText(true);
        styles.put("nodeEqual", style);

        Font nodeFontDifferent = wb.createFont();
        nodeFontDifferent.setFontHeightInPoints((short)11);
        nodeFontDifferent.setBold(false);
        nodeFontDifferent.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setFont(nodeFontDifferent);
        style.setWrapText(true);
        styles.put("nodeDifferent", style);
        
        Font nodeFontRegular = wb.createFont();
        nodeFontDifferent.setFontHeightInPoints((short)11);
        nodeFontDifferent.setBold(false);
        nodeFontDifferent.setColor(IndexedColors.AUTOMATIC.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setFont(nodeFontRegular);
        style.setWrapText(true);
        styles.put("nodeRegular", style);

        Font valueFont = wb.createFont();
        valueFont.setFontHeightInPoints((short)10);
        valueFont.setBold(false);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setFont(valueFont);
        styles.put("valueEqual", style);
        
        Font valueFontDifferent = wb.createFont();
        valueFontDifferent.setFontHeightInPoints((short)11);
        valueFontDifferent.setBold(true);
        valueFontDifferent.setColor(IndexedColors.RED1.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setFont(valueFontDifferent);
        styles.put("valueDifferent", style);

        Font valueFontRegular = wb.createFont();
        valueFontDifferent.setFontHeightInPoints((short)11);
        valueFontDifferent.setBold(true);
        valueFontDifferent.setColor(IndexedColors.AUTOMATIC.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setFont(valueFontRegular);
        styles.put("valueRegular", style);
        
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SPARSE_DOTS);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        styles.put("undefinedData", style);

        return styles;
    }
	
}
