package jp.co.ana.cas.proto.dto;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Test;

public class ReportListDtoTest {

	@Test
	public void testGetId() throws Exception {
		int testId = 1;
		ReportListDto reportDto = new ReportListDto();
		
		Field field = reportDto.getClass().getDeclaredField("id");
		field.setAccessible(true);
		field.set(reportDto, testId);

		assertEquals(testId, reportDto.getId());
	}

	@Test
	public void testSetId() throws Exception {
		int testId = 10;
		ReportListDto reportDto = new ReportListDto();

		Field field = reportDto.getClass().getDeclaredField("id");
		field.setAccessible(true);
        reportDto.setId(testId);
        
        assertEquals(testId, (int)field.get(reportDto));
	}

	@Test
	public void testGetReportName() throws Exception {
		String testReportName = "report_123456.pdf";
		ReportListDto reportDto = new ReportListDto();
		
		Field field = reportDto.getClass().getDeclaredField("reportName");
		field.setAccessible(true);
		field.set(reportDto, testReportName);

		assertEquals(testReportName, reportDto.getReportName());
	}

	@Test
	public void testSetReportName() throws Exception {
		String testReportName = "report_098765.pdf";
		ReportListDto reportDto = new ReportListDto();

		Field field = reportDto.getClass().getDeclaredField("reportName");
		field.setAccessible(true);
        reportDto.setReportName(testReportName);
        
        assertEquals(testReportName, (String)field.get(reportDto));
	}

	@Test
	public void testGetReportData() throws Exception {
		String testReportData = "jfals;fuawjffaejfaweo;ifuja0=";
		ReportListDto reportDto = new ReportListDto();
		
		Field nameField = reportDto.getClass().getDeclaredField("reportData");
        nameField.setAccessible(true);
        nameField.set(reportDto, testReportData);

		assertEquals(testReportData, reportDto.getReportData());
	}

	@Test
	public void testSetReportData() throws Exception {
		String testReportData = "jfafafawethjtrhj;lguyikuja0=";
		ReportListDto reportDto = new ReportListDto();

		Field field = reportDto.getClass().getDeclaredField("reportData");
		field.setAccessible(true);
        reportDto.setReportData(testReportData);
        
        assertEquals(testReportData, (String)field.get(reportDto));
	}

}
