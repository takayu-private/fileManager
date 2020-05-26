package jp.co.ana.cas.proto.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@PrepareForTest({ URL.class })
public class ReserveInformationTest {

	@Mock
	HttpURLConnection urlConn;

	@Mock
	PrintStream ps;

	@Mock
	BufferedReader reader;

	@InjectMocks
	ReserveInformation rsvinfo = new ReserveInformation();

	public static final Path confPath = Paths.get("src/test/resources/");
	public static final String LogFileName = "CAS-304_M0001//CAS-304_M0001//Junit.log";
	public static final String junitTest = "[JunitTest]";
 	public static final String  CLASS = "ReserveInformation";

 	private static String protocolConf;
 	private static String alteaHostConf;
 	private static String alteaPortConf;
 	private static String filePathConf;
 	private static String httpMethodConf;
 	private static String alteaUrl;
 	
// 	@BeforeEach
 	public void initEachTest() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		//Final Classのメソッドを取得しアクセス可能にする
		Class<?> clazz = Class.forName("java.lang.ProcessEnvironment");
		
		System.out.println( "All fields: " + Arrays.asList(clazz.getDeclaredFields()));
		
		Field theCaseInsensitiveEnvironment = clazz.getDeclaredField("theCaseInsensitiveEnvironment");
		theCaseInsensitiveEnvironment.setAccessible(true);

		//システム環境変数で必要なものだけを差し替える
		Map<String,String> sytemEnviroment = (Map<String, String>) theCaseInsensitiveEnvironment.get(null);
		sytemEnviroment.put("ALTEA_USER","");
		sytemEnviroment.put("ALTEA_PASSWD","");
		sytemEnviroment.put("REPORT_PROTOCOL","http");
		sytemEnviroment.put("ALTEA_HOST","3.112.212.135");
		sytemEnviroment.put("ALTEA_PORT","8081");
		sytemEnviroment.put("REPORT_FILE_PATH","");
		sytemEnviroment.put("REPORT_HTTP_METHOD","POST");

		// Inject対象のクラスに対してモックを注入する。
		// (当該メンバ変数にモックのインスタンスが設定される。
		MockitoAnnotations.initMocks(this);
 	}
 	
 	@Test
    public void function() throws Exception{
		String confFile = "config.json";
		ReserveInformation rsvinfo = new ReserveInformation();
		variableConf(rsvinfo, confFile);
        URL u = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withArguments(anyString(), anyString(), anyInt(), anyString()).thenReturn(u);
        HttpURLConnection huc = PowerMockito.mock(HttpURLConnection.class);
        PowerMockito.when(u.openConnection()).thenReturn(huc);
        PowerMockito.when(huc.getResponseCode()).thenReturn(200);
        rsvinfo.openConnection();
        assertNotNull(rsvinfo.getReserveInformationRequest("123456"));
    }

// 	@Test
// 	public void test() throws Exception {
// 		public class UrlWrapper {
// 			URL url;
// 			
// 			public UrlWrapper(String protocol, String host, int port, String filePath) throws Exception {
// 				url = new URL(protocol, host, port, filePath);
// 			}
// 			
// 			public URLConnection openConnection() throws Exception {
// 				return url.openConnection();
// 			}
// 		}
// 		
// 		UrlWrapper url = Mockito.mock(UrlWrapper.class);
// 		HttpURLConnection huc = PowerMockito.mock(HttpURLConnection.class);
// 		PowerMockito.when(url.openConnection()).thenReturn(huc);
// 		PowerMockito.when(huc.getResponseCode()).thenReturn(200);
// 		rsvinfo.openConnection();
// 		assertNotNull(rsvinfo.getReserveInformationRequest("123456"));
// 	}
 	
//	@Test
	public void getReserveInformation_test() throws Exception {
		String method = "getReserveInformation";
		int number = 1;

		// モック戻り値の設定
		doReturn(200).when(urlConn).getResponseCode();
		doReturn("Reserve_", "Information_", "1234567", null).when(reader).readLine();

		String requestMsg = rsvinfo.createReserveInformationRequest("123456");
		assertNotNull(requestMsg);
		String ut = junitTest+","+ CLASS+","+method+","+(number++)+", Alteaへのリクエストメッセージが作成されること";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut);

		String responseMsg = rsvinfo.getReserveInformationRequest("123456");
		String expected = "Reserve_Information_1234567";
		assertEquals(expected, responseMsg);
		ut = junitTest+","+ CLASS+","+method+","+(number++)+", Alteaから予約情報を取得することを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut);
	}

