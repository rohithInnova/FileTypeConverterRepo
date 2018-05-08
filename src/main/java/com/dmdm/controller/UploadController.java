package com.dmdm.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dmdm.model.UploadedFile;
import com.dmdm.validator.FileValidator;

@Controller
public class UploadController {
	
	private static final Logger logger = Logger.getLogger(UploadController.class);

	@Autowired
	FileValidator fileValidator;
	
	File newFile;

	@RequestMapping("/fileUploadForm")
	public ModelAndView getUploadForm(
			@ModelAttribute("uploadedFile") UploadedFile uploadedFile,
			BindingResult result) {
		return new ModelAndView("uploadForm");
	}

	@RequestMapping("/fileUpload")
	public ModelAndView fileUploaded(
			@ModelAttribute("uploadedFile") UploadedFile uploadedFile,
			BindingResult result) {
		InputStream inputStream = null;
		OutputStream outputStream = null;

		MultipartFile file = uploadedFile.getFile();
		fileValidator.validate(uploadedFile, result);

		String fileName = file.getOriginalFilename();
		
		if (result.hasErrors()) {
			return new ModelAndView("uploadForm");
		}

		try {
			//logic to read from excel and write into new location
			inputStream = file.getInputStream();

			newFile = new File("C:/DMDM/" + fileName);
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
			outputStream = new FileOutputStream(newFile,false);//false to overwrite the file
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (IOException e) {
			logger.error("Error at fileUploaded() : "+ e.getMessage());
		}
		String filepath = newFile.getAbsolutePath();
		try {
			excelToXml (filepath);	
		}catch(Exception e) {
			return new ModelAndView("errorFile", "message", e.getMessage());
		}
		
			
		return new ModelAndView("showFile", "message", fileName);
	}
	
	public static boolean isCellEmpty(final HSSFCell cell) {
	    if (cell == null) {
	        return true;
	    }

	    if (cell.getStringCellValue().isEmpty()) {
	        return true;
	    }

	    return false;
	}
	
	public void excelToXml (String path) throws Exception
	{
	    InputStream inputStream = null; 
	    try
	    {
	        inputStream = new FileInputStream (path);
	    }
	    catch (FileNotFoundException e)
	    {
	        logger.error("File not found in the specified path.");
	        logger.error(e.getStackTrace());
	        throw new Exception(e.getMessage());
	    }

	    POIFSFileSystem fileSystem = null;
	    HSSFWorkbook      workBook = null;
	    try {
	        //Initializing the XML document
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document1 = builder.newDocument();
	        Document document2 = builder.newDocument();
	        fileSystem = new POIFSFileSystem (inputStream);
	        workBook = new HSSFWorkbook (fileSystem);
	        HSSFSheet         sheet2    = workBook.getSheet("Sheet2");
	        HSSFSheet sourceSheet = workBook.getSheet("source");
	        HSSFSheet stage1Sheet = workBook.getSheet("stage1");
	        HSSFSheet stage2Sheet = workBook.getSheet("stage2");
	        HSSFSheet prodSheet = workBook.getSheet("prod");
	        HSSFSheet lkpStage1Sheet = workBook.getSheet("lkpStage1");
	        HSSFSheet lkpProdSheet = workBook.getSheet("lkpProd");
	        
	        int rowStart = 13; 
	        int sourceRowEnd = sourceSheet.getLastRowNum();
	        int stage1RowEnd = stage1Sheet.getLastRowNum();
	        int stage2RowEnd = stage2Sheet.getLastRowNum();
	        int prodRowEnd = prodSheet.getLastRowNum();
	        int lkpStage1RowEnd = lkpStage1Sheet.getLastRowNum();
	        int lkpProdRowEnd = lkpProdSheet.getLastRowNum();
	        
	        
	        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	        Date date = new Date();
	        
	        //reading data from excel
	        String b_folder = sourceSheet.getRow(0).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        int d_version = (int)(sourceSheet.getRow(0).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	        String b_shortcutFolder = sourceSheet.getRow(1).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();;
	        String b_mapping = sourceSheet.getRow(2).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue(); 
	        String b_session = sourceSheet.getRow(3).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue(); 
	        String b_worklet = sourceSheet.getRow(4).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String z_worklet = prodSheet.getRow(4).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String b_dbtype = sourceSheet.getRow(5).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String i_dbtype = stage1Sheet.getRow(5).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String b_dbdName = sourceSheet.getRow(6).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String b_conn = sourceSheet.getRow(8).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String i_conn = stage1Sheet.getRow(8).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String b_schema = sourceSheet.getRow(9).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue(); 
	        String b_entity = sourceSheet.getRow(10).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue(); 
	        String i_entity = stage1Sheet.getRow(10).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String n_descr =  lkpProdSheet.getRow(11).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	       String v_descr =  lkpStage1Sheet.getRow(11).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	       String z_mapping =  prodSheet.getRow(2).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String z_session =  prodSheet.getRow(3).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String del_mapping =  stage2Sheet.getRow(0).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String del_session =  stage2Sheet.getRow(1).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String upd_mapping =  stage2Sheet.getRow(2).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String upd_session =  stage2Sheet.getRow(3).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String r_entity =  stage2Sheet.getRow(10).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String b_lkpSqlOverride = sheet2.getRow(16).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String b_trnsf_1_lkpCondition = sheet2.getRow(17).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String b_trnsf_2_lkpCondition = sheet2.getRow(19).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String z_entity = prodSheet.getRow(10).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String z_conn = prodSheet.getRow(8).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String r_conn = stage2Sheet.getRow(8).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String i_schema =  stage1Sheet.getRow(9).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String etlEntity = stage2Sheet.getRow(10).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String etlDbtype = stage2Sheet.getRow(5).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	       String z_Dbtype = prodSheet.getRow(5).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        
	        //preparing 1st xml data in sb1
	        StringBuilder sb1 = new StringBuilder();
	        sb1.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
	        		"<POWERMART CREATION_DATE=\""+dateFormat.format(date)+"\" REPOSITORY_VERSION=\""+d_version+"\">\r\n"+
	        		"	<REPOSITORY NAME=\"REPO_HAEA_EDW_PROD\" VERSION=\""+d_version+"\" CODEPAGE=\"UTF-8\" DATABASETYPE=\"Oracle\">\r\n"+
	        		//powermart --> repository --> 1st folder
	        		"		<FOLDER NAME=\""+b_folder+"\" GROUP=\"\" OWNER=\"INFA_ADMIN\" SHARED=\"NOTSHARED\" DESCRIPTION=\"\" PERMISSIONS=\"rwx------\" UUID=\""+UUID.randomUUID().toString()+"\">\r\n"+
	        		"        	<MAPPING DESCRIPTION =\"\" ISVALID =\"YES\" NAME =\""+b_mapping+"\" OBJECTVERSION =\"1\" VERSIONNUMBER =\"1\">\r\n"+
	        		//powermart --> repository --> 1st folder --> 1st transformation
	        		"        		<TRANSFORMATION DESCRIPTION =\"\" NAME =\"SQ_sc_"+b_entity+"\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Source Qualifier\" VERSIONNUMBER =\"1\">\r\n");

	        for(int row=rowStart; row<=sourceRowEnd; row++) {
	        	   if(!isCellEmpty(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
	        		   String name = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	   if(!name.isEmpty()) {
			    		   String type = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			    		   String typeInxml = "";
			    		   if(type.equalsIgnoreCase("char")) {
			    			   typeInxml = "string";
			    		   }else if(type.equalsIgnoreCase("decimal")) {
			    			   typeInxml = "decimal";
			    		   }
			    		   int precision = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
				    	   int scale = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			sb1.append("				<TRANSFORMFIELD DATATYPE =\""+typeInxml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\""+name+"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");		    		   
			    	   }
	        	   }
		       }
	        
	        sb1.append("				<TABLEATTRIBUTE NAME =\"Sql Query\" VALUE =\"\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"User Defined Join\" VALUE =\"\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Source Filter\" VALUE =\"\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Number Of Sorted Ports\" VALUE =\"0\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Select Distinct\" VALUE =\"NO\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Is Partitionable\" VALUE =\"NO\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Pre SQL\" VALUE =\"\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Post SQL\" VALUE =\"\"/>\r\n" + 
	        		   "            	<TABLEATTRIBUTE NAME =\"Output is deterministic\" VALUE =\"NO\"/>\r\n" + 
	        		   "       			<TABLEATTRIBUTE NAME =\"Output is repeatable\" VALUE =\"Never\"/>\r\n" + 
	        		   "			</TRANSFORMATION>\r\n")
	        
	        // powermart --> repository --> 1st folder --> 2nd transformation
	        .append("				<TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXPTRANS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n"); 
	        		for(int row=rowStart; row<=sourceRowEnd; row++) {
	        			if(!isCellEmpty(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
	        				String name = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  	 		    	   	if(!name.isEmpty()) {
	  	 		    		   String type = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  	 		    		   String typeInxml = "";
	  	 		    		   if(type.equalsIgnoreCase("char")) {
	  	 		    			   typeInxml = "string";
	  	 		    			  int precision = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  		 			    	   int scale = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	  	sb1.append("					<TRANSFORMFIELD DATATYPE =\""+typeInxml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\""+name+"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	  	 		    		   }
	  	 		    	   }
	        			}	
	 		       }
	        		
	        		for(int row=rowStart; row<=sourceRowEnd; row++) {
	        			if(!isCellEmpty(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
	        				 String name = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			 		    	   if(!name.isEmpty()) {
			 		    		   String type = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			 		    		   String typeInxml = "";
			 		    		   String expression = "";
			 		    		   String portType = "OUTPUT";
			 		    		   String nameInxml = "";
			 		    		   if(type.equalsIgnoreCase("char")) {
			 		    			   typeInxml = "string";
			 		    			   expression = "LTRIM(RTRIM(UPPER("+name+")))";
			 		    			   nameInxml = "O_"+name;
			 		    		   }else if(type.equalsIgnoreCase("decimal")) {
			 		    			   typeInxml = "decimal";
			 		    			   expression = name;
			 		    			   portType = "INPUT/OUTPUT";
			 		    			   nameInxml = name;
			 		    		   }
			 		    		  int precision = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			 			    	  int scale = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		 sb1.append("					<TRANSFORMFIELD DATATYPE =\""+typeInxml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\""+name+"\" EXPRESSION =\""+expression+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+nameInxml+"\" PICTURETEXT =\"\" PORTTYPE =\""+portType+"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
			 		    	   }
	        			}
		 		       }
	     sb1.append("					<TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_REC_CREATE_DATE\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n" +
	        		"            		<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        		"        		</TRANSFORMATION>\r\n" +
	        		"		 		<INSTANCE DESCRIPTION =\"\" NAME =\"sc_"+i_entity+"\" TRANSFORMATION_NAME =\"sc_"+i_entity+"\" TRANSFORMATION_TYPE =\"Target Definition\" TYPE =\"TARGET\"/>\r\n" + 
	        		"        		<INSTANCE DESCRIPTION =\"\" NAME =\"SQ_sc_"+b_entity+"\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"SQ_sc_"+b_entity+"\" TRANSFORMATION_TYPE =\"Source Qualifier\" TYPE =\"TRANSFORMATION\">\r\n" + 
	        		"            		<ASSOCIATED_SOURCE_INSTANCE NAME =\"sc_"+b_entity+"\"/>\r\n" + 
	        		"        		</INSTANCE>\r\n" + 
	        		"        		<INSTANCE DESCRIPTION =\"\" NAME =\"EXPTRANS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXPTRANS\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	        		"		        <INSTANCE DBDNAME =\""+b_dbdName+"\" DESCRIPTION =\"\" NAME =\"sc_"+b_entity+"\" TRANSFORMATION_NAME =\"sc_"+b_entity+"\" TRANSFORMATION_TYPE =\"Source Definition\" TYPE =\"SOURCE\"/>\r\n" );
		       for(int row=rowStart; row<=stage1RowEnd; row++) {
		    	   if(!isCellEmpty(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
		    		   String name = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	   String type = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			    	   if(type.equalsIgnoreCase("VARCHAR2")) {
		 sb1.append("				<CONNECTOR FROMFIELD =\"O_"+name+"\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+i_entity+"\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");   
			    	   }else {
		 sb1.append("				<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+i_entity+"\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
			    	   }
		    	   }
		       }
		 sb1.append("				<CONNECTOR FROMFIELD =\"O_REC_CREATE_DATE\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_CREATE_DATE\" TOINSTANCE =\"sc_"+i_entity+"\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
		       for(int row=rowStart; row<=sourceRowEnd; row++) {
		    	   if(!isCellEmpty(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
		    		   String name = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	   if(!name.isEmpty())
		 sb1.append("				<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"sc_"+b_entity+"\" FROMINSTANCETYPE =\"Source Definition\" TOFIELD =\""+name+"\" TOINSTANCE =\"SQ_sc_"+b_entity+"\" TOINSTANCETYPE =\"Source Qualifier\"/>\r\n");
		    	   }
		       }
		       for(int row=rowStart; row<=sourceRowEnd; row++) {
		    	   if(!isCellEmpty(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
		    		   String name = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	   if(!name.isEmpty())
		 sb1.append("				<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"SQ_sc_"+b_entity+"\" FROMINSTANCETYPE =\"Source Qualifier\" TOFIELD =\""+name+"\" TOINSTANCE =\"EXPTRANS\" TOINSTANCETYPE =\"Expression\"/>\r\n");
		    	   }
		       }
	     sb1.append("				<TARGETLOADORDER ORDER =\"1\" TARGETINSTANCE =\"sc_"+i_entity+"\"/>\r\n"+ 
	        		"        		<ERPINFO/>\r\n"+ 
	        		"        	</MAPPING>\r\n"+
	        		"       	<SHORTCUT COMMENTS =\"DOM reporting - PLC major code header group       \" DBDNAME =\""+b_dbdName+"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\""+"sc_"+b_entity+"\" OBJECTSUBTYPE =\"Source Definition\" OBJECTTYPE =\"SOURCE\" REFERENCEDDBD =\""+b_dbdName+"\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\""+b_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_PROD\" VERSIONNUMBER =\"1\"/>\r\n"+
	        		"       	<SHORTCUT COMMENTS =\"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\""+"sc_"+i_entity+"\" OBJECTSUBTYPE =\"Target Definition\" OBJECTTYPE =\"TARGET\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\""+i_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_PROD\" VERSIONNUMBER =\"1\"/>\r\n"+
					"        	<CONFIG DESCRIPTION =\"Default session configuration object\" ISDEFAULT =\"YES\" NAME =\"default_session_config\" VERSIONNUMBER =\"1\">\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Advanced\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Constraint based load ordering\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Cache LOOKUP() function\" VALUE =\"YES\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Default buffer block size\" VALUE =\"256000\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Line Sequential buffer length\" VALUE =\"2048\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Maximum Memory Allowed For Auto Memory Attributes\" VALUE =\"640MB\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Maximum Percentage of Total Memory Allowed For Auto Memory Attributes\" VALUE =\"10\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Additional Concurrent Pipelines for Lookup Cache Creation\" VALUE =\"0\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Custom Properties\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Pre-build lookup cache\" VALUE =\"Auto\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Optimization Level\" VALUE =\"Medium\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"DateTime Format String\" VALUE =\"MM/DD/YYYY HH24:MI:SS.US\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Pre 85 Timestamp Compatibility\" VALUE =\"YES\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Log Options\" VALUE =\"0\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Save session log by\" VALUE =\"Session runs\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Save session log for these runs\" VALUE =\"0\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Session Log File Max Size\" VALUE =\"0\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Session Log File Max Time Period\" VALUE =\"0\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Maximum Partial Session Log Files\" VALUE =\"1\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Writer Commit Statistics Log Frequency\" VALUE =\"1\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Writer Commit Statistics Log Interval\" VALUE =\"0\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Error handling\" VALUE =\"\"/>\r\n" + 
	        		"       		<ATTRIBUTE NAME =\"Stop on errors\" VALUE =\"0\"/>\r\n" + 
	        		"       		<ATTRIBUTE NAME =\"Override tracing\" VALUE =\"None\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"On Stored Procedure error\" VALUE =\"Stop\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"On Pre-session command task error\" VALUE =\"Stop\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"On Pre-Post SQL error\" VALUE =\"Stop\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Enable Recovery\" VALUE =\"NO\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Error Log Type\" VALUE =\"None\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Error Log Table Name Prefix\" VALUE =\"\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Error Log File Name\" VALUE =\"PMError.log\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Log Source Row Data\" VALUE =\"NO\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Data Column Delimiter\" VALUE =\"|\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Partitioning Options\" VALUE =\"\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Dynamic Partitioning\" VALUE =\"Disabled\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Number of Partitions\" VALUE =\"1\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Multiplication Factor\" VALUE =\"Auto\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Session on Grid\" VALUE =\"\"/>\r\n" + 
	        		"    		    <ATTRIBUTE NAME =\"Is Enabled\" VALUE =\"NO\"/>\r\n" + 
	        		"    		</CONFIG>\r\n"+
	        		//powermart --> repository --> 1st folder --> session
	        		"			<SESSION DESCRIPTION =\"\" ISVALID =\"YES\" MAPPINGNAME =\""+b_mapping+"\" NAME =\""+b_session+"\" REUSABLE =\"YES\" SORTORDER =\"Binary\" VERSIONNUMBER =\"1\">\r\n" + 
	        		"        		<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"sc_"+i_entity+"\" STAGE =\"1\" TRANSFORMATIONNAME =\"sc_"+i_entity+"\" TRANSFORMATIONTYPE =\"Target Definition\"/>\r\n" + 
	        		"        		<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"SQ_sc_"+b_entity+"\" STAGE =\"2\" TRANSFORMATIONNAME =\"SQ_sc_"+b_entity+"\" TRANSFORMATIONTYPE =\"Source Qualifier\"/>\r\n" + 
	        		"        		<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXPTRANS\" STAGE =\"2\" TRANSFORMATIONNAME =\"EXPTRANS\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	        		"        		    <PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	        		"        		</SESSTRANSFORMATIONINST>\r\n" + 
	        		"        		<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"0\" SINSTANCENAME =\"sc_"+b_entity+"\" STAGE =\"0\" TRANSFORMATIONNAME =\"sc_"+b_entity+"\" TRANSFORMATIONTYPE =\"Source Definition\">\r\n" + 
	        		"        		    <ATTRIBUTE NAME =\"Owner Name\" VALUE =\""+b_schema+"\"/>\r\n" + 
	        		"        		</SESSTRANSFORMATIONINST>\r\n" + 
	        		"        		<CONFIGREFERENCE REFOBJECTNAME =\"default_session_config\" TYPE =\"Session config\"/>\r\n" + 
	        		"        		<SESSIONEXTENSION NAME =\"Relational Writer\" SINSTANCENAME =\"sc_STG_"+b_entity+"\" SUBTYPE =\"Relational Writer\" TRANSFORMATIONTYPE =\"Target Definition\" TYPE =\"WRITER\">\r\n" + 
	        		"            		<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+i_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+i_dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	        		"            		<ATTRIBUTE NAME =\"Target load type\" VALUE =\"Normal\"/>\r\n" + 
	        		"            		<ATTRIBUTE NAME =\"Insert\" VALUE =\"YES\"/>\r\n" + 
	        		"            		<ATTRIBUTE NAME =\"Update as Update\" VALUE =\"NO\"/>\r\n" + 
	        		"            		<ATTRIBUTE NAME =\"Update as Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"            		<ATTRIBUTE NAME =\"Update else Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"           		<ATTRIBUTE NAME =\"Delete\" VALUE =\"NO\"/>\r\n" + 
	        		"        		    <ATTRIBUTE NAME =\"Truncate target table option\" VALUE =\"YES\"/>\r\n" + 
	        		"        		    <ATTRIBUTE NAME =\"Reject file directory\" VALUE =\"$PMBadFileDir\\"+z_conn+"\"/>\r\n" + 
	        		"        		    <ATTRIBUTE NAME =\"Reject filename\" VALUE =\"sc_stg_"+b_entity.toLowerCase()+"1.bad\"/>\r\n" + 
	        		"        		</SESSIONEXTENSION>\r\n" + 
	        		"        		<SESSIONEXTENSION NAME =\"Relational Reader\" SINSTANCENAME =\"SQ_sc_"+b_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Qualifier\" TYPE =\"READER\">\r\n" + 
	        		"        		    <CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" COMPONENTVERSION =\"8005000\" CONNECTIONNAME =\""+b_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\"PWX DB2i5OS\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	        		"        		</SESSIONEXTENSION>\r\n" + 
	        		"        		<SESSIONEXTENSION DSQINSTNAME =\"SQ_sc_"+b_entity+"\" DSQINSTTYPE =\"Source Qualifier\" NAME =\"Relational Reader\" SINSTANCENAME =\"sc_"+b_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Definition\" TYPE =\"READER\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"General Options\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Write Backward Compatible Session Log File\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Session Log File Name\" VALUE =\""+b_session+".log\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Session Log File directory\" VALUE =\"$PMSessionLogDir\\KMA_PROD\\SESSION\\\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Parameter Filename\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Enable Test Load\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"$Source connection value\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"$Target connection value\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Treat source rows as\" VALUE =\"Insert\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Commit Type\" VALUE =\"Target\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Commit Interval\" VALUE =\"10000\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Commit On End Of File\" VALUE =\"YES\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Rollback Transactions on Errors\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Recovery Strategy\" VALUE =\"Restart task\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Java Classpath\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Performance\" VALUE =\"\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"DTM buffer size\" VALUE =\"500000000\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Collect performance data\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Write performance data to repository\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Incremental Aggregation\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Enable high precision\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Session retry on deadlock\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Pushdown Optimization\" VALUE =\"None\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Allow Temporary View for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Allow Temporary Sequence for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Allow Pushdown for User Incompatible Connections\" VALUE =\"NO\"/>\r\n" + 
	        		"    		</SESSION>\r\n" +
	        		//powermart --> repository --> 1st folder --> worklet
	        		"			<WORKLET DESCRIPTION =\"\" ISVALID =\"YES\" NAME =\""+b_worklet+"\" REUSABLE =\"YES\" VERSIONNUMBER =\"1\">\r\n" + 
	        		"        		<TASK DESCRIPTION =\"\" NAME =\"DEC_FAIL\" REUSABLE =\"NO\" TYPE =\"Decision\" VERSIONNUMBER =\"1\">\r\n" + 
	        		"            		<ATTRIBUTE NAME =\"Decision Name\" VALUE =\"\"/>\r\n" + 
	        		"        		</TASK>\r\n" + 
	        		"        		<TASK DESCRIPTION =\"\" NAME =\"Start\" REUSABLE =\"NO\" TYPE =\"Start\" VERSIONNUMBER =\"1\"/>\r\n" + 
	        		"        		<TASK DESCRIPTION =\"\" NAME =\"CONTROL\" REUSABLE =\"NO\" TYPE =\"Control\" VERSIONNUMBER =\"1\">\r\n" + 
	        		"           		 <ATTRIBUTE NAME =\"Control Option\" VALUE =\"Abort top-level workflow\"/>\r\n" + 
	        		"        		</TASK>\r\n" + 
	        		"        		<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"YES\" ISENABLED =\"YES\" NAME =\"DEC_FAIL\" REUSABLE =\"NO\" TASKNAME =\"DEC_FAIL\" TASKTYPE =\"Decision\" TREAT_INPUTLINK_AS_AND =\"NO\"/>\r\n" + 
	        		"        		<TASKINSTANCE DESCRIPTION =\"\" ISENABLED =\"YES\" NAME =\"Start\" REUSABLE =\"NO\" TASKNAME =\"Start\" TASKTYPE =\"Start\"/>\r\n" + 
	        		"        		<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"YES\" ISENABLED =\"YES\" NAME =\"CONTROL\" REUSABLE =\"NO\" TASKNAME =\"CONTROL\" TASKTYPE =\"Control\" TREAT_INPUTLINK_AS_AND =\"NO\"/>\r\n" + 
	        		"        		<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"NO\" ISENABLED =\"YES\" NAME =\""+b_session+"\" REUSABLE =\"YES\" TASKNAME =\""+b_session+"\" TASKTYPE =\"Session\" TREAT_INPUTLINK_AS_AND =\"YES\"/>\r\n" + 
	        		"        		<WORKFLOWLINK CONDITION =\"$"+b_session+".Status= FAILED OR $"+b_session+".Status= ABORTED OR $"+b_session+".Status = STOPPED\" FROMTASK =\""+b_session+"\" TOTASK =\"DEC_FAIL\"/>\r\n" + 
	        		"        		<WORKFLOWLINK CONDITION =\"\" FROMTASK =\"DEC_FAIL\" TOTASK =\"CONTROL\"/>\r\n" + 
	        		"        		<WORKFLOWLINK CONDITION =\"\" FROMTASK =\"Start\" TOTASK =\""+b_session+"\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL.StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL.EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL.Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL.PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL.ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL.ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Evaluation result of condition expression\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL.Condition\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CONTROL.StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CONTROL.EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CONTROL.Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CONTROL.PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CONTROL.ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CONTROL.ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".SrcSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".SrcFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully loaded\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".TgtSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to load\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".TgtFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Total number of transformation errors\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".TotalTransErrors\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error code\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".FirstErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error message\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+b_session+".FirstErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"        		<ATTRIBUTE NAME =\"Allow Concurrent Run\" VALUE =\"NO\"/>\r\n" + 
	        		"    		</WORKLET>\r\n" +
	       
	        		"		</FOLDER>\r\n" +
	        //powermart --> repository --> 2nd folder
	        		"		<FOLDER NAME=\""+b_shortcutFolder+"\" GROUP=\"\" OWNER=\"INFA_ADMIN\" SHARED=\"SHARED\" DESCRIPTION=\"\" PERMISSIONS=\"rwx------\" UUID=\""+UUID.randomUUID().toString()+"\">\r\n" +
	        		"			<SOURCE BUSINESSNAME =\"\" DATABASETYPE =\""+b_dbtype+"\" DBDNAME =\""+b_dbdName+"\" DESCRIPTION =\"DOM reporting - PLC major code header group       \" NAME =\""+b_entity+"\" OBJECTVERSION =\"1\" OWNERNAME =\""+b_schema+"\" VERSIONNUMBER =\"1\">\r\n");
	        
	        int count = 0;
	       for(int row=rowStart; row<=sourceRowEnd; row++) {
	    	   if(!isCellEmpty(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
	    		   count++;
		    	   String dataType = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    	   if(!dataType.isEmpty()) {
			    	   String name = sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	   int precision = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	   int scale = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	   int length = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getNumericCellValue());
			    	   int physical_length = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("F")).getNumericCellValue());
			    	   int physical_offset = (int)(sourceSheet.getRow(row).getCell(CellReference.convertColStringToIndex("G")).getNumericCellValue());
			    	   
		 sb1.append("				<SOURCEFIELD BUSINESSNAME =\"\" DATATYPE =\""+dataType+"\" DESCRIPTION =\""+name+"\" FIELDNUMBER =\""+count+"\" FIELDPROPERTY =\"0\" FIELDTYPE =\"ELEMITEM\" HIDDEN =\"NO\" KEYTYPE =\"NOT A KEY\" LENGTH =\""+length+"\" LEVEL =\"0\" NAME =\""+name+"\" NULLABLE =\"NOTNULL\" OCCURS =\"0\" OFFSET =\"0\" PHYSICALLENGTH =\""+physical_length+"\" PHYSICALOFFSET =\""+physical_offset+"\" PICTURETEXT =\"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\" USAGE_FLAGS =\"\"/>\r\n");
		    	   }
	    	   }
	       }
	        
	     sb1.append("			</SOURCE>\r\n");
	        String dbtype = stage1Sheet.getRow(5).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	     sb1.append("			<TARGET BUSINESSNAME =\"\" CONSTRAINT =\"\" DATABASETYPE =\""+dbtype+"\" DESCRIPTION =\"\" NAME =\""+i_entity+"\" OBJECTVERSION =\"1\" TABLEOPTIONS =\"\" VERSIONNUMBER =\"1\">\r\n");

	        count = 0;
	       for(int row=rowStart; row<=stage1RowEnd; row++) {
	    	   if(!isCellEmpty(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
	    		   count++;
		    	   String dataType = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    	   if(!dataType.isEmpty()) {
			    	   String name = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	   int precision = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	   int scale = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		 sb1.append("				<TARGETFIELD BUSINESSNAME =\"\" DATATYPE =\""+dataType+"\" DESCRIPTION =\"\" FIELDNUMBER =\""+count+"\" KEYTYPE =\"NOT A KEY\" NAME =\""+name+"\" NULLABLE =\"NULL\" PICTURETEXT =\"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
		    	   }
	    	   }
	       }
	     sb1.append("				<TARGETFIELD BUSINESSNAME =\"\" DATATYPE =\"date\" DESCRIPTION =\"\" FIELDNUMBER =\"5\" KEYTYPE =\"NOT A KEY\" NAME =\"REC_CREATE_DATE\" NULLABLE =\"NULL\" PICTURETEXT =\"\" PRECISION =\"19\" SCALE =\"0\"/>\r\n" +
	        		"			</TARGET>\r\n" +
	        		"		</FOLDER>\r\n" +
	        		"	</REPOSITORY>\r\n" +
	        		"</POWERMART>\r\n");
	       
	       
	       //preparing 2nd xml data in sb2
	        StringBuilder sb2 = new StringBuilder();
	        sb2.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
	        		"<POWERMART CREATION_DATE=\""+dateFormat.format(date)+"\" REPOSITORY_VERSION=\""+d_version+"\">\r\n"+
	        		"	<REPOSITORY NAME=\"REPO_HAEA_EDW_DEV\" VERSION=\""+d_version+"\" CODEPAGE=\"UTF-8\" DATABASETYPE=\"Oracle\">\r\n" +
	        		//powermart --> repository --> 1st folder
	        		"		<FOLDER NAME=\""+b_folder+"\" GROUP=\"\" OWNER=\"INFA_ADMIN\" SHARED=\"NOTSHARED\" DESCRIPTION=\"\" PERMISSIONS=\"rwx------\" UUID=\""+UUID.randomUUID().toString()+"\">\r\n" +
	        		
	        		//powermart --> repository --> 1st folder --> 1st mapping open
	        		"			<MAPPING DESCRIPTION =\"\" ISVALID =\"YES\" NAME =\""+z_mapping+"\" OBJECTVERSION =\"1\" VERSIONNUMBER =\"1\">\r\n" + 
	        		
	        		//powermart --> repository --> 1st folder --> 1st transformation
	        		"				<TRANSFORMATION DESCRIPTION =\"\" NAME =\"SQ_sc_"+r_entity+"\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Source Qualifier\" VERSIONNUMBER =\"1\">\r\n" );
	        		
	        for(int row=rowStart; row<=stage2RowEnd; row++) {
		    	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
			    	   String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			    	   if(!dataType.isEmpty()) {
			    		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
				    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
				    	String dataTypeinXml = "";
				    	if(dataType.equalsIgnoreCase("varchar2")) {
				    		dataTypeinXml = "string";
				    	}else if(dataType.equalsIgnoreCase("date")) {
				    		dataTypeinXml = "date/time";
				    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
				    		dataTypeinXml = "decimal";
				    	}
		sb2.append("					<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
			    	   }
			    	}
	        }
	        		
		 sb2.append("					<TABLEATTRIBUTE NAME =\"Sql Query\" VALUE =\"\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"User Defined Join\" VALUE =\"\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Source Filter\" VALUE =\"\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Number Of Sorted Ports\" VALUE =\"0\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Select Distinct\" VALUE =\"NO\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Is Partitionable\" VALUE =\"NO\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Pre SQL\" VALUE =\"\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Post SQL\" VALUE =\"\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Output is deterministic\" VALUE =\"YES\"/>\r\n" + 
	        		"					<TABLEATTRIBUTE NAME =\"Output is repeatable\" VALUE =\"Never\"/>\r\n" + 
	        		"				</TRANSFORMATION>\r\n" +
	        		
	        		//powermart --> repository --> 1st folder --> 2nd transformation
	        		//powermart --> repository --> 1st folder --> 3rd transformation
		 			   
	        		"					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"RTRTRANS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Router\" VERSIONNUMBER =\"1\">\r\n" + 
	        		"						<GROUP DESCRIPTION =\"\" NAME =\"INPUT\" ORDER =\"1\" TYPE =\"INPUT\"/>\r\n" ); 
	        		
String b_insert = sheet2.getRow(13).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
String b_update = sheet2.getRow(14).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();

	        		
	        sb2.append("						<GROUP DESCRIPTION =\"\" EXPRESSION =\""+b_insert+"\" NAME =\"INSERT\" ORDER =\"2\" TYPE =\"OUTPUT\"/>\r\n" + 
	        		"						<GROUP DESCRIPTION =\"Path for the data when none of the group conditions are satisfied.\" NAME =\"DEFAULT1\" ORDER =\"4\" TYPE =\"OUTPUT/DEFAULT\"/>\r\n" + 
	        		"						<GROUP DESCRIPTION =\"\" EXPRESSION =\""+b_update+"\" NAME =\"UPDATE\" ORDER =\"3\" TYPE =\"OUTPUT\"/>\r\n" );
	        for(int row=rowStart; row<=stage2RowEnd; row++) {
		    	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
			    	   String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			    	   if(!dataType.isEmpty()) {
			    		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
				    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
				    	String dataTypeinXml = "";
				    	if(dataType.equalsIgnoreCase("varchar2")) {
				    		dataTypeinXml = "string";
				    	}else if(dataType.equalsIgnoreCase("date")) {
				    		dataTypeinXml = "date/time";
				    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
				    		dataTypeinXml = "decimal";
				    	}
		sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
			    	   }
			    	}
	        } 	
	        
	        
	        		for(int row=rowStart; row<=stage2RowEnd; row++) {
		 		    	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
		 			    	   String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		 			    	   if(!dataType.isEmpty()) {
		 			    		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		 			    		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		 				    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		 				    	String dataTypeinXml = "";
		 				    	if(dataType.equalsIgnoreCase("varchar2")) {
		 				    		dataTypeinXml = "string";
		 				    	}else if(dataType.equalsIgnoreCase("date")) {
		 				    		dataTypeinXml = "date/time";
		 				    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
		 				    		dataTypeinXml = "decimal";
		 				    	}
		 		sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INSERT\" NAME =\""+name+"1\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
		 			    	   }
		 			    	}
		 	        } 			
	        		
for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
  	   String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
  	   if(!dataType.isEmpty()) {
  		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
  		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	    	String dataTypeinXml = "";
	    	if(dataType.equalsIgnoreCase("varchar2")) {
	    		dataTypeinXml = "string";
	    	}else if(dataType.equalsIgnoreCase("date")) {
	    		dataTypeinXml = "date/time";
	    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	    		dataTypeinXml = "decimal";
	    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"UPDATE\" NAME =\""+name+"3\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
  	   }
  	}
} 		
	        		
for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
  	   String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
  	   if(!dataType.isEmpty()) {
  		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
  		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	    	String dataTypeinXml = "";
	    	if(dataType.equalsIgnoreCase("varchar2")) {
	    		dataTypeinXml = "string";
	    	}else if(dataType.equalsIgnoreCase("date")) {
	    		dataTypeinXml = "date/time";
	    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	    		dataTypeinXml = "decimal";
	    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\""+name+"2\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
  	   }
  	}
} 		
	        		
sb2.append("						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	       "					</TRANSFORMATION>\r\n" +
	        		//powermart --> repository --> 1st folder --> 4th transformation
	       "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXP_"+z_entity+"_INS_REN\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n" ); 
for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
	   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	   if(!dataType.isEmpty()) {
		String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    	int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	    	String porttype =  "INPUT/OUTPUT";
	    	String dataTypeinXml = "";
	    	String nameinXml="";
	    	if(dataType.equalsIgnoreCase("varchar2")) {
	    		dataTypeinXml = "string";
	    		nameinXml = name+"1";
	    	}else if(dataType.equalsIgnoreCase("date")) {
	    		dataTypeinXml = "date/time";
	    		nameinXml = "O_"+name;
	    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	    		dataTypeinXml = "decimal";
	    		nameinXml = name+"1";
	    	}
	    	String expression =  nameinXml;
	    	if(name.equals("EFF_FROM_DATE")) {
	    		expression = "TO_DATE($$EFF_DATE, 'MM/DD/YYYY HH24:MI:SS')";
	    	}
	    	if(name.equals("EFF_TO_DATE")) {
	    		expression = "TO_DATE('12/31/9999', 'MM/DD/YYYY')";
	    	}
	    	if(name.equals("REC_CREATE_DATE") || name.equals("REC_UPDATE_DATE")) {
	    		expression = "SYSDATE";
	    	}
	    	if(nameinXml.startsWith("O_")) {
	    		porttype = "OUTPUT";
	    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+expression+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+nameinXml+"\" PICTURETEXT =\"\" PORTTYPE =\""+porttype+"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	   }
	}
} 		        	

sb2.append("						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	       "					</TRANSFORMATION>\r\n" +
	        	
	        		//powermart --> repository --> 1st folder --> 5th transformation
	       "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXP_"+z_entity+"_UPD_DEL\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n" );
for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
	   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	   if(!dataType.isEmpty()) {
		String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		if(!name.equals("REC_CREATE_DATE")) {
			int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    	int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	    	String porttype =  "INPUT/OUTPUT";
	    	String dataTypeinXml = "";
	    	String nameinXml= name+"3";;
	    	if(dataType.equalsIgnoreCase("varchar2")) {
	    		dataTypeinXml = "string";
	    	}else if(dataType.equalsIgnoreCase("date")) {
	    		dataTypeinXml = "date/time";
	    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	    		dataTypeinXml = "decimal";
	    	}
	    	String expression =  nameinXml;
	    	if(name.equals("EFF_TO_DATE")) {
	    		nameinXml = "O_"+name+"3";
	    		expression = "SYSDATE";
	    	}
	    	if(name.equals("REC_CREATE_DATE") || name.equals("REC_UPDATE_DATE")) {
	    		nameinXml = "O_"+name+"3";
	    		expression = "SYSDATE";
	    	}
	    	if(nameinXml.startsWith("O_")) {
	    		porttype = "OUTPUT";
	    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+expression+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+nameinXml+"\" PICTURETEXT =\"\" PORTTYPE =\""+porttype+"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
		}
	   }
	}
}  	        		
sb2.append(    		"						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        		"					</TRANSFORMATION>\r\n" +
	        		
	        		//powermart --> repository --> 1st folder --> 6th transformation
	        		
	        		"					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"UPDTRANS_UPD\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Update Strategy\" VERSIONNUMBER =\"1\">\r\n" );
for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")))) {
		   String primaryKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
	   if(primaryKey.equals("Y")) {
		   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    	int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	    	String porttype =  "INPUT/OUTPUT";
	    	String dataTypeinXml = "";
	    	String nameinXml = name+"3";
	    	if(dataType.equalsIgnoreCase("varchar2")) {
	    		dataTypeinXml = "string";
	    	}else if(dataType.equalsIgnoreCase("date")) {
	    		dataTypeinXml = "date/time";
	    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	    		dataTypeinXml = "decimal";
	    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+nameinXml+"\" PICTURETEXT =\"\" PORTTYPE =\""+porttype+"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	   }
	}
}
sb2.append("<TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\"O_EFF_TO_DATE3\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n" + 
		"            <TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\"O_REC_UPDATE_DATE3\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n"+
"						<TABLEATTRIBUTE NAME =\"Update Strategy Expression\" VALUE =\"DD_UPDATE\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Forward Rejected Rows\" VALUE =\"YES\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	       "					</TRANSFORMATION>\r\n" +
	        		//powermart --> repository --> 1st folder --> instances and connectors
	        		
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_"+z_entity+"_INS_REN\" TRANSFORMATION_NAME =\"sc_"+z_entity+"\" TRANSFORMATION_TYPE =\"Target Definition\" TYPE =\"TARGET\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_"+z_entity+"_UPD_DEL\" TRANSFORMATION_NAME =\"sc_"+z_entity+"\" TRANSFORMATION_TYPE =\"Target Definition\" TYPE =\"TARGET\"/>\r\n" + 
	       "					<INSTANCE DBDNAME =\""+i_conn+"\" DESCRIPTION =\"\" NAME =\"sc_"+r_entity+"\" TRANSFORMATION_NAME =\"sc_"+r_entity+"\" TRANSFORMATION_TYPE =\"Source Definition\" TYPE =\"SOURCE\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"SQ_sc_"+r_entity+"\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"SQ_sc_"+r_entity+"\" TRANSFORMATION_TYPE =\"Source Qualifier\" TYPE =\"TRANSFORMATION\">\r\n" + 
	       "						<ASSOCIATED_SOURCE_INSTANCE NAME =\"sc_"+r_entity+"\"/>\r\n" + 
	       "					</INSTANCE>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"RTRTRANS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"RTRTRANS\" TRANSFORMATION_TYPE =\"Router\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXP_"+z_entity+"_INS_REN\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXP_"+z_entity+"_INS_REN\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXP_"+z_entity+"_UPD_DEL\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXP_"+z_entity+"_UPD_DEL\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"UPDTRANS_UPD\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"UPDTRANS_UPD\" TRANSFORMATION_TYPE =\"Update Strategy\" TYPE =\"TRANSFORMATION\"/>\r\n" ); 

for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")))) {
		   String primaryKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
	   if(primaryKey.equals("Y")) {
		   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	    	String nameinXml = name+"3";
	    	sb2.append("					<CONNECTOR FROMFIELD =\""+nameinXml+"\" FROMINSTANCE =\"UPDTRANS_UPD\" FROMINSTANCETYPE =\"Update Strategy\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+z_entity+"_UPD_DEL\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	   }
	}
}
sb2.append("<CONNECTOR FROMFIELD =\"O_REC_UPDATE_DATE3\" FROMINSTANCE =\"UPDTRANS_UPD\" FROMINSTANCETYPE =\"Update Strategy\" TOFIELD =\"REC_UPDATE_DATE\" TOINSTANCE =\"sc_"+z_entity+"_UPD_DEL\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
sb2.append("<CONNECTOR FROMFIELD =\"O_EFF_TO_DATE3\" FROMINSTANCE =\"UPDTRANS_UPD\" FROMINSTANCETYPE =\"Update Strategy\" TOFIELD =\"EFF_TO_DATE\" TOINSTANCE =\"sc_"+z_entity+"_UPD_DEL\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");


for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
		   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		   if(!dataType.isEmpty()) {
				String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	String nameInXml = name+"1";
			    	if(dataType.equalsIgnoreCase("date")) {
			    		nameInXml = "O_"+name;
			    	}
	    	sb2.append("					<CONNECTOR FROMFIELD =\""+nameInXml+"\" FROMINSTANCE =\"EXP_"+z_entity+"_INS_REN\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+z_entity+"_INS_REN\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	   }
	}
}

for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   if(!name.isEmpty()) {
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"sc_"+r_entity+"\" FROMINSTANCETYPE =\"Source Definition\" TOFIELD =\""+name+"\" TOINSTANCE =\"SQ_sc_"+r_entity+"\" TOINSTANCETYPE =\"Source Qualifier\"/>\r\n");
	   }
	}
} 

for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   if(!name.isEmpty()) {
		   sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"SQ_sc_"+r_entity+"\" FROMINSTANCETYPE =\"Source Qualifier\" TOFIELD =\""+name+"\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	   }
	}
}

for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
		   String connectorKey =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
	   if(connectorKey.equals("Y")) {
		   String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		   
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"1"+"\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\""+name+"1"+"\" TOINSTANCE =\"EXP_"+z_entity+"_INS_REN\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	   }
	}
}

for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
		   String connectorKey =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
	   if(connectorKey.equals("Y")) {
		   String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		   
		   sb2.append("					<CONNECTOR FROMFIELD =\""+name+"3"+"\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\""+name+"3"+"\" TOINSTANCE =\"EXP_"+z_entity+"_UPD_DEL\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	   }
	}
}
sb2.append("<CONNECTOR FROMFIELD =\"EFF_FROM_DATE3\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\"EFF_FROM_DATE3\" TOINSTANCE =\"EXP_"+z_entity+"_UPD_DEL\" TOINSTANCETYPE =\"Expression\"/>\r\n");

for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")))) {
		   String primaryKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
	   if(primaryKey.equals("Y")) {
		   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	    	String nameinXml = name+"3";
	    	sb2.append("					<CONNECTOR FROMFIELD =\""+nameinXml+"\" FROMINSTANCE =\"EXP_"+z_entity+"_UPD_DEL\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+nameinXml+"\" TOINSTANCE =\"UPDTRANS_UPD\" TOINSTANCETYPE =\"Update Strategy\"/>\r\n");
	   }
	}
}

sb2.append("<CONNECTOR FROMFIELD =\"O_EFF_TO_DATE3\" FROMINSTANCE =\"EXP_"+z_entity+"_UPD_DEL\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"O_EFF_TO_DATE3\" TOINSTANCE =\"UPDTRANS_UPD\" TOINSTANCETYPE =\"Update Strategy\"/>\r\n" + 
		"        <CONNECTOR FROMFIELD =\"O_REC_UPDATE_DATE3\" FROMINSTANCE =\"EXP_"+z_entity+"_UPD_DEL\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"O_REC_UPDATE_DATE3\" TOINSTANCE =\"UPDTRANS_UPD\" TOINSTANCETYPE =\"Update Strategy\"/>\r\n");

sb2.append("					<TARGETLOADORDER ORDER =\"1\" TARGETINSTANCE =\"sc_"+z_entity+"_INS_REN\"/>\r\n" + 
	       "					<TARGETLOADORDER ORDER =\"1\" TARGETINSTANCE =\"sc_"+z_entity+"_UPD_DEL\"/>\r\n" + 
	       "					<MAPPINGVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" ISEXPRESSIONVARIABLE =\"NO\" ISPARAM =\"YES\" NAME =\"$$EFF_DATE\" PRECISION =\"2910\" SCALE =\"0\" USERDEFINED =\"YES\"/>\r\n" + 
	       "					<ERPINFO/>\r\n" + 
	     //powermart --> repository --> 1st folder --> 1st mapping closed
	       "				</MAPPING>\r\n" +
	        		
	       "				<SHORTCUT COMMENTS =\"\" DBDNAME =\""+i_conn+"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\"sc_"+r_entity+"\" OBJECTSUBTYPE =\"Source Definition\" OBJECTTYPE =\"SOURCE\" REFERENCEDDBD =\""+i_conn+"\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\""+r_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_DEV\" VERSIONNUMBER =\"1\"/>\r\n" + 
	       "				<SHORTCUT COMMENTS =\"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\"sc_"+z_entity+"\" OBJECTSUBTYPE =\"Target Definition\" OBJECTTYPE =\"TARGET\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\""+z_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_DEV\" VERSIONNUMBER =\"1\"/>\r\n" + 
	       "				<CONFIG DESCRIPTION =\"Default session configuration object\" ISDEFAULT =\"YES\" NAME =\"default_session_config\" VERSIONNUMBER =\"15\">\r\n" + 
	       "					<ATTRIBUTE NAME =\"Advanced\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Constraint based load ordering\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Cache LOOKUP() function\" VALUE =\"YES\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Default buffer block size\" VALUE =\"256000\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Line Sequential buffer length\" VALUE =\"2048\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Maximum Memory Allowed For Auto Memory Attributes\" VALUE =\"640MB\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Maximum Percentage of Total Memory Allowed For Auto Memory Attributes\" VALUE =\"10\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Additional Concurrent Pipelines for Lookup Cache Creation\" VALUE =\"0\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Custom Properties\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Pre-build lookup cache\" VALUE =\"Auto\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Optimization Level\" VALUE =\"Medium\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"DateTime Format String\" VALUE =\"MM/DD/YYYY HH24:MI:SS.US\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Pre 85 Timestamp Compatibility\" VALUE =\"YES\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Log Options\" VALUE =\"0\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Save session log by\" VALUE =\"Session runs\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Save session log for these runs\" VALUE =\"0\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Session Log File Max Size\" VALUE =\"0\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Session Log File Max Time Period\" VALUE =\"0\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Maximum Partial Session Log Files\" VALUE =\"1\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Writer Commit Statistics Log Frequency\" VALUE =\"1\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Writer Commit Statistics Log Interval\" VALUE =\"0\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Error handling\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Stop on errors\" VALUE =\"0\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Override tracing\" VALUE =\"None\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"On Stored Procedure error\" VALUE =\"Stop\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"On Pre-session command task error\" VALUE =\"Stop\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"On Pre-Post SQL error\" VALUE =\"Stop\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Enable Recovery\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Error Log Type\" VALUE =\"None\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Error Log Table Name Prefix\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Error Log File Name\" VALUE =\"PMError.log\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Log Source Row Data\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Data Column Delimiter\" VALUE =\"|\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Partitioning Options\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Dynamic Partitioning\" VALUE =\"Disabled\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Number of Partitions\" VALUE =\"1\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Multiplication Factor\" VALUE =\"Auto\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Session on Grid\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Is Enabled\" VALUE =\"NO\"/>\r\n" + 
	       "				</CONFIG>\r\n" + 
	       "				<SESSION DESCRIPTION =\"\" ISVALID =\"YES\" MAPPINGNAME =\""+z_mapping+"\" NAME =\""+z_session+"\" REUSABLE =\"YES\" SORTORDER =\"Binary\" VERSIONNUMBER =\"2\">\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"sc_"+z_entity+"_INS_REN\" STAGE =\"1\" TRANSFORMATIONNAME =\"sc_"+z_entity+"_INS_REN\" TRANSFORMATIONTYPE =\"Target Definition\"/>\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"sc_"+z_entity+"_UPD_DEL\" STAGE =\"2\" TRANSFORMATIONNAME =\"sc_"+z_entity+"_UPD_DEL\" TRANSFORMATIONTYPE =\"Target Definition\"/>\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"0\" SINSTANCENAME =\"sc_"+r_entity+"\" STAGE =\"0\" TRANSFORMATIONNAME =\"sc_"+r_entity+"\" TRANSFORMATIONTYPE =\"Source Definition\"/>\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"SQ_sc_"+r_entity+"\" STAGE =\"3\" TRANSFORMATIONNAME =\"SQ_sc_"+r_entity+"\" TRANSFORMATIONTYPE =\"Source Qualifier\"/>\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"RTRTRANS\" STAGE =\"3\" TRANSFORMATIONNAME =\"RTRTRANS\" TRANSFORMATIONTYPE =\"Router\">\r\n" + 
	       "						<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "					</SESSTRANSFORMATIONINST>\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXP_"+z_entity+"_INS_REN\" STAGE =\"3\" TRANSFORMATIONNAME =\"EXP_"+z_entity+"_INS_REN\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	       "						<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "					</SESSTRANSFORMATIONINST>\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXP_"+z_entity+"_UPD_DEL\" STAGE =\"3\" TRANSFORMATIONNAME =\"EXP_"+z_entity+"_UPD_DEL\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	       "						<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "					</SESSTRANSFORMATIONINST>\r\n" + 
	       "					<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"UPDTRANS_UPD\" STAGE =\"3\" TRANSFORMATIONNAME =\"UPDTRANS_UPD\" TRANSFORMATIONTYPE =\"Update Strategy\">\r\n" + 
	       "						<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "					</SESSTRANSFORMATIONINST>\r\n" + 
	       "					<CONFIGREFERENCE REFOBJECTNAME =\"default_session_config\" TYPE =\"Session config\">\r\n" + 
	       "						<ATTRIBUTE NAME =\"Error Log Type\" VALUE =\"Relational Database\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Error Log DB Connection\" VALUE =\"Relational:KMA_CTRL\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Error Log Table Name Prefix\" VALUE =\"CTRL_ETL_AUDIT_INF_\"/>\r\n" + 
	       "					</CONFIGREFERENCE>\r\n" + 
	       "					<SESSIONEXTENSION NAME =\"Relational Writer\" SINSTANCENAME =\"sc_"+z_entity+"_INS_REN\" SUBTYPE =\"Relational Writer\" TRANSFORMATIONTYPE =\"Target Definition\" TYPE =\"WRITER\">\r\n" + 
	       "						<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+z_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+z_Dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Target load type\" VALUE =\"Normal\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Insert\" VALUE =\"YES\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Update as Update\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Update as Insert\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Update else Insert\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Delete\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Truncate target table option\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Reject file directory\" VALUE =\"$PMBadFileDir\\"+z_conn+"\\\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Reject filename\" VALUE =\"sc_"+z_entity.toLowerCase()+"_ins_ren1.bad\"/>\r\n" + 
	       "					</SESSIONEXTENSION>\r\n" + 
	       "					<SESSIONEXTENSION NAME =\"Relational Writer\" SINSTANCENAME =\"sc_"+z_entity+"_UPD_DEL\" SUBTYPE =\"Relational Writer\" TRANSFORMATIONTYPE =\"Target Definition\" TYPE =\"WRITER\">\r\n" + 
	       "						<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+z_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+z_Dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Target load type\" VALUE =\"Normal\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Insert\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Update as Update\" VALUE =\"YES\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Update as Insert\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Update else Insert\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Delete\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Truncate target table option\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Reject file directory\" VALUE =\"$PMBadFileDir\\"+z_conn+"\\\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Reject filename\" VALUE =\"sc_"+z_entity.toLowerCase()+"_upd_del1.bad\"/>\r\n" + 
	       "					</SESSIONEXTENSION>\r\n" + 
	       "					<SESSIONEXTENSION DSQINSTNAME =\"SQ_sc_"+r_entity+"\" DSQINSTTYPE =\"Source Qualifier\" NAME =\"Relational Reader\" SINSTANCENAME =\"sc_"+r_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Definition\" TYPE =\"READER\"/>\r\n" + 
	       "					<SESSIONEXTENSION NAME =\"Relational Reader\" SINSTANCENAME =\"SQ_sc_"+r_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Qualifier\" TYPE =\"READER\">\r\n" + 
	       "						<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+i_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+i_dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	       "					</SESSIONEXTENSION>\r\n" + 
	       "						<ATTRIBUTE NAME =\"General Options\" VALUE =\"\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Write Backward Compatible Session Log File\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Session Log File Name\" VALUE =\""+z_session+".log\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Session Log File directory\" VALUE =\"$PMSessionLogDir\\"+z_conn+"\\SESSION\\\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Parameter Filename\" VALUE =\"$PMLookupFileDir\\"+z_conn+"\\ETL_PARAM_KMA_DW_GRP6.TXT\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Enable Test Load\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"$Source connection value\" VALUE =\"\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"$Target connection value\" VALUE =\"\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Treat source rows as\" VALUE =\"Data driven\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Commit Type\" VALUE =\"Target\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Commit Interval\" VALUE =\"10000\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Commit On End Of File\" VALUE =\"YES\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Rollback Transactions on Errors\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Recovery Strategy\" VALUE =\"Restart task\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Java Classpath\" VALUE =\"\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Performance\" VALUE =\"\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"DTM buffer size\" VALUE =\"50000000\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Collect performance data\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Write performance data to repository\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Incremental Aggregation\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Enable high precision\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Session retry on deadlock\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Pushdown Optimization\" VALUE =\"None\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Allow Temporary View for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Allow Temporary Sequence for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	       "						<ATTRIBUTE NAME =\"Allow Pushdown for User Incompatible Connections\" VALUE =\"NO\"/>\r\n" + 
	       "					</SESSION>\r\n" + 
	     //powermart --> repository --> 1st folder --> 2nd mapping open
	 
	 "					<MAPPING DESCRIPTION =\"\" ISVALID =\"YES\" NAME =\""+del_mapping+"\" OBJECTVERSION =\"1\" VERSIONNUMBER =\"1\">\r\n" + 
	       "						<TRANSFORMATION DESCRIPTION =\"\" NAME =\"SQ_sc_"+z_entity+"\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Source Qualifier\" VERSIONNUMBER =\"1\">\r\n" );
	        		for(int row=rowStart; row<=prodRowEnd; row++) {
	 		    	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
	 			    	   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	 			    	   if(!dataType.isEmpty()) {
	 			    		String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	 			    		int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	 				    	int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	 				    	String dataTypeinXml = "";
	 				    	if(dataType.equalsIgnoreCase("varchar2")) {
	 				    		dataTypeinXml = "string";
	 				    	}else if(dataType.equalsIgnoreCase("date")) {
	 				    		dataTypeinXml = "date/time";
	 				    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	 				    		dataTypeinXml = "decimal";
	 				    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	 			    	   }
	 			    	}
	 	        }
	        	String sourceFilter = sheet2.getRow(6).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        	String deleteExpr = sheet2.getRow(8).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
sb2.append("						<TABLEATTRIBUTE NAME =\"Sql Query\" VALUE =\"\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"User Defined Join\" VALUE =\"\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Source Filter\" VALUE =\""+sourceFilter+"\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Number Of Sorted Ports\" VALUE =\"0\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Select Distinct\" VALUE =\"NO\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Is Partitionable\" VALUE =\"NO\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Pre SQL\" VALUE =\"\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Post SQL\" VALUE =\"\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Output is deterministic\" VALUE =\"YES\"/>\r\n" + 
	       "						<TABLEATTRIBUTE NAME =\"Output is repeatable\" VALUE =\"Never\"/>\r\n" + 
	       "					</TRANSFORMATION>\r\n" + 
	      "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXPTRANS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n" );
	       
for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
  	   String expTrans = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
  	   if(!expTrans.isEmpty()) {
  		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
  		String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
  		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	    	String dataTypeinXml = "";
	    	String porttype = "INPUT/OUTPUT";
	    	String expression=name;
	    	if(dataType.equalsIgnoreCase("varchar2")) {
	    		dataTypeinXml = "string";
	    	}else if(dataType.equalsIgnoreCase("date")) {
	    		dataTypeinXml = "date/time";
	    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	    		dataTypeinXml = "decimal";
	    	}
	    	String nameInxml = name;
	    	if(expTrans.equalsIgnoreCase("D")) {
	    		expression="'"+expTrans+"'";
	    		nameInxml = "O_"+name;
	    		porttype = "OUTPUT";
	    	}
	    	
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+expression+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+nameInxml+"\" PICTURETEXT =\"\" PORTTYPE =\""+porttype+"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
  	   }
  	}
}
	        		
