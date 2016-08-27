package com.sms800.quickwin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CommonUtil {
	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	
	public static boolean rejectLinePattrn(String line, String rejectPtrn){
		if(line.trim().length()==0)return true;
		String[] pattrnArray = rejectPtrn.split("\\|\\|");
		for (String pat : pattrnArray) {
			String[] rejectionPattern=pat.split(",") ;
			for (String ptrn : rejectionPattern) {
				ptrn = "(.*)("+ptrn+")" ;
				Pattern r = Pattern.compile(ptrn);
				Matcher m = r.matcher(line);
				if (m.find( ))
					return true;			
			}
			
		}
				
		return false;
	}
	
	public static boolean rejectLine(String line, String rejectPtrn){
		if(line.trim().length()==0)return true;
		String[] rejectionPattern=rejectPtrn.split(",")	;
		for (String ptrn : rejectionPattern) {
			Pattern r = Pattern.compile(ptrn);
			Matcher m = r.matcher(line);
			if (m.find( ))
				return true;			
		}		
		return false;
	}
	
	public static boolean validateExpession(List<String> patterns,String line){
        for(int cnt=0;cnt<patterns.size();cnt++){
               String pattern = patterns.get(cnt);
               Pattern r = Pattern.compile(pattern);
               Matcher m = r.matcher(line);
               if (!m.find( )) {
                     return false;
               }
        }
        return true;
}

	public static String getExactVal(String str, String pattren, int rowNumber) throws Exception{
		if(str!=null && !"".equals(str) && pattren!=null && !"".equals(pattren)){
			String colPat=pattren.substring(pattren.indexOf("[")+1,pattren.length()-1);
			if(colPat!=null && "0".equals(colPat)){
				return str;
			} else if(colPat!=null && !"".equals(colPat)){
				String[] indexs=colPat.split(Constant.HYPHEN_DELEMETER);
				if(indexs!=null && indexs.length==2 && str.length()>Integer.parseInt(indexs[1])){
					return str.substring(Integer.parseInt(indexs[0]), Integer.parseInt(indexs[1])+1);
				} else{
					throw new Exception("Error occureed in Retriving Column Value for Row ::"+rowNumber+"	& Value: "+str +"	& Pattern : "+ pattren);
				}
			}
		} else{
			throw new Exception("Error occureed in Retriving Column Value for Row ::"+rowNumber+"	& Value: "+str +"	& Pattern : "+ pattren);
		}
		return null;
	}
	
	//Load Mapping File
	public static NodeList loadMappingFile(String mappingConfFile) throws Exception{
		NodeList mappings = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(mappingConfFile);
			if (document != null) {
				Element root = document.getDocumentElement();
				mappings = root.getElementsByTagName(Constant.TAG_JOB_TYPE);
			} else{
				throw new Exception("XML Mapping Document cannot be null");
			}
		} catch (Exception e) {
			logger.debug("[[Exception -QueryGeneratorUtil-->loadMappingFile Detils : "+e.getMessage());
			throw new Exception(e);
		}
		return mappings;
	}
	
	//Load Property from mapping File
	public static Map<String, Map<String,Object>> loadMappingDetails(NodeList mappings, String[] mappingAttr){
		Map<String, Map<String,Object>> xlTableMap = new HashMap<>();
		try {
			for(int mapingCnt=0; mapingCnt < mappings.getLength(); mapingCnt++){
				Node mapping = mappings.item(mapingCnt);
				NamedNodeMap mappingAttrs = mapping.getAttributes();
				Map<String,Object> outerMap = new HashMap<>();
				outerMap.put(Constant.ATTR_JOB_TABLE, mappingAttrs.getNamedItem(Constant.ATTR_JOB_TABLE).getTextContent());
				NodeList columns = ((Element)mapping).getElementsByTagName(Constant.ATTR_TABLE_NODE);
				List<Map<String,String>> innerMapLst = new ArrayList<>();
				for(int colCnt=0; colCnt<columns.getLength(); colCnt++){
					Map<String,String> innerMap = new HashMap<>();
					Node column = columns.item(colCnt);
					NamedNodeMap columnMap = column.getAttributes();
					
					for (String attr: mappingAttr) {
						if(columnMap.getNamedItem(attr)!=null){
							innerMap.put(attr, columnMap.getNamedItem(attr).getTextContent());
						}else{
							throw new Exception("Error: Attribute "+attr +" doesn't exist in XML mapping ");
						}
					}
					if(Constant.DFLT_COLMN_NAME.equalsIgnoreCase(innerMap.get(Constant.ATTR_CSV_COL))){
						innerMap.put(Constant.ATTR_DFLT_VAL, columnMap.getNamedItem(Constant.ATTR_DFLT_VAL).getTextContent());
					}
					if("DATE".equalsIgnoreCase(innerMap.get(Constant.ATTR_DATATYPE))){
						innerMap.put(Constant.ATTR_DATAFORMAT, columnMap.getNamedItem(Constant.ATTR_DATAFORMAT).getTextContent());
					}
					innerMapLst.add(innerMap);
				}
				outerMap.put(Constant.TABLE_META_DATA, innerMapLst);
				xlTableMap.put(mappingAttrs.getNamedItem(Constant.ATTR_JOB_ALIAS).getTextContent(), outerMap);
			}
		} catch (Exception e) {
			logger.debug("[[Exception -QueryGeneratorUtil-->loadMappingDetails Detils : "+e.getMessage());
			//logger.info(e.getMessage());
		}
		return xlTableMap;
	}
	
	//Retrieve ALIAS Mapping details
	//Load Property from mapping File
	public static Map<String, Map<String,Object>> loadAliasMappingDetails(NodeList mappings, String[] mappingAttr){
		Map<String, Map<String,Object>> xlTableMap = new HashMap<>();
		try {
			for(int mapingCnt=0; mapingCnt < mappings.getLength(); mapingCnt++){
				Node mapping = mappings.item(mapingCnt);
				NamedNodeMap mappingAttrs = mapping.getAttributes();
				Map<String,Object> outerMap = new HashMap<>();
				outerMap.put(Constant.ATTR_JOB_ALIAS, mappingAttrs.getNamedItem(Constant.ATTR_JOB_ALIAS).getTextContent());
				NodeList columns = ((Element)mapping).getElementsByTagName(Constant.ATTR_TABLE_NODE);
				List<Map<String,String>> innerMapLst = new ArrayList<>();
				for(int colCnt=0; colCnt<columns.getLength(); colCnt++){
					Map<String,String> innerMap = new HashMap<>();
					Node column = columns.item(colCnt);
					NamedNodeMap columnMap = column.getAttributes();
					
					for (String attr: mappingAttr) {
						if(columnMap.getNamedItem(attr)!=null){
							innerMap.put(attr, columnMap.getNamedItem(attr).getTextContent());
						}else{
							throw new Exception("Error: Attribute "+attr +" doesn't exist in XML mapping ");
						}
					}
					innerMapLst.add(innerMap);
				}
				outerMap.put(Constant.CSV_CONF_DATA, innerMapLst);
				xlTableMap.put(mappingAttrs.getNamedItem(Constant.ATTR_JOB_ALIAS).getTextContent(), outerMap);
			}
		} catch (Exception e) {
			logger.debug("[[Exception -QueryGeneratorUtil-->loadMappingDetails Detils : "+e.getMessage());
			//logger.info(e.getMessage());
		}
		return xlTableMap;
	}
	
	
}
