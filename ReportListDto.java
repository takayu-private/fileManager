package jp.co.ana.cas.proto.dto;

public class ReportListDto {
	private int id;
	private String reportName;
	private String reportData;
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getReportName() {
		return this.reportName;
	}
	
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public String getReportData() {
		return this.reportData;
	}
	
	public void setReportData(String reportData) {
		this.reportData = reportData;
	}
}
