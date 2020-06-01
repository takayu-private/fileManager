package jp.co.ana.cas.proto.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jp.co.ana.cas.proto.dao.ReportListDao;
import jp.co.ana.cas.proto.dto.ReportListDto;

/**
 * 帳票出力クラス.
 * @author 113105A00ACF0
 *
 */
public class OutputReport {
	//SVF情報
	private String protocol = System.getenv("REPORT_PROTOCOL");
	private String svfHost = System.getenv("SVF_HOST");
	private String svfPortstr = System.getenv("SVF_PORT");
	private String filePath = System.getenv("REPORT_FILE_PATH");
	private String httpMethod = System.getenv("REPORT_HTTP_METHOD");

	// 予約情報
	private String csvReserveInfo;

	private HttpURLConnection urlConn;

	private PrintStream ps;

	private BufferedReader reader;

	private ReserveInformation reserveInfo = new ReserveInformation();

	private ReportListDao reportDao = new ReportListDao();
	private ReportListDto reportDto = new ReportListDto();

	/**
	 * 帳票出力メイン処理.
	 * @param reserveNum
	 */
	public void outputReportMain(String reserveNum) {
		try {
			//予約情報取得
			csvReserveInfo = reserveInfo.getReserveInformation(reserveNum);
			OutputLog.outputLogMessage(OutputLog.DEBUG, "csvReserveInfo: " + csvReserveInfo);

			System.out.println("HttpConnect START!!");

			//接続するURLを指定する
			int svfPort = 34567;
			if (svfPortstr != null) {
				svfPort = Integer.parseInt(svfPortstr);
			}
			URL url = new URL( protocol, svfHost, svfPort, filePath );

			//帳票作成
			openConnection(url);
			String reportFile = outputReportRequest(csvReserveInfo);
			closeConnection();
			OutputLog.outputLogMessage(OutputLog.DEBUG, "reportFile: " + reportFile);
			
			//帳票格納
			insertReport(reserveNum, reportFile);

			//ログ出力
			OutputLog.outputLogMessage(OutputLog.INFO, "帳票出力が完了しました。");
		}
		catch(Exception e) {
			//ログ出力
			OutputLog.outputLogMessage(OutputLog.ERROR, "帳票出力が失敗しました。" + e);
		}
	}

	public void openConnection(URL url) throws IOException {
		//コネクションを取得する
		urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setConnectTimeout(10000);

		// HTTPメソッドを設定する
		//    urlConn.setRequestMethod("GET");
		urlConn.setRequestMethod(httpMethod);
		// リスエストとボディ送信を許可する
		urlConn.setDoOutput(true);
		// レスポンスのボディ受信を許可する
		urlConn.setDoInput(true);
		// コネクション接続
		urlConn.connect();

		ps = new PrintStream(urlConn.getOutputStream(), true, "utf-8");
		InputStream in = urlConn.getInputStream();
		reader = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")));
	}

	public void closeConnection() {
		try {
			if( ps != null ) {
				ps.close();
				ps = null;
			}
			if( reader != null ) {
				reader.close();
				reader = null;
			}
			if( urlConn != null ) {
				urlConn.disconnect();
				urlConn = null;
			}
		}
		catch(Exception e) {
			// 何もしない。
			e.printStackTrace();
		}
	}

	public String outputReportRequest(String csvReserveInfo) throws IOException {
		String report = null;

		ps.print(csvReserveInfo);

		int status = urlConn.getResponseCode();
		System.out.println("HTTP status code:" + status);
		OutputLog.outputLogMessage(OutputLog.DEBUG, "SVF HTTP status code:" + status);

		if(status == HttpURLConnection.HTTP_OK) {
			StringBuilder sb = new StringBuilder();
			String line;
			// レスポンスのHTTP body取得
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			report = sb.toString();
			System.out.println("body部: "+report);
			OutputLog.outputLogMessage(OutputLog.DEBUG, "SVF HTTP body:" + report);
		}


		return report;
	}

	public void insertReport(String reserveNum, String reportFile) {
		//帳票のファイル名
		String repFileName = "report_" + reserveNum + ".pdf";

		reportDto.setReportName(repFileName);
		reportDto.setReportData(reportFile);
		
		try {
//			reportDao.connectDB();
			reportDao.insertReportToDB(reportDto);
//			reportDao.closeDB();
		}
		catch(Exception e) {
			OutputLog.outputLogMessage(OutputLog.ERROR, "Insert失敗. " + e);
		}
	}
}