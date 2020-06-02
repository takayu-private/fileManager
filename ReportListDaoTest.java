package jp.co.ana.cas.proto.dao;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import jp.co.ana.cas.proto.dto.ReportListDto;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DriverManager.class, ReportListDao.class})
public class ReportListDaoTest {
	private String driverUrl = "jdbc://testtest/test";
	private String dbUser = "testUser";
	private String dbPasswd = "testPasswd";
	private String insertSql = "INSERT INTO report_list (file_name,file_data) values(?,?);";

	@Mock
	Connection dbConn = null;
	
	@InjectMocks
	ReportListDao reportDao = new ReportListDao();
	
	@BeforeEach
	public void initEachTest() {
		MockitoAnnotations.initMocks(this);
	}
	
//	@Test
	public void testReportListDao() {
		fail("まだ実装されていません");
	}

	@Test
	public void testInsertReportToDB() {
	        PowerMockito.mockStatic(DriverManager.class);
	        PreparedStatement ps = mock(PreparedStatement.class);

	        try {
				when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(dbConn);
				when(dbConn.prepareStatement(insertSql)).thenReturn(ps);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		ReportListDao reportDaoSpy = spy(reportDao);

		Whitebox.setInternalState(reportDaoSpy, "driverUrl", driverUrl);
		Whitebox.setInternalState(reportDaoSpy, "dbUser", dbUser);
		Whitebox.setInternalState(reportDaoSpy, "dbPasswd", dbPasswd);
		Whitebox.setInternalState(reportDaoSpy, "insertSql", insertSql);
		
		ReportListDto reportDto = new ReportListDto();
		reportDto.setReportName("report_123456.pdf");
		reportDto.setReportData("falkfgajtgafjapoit=");
		
		reportDaoSpy.insertReportToDB(reportDto);
	}
	
	@Test
	public void testInsertReportToDB_NG() {
	       PreparedStatement ps = mock(PreparedStatement.class);
	       
	        PowerMockito.mockStatic(DriverManager.class);
	        try {
				when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenThrow(SQLException.class);
				when(dbConn.prepareStatement(insertSql)).thenReturn(ps);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		ReportListDao reportDaoSpy = spy(reportDao);

		Whitebox.setInternalState(reportDaoSpy, "driverUrl", driverUrl);
		Whitebox.setInternalState(reportDaoSpy, "dbUser", dbUser);
		Whitebox.setInternalState(reportDaoSpy, "dbPasswd", dbPasswd);
		Whitebox.setInternalState(reportDaoSpy, "insertSql", insertSql);
		
		ReportListDto reportDto = new ReportListDto();
		reportDto.setReportName("report_123456.pdf");
		reportDto.setReportData("falkfgajtgafjapoit=");
		
		StringWriter writer = new StringWriter();
		WriterAppender appender = new WriterAppender(new PatternLayout("%p, %m%n"), writer);
		LogManager.getRootLogger().addAppender(appender);
		LogManager.getRootLogger().setAdditivity(false);
		
		try {
			reportDaoSpy.insertReportToDB(reportDto);
			String logString = writer.toString();
			 assertTrue(logString.contains("DB Connect ERROR."));
		} finally {
			LogManager.getRootLogger().removeAppender(appender);
			LogManager.getRootLogger().setAdditivity(true);
		}
		
		
	}
}