sb2.append("						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	       "					</TRANSFORMATION>\r\n" + 
	       "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"RTRTRANS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Router\" VERSIONNUMBER =\"1\">\r\n" + 
	       "						<GROUP DESCRIPTION =\"\" NAME =\"INPUT\" ORDER =\"1\" TYPE =\"INPUT\"/>\r\n" + 
	       "						<GROUP DESCRIPTION =\"\" EXPRESSION =\""+deleteExpr+"\" NAME =\"DELETE\" ORDER =\"2\" TYPE =\"OUTPUT\"/>\r\n" + 
	       "						<GROUP DESCRIPTION =\"Path for the data when none of the group conditions are satisfied.\" NAME =\"DEFAULT1\" ORDER =\"3\" TYPE =\"OUTPUT/DEFAULT\"/>\r\n" );

for(int row=rowStart; row<=stage1RowEnd; row++) {
	   if(!isCellEmpty(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")))) {
	   String rtrTrans = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")).getStringCellValue();
	   if(!rtrTrans.isEmpty()) {
		   if(rtrTrans.equals("Y")) {
			   String name = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			   String dataType =  stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
				int precision = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	int scale = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String dataTypeinXml = "";
			    	if(dataType.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(dataType.equalsIgnoreCase("date")) {
			    		dataTypeinXml = "date/time";
			    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
		sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
		   }
		}
	}
}
for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
	   String rtrTrans = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
	   if(!rtrTrans.isEmpty()) {
		   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		   String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		    	String dataTypeinXml = "";
		    	if(dataType.equalsIgnoreCase("varchar2")) {
		    		dataTypeinXml = "string";
		    	}else if(dataType.equalsIgnoreCase("date")) {
		    		dataTypeinXml = "date/time";
		    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
		    		dataTypeinXml = "decimal";
		    	}
		sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
		}
	}
}
for(int row=rowStart; row<=stage1RowEnd; row++) {
	   if(!isCellEmpty(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")))) {
	   String rtrTrans = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")).getStringCellValue();
	   if(!rtrTrans.isEmpty()) {
		   if(rtrTrans.equals("Y")) {
			   String name = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			   String dataType =  stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
				int precision = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	int scale = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String dataTypeinXml = "";
			    	if(dataType.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(dataType.equalsIgnoreCase("date")) {
			    		dataTypeinXml = "date/time";
			    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
			    	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DELETE\" NAME =\""+name+"1"+"\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
		   }
		}
	}
}
for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
	   String rtrTrans = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
	   if(!rtrTrans.isEmpty()) {
		   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		   String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		    	String dataTypeinXml = "";
		    	if(dataType.equalsIgnoreCase("varchar2")) {
		    		dataTypeinXml = "string";
		    	}else if(dataType.equalsIgnoreCase("date")) {
		    		dataTypeinXml = "date/time";
		    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
		    		dataTypeinXml = "decimal";
		    	}
		    	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DELETE\" NAME =\""+name+"1"+"\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
		}
	}
}
for(int row=rowStart; row<=stage1RowEnd; row++) {
	   if(!isCellEmpty(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")))) {
	   String rtrTrans = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")).getStringCellValue();
	   if(!rtrTrans.isEmpty()) {
		   if(rtrTrans.equals("Y")) {
			   String name = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			   String dataType =  stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
				int precision = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	int scale = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String dataTypeinXml = "";
			    	if(dataType.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(dataType.equalsIgnoreCase("date")) {
			    		dataTypeinXml = "date/time";
			    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
			    	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\""+name+"2"+"\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
		   }
		}
	}
}
for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
	   String rtrTrans = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
	   if(!rtrTrans.isEmpty()) {
		   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		   String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		    	String dataTypeinXml = "";
		    	if(dataType.equalsIgnoreCase("varchar2")) {
		    		dataTypeinXml = "string";
		    	}else if(dataType.equalsIgnoreCase("date")) {
		    		dataTypeinXml = "date/time";
		    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
		    		dataTypeinXml = "decimal";
		    	}
		    	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\""+name+"2"+"\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
		}
	}
}
	        		
sb2.append("						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
		   "					</TRANSFORMATION>\r\n" + 
		   "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_DEL\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n"); 

for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
	   String transkey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
	   if(!transkey.isEmpty()) {
		   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	    	String expression =  name+"1";
	    	String dataTypeinXml = "";
	    	if(dataType.equalsIgnoreCase("varchar2")) {
	    		dataTypeinXml = "string";
	    	}else if(dataType.equalsIgnoreCase("date")) {
	    		dataTypeinXml = "date/time";
	    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	    		dataTypeinXml = "decimal";
	    	}
	    	if(transkey.equals("D")) {
	    		expression = name;
	    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+expression+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+expression+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	   }
	}
}
sb2.append("<TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_REC_CREATE_DATE\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n" + 
		"            <TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_REC_UPDATE_DATE\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n");	        		
sb2.append("						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
		   "					</TRANSFORMATION>\r\n" + 
		   "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_"+r_entity+"_DEL\" TRANSFORMATION_NAME =\"sc_"+r_entity+"\" TRANSFORMATION_TYPE =\"Target Definition\" TYPE =\"TARGET\"/>\r\n" + 
		   "					<INSTANCE DESCRIPTION =\"\" NAME =\"SQ_sc_"+z_entity+"\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"SQ_sc_"+z_entity+"\" TRANSFORMATION_TYPE =\"Source Qualifier\" TYPE =\"TRANSFORMATION\">\r\n" + 
		   "						<ASSOCIATED_SOURCE_INSTANCE NAME =\"sc_"+z_entity+"\"/>\r\n" + 
		   "					</INSTANCE>\r\n" + 
		   "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXPTRANS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXPTRANS\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
		   "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_RLKP_"+i_entity+"\" REUSABLE =\"YES\" TRANSFORMATION_NAME =\"sc_RLKP_"+i_entity+"\" TRANSFORMATION_TYPE =\"Lookup Procedure\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
		   "					<INSTANCE DESCRIPTION =\"\" NAME =\"RTRTRANS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"RTRTRANS\" TRANSFORMATION_TYPE =\"Router\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
		   "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_DEL\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXP_"+r_entity+"_DEL\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
		   "					<INSTANCE DBDNAME =\""+z_conn+"\" DESCRIPTION =\"\" NAME =\"sc_"+z_entity+"\" TRANSFORMATION_NAME =\"sc_"+z_entity+"\" TRANSFORMATION_TYPE =\"Source Definition\" TYPE =\"SOURCE\"/>\r\n" ); 
		   
for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
	   String transkey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
	   if(!transkey.isEmpty()) {
		   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		   String fromField = name+"1";
		   if(transkey.equals("D")) {
			   fromField = name;
		   }
sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"EXP_"+r_entity+"_DEL\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+r_entity+"_DEL\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	   }
	}
}
sb2.append("<CONNECTOR FROMFIELD =\"O_REC_CREATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_DEL\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_CREATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_DEL\" TOINSTANCETYPE =\"Target Definition\"/>\r\n" + 
		"        <CONNECTOR FROMFIELD =\"O_REC_UPDATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_DEL\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_UPDATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_DEL\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");

