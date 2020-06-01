package jp.co.ana.cas.proto.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import jp.co.ana.cas.proto.dao.ReportListDao;
import jp.co.ana.cas.proto.dto.ReportListDto;

//@PrepareForTest(OutputReport.class)
public class OutputReportTest{
	public static final String LogFileName = "CAS-304_M0001//CAS-304_M0001\\Junit.log";
	public static final String junitTest = "[JunitTest]";
	public static final String  CLASS = "OutputReport";
	public static final String SVF_HOST = "52.193.176.86";
	public static final String DRIVER_URL = "jdbc:postgresql://18.182.8.188:5432/postgres";
	public static final String DB_USER = "postgres";
	public static final String DB_PASSWD = "postgres";
	public static final String INSERT_SQL = "INSERT INTO report_strage.report_list (file_name,file_data) values(?,?);";

	@Mock
	HttpURLConnection urlConn;

	@Mock
	PrintStream ps;

	@Mock
	BufferedReader reader;

	@Mock
	Connection dbConn;
	
	@Mock
	ReportListDao reportListDao;

	@InjectMocks
	OutputReport outputReport = new OutputReport();

	// モックの初期化
	@BeforeEach
 	public void initEachTest() {
		MockitoAnnotations.initMocks(this);
 	}

	// 環境変数の設定(のつもりで内部変数に直接設定
	@BeforeEach
	public void setenv() {
//		Whitebox.setInternalState(outputReport, "driverUrl", DRIVER_URL);
//		Whitebox.setInternalState(outputReport, "dbUser", DB_USER);
//		Whitebox.setInternalState(outputReport, "dbPasswd", DB_PASSWD);
//		Whitebox.setInternalState(outputReport, "insertSql", INSERT_SQL);

		Whitebox.setInternalState(outputReport, "protocol", "http");
		Whitebox.setInternalState(outputReport, "svfHost", SVF_HOST);
		Whitebox.setInternalState(outputReport, "svfPortstr", "23456");
		Whitebox.setInternalState(outputReport, "filePath", "FilePath");
		Whitebox.setInternalState(outputReport, "httpMethod", "GET");
	}



//	@Test
	public void connectDBinsertReport_OK() throws SQLException, ClassNotFoundException {
//		Class.forName("org.postgresql.Driver");
		Connection conn = Mockito.mock(Connection.class);
		PowerMockito.spy(DriverManager.class);
		PowerMockito.doReturn(conn).when(DriverManager.class);
		DriverManager.getConnection(anyString(), anyString(), anyString());

//		PowerMockito.when(DriverManager.getConnection("")).thenReturn(conn);
//		given(DriverManager.getConnection(DRIVER_URL, DB_USER, DB_PASSWD)).willReturn(conn);
		//PowerMockito.doReturn(conn).when(DriverManager.getConnection(""));
		//Connection conn = DriverManager.getConnection("","","");
	}

	@Test
	public void insertReport_OK() throws SQLException, ClassNotFoundException {
		ReportListDto dtoMock = mock(ReportListDto.class);
		doNothing().when(reportListDao).insertReportToDB(reportListDto);
		
		String testReserveNum = "123456";
		String testReportFile = "testtesttesttest";
		
		outputReport.insertReport(testReserveNum, testReportFile);
		Field dtoField = outputReport.getClass().getDeclaredField("reportDto.get");
        // private変数へのアクセス制限を解除
		reportDto.setAccessible(true);
		ReportListDto reportDto = (ReportListDto) reportDto.get(outputReport);
		
		assertEquals(testReserveNum, reportDto.getReportName());
		assertEquals(expected, reportDto.getReportData());
	}


//	@Test
	public void insertReport_test_NG() {
		String method = "insertReport";
		int number = 1;

		String reserveNum = "123456";
		String repFileName = "report_" + reserveNum + ".pdf";
		String reportFile = "Test_report_base64_encode_data";

		try {
			//PostgreSQLへ接続
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(DRIVER_URL, DB_USER, DB_PASSWD);

			//自動コミットOFF
			conn.setAutoCommit(false);

			String sql = "DELETE FROM report_strage.report_list WHERE file_name = '"+repFileName+"'";


			PreparedStatement ps = null;

			//実行するSQL文とパラメータを指定する
			ps = conn.prepareStatement(sql);

			//DELETE文を実行する
			int res = 0;

			try {
				res = ps.executeUpdate();
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			//処理件数を表示する
			System.out.println("結果：" + res);

			//コミット
			conn.commit();

			OutputReport opr = new OutputReport();
			opr.insertReport(reserveNum, reportFile);

			Statement stmt = conn.createStatement();
			sql = "SELECT * FROM report_strage.report_list WHERE file_name = '"+repFileName+"'";
			ResultSet rs = stmt.executeQuery(sql);
			String repFileName_chek = null;
			String reportFile_check = null;
			while(rs.next()){
				repFileName_chek = rs.getString("file_name");
				reportFile_check = rs.getString("file_data");
			}
			assertNull(repFileName_chek);
			String ut = junitTest+","+ CLASS+","+method+(number++)+",DBに帳票が登録されていないこと(ファイル名)";
			OutputLog.outputLogMessage(OutputLog.DEBUG, ut);
			assertNull(reportFile_check);
			ut = junitTest+","+ CLASS+","+method+(number++)+",DBに帳票が登録されていないこと(データ)";
			OutputLog.outputLogMessage(OutputLog.DEBUG, ut);


		} catch (Exception e) {

			e.printStackTrace();
			System.out.println("Altea Connect ERROR：" + e);
		}
	}

	public void renameLogFile() {
		Calendar cl = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		System.out.println(sdf.format(cl.getTime()));

		Path srcPath = Paths.get(LogFileName);
		Path trgPath = Paths.get(LogFileName+"_"+sdf.format(cl.getTime()));
		try {
			Files.move(srcPath, trgPath);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}