package com.dmdm.controller;

import java.util.ArrayList;
import java.util.List;

public class DDLGeneration {
		   // JDBC driver name and database URL
		   static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";  
		   static final String DB_URL = "jdbc:sqlserver://ec2-34-217-127-32.us-west-2.compute.amazonaws.com:1433;databaseName=RAE";

		   //  Database credentials
		   static final String USER = "RAE_USER";
		   static final String PASS = "Innova";
		   
		   public static void main(String[] args) {
			   List<String> completeList = new ArrayList<String>();
			   for(int i=0; i<10; i++){
				   completeList.add("number"+i);
			   }
			   //System.out.println(completeList);
			   StringBuilder sb = new StringBuilder();
			   sb.append("{");
			   for(String item:completeList){
				   sb.append(item).append(", ");
			   }
			   sb.deleteCharAt(sb.lastIndexOf(","));
			   sb.append("}");
			   System.out.println(sb.toString());
			   
			   /*
		   Connection conn = null;
		   Statement stmt = null;
		   try{
		      //STEP 2: Register JDBC driver
		      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		      //STEP 3: Open a connection
		      System.out.println("Connecting to a selected database...");
		      conn = DriverManager.getConnection(DB_URL, USER, PASS);
		      System.out.println("Connected database successfully...");
		      
		      //STEP 4: Execute a query
		      System.out.println("Creating table in given database...");
		      stmt = conn.createStatement();
		      
		      String sql = "CREATE TABLE SAMPLE ( USERNAME VARCHAR (50) NOT NULL , PASSWORD VARCHAR (4000) NOT NULL , UPDATED_DATE DATETIME)"; 

		      stmt.executeUpdate(sql);
		      System.out.println("Created table in given database...");
		   }catch(SQLException se){
		      //Handle errors for JDBC
		      se.printStackTrace();
		   }catch(Exception e){
		      //Handle errors for Class.forName
		      e.printStackTrace();
		   }finally{
		      //finally block used to close resources
		      try{
		         if(stmt!=null)
		            conn.close();
		      }catch(SQLException se){
		      }// do nothing
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }//end finally try
		   }//end try
		   System.out.println("Goodbye!");
		*/}//end main
		

}