for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
	   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   if(!name.isEmpty()) {
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"sc_"+z_entity+"\" FROMINSTANCETYPE =\"Source Definition\" TOFIELD =\""+name+"\" TOINSTANCE =\"SQ_sc_"+z_entity+"\" TOINSTANCETYPE =\"Source Qualifier\"/>\r\n");
	   }
	}
}

for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")))) {
	   String primKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
	   if(primKey.equals("Y")) {
		   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"SQ_sc_"+z_entity+"\" FROMINSTANCETYPE =\"Source Qualifier\" TOFIELD =\""+name+"\" TOINSTANCE =\"EXPTRANS\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	   }
	}
}
for(int row=rowStart; row<=prodRowEnd; row++) {
	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")))) {
	   String primKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
	   if(primKey.equals("Y")) {
		   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	   }
	}
}
	
for(int row=rowStart; row<=lkpStage1RowEnd; row++) {
	   if(!isCellEmpty(lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")))) {
	   String fromField = lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getStringCellValue();
	   String toField = lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   if(toField.startsWith("I_")) {
		   sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+toField+"\" TOINSTANCE =\"sc_"+v_descr+"\" TOINSTANCETYPE =\"Lookup Procedure\"/>\r\n");
	   }else {
		   sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"sc_"+v_descr+"\" FROMINSTANCETYPE =\"Lookup Procedure\" TOFIELD =\""+toField+"\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	   }
	}
}

for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
		  String expTransVal = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
		  if(expTransVal.equals("D")){
			  String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		sb2.append("					<CONNECTOR FROMFIELD =\"O_"+name+"\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
			
		  }
	  }
}