//	@Test
	public void extractValue_test() {
		String method = "extractValue";
		int number = 1;

		String responseMsg = "test<soapenv:Envelope>test<soapenv:Header>test</soapenv:Header><soapenv:Body><reserveDetails><reserveInformation>test_reserveInformation_123</reserveInformation></reserveDetails></soapenv:Body></soapenv:Envelope>";
		String testValue = rsvinfo.extractValue(responseMsg, ReserveInformation.TAG_SOAP_ENVELOPE);
		String expected = "test<soapenv:Header>test</soapenv:Header><soapenv:Body><reserveDetails><reserveInformation>test_reserveInformation_123</reserveInformation></reserveDetails></soapenv:Body>";
		assertEquals(expected, testValue);
		String ut = junitTest+","+ CLASS+","+method+","+(number++)+", soapenv:Envelopeの値を取り出すことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testValue);

		testValue = rsvinfo.extractValue(testValue, ReserveInformation.TAG_SOAP_BODY);
		expected = "<reserveDetails><reserveInformation>test_reserveInformation_123</reserveInformation></reserveDetails>";
		assertEquals(expected, testValue);
		ut = junitTest+","+ CLASS+","+method+","+(number++)+", soapenv:Bodyの値を取り出すことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testValue);

		testValue = rsvinfo.extractValue(testValue, ReserveInformation.TAG_RESERVE_DETAIL);
		expected = "<reserveInformation>test_reserveInformation_123</reserveInformation>";
		assertEquals(expected, testValue);
		ut = junitTest+","+ CLASS+","+method+","+(number++)+", reserveDetailsの値を取り出すことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testValue);

		testValue = rsvinfo.extractValue(testValue, ReserveInformation.TAG_RESERVE_INFO);
		expected = "test_reserveInformation_123";
		assertEquals(expected, testValue);
		ut = junitTest+","+ CLASS+","+method+","+(number++)+", reserveInformationの値を取り出すことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testValue);

		testValue = rsvinfo.extractValue(null, ReserveInformation.TAG_RESERVE_INFO);
		assertNull(testValue);
		ut = junitTest+","+ CLASS+","+method+","+(number++)+", 第一引数がnullの場合、nullを返すことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testValue);

		testValue = rsvinfo.extractValue("<reserveInformation>test_reserveInformation_123</reserveInformation>", null);
		expected = "<reserveInformation>test_reserveInformation_123</reserveInformation>";
		assertEquals(expected, testValue);
		ut = junitTest+","+ CLASS+","+method+","+(number++)+", 第二引数がnullの場合、第一引数を返すことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testValue);

		testValue = rsvinfo.extractValue("<reserveInformation>test_reserveInformation_123</reserveInformation>", "dummytag");
		expected = "<reserveInformation>test_reserveInformation_123</reserveInformation>";
		assertEquals(expected, testValue);
		ut = junitTest+","+ CLASS+","+method+","+(number++)+", 第一引数の文字列の中にtagが存在しない場合、第一引数を返すことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testValue);
	}

//	@Test
	public void wsSecurity_test() {
		String method = "wsSecurity";
		int number = 1;

		HashMap<String, String> testHash = rsvinfo.wsSecurity();

		assertNotNull(testHash.get("Created"));
        String ut = junitTest+","+ CLASS+","+method+","+(number++)+", Createdの値が生成されることを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testHash.get("Created"));

		assertNotNull(testHash.get("Nonce"));
        ut = junitTest+","+ CLASS+","+method+","+(number++)+", Nonceの値が生成されることを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testHash.get("Nonce"));

		assertNotNull(testHash.get("Password"));
        ut = junitTest+","+ CLASS+","+method+","+(number++)+", Passwordの値が生成されることを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+testHash.get("Password"));
	}


//	@Test
	public void createPasswd_test() {
		String method = "createPasswd";
		int number = 1;

		String dateStr = "2020-03-16T08:18:11.029Z";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("Zulu"));
		Date date=null;
		try {
			date = df.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long Epoch = date.getTime();

		//Created
		String created = rsvinfo.createCreated(Epoch);
		String createdHex = rsvinfo.createCreatedHex(created);
		//Nonce
		String noceHex = rsvinfo.createNonceHex(Epoch);
		String nonce = rsvinfo.createNonce(noceHex);
		//password
		String passwdSha = rsvinfo.createPasswdSha();
		String passwd = rsvinfo.createPasswd(noceHex, createdHex, passwdSha);

		String expected = "C3160D7F0030A1A71194DF29D2DBB16D38620DE0";
//		assertEquals(expected, passwd);
        String ut = junitTest+","+ CLASS+","+method+","+(number++)+", NonceとCreatedとPasswordのハッシュ値をBase64エンコードした値が生成されることを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+passwd);
	}

