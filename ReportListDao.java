package jp.co.ana.cas.proto.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jp.co.ana.cas.proto.dto.ReportListDto;
import jp.co.ana.cas.proto.util.OutputLog;

public class ReportListDao {
	//DB情報
	private String driverUrl;
	private String dbUser;
	private String dbPasswd;
	private String insertSql;

	private Connection dbConn = null;

	public ReportListDao() {
		driverUrl = System.getenv("REPORT_DRIVER_URL");
		dbUser = System.getenv("REPORT_DB_USER");
		dbPasswd = System.getenv("REPORT_DB_PASSWD");
		insertSql = System.getenv("REPORT_INSERT_SQL");
	}

	public void insertReportToDB(ReportListDto reportDto) {

//		OutputLog.outputLogMessage(OutputLog.DEBUG, "INSERT_SQL_Before:" + insertSql);
		try {
			//PostgreSQLへ接続
			DriverManager.setLoginTimeout(10000);
			dbConn = DriverManager.getConnection(driverUrl, dbUser, dbPasswd);	
			//自動コミットOFF
			dbConn.setAutoCommit(false);
			//Insert文編集
			PreparedStatement ps = dbConn.prepareStatement(insertSql);
			ps.setString(1, reportDto.getReportName());
			ps.setString(2, reportDto.getReportData());
			OutputLog.outputLogMessage(OutputLog.DEBUG, "INSERT_SQL: " + ps.toString());
//			OutputLog.outputLogMessage(OutputLog.DEBUG, "INSERT_SQL_After: " + ps.toString());

			//Insert実行
			ps.executeUpdate();
			dbConn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			OutputLog.outputLogMessage(OutputLog.ERROR, "DB Connect ERROR. ");
		} finally {
			if( dbConn != null ) {
				try {
					dbConn.close();
				}
				catch(Exception e) {
					// 何もしない
					e.printStackTrace();
				}
			}
		}
	}
}