for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("M")))) {
	   String fromField = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   String toField = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("M")).getStringCellValue();
sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"1\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\""+toField+"\" TOINSTANCE =\"EXP_"+r_entity+"_DEL\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	}
}

sb2.append("					<TARGETLOADORDER ORDER =\"1\" TARGETINSTANCE =\"sc_"+r_entity+"_DEL\"/>\r\n" + 
		   "					<ERPINFO/>\r\n" + 
	        		//powermart --> repository --> 1st folder --> 2nd mapping close
	       "				</MAPPING>\r\n" + 
	       "				<SHORTCUT COMMENTS =\"\" DBDNAME =\""+z_conn+"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\"sc_"+z_entity+"\" OBJECTSUBTYPE =\"Source Definition\" OBJECTTYPE =\"SOURCE\" REFERENCEDDBD =\""+z_conn+"\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\""+z_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_DEV\" VERSIONNUMBER =\"1\"/>\r\n" + 
	       "				<SHORTCUT COMMENTS =\"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\"sc_"+r_entity+"\" OBJECTSUBTYPE =\"Target Definition\" OBJECTTYPE =\"TARGET\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\""+r_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_DEV\" VERSIONNUMBER =\"1\"/>\r\n" + 
	       "				<SHORTCUT COMMENTS =\"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\"sc_RLKP_"+i_entity+"\" OBJECTSUBTYPE =\"Lookup Procedure\" OBJECTTYPE =\"TRANSFORMATION\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\"RLKP_"+i_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_DEV\" VERSIONNUMBER =\"1\"/>\r\n" + 
	       "				<SESSION DESCRIPTION =\"\" ISVALID =\"YES\" MAPPINGNAME =\""+del_mapping+"\" NAME =\""+del_session+"\" REUSABLE =\"YES\" SORTORDER =\"Binary\" VERSIONNUMBER =\"2\">\r\n" + 
	       "				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"sc_"+r_entity+"_DEL\" STAGE =\"1\" TRANSFORMATIONNAME =\"sc_"+r_entity+"_DEL\" TRANSFORMATIONTYPE =\"Target Definition\"/>\r\n" + 
	       "				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"SQ_sc_"+z_entity+"\" STAGE =\"2\" TRANSFORMATIONNAME =\"SQ_sc_"+z_entity+"\" TRANSFORMATIONTYPE =\"Source Qualifier\"/>\r\n" + 
	       "				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXPTRANS\" STAGE =\"2\" TRANSFORMATIONNAME =\"EXPTRANS\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	       "					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "				</SESSTRANSFORMATIONINST>\r\n" + 
	       "				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"sc_RLKP_"+i_entity+"\" STAGE =\"2\" TRANSFORMATIONNAME =\"sc_RLKP_"+i_entity+"\" TRANSFORMATIONTYPE =\"Lookup Procedure\">\r\n" + 
	       "					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "				</SESSTRANSFORMATIONINST>\r\n" + 
	       "				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"RTRTRANS\" STAGE =\"2\" TRANSFORMATIONNAME =\"RTRTRANS\" TRANSFORMATIONTYPE =\"Router\">\r\n" + 
	       "					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "				</SESSTRANSFORMATIONINST>\r\n" + 
	       "				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXP_"+r_entity+"_DEL\" STAGE =\"2\" TRANSFORMATIONNAME =\"EXP_"+r_entity+"_DEL\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	       "					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	       "				</SESSTRANSFORMATIONINST>\r\n" + 
	       "				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"0\" SINSTANCENAME =\"sc_"+z_entity+"\" STAGE =\"0\" TRANSFORMATIONNAME =\"sc_"+z_entity+"\" TRANSFORMATIONTYPE =\"Source Definition\"/>\r\n" + 
	       "				<CONFIGREFERENCE REFOBJECTNAME =\"default_session_config\" TYPE =\"Session config\">\r\n" + 
	       "					<ATTRIBUTE NAME =\"Error Log Type\" VALUE =\"Relational Database\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Error Log DB Connection\" VALUE =\"Relational:KMA_CTRL\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Error Log Table Name Prefix\" VALUE =\"CTRL_ETL_AUDIT_INF_\"/>\r\n" + 
	       "				</CONFIGREFERENCE>\r\n" + 
	       "				<SESSIONEXTENSION NAME =\"Relational Writer\" SINSTANCENAME =\"sc_"+r_entity+"_DEL\" SUBTYPE =\"Relational Writer\" TRANSFORMATIONTYPE =\"Target Definition\" TYPE =\"WRITER\">\r\n" + 
	       "					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+i_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+i_dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Target load type\" VALUE =\"Normal\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Insert\" VALUE =\"YES\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Update as Update\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Update as Insert\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Update else Insert\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Delete\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Truncate target table option\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Reject file directory\" VALUE =\"$PMBadFileDir\\"+z_conn+"\\\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Reject filename\" VALUE =\"sc_"+r_entity.toLowerCase()+"_del1.bad\"/>\r\n" + 
	       "				</SESSIONEXTENSION>\r\n" + 
	       "				<SESSIONEXTENSION NAME =\"Relational Reader\" SINSTANCENAME =\"SQ_sc_"+z_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Qualifier\" TYPE =\"READER\">\r\n" + 
	       "					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+z_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+z_Dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	       "				</SESSIONEXTENSION>\r\n" + 
	       "				<SESSIONEXTENSION NAME =\"Relational Lookup\" SINSTANCENAME =\"sc_RLKP_"+i_entity+"\" SUBTYPE =\"Relational Lookup\" TRANSFORMATIONTYPE =\"Lookup Procedure\" TYPE =\"LOOKUPEXTENSION\">\r\n" + 
	       "					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+i_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+i_dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	       "				</SESSIONEXTENSION>\r\n" + 
	       "				<SESSIONEXTENSION DSQINSTNAME =\"SQ_sc_"+z_entity+"\" DSQINSTTYPE =\"Source Qualifier\" NAME =\"Relational Reader\" SINSTANCENAME =\"sc_"+z_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Definition\" TYPE =\"READER\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"General Options\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Write Backward Compatible Session Log File\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Session Log File Name\" VALUE =\""+del_session+".log\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Session Log File directory\" VALUE =\"$PMSessionLogDir\\"+z_conn+"\\SESSION\\\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Parameter Filename\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Enable Test Load\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"$Source connection value\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"$Target connection value\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Treat source rows as\" VALUE =\"Insert\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Commit Type\" VALUE =\"Target\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Commit Interval\" VALUE =\"10000\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Commit On End Of File\" VALUE =\"YES\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Rollback Transactions on Errors\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Recovery Strategy\" VALUE =\"Restart task\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Java Classpath\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Performance\" VALUE =\"\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"DTM buffer size\" VALUE =\"50000000\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Collect performance data\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Write performance data to repository\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Incremental Aggregation\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Enable high precision\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Session retry on deadlock\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Pushdown Optimization\" VALUE =\"None\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Allow Temporary View for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Allow Temporary Sequence for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	       "					<ATTRIBUTE NAME =\"Allow Pushdown for User Incompatible Connections\" VALUE =\"NO\"/>\r\n" + 
	       "				</SESSION>\r\n" + 
	        		
//powermart --> repository --> 1st folder --> 3rd mapping open
			"				<MAPPING DESCRIPTION =\"\" ISVALID =\"YES\" NAME =\""+upd_mapping+"\" OBJECTVERSION =\"1\" VERSIONNUMBER =\"2\">\r\n" + 
		    "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"SQTRANS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Source Qualifier\" VERSIONNUMBER =\"1\">\r\n" );
    