//	@Test
	public void createNonce_test() {
		String method = "createNonce";
		int number = 1;

		String dateStr = "2020-03-16T08:18:11.029Z";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("Zulu"));
		Date date=null;
		try {
			date = df.parse(dateStr);
		} catch (ParseException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		long Epoch = date.getTime();

		ReserveInformation rsvinfo = new ReserveInformation();
		String noceHex = rsvinfo.createNonceHex(Epoch);
		String nonce = rsvinfo.createNonce(noceHex);
		String expected = "TqNLTKpSlKrr+WWbghe+jA==";
//		assertEquals(expected, nonce);
//        String ut = junitTest+","+ CLASS+","+method+","+(number++)+", NonceをBase64エンコードした値が生成されることを確認";
//		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+nonce);
	}

//	@Test
	public void createNonceHex_test() {
		String method = "createNonceHex";
		int number = 1;

		String dateStr = "2020-03-16T08:18:11.029Z";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("Zulu"));
		Date date=null;
		try {
			date = df.parse(dateStr);
		} catch (ParseException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		long Epoch = date.getTime();
		System.out.println("Epoch_nc: "+Epoch);

		ReserveInformation rsvinfo = new ReserveInformation();
		String noceHex = rsvinfo.createNonceHex(Epoch);
		String expected = "4EA34B4CAA5294AAEBF9659B8217BE8C";
//		assertEquals(expected.toLowerCase(), noceHex);
        String ut = junitTest+","+ CLASS+","+method+","+(number++)+", CreatedをHEX変換した値が生成されることを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+noceHex);
	}


//	@Test
	public void createCreatedHex_test() {
		String method = "createCreatedHex";
		int number = 1;

		String dateStr = "2020-03-16T08:18:11.029Z";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("Zulu"));
		Date date=null;
		try {
			date = df.parse(dateStr);
		} catch (ParseException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		long Epoch = date.getTime();

		ReserveInformation rsvinfo = new ReserveInformation();
		String created = rsvinfo.createCreated(Epoch);
		String createdHex = rsvinfo.createCreatedHex(created);
		String expected = "323032302d30332d31365430383a31383a31312e3032395a";
//		assertEquals(expected, createdHex);
//        String ut = junitTest+","+ CLASS+","+method+","+(number++)+", CreatedをHEX変換した値が生成されることを確認";
//		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+createdHex);
	}

//	@Test
	public void createCreated_test() {
		String method = "createCreated";
		int number = 1;

		String dateStr = "2020-03-16T08:18:11.029Z";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("Zulu"));
		Date date=null;
		try {
			date = df.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long Epoch = date.getTime();
		System.out.println("Epoch_cr: "+Epoch);

		ReserveInformation rsvinfo = new ReserveInformation();
		String created = rsvinfo.createCreated(Epoch);
		String expected = dateStr;
//		assertEquals(expected, created);
//        String ut = junitTest+","+ CLASS+","+method+","+(number++)+", 時刻情報からCreatedが生成されることを確認";
//		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+created);
	}


//	@Test
	public void createPasswdSha_test() {
		String method = "createPasswdSha";
		int number = 1;

		ReserveInformation rsvinfo = new ReserveInformation();
		String passSha = rsvinfo.createPasswdSha();
		String expected = "4243f3ff732f14a562aca4cb6d837cc05726055d";
//		assertEquals(expected, passSha);
//        String ut = junitTest+","+ CLASS+","+method+","+(number++)+", AlteaのパスワードからSHA-1のメッセージダイジェストが生成されることを確認";
//		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+passSha);
	}


//	@Test
	public void createReserveInformationRequest_test() {
		String method = "createReserveInformationRequest";
		int number = 1;

        Random random = new Random();
        int randomValue = random.nextInt(1000000);

        String reserveNum = String.format("%06d", randomValue);
        System.out.println("reserveNum: "+reserveNum);

        ReserveInformation rsvinfo = new ReserveInformation();
		String requestMsg = rsvinfo.createReserveInformationRequest(reserveNum);

		assertNotNull(requestMsg);
		String ut = junitTest+","+ CLASS+","+method+","+(number++)+", 予約情報取得要求を作成することを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +",requestMsg:"+requestMsg);

	}