for(int row=rowStart; row<=prodRowEnd; row++) {
		    	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
		    		   String connector =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
			    	   if(!connector.isEmpty()) {
			    		   String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    			String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	 			    		int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	 				    	int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	 				    	String dataTypeinXml = "";
	 				    	if(dataType.equalsIgnoreCase("varchar2")) {
	 				    		dataTypeinXml = "string";
	 				    	}else if(dataType.equalsIgnoreCase("date")) {
	 				    		dataTypeinXml = "date/time";
	 				    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	 				    		dataTypeinXml = "decimal";
	 				    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
			    		}
			    		
			    	}
	        }
	        
	        String mappingSQTRANSsqlQuery = sheet2.getRow(21).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String rtrtransInsertQuery = sheet2.getRow(1).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String rtrtransUpdateQuery = sheet2.getRow(2).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	        String rtrtransRenewQuery = sheet2.getRow(3).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
sb2.append("						<TABLEATTRIBUTE NAME =\"Sql Query\" VALUE =\""+mappingSQTRANSsqlQuery+"\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"User Defined Join\" VALUE =\"\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Source Filter\" VALUE =\"\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Number Of Sorted Ports\" VALUE =\"0\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Select Distinct\" VALUE =\"NO\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Is Partitionable\" VALUE =\"NO\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Pre SQL\" VALUE =\"\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Post SQL\" VALUE =\"\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Output is deterministic\" VALUE =\"YES\"/>\r\n" + 
    	   "						<TABLEATTRIBUTE NAME =\"Output is repeatable\" VALUE =\"Never\"/>\r\n" + 
    	   "					</TRANSFORMATION>\r\n" + 
    	 //powermart --> repository --> 1st folder --> 3rd mapping --> 2nd transformation
    	   "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXPTRANS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"2\">\r\n"); 
    	   for(int row=rowStart; row<=prodRowEnd; row++) {
		    	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
			    	   String expr = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
			    		   if(!expr.isEmpty()) {
			    			   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
				    			String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
				    			int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
				    			int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	 				    	String dataTypeinXml = "";
	 				    	if(dataType.equalsIgnoreCase("varchar2")) {
	 				    		dataTypeinXml = "string";
	 				    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
	 				    		dataTypeinXml = "decimal";
	 				    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+name+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	 			    	   }
			    	}
	        }
    	   
    	   for(int row=rowStart; row<=stage2RowEnd; row++) {
		    	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
			    	   String expr = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
			    	   if(!expr.isEmpty()) {
			    		   if(expr.equalsIgnoreCase("D")) {
			    			String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    			String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			    			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
 				    	String dataTypeinXml = "";
 				    	if(dataType.equalsIgnoreCase("varchar2")) {
 				    		dataTypeinXml = "string";
 				    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
 				    		dataTypeinXml = "decimal";
 				    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"'I'\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_"+name+"_INS\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n" +
		   "						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"'U'\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_"+name+"_UPD\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n" +
		   "						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"'R'\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_"+name+"_REN\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
 			    	   }
		    	   }
			    	}
	        } 

	        		
sb2.append("						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	       "					</TRANSFORMATION>\r\n" + 
	     
	       //powermart --> repository --> 1st folder --> 3rd mapping --> 3rd transformation
	       "					<TRANSFORMATION DESCRIPTION =\"\" NAME =\"RTRTRANS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Router\" VERSIONNUMBER =\"1\">\r\n" + 
	       "						<GROUP DESCRIPTION =\"\" NAME =\"INPUT\" ORDER =\"1\" TYPE =\"INPUT\"/>\r\n" + 
	       "						<GROUP DESCRIPTION =\"\" EXPRESSION =\""+rtrtransInsertQuery+"\" NAME =\"INSERT\" ORDER =\"2\" TYPE =\"OUTPUT\"/>\r\n" + 
	       "						<GROUP DESCRIPTION =\"Path for the data when none of the group conditions are satisfied.\" NAME =\"DEFAULT1\" ORDER =\"5\" TYPE =\"OUTPUT/DEFAULT\"/>\r\n" + 
	       "						<GROUP DESCRIPTION =\"\" EXPRESSION =\""+rtrtransUpdateQuery+"\" NAME =\"UPDATE\" ORDER =\"3\" TYPE =\"OUTPUT\"/>\r\n" + 
	       "						<GROUP DESCRIPTION =\"\" EXPRESSION =\""+rtrtransRenewQuery+"\" NAME =\"RENEW\" ORDER =\"4\" TYPE =\"OUTPUT\"/>\r\n");
	
	for(int row=rowStart; row<=lkpProdRowEnd; row++) {
		   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")))) {
	  	   String rtrKey = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+datatype+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\"LKP_"+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	} 
	for(int row=rowStart; row<=prodRowEnd; row++) {
		   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
	  	   String rtrKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	  			String dataTypeinXml = "";
			    	if(datatype.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(datatype.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	} 
	for(int row=rowStart; row<=stage2RowEnd; row++) {
 	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
	    	   String expr = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
	    	   if(!expr.isEmpty()) {
	    		   if(expr.equalsIgnoreCase("D")) {
	    			String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	    			String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	    			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	    			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		    	String dataTypeinXml = "";
		    	if(dataType.equalsIgnoreCase("varchar2")) {
		    		dataTypeinXml = "string";
		    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
		    		dataTypeinXml = "decimal";
		    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\""+name+"_INS\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\""+name+"_UPD\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INPUT\" NAME =\""+name+"_REN\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	    	   }
 	   }
	    	}
 } 
	
	for(int row=rowStart; row<=lkpProdRowEnd; row++) {
		   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")))) {
	  	   String rtrKey = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+datatype+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INSERT\" NAME =\"LKP_"+name+"1\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\"LKP_"+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	}
	for(int row=rowStart; row<=prodRowEnd; row++) {
		   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
	  	   String rtrKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	  			String dataTypeinXml = "";
			    	if(datatype.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(datatype.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INSERT\" NAME =\""+name+"1\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	}
	for(int row=rowStart; row<=stage2RowEnd; row++) {
	 	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
		    	   String expr = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
		    	   if(!expr.isEmpty()) {
		    		   if(expr.equalsIgnoreCase("D")) {
		    			String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    			String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		    			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String dataTypeinXml = "";
			    	if(dataType.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INSERT\" NAME =\""+name+"_INS1\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_INS\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INSERT\" NAME =\""+name+"_UPD1\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_UPD\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"INSERT\" NAME =\""+name+"_REN1\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_REN\" SCALE =\""+scale+"\"/>\r\n");
		    	   }
	 	   }
		    	}
	 } 
	
	for(int row=rowStart; row<=lkpProdRowEnd; row++) {
		   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")))) {
	  	   String rtrKey = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+datatype+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"UPDATE\" NAME =\"LKP_"+name+"3\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\"LKP_"+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	} 
	for(int row=rowStart; row<=prodRowEnd; row++) {
		   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
	  	   String rtrKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	  			String dataTypeinXml = "";
			    	if(datatype.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(datatype.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"UPDATE\" NAME =\""+name+"3\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	}
	for(int row=rowStart; row<=stage2RowEnd; row++) {
	 	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
		    	   String expr = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
		    	   if(!expr.isEmpty()) {
		    		   if(expr.equalsIgnoreCase("D")) {
		    			String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    			String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		    			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String dataTypeinXml = "";
			    	if(dataType.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"UPDATE\" NAME =\""+name+"_INS3\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_INS\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"UPDATE\" NAME =\""+name+"_UPD3\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_UPD\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"UPDATE\" NAME =\""+name+"_REN3\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_REN\" SCALE =\""+scale+"\"/>\r\n");
		    	   }
	 	   }
		    	}
	 }
	for(int row=rowStart; row<=lkpProdRowEnd; row++) {
		   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")))) {
	  	   String rtrKey = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+datatype+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"RENEW\" NAME =\"LKP_"+name+"4\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\"LKP_"+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	} 
	for(int row=rowStart; row<=prodRowEnd; row++) {
		   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
	  	   String rtrKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	  			String dataTypeinXml = "";
			    	if(datatype.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(datatype.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"RENEW\" NAME =\""+name+"4\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	}
	for(int row=rowStart; row<=stage2RowEnd; row++) {
	 	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
		    	   String expr = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
		    	   if(!expr.isEmpty()) {
		    		   if(expr.equalsIgnoreCase("D")) {
		    			String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    			String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		    			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String dataTypeinXml = "";
			    	if(dataType.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"RENEW\" NAME =\""+name+"_INS4\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_INS\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"RENEW\" NAME =\""+name+"_UPD4\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_UPD\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"RENEW\" NAME =\""+name+"_REN4\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_REN\" SCALE =\""+scale+"\"/>\r\n");
		    	   }
	 	   }
		    	}
	 }
	for(int row=rowStart; row<=lkpProdRowEnd; row++) {
		   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")))) {
	  	   String rtrKey = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+datatype+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\"LKP_"+name+"2\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\"LKP_"+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	} 
	for(int row=rowStart; row<=prodRowEnd; row++) {
		   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
	  	   String rtrKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
	  	   if(rtrKey.equals("Y")) {
	  		 String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	  			String datatype =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	  			int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	  			int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	  			String dataTypeinXml = "";
			    	if(datatype.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(datatype.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\""+name+"2\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"\" SCALE =\""+scale+"\"/>\r\n");
	   	   }
	  	}
	}
	for(int row=rowStart; row<=stage2RowEnd; row++) {
	 	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
		    	   String expr = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")).getStringCellValue();
		    	   if(!expr.isEmpty()) {
		    		   if(expr.equalsIgnoreCase("D")) {
		    			String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    			String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
		    			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String dataTypeinXml = "";
			    	if(dataType.equalsIgnoreCase("varchar2")) {
			    		dataTypeinXml = "string";
			    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
			    		dataTypeinXml = "decimal";
			    	}
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\""+name+"_INS2\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_INS\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\""+name+"_UPD2\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_UPD\" SCALE =\""+scale+"\"/>\r\n");
	sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" GROUP =\"DEFAULT1\" NAME =\""+name+"_REN2\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\""+precision+"\" REF_FIELD =\""+name+"_REN\" SCALE =\""+scale+"\"/>\r\n");
		    	   }
	 	   }
		    	}
	 }
	
 sb2.append("						<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
		 	"        			</TRANSFORMATION>\r\n" +
 
//powermart --> repository --> 1st folder --> 3rd mapping --> 4th transformation 
	        "        			<TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_INS\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n" );
	
	for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     	   if(!name.isEmpty()) {
	     			String datatype =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
	     			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
	     			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	     			String exprKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     			String expr = name;
	     			if(exprKey.indexOf('D')>=0) {
	     				expr = name+"_INS";
	     				name = name+"_INS";
	     			}
	     			String dataTypeinXml = "";
				    	if(datatype.equalsIgnoreCase("varchar2")) {
				    		dataTypeinXml = "string";
				    	}else if(datatype.equalsIgnoreCase("date")) {
				    		dataTypeinXml = "date/time";
				    	}else if(datatype.equalsIgnoreCase("number(p,s)")) {
				    		dataTypeinXml = "decimal";
				    	}
 sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+expr+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	     	   }
	     	}
	     } 	   
	
	sb2.append("<TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_REC_CREATE_DATE\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n" + 
			"            <TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_REC_UPDATE_DATE\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n");

 sb2.append("			            <TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        "			        </TRANSFORMATION>\r\n" + 
	        		//powermart --> repository --> 1st folder --> 3rd mapping --> 5th transformation 	       
	        "			        <TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_UPD\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n" );         
 for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   if(!name.isEmpty()) {
			String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			String exprKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
			String expr = name;
			if(exprKey.indexOf('D')>=0) {
				expr = name+"_UPD";
				name= name+"_UPD";
			}
			String dataTypeinXml = "";
		    	if(dataType.equalsIgnoreCase("varchar2")) {
		    		dataTypeinXml = "string";
		    	}else if(dataType.equalsIgnoreCase("date")) {
		    		dataTypeinXml = "date/time";
		    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
		    		dataTypeinXml = "decimal";
		    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+expr+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	   }
	}
}     				    		
	sb2.append("<TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"LKP_EFF_FROM_DATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"LKP_EFF_FROM_DATE\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n" + 
			"            <TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_REC_CREATE_DATE\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n" + 
			"            <TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\"O_REC_UPDATE_DATE\" PICTURETEXT =\"\" PORTTYPE =\"OUTPUT\" PRECISION =\"29\" SCALE =\"9\"/>\r\n");        		
  sb2.append("            			<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        " 			       </TRANSFORMATION>\r\n" + 
	        		//powermart --> repository --> 1st folder --> 3rd mapping --> 6th transformation 
	        "        		   <TRANSFORMATION DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_REN\" OBJECTVERSION =\"1\" REUSABLE =\"NO\" TYPE =\"Expression\" VERSIONNUMBER =\"1\">\r\n" ); 
  for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   String exprKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	   if(!name.isEmpty()) {
			String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			
			String expr = name;
			if(exprKey.indexOf('D')>=0) {
				expr = name+"_REN";
				name= name+"_REN";
			}
			String dataTypeinXml = "";
		    	if(dataType.equalsIgnoreCase("varchar2")) {
		    		dataTypeinXml = "string";
		    	}else if(dataType.equalsIgnoreCase("date")) {
		    		dataTypeinXml = "date/time";
		    	}else if(dataType.equalsIgnoreCase("number(p,s)")) {
		    		dataTypeinXml = "decimal";
		    	}
sb2.append("						<TRANSFORMFIELD DATATYPE =\""+dataTypeinXml+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\""+expr+"\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
	   }
	}
}  	
  for(int row=rowStart; row<=stage2RowEnd; row++) {
	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("N")))) {
	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	   if(!name.isEmpty()) {
		   String exprKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("N")).getStringCellValue();
		   if(exprKey.indexOf('O')==0) {
				name= "O_"+name;
				String dataType =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
				int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
				int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
	sb2.append("						<TRANSFORMFIELD DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" EXPRESSION =\"SYSDATE\" EXPRESSIONTYPE =\"GENERAL\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"OUPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
			}
	   }
	}
  }