//	@Test
//	public void getReserveInformationRequest_test() {
//		String method = "getReserveInformationRequest";
//		int number = 1;
//
//        Random random = new Random();
//        int randomValue = random.nextInt(1000000);
//
//        String reserveNum = String.format("%06d", randomValue);
//        System.out.println("reserveNum: "+reserveNum);
//
//        ReserveInformation rsvinfo = new ReserveInformation();
////		SOAPMessage requestMsg = rsvinfo.createReserveInformationRequest(reserveNum);
//		//Deencapsulation.setField(rsvinfo, "ALTEA_HOST", "52.193.176.86");
//		String responseMsg = rsvinfo.getReserveInformationRequest("aaaaa,bbbb,cccc");
//
//		String expected = "Reserve_Information_1234567";
////		assertEquals(expected, responseMsg);
//		String ut = junitTest+","+ CLASS+","+method+","+(number++)+", Alteaから予約情報を取得することを確認";
//		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+responseMsg);
//
////		renameLogFile();
//	}

//	@Test
	public void getReserveInformationRequest_NG_test() {
		String method = "getReserveInformationRequest";
		int number = 2;

        Random random = new Random();
        int randomValue = random.nextInt(1000000);

        String reserveNum = String.format("%06d", randomValue);
        System.out.println("reserveNum: "+reserveNum);

        ReserveInformation rsvinfo = new ReserveInformation();
//		SOAPMessage requestMsg = rsvinfo.createReserveInformationRequest(reserveNum);

		String responseMsg = rsvinfo.getReserveInformationRequest("aaaaa,bbbb,cccc");

		//Deencapsulation.setField(rsvinfo, "ALTEA_URL", "http://xxxxx/xxxxx");

		StringWriter writer = new StringWriter();
		WriterAppender appender = new WriterAppender(new PatternLayout("%p, %m%n"), writer);
		LogManager.getRootLogger().addAppender(appender);
		LogManager.getRootLogger().setAdditivity(false);
		//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
		try {
			String logString = writer.toString();
//			assertNull(responseMsg);
//			assertTrue(logString.contains("Altea IO ERROR."));
		} finally {
			LogManager.getRootLogger().removeAppender(appender);
			LogManager.getRootLogger().setAdditivity(true);
		}
		//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
		String ut = junitTest+","+ CLASS+","+method+","+(number++)+", Alteaとの接続失敗時、予約情報を取得できないことを確認";
		OutputLog.outputLogMessage(OutputLog.DEBUG, ut +","+responseMsg);

	}

	public void renameLogFile() {
		Calendar cl = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        System.out.println(sdf.format(cl.getTime()));

        try {
        	Path srcPath = Paths.get(LogFileName);
        	Path trgPath = Paths.get(LogFileName+"_"+sdf.format(cl.getTime()));

			Files.move(srcPath, trgPath);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();
		}
	}
	
 	public void variableConf(ReserveInformation rsvinfo, String confFile) {
 		try {
			//private変数取得
			Field protocol = rsvinfo.getClass().getDeclaredField("protocol");
			Field alteaHost = rsvinfo.getClass().getDeclaredField("alteaHost");
			Field alteaPort = rsvinfo.getClass().getDeclaredField("alteaPortStr");
			Field filePath = rsvinfo.getClass().getDeclaredField("filePath");
			Field httpMethod = rsvinfo.getClass().getDeclaredField("httpMethod");
			Field alteaUrl = rsvinfo.getClass().getDeclaredField("httpMethod");
			// private変数へのアクセス制限を解除
			protocol.setAccessible(true);	        
			alteaHost.setAccessible(true);
			alteaPort.setAccessible(true);
			filePath.setAccessible(true);
			httpMethod.setAccessible(true);
			alteaUrl.setAccessible(true);

			setAlteaConfig(confFile);
			
	        // private変数に値を設定
			protocol.set(rsvinfo, protocolConf);
			alteaHost.set(rsvinfo, alteaHostConf);
			alteaPort.set(rsvinfo, alteaPortConf);
			filePath.set(rsvinfo, filePathConf);
			httpMethod.set(rsvinfo, httpMethodConf);
			alteaUrl.set(rsvinfo, httpMethodConf);

 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
	
	public void setAlteaConfig(String confFile) {
		Path confJson = confPath.resolve(confFile);

        try {
 	 		ObjectMapper mapper = new ObjectMapper();
 	 		JsonNode jNode = mapper.readTree(confJson.toFile());
 	 		protocolConf = jNode.get("altea").get(0).get("protocol").asText();
 	 		alteaHostConf = jNode.get("altea").get(0).get("alteaHost").asText();
 	 		alteaPortConf = jNode.get("altea").get(0).get("alteaPort").asText();
 	 		filePathConf = jNode.get("altea").get(0).get("filePath").asText();
 	 		httpMethodConf = jNode.get("altea").get(0).get("httpMethod").asText();
// 	 		alteaUrl = jNode.get("altea").get(0).get("alteaUrl").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}