sb2.append("			            <TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
    	   "					</TRANSFORMATION>\r\n" + 	        
	
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_"+r_entity+"_INS\" TRANSFORMATION_NAME =\"sc_"+r_entity+"\" TRANSFORMATION_TYPE =\"Target Definition\" TYPE =\"TARGET\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_"+r_entity+"_REN\" TRANSFORMATION_NAME =\"sc_"+r_entity+"\" TRANSFORMATION_TYPE =\"Target Definition\" TYPE =\"TARGET\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_"+r_entity+"_UPD\" TRANSFORMATION_NAME =\"sc_"+r_entity+"\" TRANSFORMATION_TYPE =\"Target Definition\" TYPE =\"TARGET\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"SQTRANS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"SQTRANS\" TRANSFORMATION_TYPE =\"Source Qualifier\" TYPE =\"TRANSFORMATION\">\r\n" + 
	       "						<ASSOCIATED_SOURCE_INSTANCE NAME =\"sc_"+i_entity+"\"/>\r\n" + 
	       "						<ASSOCIATED_SOURCE_INSTANCE NAME =\"sc_"+z_entity+"\"/>\r\n" + 
	       "					</INSTANCE>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXPTRANS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXPTRANS\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"RTRTRANS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"RTRTRANS\" TRANSFORMATION_TYPE =\"Router\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_INS\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXP_"+r_entity+"_INS\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_UPD\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXP_"+r_entity+"_UPD\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"EXP_"+r_entity+"_REN\" REUSABLE =\"NO\" TRANSFORMATION_NAME =\"EXP_"+r_entity+"_REN\" TRANSFORMATION_TYPE =\"Expression\" TYPE =\"TRANSFORMATION\"/>\r\n" + 
	       "					<INSTANCE DBDNAME =\""+i_conn+"\" DESCRIPTION =\"\" NAME =\"sc_"+i_entity+"\" TRANSFORMATION_NAME =\"sc_"+i_entity+"\" TRANSFORMATION_TYPE =\"Source Definition\" TYPE =\"SOURCE\"/>\r\n" + 
	       "					<INSTANCE DBDNAME =\""+z_conn+"\" DESCRIPTION =\"\" NAME =\"sc_"+z_entity+"\" TRANSFORMATION_NAME =\"sc_"+z_entity+"\" TRANSFORMATION_TYPE =\"Source Definition\" TYPE =\"SOURCE\"/>\r\n" + 
	       "					<INSTANCE DESCRIPTION =\"\" NAME =\"sc_RLKP_"+z_entity+"\" REUSABLE =\"YES\" TRANSFORMATION_NAME =\"sc_RLKP_"+z_entity+"\" TRANSFORMATION_TYPE =\"Lookup Procedure\" TYPE =\"TRANSFORMATION\"/>\r\n"); 
	      //connectors after 1st folder --> 3rdmapping --> transformations
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     	   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     	  String fromField = name;
	     		if(iurKey.indexOf('D')>=0) {
	     			fromField = name+"_INS";
	     		}
sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"EXP_"+r_entity+"_INS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+r_entity+"_INS\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	     	   }
	     	}
	      sb2.append(" <CONNECTOR FROMFIELD =\"O_REC_CREATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_INS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_CREATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_INS\" TOINSTANCETYPE =\"Target Definition\"/>\r\n" +
	                 " <CONNECTOR FROMFIELD =\"O_REC_UPDATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_INS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_UPDATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_INS\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	      
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     	   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     	  String fromField = name;
	     		if(iurKey.indexOf('D')>=0) {
	     			fromField = name+"_REN";
	     		}
sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"EXP_"+r_entity+"_REN\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+r_entity+"_REN\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	     	   }
	     	}
	      sb2.append(" <CONNECTOR FROMFIELD =\"O_REC_CREATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_REN\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_CREATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_REN\" TOINSTANCETYPE =\"Target Definition\"/>\r\n" +
	                 " <CONNECTOR FROMFIELD =\"O_REC_UPDATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_REN\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_UPDATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_REN\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	        	
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     	   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     	   String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     	   String fromField = name;
	     		if(iurKey.indexOf('D')>=0) {
	     			fromField = name+"_UPD";
	     		}
sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"EXP_"+r_entity+"_UPD\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+r_entity+"_UPD\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	     	   }
	     	}
	      sb2.append(" <CONNECTOR FROMFIELD =\"LKP_EFF_FROM_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_UPD\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"EFF_FROM_DATE\" TOINSTANCE =\"sc_"+r_entity+"_UPD\" TOINSTANCETYPE =\"Target Definition\"/>\r\n" +
	    		     " <CONNECTOR FROMFIELD =\"O_REC_CREATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_UPD\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_CREATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_UPD\" TOINSTANCETYPE =\"Target Definition\"/>\r\n" +
	                 " <CONNECTOR FROMFIELD =\"O_REC_UPDATE_DATE\" FROMINSTANCE =\"EXP_"+r_entity+"_UPD\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\"REC_UPDATE_DATE\" TOINSTANCE =\"sc_"+r_entity+"_UPD\" TOINSTANCETYPE =\"Target Definition\"/>\r\n");
	        	 	
	      for(int row=rowStart; row<=prodRowEnd; row++) {
	     	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("K")))) {
	     	   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"sc_"+z_entity+"\" FROMINSTANCETYPE =\"Source Definition\" TOFIELD =\""+name+"\" TOINSTANCE =\"SQTRANS\" TOINSTANCETYPE =\"Source Qualifier\"/>\r\n");
	     	   }
	     	}
	   for(int row=rowStart; row<=stage1RowEnd; row++) {
     	   if(!isCellEmpty(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")))) {
     	   String name = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"sc_"+i_entity+"\" FROMINSTANCETYPE =\"Source Definition\" TOFIELD =\""+name+"\" TOINSTANCE =\"SQTRANS\" TOINSTANCETYPE =\"Source Qualifier\"/>\r\n");
     	   }
     	}
	   
	      for(int row=rowStart; row<=prodRowEnd; row++) {
	     	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")))) {
	     	   String name = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"SQTRANS\" FROMINSTANCETYPE =\"Source Qualifier\" TOFIELD =\""+name+"\" TOINSTANCE =\"EXPTRANS\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	     	   }
	     	}
	      
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     		   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     	   if(iurKey.equals("Y")) {
	     		  String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     		 sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	     	   }
	     	   }
	     	}
	      
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     		   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     	   if(iurKey.indexOf('D')>=0) {
	     		  String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     		 sb2.append("					<CONNECTOR FROMFIELD =\"O_"+name+"_INS\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"_INS\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	     		sb2.append("					<CONNECTOR FROMFIELD =\"O_"+name+"_UPD\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"_UPD\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	     		sb2.append("					<CONNECTOR FROMFIELD =\"O_"+name+"_REN\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"_REN\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	     	   }
	     	   }
	     	}
	      
	      for(int row=rowStart; row<=lkpProdRowEnd; row++) {
	     	   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")))) {
	     	   String name = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     	   if(name.startsWith("I_")) {
	     		   String fromField = name.replaceFirst("I_", "");
	     		  sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"EXPTRANS\" FROMINSTANCETYPE =\"Expression\" TOFIELD =\""+name+"\" TOINSTANCE =\"sc_"+n_descr+"\" TOINSTANCETYPE =\"Lookup Procedure\"/>\r\n");
	     	   }
	     	   }
	     	}
	      
	      for(int row=rowStart; row<=lkpProdRowEnd; row++) {
	     	   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")))) {
	     	   String name = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
sb2.append("					<CONNECTOR FROMFIELD =\""+name+"\" FROMINSTANCE =\"sc_"+n_descr+"\" FROMINSTANCETYPE =\"Lookup Procedure\" TOFIELD =\"LKP_"+name+"\" TOINSTANCE =\"RTRTRANS\" TOINSTANCETYPE =\"Router\"/>\r\n");
	     	   }
	     	}
	      
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     		   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     		  String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     		  String fromField = name+"1";
	     		  String toField = name;
	     	   if(iurKey.indexOf('D')>=0) {
	     		   fromField = name+"_INS1";
	     		   toField = name+"_INS";
	     	   }
	     		 sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\""+toField+"\" TOINSTANCE =\"EXP_"+r_entity+"_INS\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	     	   }
	     	}
	sb2.append("<CONNECTOR FROMFIELD =\"LKP_EFF_FROM_DATE3\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\"LKP_EFF_FROM_DATE\" TOINSTANCE =\"EXP_"+r_entity+"_UPD\" TOINSTANCETYPE =\"Expression\"/>\r\n");      
	      
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     		   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     		  String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     		  String fromField = name+"3";
	     		  String toField = name;
	     	   if(iurKey.indexOf('D')>=0) {
	     		   fromField = name+"_UPD3";
	     		   toField = name+"_UPD";
	     	   }
	     		 sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\""+toField+"\" TOINSTANCE =\"EXP_"+r_entity+"_UPD\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	     	   }
	     	}
	      
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")))) {
	     		   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("L")).getStringCellValue();
	     		  String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
	     		  String fromField = name+"4";
	     		  String toField = name;
	     	   if(iurKey.indexOf('D')>=0) {
	     		   fromField = name+"_REN4";
	     		   toField = name+"_REN";
	     	   }
	     		 sb2.append("					<CONNECTOR FROMFIELD =\""+fromField+"\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\""+toField+"\" TOINSTANCE =\"EXP_"+r_entity+"_REN\" TOINSTANCETYPE =\"Expression\"/>\r\n");
	     	   }
	     	}
	      for(int row=rowStart; row<=stage2RowEnd; row++) {
	     	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("O")))) {
	     		   String iurKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("O")).getStringCellValue();
	     		  if(iurKey.indexOf('L')==0) {
	     			 String name = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		     		 sb2.append("					<CONNECTOR FROMFIELD =\"LKP_"+name+"4\" FROMINSTANCE =\"RTRTRANS\" FROMINSTANCETYPE =\"Router\" TOFIELD =\""+name+"\" TOINSTANCE =\"EXP_"+r_entity+"_REN\" TOINSTANCETYPE =\"Expression\"/>\r\n");
		     	   }
	     	   }
	     	}
	      sb2.append("					<TARGETLOADORDER ORDER =\"1\" TARGETINSTANCE =\"sc_"+r_entity+"_INS\"/>\r\n" + 
				       "					<TARGETLOADORDER ORDER =\"1\" TARGETINSTANCE =\"sc_"+r_entity+"_REN\"/>\r\n" + 
				       "					<TARGETLOADORDER ORDER =\"1\" TARGETINSTANCE =\"sc_"+r_entity+"_UPD\"/>\r\n" + 
				       "					<ERPINFO/>\r\n" + 
	        		//powermart --> repository --> 1st folder --> 3rd mapping close
	        		"			</MAPPING>\r\n" + 
	        		"			<SHORTCUT COMMENTS =\"\" DBDNAME =\""+i_conn+"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\"sc_"+i_entity+"\" OBJECTSUBTYPE =\"Source Definition\" OBJECTTYPE =\"SOURCE\" REFERENCEDDBD =\""+i_conn+"\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\""+i_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_DEV\" VERSIONNUMBER =\"1\"/>\r\n" + 
	        		"			<SHORTCUT COMMENTS =\"\" FOLDERNAME =\""+b_shortcutFolder+"\" NAME =\"sc_RLKP_"+z_entity+"\" OBJECTSUBTYPE =\"Lookup Procedure\" OBJECTTYPE =\"TRANSFORMATION\" REFERENCETYPE =\"LOCAL\" REFOBJECTNAME =\"RLKP_"+z_entity+"\" REPOSITORYNAME =\"REPO_HAEA_EDW_DEV\" VERSIONNUMBER =\"1\"/>\r\n" + 
	        		"			<SESSION DESCRIPTION =\"\" ISVALID =\"YES\" MAPPINGNAME =\""+upd_mapping+"\" NAME =\""+upd_session+"\" REUSABLE =\"YES\" SORTORDER =\"Binary\" VERSIONNUMBER =\"2\">\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"sc_"+r_entity+"_UPD\" STAGE =\"1\" TRANSFORMATIONNAME =\"sc_"+r_entity+"_UPD\" TRANSFORMATIONTYPE =\"Target Definition\"/>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"sc_"+r_entity+"_INS\" STAGE =\"2\" TRANSFORMATIONNAME =\"sc_"+r_entity+"_INS\" TRANSFORMATIONTYPE =\"Target Definition\"/>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"sc_"+r_entity+"_REN\" STAGE =\"3\" TRANSFORMATIONNAME =\"sc_"+r_entity+"_REN\" TRANSFORMATIONTYPE =\"Target Definition\"/>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"0\" SINSTANCENAME =\"sc_"+i_entity+"\" STAGE =\"0\" TRANSFORMATIONNAME =\"sc_"+i_entity+"\" TRANSFORMATIONTYPE =\"Source Definition\"/>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"YES\" PARTITIONTYPE =\"PASS THROUGH\" PIPELINE =\"1\" SINSTANCENAME =\"SQTRANS\" STAGE =\"4\" TRANSFORMATIONNAME =\"SQTRANS\" TRANSFORMATIONTYPE =\"Source Qualifier\"/>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"0\" SINSTANCENAME =\"sc_"+z_entity+"\" STAGE =\"0\" TRANSFORMATIONNAME =\"sc_"+z_entity+"\" TRANSFORMATIONTYPE =\"Source Definition\"/>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXPTRANS\" STAGE =\"4\" TRANSFORMATIONNAME =\"EXPTRANS\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	        		"					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	        		"				</SESSTRANSFORMATIONINST>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"sc_RLKP_"+z_entity+"\" STAGE =\"4\" TRANSFORMATIONNAME =\"sc_RLKP_"+z_entity+"\" TRANSFORMATIONTYPE =\"Lookup Procedure\">\r\n" + 
	        		"					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	        		"				</SESSTRANSFORMATIONINST>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"RTRTRANS\" STAGE =\"4\" TRANSFORMATIONNAME =\"RTRTRANS\" TRANSFORMATIONTYPE =\"Router\">\r\n" + 
	        		"					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	        		"				</SESSTRANSFORMATIONINST>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXP_"+r_entity+"_INS\" STAGE =\"4\" TRANSFORMATIONNAME =\"EXP_"+r_entity+"_INS\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	        		"					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	        		"				</SESSTRANSFORMATIONINST>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXP_"+r_entity+"_UPD\" STAGE =\"4\" TRANSFORMATIONNAME =\"EXP_"+r_entity+"_UPD\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	        		"					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	        		"				</SESSTRANSFORMATIONINST>\r\n" + 
	        		"				<SESSTRANSFORMATIONINST ISREPARTITIONPOINT =\"NO\" PIPELINE =\"1\" SINSTANCENAME =\"EXP_"+r_entity+"_REN\" STAGE =\"4\" TRANSFORMATIONNAME =\"EXP_"+r_entity+"_REN\" TRANSFORMATIONTYPE =\"Expression\">\r\n" + 
	        		"					<PARTITION DESCRIPTION =\"\" NAME =\"Partition #1\"/>\r\n" + 
	        		"				</SESSTRANSFORMATIONINST>\r\n" + 
	        		"				<CONFIGREFERENCE REFOBJECTNAME =\"default_session_config\" TYPE =\"Session config\">\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Error Log Type\" VALUE =\"Relational Database\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Error Log DB Connection\" VALUE =\"Relational:KMA_CTRL\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Error Log Table Name Prefix\" VALUE =\"CTRL_ETL_AUDIT_INF_\"/>\r\n" + 
	        		"				</CONFIGREFERENCE>\r\n" + 
	        		"				<SESSIONEXTENSION NAME =\"Relational Writer\" SINSTANCENAME =\"sc_"+r_entity+"_UPD\" SUBTYPE =\"Relational Writer\" TRANSFORMATIONTYPE =\"Target Definition\" TYPE =\"WRITER\">\r\n" + 
	        		"					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+i_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+i_dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Target load type\" VALUE =\"Normal\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Insert\" VALUE =\"YES\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update as Update\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update as Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update else Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Delete\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Truncate target table option\" VALUE =\"YES\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Reject file directory\" VALUE =\"$PMBadFileDir\\"+z_conn+"\\\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Reject filename\" VALUE =\"sc_"+r_entity.toLowerCase()+"_upd1.bad\"/>\r\n" + 
	        		"				</SESSIONEXTENSION>\r\n" + 
	        		"				<SESSIONEXTENSION NAME =\"Relational Writer\" SINSTANCENAME =\"sc_"+r_entity+"_INS\" SUBTYPE =\"Relational Writer\" TRANSFORMATIONTYPE =\"Target Definition\" TYPE =\"WRITER\">\r\n" + 
	        		"					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+i_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+i_dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Target load type\" VALUE =\"Normal\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Insert\" VALUE =\"YES\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update as Update\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update as Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update else Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Delete\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Truncate target table option\" VALUE =\"YES\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Reject file directory\" VALUE =\"$PMBadFileDir\\"+z_conn+"\\\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Reject filename\" VALUE =\"sc_"+r_entity.toLowerCase()+"_ins1.bad\"/>\r\n" + 
	        		"				</SESSIONEXTENSION>\r\n" + 
	        		"				<SESSIONEXTENSION NAME =\"Relational Writer\" SINSTANCENAME =\"sc_"+r_entity+"_REN\" SUBTYPE =\"Relational Writer\" TRANSFORMATIONTYPE =\"Target Definition\" TYPE =\"WRITER\">\r\n" + 
	        		"					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+i_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+i_dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Target load type\" VALUE =\"Normal\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Insert\" VALUE =\"YES\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update as Update\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update as Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Update else Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Delete\" VALUE =\"NO\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Truncate target table option\" VALUE =\"YES\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Reject file directory\" VALUE =\"$PMBadFileDir\\"+z_conn+"\\\"/>\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Reject filename\" VALUE =\"sc_"+r_entity.toLowerCase()+"_ren1.bad\"/>\r\n" + 
	        		"				</SESSIONEXTENSION>\r\n" + 
	        		"				<SESSIONEXTENSION DSQINSTNAME =\"SQTRANS\" DSQINSTTYPE =\"Source Qualifier\" NAME =\"Relational Reader\" SINSTANCENAME =\"sc_"+i_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Definition\" TYPE =\"READER\"/>\r\n" + 
	        		"				<SESSIONEXTENSION NAME =\"Relational Reader\" SINSTANCENAME =\"SQTRANS\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Qualifier\" TYPE =\"READER\">\r\n" + 
	        		"					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+r_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+etlDbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	        		"				</SESSIONEXTENSION>\r\n" + 
	        		"				<SESSIONEXTENSION DSQINSTNAME =\"SQTRANS\" DSQINSTTYPE =\"Source Qualifier\" NAME =\"Relational Reader\" SINSTANCENAME =\"sc_"+z_entity+"\" SUBTYPE =\"Relational Reader\" TRANSFORMATIONTYPE =\"Source Definition\" TYPE =\"READER\"/>\r\n" + 
	        		"				<SESSIONEXTENSION NAME =\"Relational Lookup\" SINSTANCENAME =\"sc_RLKP_"+z_entity+"\" SUBTYPE =\"Relational Lookup\" TRANSFORMATIONTYPE =\"Lookup Procedure\" TYPE =\"LOOKUPEXTENSION\">\r\n" + 
	        		"					<CONNECTIONREFERENCE CNXREFNAME =\"DB Connection\" CONNECTIONNAME =\""+z_conn+"\" CONNECTIONNUMBER =\"1\" CONNECTIONSUBTYPE =\""+z_Dbtype+"\" CONNECTIONTYPE =\"Relational\" VARIABLE =\"\"/>\r\n" + 
	        		"				</SESSIONEXTENSION>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"General Options\" VALUE =\"\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Write Backward Compatible Session Log File\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Session Log File Name\" VALUE =\""+upd_session+".log\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Session Log File directory\" VALUE =\"$PMSessionLogDir\\"+z_conn+"\\SESSION\\\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Parameter Filename\" VALUE =\"\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Enable Test Load\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"$Source connection value\" VALUE =\"\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"$Target connection value\" VALUE =\"\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Treat source rows as\" VALUE =\"Insert\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Commit Type\" VALUE =\"Target\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Commit Interval\" VALUE =\"10000\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Commit On End Of File\" VALUE =\"YES\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Rollback Transactions on Errors\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Recovery Strategy\" VALUE =\"Restart task\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Java Classpath\" VALUE =\"\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Performance\" VALUE =\"\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"DTM buffer size\" VALUE =\"50000000\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Collect performance data\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Write performance data to repository\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Incremental Aggregation\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Enable high precision\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Session retry on deadlock\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Pushdown Optimization\" VALUE =\"None\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Allow Temporary View for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Allow Temporary Sequence for Pushdown\" VALUE =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Allow Pushdown for User Incompatible Connections\" VALUE =\"NO\"/>\r\n" + 
	        		"			</SESSION>\r\n" + 
	        		
	        		"			<WORKLET DESCRIPTION =\"\" ISVALID =\"YES\" NAME =\""+z_worklet+"\" REUSABLE =\"YES\" VERSIONNUMBER =\"2\">\r\n" + 
	        		"				<TASK DESCRIPTION =\"\" NAME =\"Start\" REUSABLE =\"NO\" TYPE =\"Start\" VERSIONNUMBER =\"2\"/>\r\n" + 
	        		"				<TASK DESCRIPTION =\"\" NAME =\"DEC_FAIL_SESSION\" REUSABLE =\"NO\" TYPE =\"Decision\" VERSIONNUMBER =\"2\">\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Decision Name\" VALUE =\"\"/>\r\n" + 
	        		"				</TASK>\r\n" + 
	        		"				<TASK DESCRIPTION =\"\" NAME =\"CTRL_STOP_JOB\" REUSABLE =\"NO\" TYPE =\"Control\" VERSIONNUMBER =\"2\">\r\n" + 
	        		"					<ATTRIBUTE NAME =\"Control Option\" VALUE =\"Abort top-level workflow\"/>\r\n" + 
	        		"				</TASK>\r\n" + 
	        		"				<TASKINSTANCE DESCRIPTION =\"\" ISENABLED =\"YES\" NAME =\"Start\" REUSABLE =\"NO\" TASKNAME =\"Start\" TASKTYPE =\"Start\"/>\r\n" + 
	        		"				<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"NO\" ISENABLED =\"YES\" NAME =\""+z_session+"\" REUSABLE =\"YES\" TASKNAME =\""+z_session+"\" TASKTYPE =\"Session\" TREAT_INPUTLINK_AS_AND =\"YES\"/>\r\n" + 
	        		"				<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"NO\" ISENABLED =\"YES\" NAME =\""+upd_session+"\" REUSABLE =\"YES\" TASKNAME =\""+upd_session+"\" TASKTYPE =\"Session\" TREAT_INPUTLINK_AS_AND =\"YES\"/>\r\n" + 
	        		"				<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"NO\" ISENABLED =\"YES\" NAME =\""+del_session+"\" REUSABLE =\"YES\" TASKNAME =\""+del_session+"\" TASKTYPE =\"Session\" TREAT_INPUTLINK_AS_AND =\"YES\"/>\r\n" + 
	        		"				<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"YES\" ISENABLED =\"YES\" NAME =\"DEC_FAIL_SESSION\" REUSABLE =\"NO\" TASKNAME =\"DEC_FAIL_SESSION\" TASKTYPE =\"Decision\" TREAT_INPUTLINK_AS_AND =\"YES\"/>\r\n" + 
	        		"				<TASKINSTANCE DESCRIPTION =\"\" FAIL_PARENT_IF_INSTANCE_DID_NOT_RUN =\"NO\" FAIL_PARENT_IF_INSTANCE_FAILS =\"YES\" ISENABLED =\"YES\" NAME =\"CTRL_STOP_JOB\" REUSABLE =\"NO\" TASKNAME =\"CTRL_STOP_JOB\" TASKTYPE =\"Control\" TREAT_INPUTLINK_AS_AND =\"YES\"/>\r\n" + 
	        		"				<WORKFLOWLINK CONDITION =\"$"+del_session+".Status=succeeded\" FROMTASK =\""+del_session+"\" TOTASK =\""+z_session+"\"/>\r\n" + 
	        		"				<WORKFLOWLINK CONDITION =\"\" FROMTASK =\"Start\" TOTASK =\""+upd_session+"\"/>\r\n" + 
	        		"				<WORKFLOWLINK CONDITION =\"$"+upd_session+".Status=succeeded\" FROMTASK =\""+upd_session+"\" TOTASK =\""+del_session+"\"/>\r\n" + 
	        		"				<WORKFLOWLINK CONDITION =\"$"+upd_session+".Status=FAILED  OR $"+upd_session+".Status=ABORTED OR $"+upd_session+".Status=STOPPED\" FROMTASK =\""+upd_session+"\" TOTASK =\"DEC_FAIL_SESSION\"/>\r\n" + 
	        		"				<WORKFLOWLINK CONDITION =\"$"+del_session+".Status= FAILED  OR $"+del_session+".Status= ABORTED  OR $"+del_session+".Status= STOPPED\" FROMTASK =\""+del_session+"\" TOTASK =\"DEC_FAIL_SESSION\"/>\r\n" + 
	        		"				<WORKFLOWLINK CONDITION =\"$"+z_session+".Status= FAILED  OR $"+z_session+".Status= ABORTED  OR $"+z_session+".Status= STOPPED\" FROMTASK =\""+z_session+"\" TOTASK =\"DEC_FAIL_SESSION\"/>\r\n" + 
	        		"				<WORKFLOWLINK CONDITION =\"\" FROMTASK =\"DEC_FAIL_SESSION\" TOTASK =\"CTRL_STOP_JOB\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$Start.ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".SrcSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".SrcFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully loaded\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".TgtSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to load\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".TgtFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Total number of transformation errors\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".TotalTransErrors\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error code\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".FirstErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error message\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+z_session+".FirstErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".SrcSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".SrcFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully loaded\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".TgtSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to load\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".TgtFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Total number of transformation errors\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".TotalTransErrors\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error code\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".FirstErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error message\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+upd_session+".FirstErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".SrcSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to read\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".SrcFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows successfully loaded\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".TgtSuccessRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Rows failed to load\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".TgtFailedRows\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Total number of transformation errors\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".TotalTransErrors\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error code\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".FirstErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"First error message\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$"+del_session+".FirstErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL_SESSION.StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL_SESSION.EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL_SESSION.Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL_SESSION.PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL_SESSION.ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL_SESSION.ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Evaluation result of condition expression\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$DEC_FAIL_SESSION.Condition\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task started\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CTRL_STOP_JOB.StartTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"date/time\" DEFAULTVALUE =\"\" DESCRIPTION =\"The time this task completed\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CTRL_STOP_JOB.EndTime\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CTRL_STOP_JOB.Status\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Status of the previous task that is not disabled\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CTRL_STOP_JOB.PrevTaskStatus\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"integer\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error code for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CTRL_STOP_JOB.ErrorCode\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<WORKFLOWVARIABLE DATATYPE =\"string\" DEFAULTVALUE =\"\" DESCRIPTION =\"Error message for this task\'s execution\" ISNULL =\"NO\" ISPERSISTENT =\"NO\" NAME =\"$CTRL_STOP_JOB.ErrorMsg\" USERDEFINED =\"NO\"/>\r\n" + 
	        		"				<ATTRIBUTE NAME =\"Allow Concurrent Run\" VALUE =\"NO\"/>\r\n" + 
	        		"			</WORKLET>\r\n" + 
	        		
	        		
	        		"		</FOLDER>\r\n" +
	        		//1st folder complete
	        		//powermart --> repository --> 2nd 

	        		
	        		"		<FOLDER NAME=\""+b_shortcutFolder+"\" GROUP=\"\" OWNER=\"INFA_ADMIN\" SHARED=\"SHARED\" DESCRIPTION=\"\" PERMISSIONS=\"rwx------\" UUID=\""+UUID.randomUUID().toString()+"\">\r\n" +
	        		//powermart --> repository --> 2nd folder -->1st transformation
	        		"			<TRANSFORMATION DESCRIPTION =\"\" NAME =\""+n_descr+"\" OBJECTVERSION =\"1\" REUSABLE =\"YES\" TYPE =\"Lookup Procedure\" VERSIONNUMBER =\"1\">\r\n"); 
	        for(int row=rowStart; row<=lkpProdRowEnd; row++) {
		    	   if(!isCellEmpty(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
			    	   String dataType = lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
			    		String name =  lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    		int precision = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
				    	int scale = (int)(lkpProdSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
				    	if(name.startsWith("I_")) {
				    		sb2.append("				<TRANSFORMFIELD DATATYPE =\""+dataType+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
				    	}else {
				    		sb2.append("				<TRANSFORMFIELD DATATYPE =\""+dataType+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"LOOKUP/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");				    		
				    	}
			    	}
	        }
	        
	     sb2.append("				<TABLEATTRIBUTE NAME =\"Lookup Sql Override\" VALUE =\""+b_lkpSqlOverride+"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup table name\" VALUE =\""+z_entity+"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup Source Filter\" VALUE =\"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup caching enabled\" VALUE =\"YES\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup policy on multiple match\" VALUE =\"Use Last Value\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup condition\" VALUE =\""+b_trnsf_1_lkpCondition+"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Connection Information\" VALUE =\""+z_conn+"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Source Type\" VALUE =\"Database\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Recache if Stale\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup cache directory name\" VALUE =\"$PMCacheDir\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup cache initialize\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup cache persistent\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup Data Cache Size\" VALUE =\"20000000\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup Index Cache Size\" VALUE =\"10000000\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Dynamic Lookup Cache\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Synchronize Dynamic Cache\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Output Old Value On Update\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Update Dynamic Cache Condition\" VALUE =\"TRUE\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Cache File Name Prefix\" VALUE =\"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Re-cache from lookup source\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Insert Else Update\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Update Else Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Datetime Format\" VALUE =\"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Thousand Separator\" VALUE =\"None\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Decimal Separator\" VALUE =\".\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Case Sensitive String Comparison\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Null ordering\" VALUE =\"Null Is Highest Value\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Sorted Input\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup source is static\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Pre-build lookup cache\" VALUE =\"Auto\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Subsecond Precision\" VALUE =\"6\"/>\r\n" + 
    		        "			</TRANSFORMATION>\r\n" +
	        		
	        		//powermart --> repository --> 2nd folder --> 2nd transformation
	        		"			<TRANSFORMATION DESCRIPTION =\"\" NAME =\""+v_descr+"\" OBJECTVERSION =\"1\" REUSABLE =\"YES\" TYPE =\"Lookup Procedure\" VERSIONNUMBER =\"1\">\r\n" );
					     for(int row=rowStart; row<=lkpStage1RowEnd; row++) {
					    	   if(!isCellEmpty(lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
						    	   String dataType = lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
						    		String name =  lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
						    		int precision = (int)(lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
							    	int scale = (int)(lkpStage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
							    	if(name.startsWith("I_")) {
	      sb2.append("				<TRANSFORMFIELD DATATYPE =\""+dataType+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"INPUT/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");					    		
							    	}else {
		  sb2.append("				<TRANSFORMFIELD DATATYPE =\""+dataType+"\" DEFAULTVALUE =\"\" DESCRIPTION =\"\" NAME =\""+name+"\" PICTURETEXT =\"\" PORTTYPE =\"LOOKUP/OUTPUT\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");				    		
							    	}
						    	}
				      }
					     
	      sb2.append("				<TABLEATTRIBUTE NAME =\"Lookup Sql Override\" VALUE =\"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup table name\" VALUE =\""+i_entity+"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup Source Filter\" VALUE =\"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup caching enabled\" VALUE =\"YES\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup policy on multiple match\" VALUE =\"Use Last Value\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup condition\" VALUE =\""+b_trnsf_2_lkpCondition+"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Connection Information\" VALUE =\""+i_conn+"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Source Type\" VALUE =\"Database\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Recache if Stale\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Tracing Level\" VALUE =\"Normal\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup cache directory name\" VALUE =\"$PMCacheDir\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup cache initialize\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup cache persistent\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup Data Cache Size\" VALUE =\"20000000\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup Index Cache Size\" VALUE =\"10000000\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Dynamic Lookup Cache\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Synchronize Dynamic Cache\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Output Old Value On Update\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Update Dynamic Cache Condition\" VALUE =\"TRUE\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Cache File Name Prefix\" VALUE =\"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Re-cache from lookup source\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Insert Else Update\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Update Else Insert\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Datetime Format\" VALUE =\"\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Thousand Separator\" VALUE =\"None\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Decimal Separator\" VALUE =\".\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Case Sensitive String Comparison\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Null ordering\" VALUE =\"Null Is Highest Value\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Sorted Input\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Lookup source is static\" VALUE =\"NO\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Pre-build lookup cache\" VALUE =\"Auto\"/>\r\n" + 
	        		"				<TABLEATTRIBUTE NAME =\"Subsecond Precision\" VALUE =\"6\"/>\r\n" + 
	        		"			</TRANSFORMATION>\r\n" +
	        		//powermart --> repository --> 2nd folder --> 1st source
	        		"			<SOURCE BUSINESSNAME =\"\" DATABASETYPE =\""+i_dbtype+"\" DBDNAME =\""+i_conn+"\" DESCRIPTION =\"\" NAME =\""+i_entity+"\" OBJECTVERSION =\"1\" OWNERNAME =\""+i_schema+"\" VERSIONNUMBER =\"1\">\r\n" );
	      
	       count = 0;
	       int countForDate = 0;
	       for(int row=rowStart; row<=stage1RowEnd; row++) {
	    	   if(!isCellEmpty(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
	    		   count=count+1;
	    		   countForDate = count;
		    	   String dataType = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    	   if(!dataType.isEmpty()) {
			    	   String name = stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
			    	   int precision = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	   int scale = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	   int length = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getNumericCellValue());
			    	   int physical_length = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("F")).getNumericCellValue());
			    	   int physical_offset = (int)(stage1Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("G")).getNumericCellValue());
			    	   
		 sb2.append("				<SOURCEFIELD BUSINESSNAME =\"\" DATATYPE =\""+dataType+"\" DESCRIPTION =\""+name+"\" FIELDNUMBER =\""+count+"\" FIELDPROPERTY =\"0\" FIELDTYPE =\"ELEMITEM\" HIDDEN =\"NO\" KEYTYPE =\"NOT A KEY\" LENGTH =\""+length+"\" LEVEL =\"0\" NAME =\""+name+"\" NULLABLE =\"NOTNULL\" OCCURS =\"0\" OFFSET =\"0\" PHYSICALLENGTH =\""+physical_length+"\" PHYSICALOFFSET =\""+physical_offset+"\" PICTURETEXT =\"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\" USAGE_FLAGS =\"\"/>\r\n");
		    	   }
	    	   }
	       }
	       countForDate=countForDate+1; 
	     sb2.append("				<SOURCEFIELD BUSINESSNAME =\"\" DATATYPE =\"date\" DESCRIPTION =\"\" FIELDNUMBER =\""+countForDate+"\" FIELDPROPERTY =\"0\" FIELDTYPE =\"ELEMITEM\" HIDDEN =\"NO\" KEYTYPE =\"NOT A KEY\" LENGTH =\"19\" LEVEL =\"0\" NAME =\"REC_CREATE_DATE\" NULLABLE =\"NULL\" OCCURS =\"0\" OFFSET =\"5\" PHYSICALLENGTH =\"19\" PHYSICALOFFSET =\"35\" PICTURETEXT =\"\" PRECISION =\"19\" SCALE =\"0\" USAGE_FLAGS =\"\"/>\r\n" + 
	        		"			</SOURCE>\r\n"	+
	        		
	        		//powermart --> repository --> 2nd folder --> 2nd source
	        		"			<SOURCE BUSINESSNAME =\"\" DATABASETYPE =\""+z_Dbtype+"\" DBDNAME =\""+z_conn+"\" DESCRIPTION =\"\" NAME =\""+z_entity+"\" OBJECTVERSION =\"1\" OWNERNAME =\""+z_conn+"\" VERSIONNUMBER =\"1\">\r\n" );
	     count=0;
	     for(int row=rowStart; row<=prodRowEnd; row++) {
	    	 count++;
	    	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
		    	   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    	   if(!dataType.isEmpty()) {
		    		String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    		int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	int length = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getNumericCellValue());
			    	int offset = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("F")).getNumericCellValue());
			    	int physicalLength = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("G")).getNumericCellValue());
			    	int physicalOffset = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")).getNumericCellValue());
			    	String primaryKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
			    	String nullVal = "NULL";
			    	if(primaryKey.equalsIgnoreCase("Y")) {
			    		primaryKey = "PRIMARY KEY";
			    		nullVal = "NOTNULL";
			    	}else {
			    		primaryKey = "NOT A KEY";
			    	}
		 sb2.append("				<SOURCEFIELD BUSINESSNAME =\"\" DATATYPE =\""+dataType+"\" DESCRIPTION =\"\" FIELDNUMBER =\""+count+"\" FIELDPROPERTY =\"0\" FIELDTYPE =\"ELEMITEM\" HIDDEN =\"NO\" KEYTYPE =\""+primaryKey+"\" LENGTH =\""+length+"\" LEVEL =\"0\" NAME =\""+name+"\" NULLABLE =\""+nullVal+"\" OCCURS =\"0\" OFFSET =\""+offset+"\" PHYSICALLENGTH =\""+physicalLength+"\" PHYSICALOFFSET =\""+physicalOffset+"\" PICTURETEXT =\"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\" USAGE_FLAGS =\"\"/>\r\n");
		    	   }
		    	}
    }
	        		
	     sb2.append( "			</SOURCE>\r\n" + 
	        		
					//powermart --> repository --> 2nd folder --> 3rd source 
	        		"			<SOURCE BUSINESSNAME =\"\" DATABASETYPE =\""+i_dbtype+"\" DBDNAME =\""+i_conn+"\" DESCRIPTION =\"\" NAME =\""+r_entity+"\" OBJECTVERSION =\"1\" OWNERNAME =\""+i_schema+"\" VERSIONNUMBER =\"1\">\r\n" ); 
	        		 count=0;
	     for(int row=rowStart; row<=stage2RowEnd; row++) {
	    	 count++;
	    	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
		    	   String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    	   if(!dataType.isEmpty()) {
		    		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	int length = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("E")).getNumericCellValue());
			    	int offset = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("F")).getNumericCellValue());
			    	int physicalLength = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("G")).getNumericCellValue());
			    	int physicalOffset = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("H")).getNumericCellValue());
			    	String primaryKey = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
			    	String nullVal = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("J")).getStringCellValue();
			    	if(primaryKey.equalsIgnoreCase("Y")) {
			    		primaryKey = "PRIMARY KEY";
			    	}else {
			    		primaryKey = "NOT A KEY";
			    	}
		 sb2.append("				<SOURCEFIELD BUSINESSNAME =\"\" DATATYPE =\""+dataType+"\" DESCRIPTION =\"\" FIELDNUMBER =\""+count+"\" FIELDPROPERTY =\"0\" FIELDTYPE =\"ELEMITEM\" HIDDEN =\"NO\" KEYTYPE =\""+primaryKey+"\" LENGTH =\""+length+"\" LEVEL =\"0\" NAME =\""+name+"\" NULLABLE =\""+nullVal+"\" OCCURS =\"0\" OFFSET =\""+offset+"\" PHYSICALLENGTH =\""+physicalLength+"\" PHYSICALOFFSET =\""+physicalOffset+"\" PICTURETEXT =\"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\" USAGE_FLAGS =\"\"/>\r\n");
		    	   }
		    	}
    }   		
	     sb2.append("			</SOURCE>\r\n" +
	        		
	        		
	        		//powermart --> repository --> 2nd folder --> 1st target
	        		"			<TARGET BUSINESSNAME =\"\" CONSTRAINT =\"\" DATABASETYPE =\""+etlDbtype+"\" DESCRIPTION =\"\" NAME =\""+etlEntity+"\" OBJECTVERSION =\"1\" TABLEOPTIONS =\"\" VERSIONNUMBER =\"1\">\r\n" );
	     count=0;
	     for(int row=rowStart; row<=stage2RowEnd; row++) {
	    	 count++;
	    	   if(!isCellEmpty(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
		    	   String dataType = stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    	   if(!dataType.isEmpty()) {
		    		String name =  stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    		int precision = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	int scale = (int)(stage2Sheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
		 sb2.append("				<TARGETFIELD BUSINESSNAME =\"\" DATATYPE =\""+dataType+"\" DESCRIPTION =\"\" FIELDNUMBER =\""+count+"\" KEYTYPE =\"NOT A KEY\" NAME =\""+name+"\" NULLABLE =\"NOTNULL\" PICTURETEXT =\"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
		    	   }
		    	}
    }
	     sb2.append("			</TARGET>\r\n" + 
	        		//powermart --> repository --> 2nd folder --> 2nd target
	        		"			<TARGET BUSINESSNAME =\"\" CONSTRAINT =\"\" DATABASETYPE =\""+z_Dbtype+"\" DESCRIPTION =\"\" NAME =\""+z_entity+"\" OBJECTVERSION =\"1\" TABLEOPTIONS =\"\" VERSIONNUMBER =\"1\">\r\n" ); 
	     count=0;
	     for(int row=rowStart; row<=prodRowEnd; row++) {
	    	 count++;
	    	   if(!isCellEmpty(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")))) {
		    	   String dataType = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("B")).getStringCellValue();
		    	   if(!dataType.isEmpty()) {
		    		String name =  prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("A")).getStringCellValue();
		    		int precision = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("C")).getNumericCellValue());
			    	int scale = (int)(prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("D")).getNumericCellValue());
			    	String primaryKey = prodSheet.getRow(row).getCell(CellReference.convertColStringToIndex("I")).getStringCellValue();
			    	if(primaryKey.equalsIgnoreCase("Y")) {
			    		primaryKey = "PRIMARY KEY";
			    	}else {
			    		primaryKey = "NOT A KEY";
			    	}
		 sb2.append("			<TARGETFIELD BUSINESSNAME =\"\" DATATYPE =\""+dataType+"\" DESCRIPTION =\"\" FIELDNUMBER =\""+count+"\" KEYTYPE =\""+primaryKey+"\" NAME =\""+name+"\" NULLABLE =\"NOTNULL\" PICTURETEXT =\"\" PRECISION =\""+precision+"\" SCALE =\""+scale+"\"/>\r\n");
		    	   }
		    	}
    }
	     sb2.append("			</TARGET>\r\n" +
	        		"		</FOLDER>\r\n" +
	        		//2nd folder complete
	        		"	</REPOSITORY>\r\n" +
	        		"</POWERMART>\r\n");
	        
	                
	        try {
	        	 document1 = builder.parse(new InputSource(new StringReader(sb1.toString())));
			} catch (SAXException e) {
				logger.error("Error parsing 1st xml : "+e.getMessage());
				logger.error(e.getStackTrace());
				throw new Exception("Error parsing 1st xml : "+e.getStackTrace());
			}
	        
	        try {
	        	//System.out.println(sb2.toString());
	        	 document2 = builder.parse(new InputSource(new StringReader(sb2.toString())));
			} catch (SAXException e) {
				logger.error("Error parsing 2nd xml : "+e.getMessage());
				logger.error(e.getStackTrace());
				throw new Exception("Error parsing 2nd xml : "+e.getStackTrace());
			}

	        TransformerFactory tFactory = TransformerFactory.newInstance();

	        Transformer transformer = tFactory.newTransformer();
	        //Add indentation to output
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "powrmart.dtd");
	        
	        DOMSource source1 = new DOMSource(document1);
	        StreamResult result1 = new StreamResult(new File("C:\\DMDM\\"+b_worklet+".xml"));
	        transformer.transform(source1, result1);
	        
	        DOMSource source2 = new DOMSource(document2);
	        StreamResult result2 = new StreamResult(new File("C:\\DMDM\\"+z_worklet+".xml"));
	        transformer.transform(source2, result2);

	    }
	    catch(Exception e)
	    {
	        logger.error("Exception " + e.getMessage());
	        logger.error(e.getStackTrace());
	        throw new Exception(e.getMessage());
	    }finally {
	    	try {
				workBook.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				throw new Exception(e.getMessage());
			}
	    }
	}

}
