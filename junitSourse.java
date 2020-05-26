package jp.co.ana.cas.proto.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;



/**
 * 予約情報取得クラス.
 * @author 113105A00ACF0
 *
 */
public class ReserveInformation {

	public static final String TAG_SOAP_ENVELOPE = "soapenv:Envelope";
	public static final String TAG_SOAP_BODY = "soapenv:Body";
	public static final String TAG_RESERVE_DETAIL = "reserveDetails";
	public static final String TAG_RESERVE_INFO = "reserveInformation";
	public static final String CRLF = "\r\n";

	//Alteaユーザ
	private final String alteaUser;
	//Alteaパスワード
	private final String alteaPasswd;
	//Altea接続情報
	private final String protocol;
	private final String alteaHost;
	private final String alteaPortStr;
	private final int alteaPort;
	private final String filePath;
	private final String httpMethod;


	private HttpURLConnection urlConn = null;
	private PrintStream ps = null;
	private BufferedReader reader = null;

	public ReserveInformation() {
		alteaUser = System.getenv("ALTEA_USER");
		//Alteaパスワード
		alteaPasswd = System.getenv("ALTEA_PASSWD");
		//Altea接続情報
		protocol = System.getenv("REPORT_PROTOCOL");
		alteaHost = System.getenv("ALTEA_HOST");
		alteaPortStr = System.getenv("ALTEA_PORT");
		if( alteaPortStr != null ) {
			alteaPort = Integer.parseInt(alteaPortStr);

		}
		else {
			alteaPort = 0;
		}
		filePath = System.getenv("REPORT_FILE_PATH");
		httpMethod = System.getenv("REPORT_HTTP_METHOD");
	}


	@Override
	protected void finalize() throws Throwable {
		// クローズ忘れたまま、クラスを解放してしまわないように対策しておく
		closeConnection();
		super.finalize();
	}

	public void openConnection() throws MalformedURLException, IOException {
		System.out.println("HttpConnect START!!");

		//接続するURLを指定する
		URL url = new URL( protocol, alteaHost, alteaPort, filePath );
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

		// 読み込み・書き込みストリーム取得
		ps = new PrintStream(urlConn.getOutputStream());
		reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
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
			e.printStackTrace();
		}
	}

	/**
	 * 予約情報取得メイン処理.
	 * @param reserveNum ANA予約番号
	 * @return reserveInfo 予約情報CSVファイルのパス
	 */
	public String getReserveInformation(String reserveNum) {
		String requestMsg = createReserveInformationRequest(reserveNum);
		try {
			openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String reserveInfo = getReserveInformationRequest(requestMsg);
		//		String reserveInfo = responseBody.toString();

		return reserveInfo;
		//		return "";
	}

	/**
	 * SOAPメッセージ作成処理
	 * @param reserveNum
	 * @return
	 */
	public String createReserveInformationRequest(String reserveNum) {
		//SOAPの警告対策
		System.setProperty("javax.xml.soap.SAAJMetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
		//		HashMap<String, String> userToken = wsSecurity();
		MessageFactory msgFactory = null;
		SOAPMessage requestMsg = null;
		String strMsg = null;
		try {

			StringBuilder sb = new StringBuilder();
			String body;
			// レスポンスのHTTP body取得
			sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sec=\"http://xml.amadeus.com/2010/06/Security_v1\" xmlns:typ=\"http://xml.amadeus.com/2010/06/Types_v1\" xmlns:iat=\"http://www.iata.org/IATA/2007/00/IATA2010.1\" xmlns:app=\"http://xml.amadeus.com/2010/06/AppMdw_CommonTypes_v3\" xmlns:link=\"http://wsdl.amadeus.com/2010/06/ws/Link_v1\" xmlns:ses=\"http://xml.amadeus.com/2010/06/Session_v3\" xmlns:flir=\"http://xml.amadeus.com/FLIREQ_97_3_1A\">");
			sb.append("<soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">");
			sb.append("<sec:AMA_SecurityHostedUser>");
			sb.append("<sec:UserID POS_Type=\"1\" RequestorType=\"U\" PseudoCityCode=\"TYONH0100\" AgentDutyCode=\"RC\">");
			sb.append("<typ:RequestorID>");
			sb.append("<iat:CompanyName>NH</iat:CompanyName>");
			sb.append("</typ:RequestorID>");
			sb.append("</sec:UserID>");
			sb.append("</sec:AMA_SecurityHostedUser>");
			sb.append("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
			sb.append("<wsse:UsernameToken>");
			sb.append("<wsse:Username>DMSPSS</wsse:Username>");
			sb.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">/40v3MwmmvDZ4zFS1WbPCuxTYyo=</wsse:Password>");
			sb.append("<wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">UHVRUm2SFU7Zvz9ljbfeSQ==</wsse:Nonce>");
			sb.append("<wsu:Created>2020-05-01T07:08:36.332Z</wsu:Created>");
			sb.append("</wsse:UsernameToken>");
			sb.append("</wsse:Security>");
			sb.append("<wsa:Action>http://webservices.amadeus.com/FLIREQ_97_3_1A</wsa:Action><wsa:MessageID>uuid:2078c2e0-4873-4d91-b372-c5dd0ba72f75</wsa:MessageID><wsa:To>https://nodeA1.test.webservices.amadeus.com/1ASIWGENNH</wsa:To></soapenv:Header>");
			sb.append("<soapenv:Body>");
			sb.append("<Air_FlightInfo>");
			sb.append("<generalFlightInfo>");
			sb.append("<companyDetails>");
			sb.append("<marketingCompany>NH</marketingCompany>");
			sb.append("</companyDetails>");
			sb.append("<flightIdentification>");
			sb.append("<flightNumber>377</flightNumber>");
			sb.append("</flightIdentification>");
			sb.append("</generalFlightInfo>");
			sb.append("</Air_FlightInfo>");
			sb.append("</soapenv:Body>");
			sb.append("</soapenv:Envelope>");
			body = sb.toString();
			//			Path xmlFile = Paths.get("soap/Request_Create_Message.xml");
			//			List<String> strList = Files.lines(xmlFile, StandardCharsets.UTF_8).collect(Collectors.toList());
			//			String soapTxt = String.join("", strList);
			InputStream soapByte = new ByteArrayInputStream(body.getBytes());
			msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
			requestMsg = msgFactory.createMessage(null, soapByte);
//			SOAPPart soapPart = requestMsg.getSOAPPart();
//			SOAPEnvelope env = soapPart.getEnvelope();
			//			SOAPHeader soapHeader = env.getHeader();
			//			soapHeader.getElementsByTagName("wsse:Password").item(0).setTextContent(userToken.get("Password"));
			//			soapHeader.getElementsByTagName("wsse:Nonce").item(0).setTextContent(userToken.get("Nonce"));
			//			soapHeader.getElementsByTagName("wsu:Created").item(0).setTextContent(userToken.get("Created"));

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			requestMsg.writeTo(out);
			strMsg = new String(out.toByteArray());
			System.out.println(strMsg);
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}


		return strMsg;
	}

	public String getReserveInformationRequest(String csvReserveInfo) {
		String report = null;
		System.out.println(protocol);

		System.out.println("csvReserveInfo: "+csvReserveInfo);

		try {
			ps.print(csvReserveInfo);
			//System.out.println("connect OK!!");
			int status = urlConn.getResponseCode();
			System.out.println("HTTP status code:" + status);
			OutputLog.outputLogMessage(OutputLog.DEBUG, "Altea HTTP status code:" + status);

			if(status == HttpURLConnection.HTTP_OK) {
				StringBuilder sb = new StringBuilder();
				String line;
				// レスポンスのHTTP body取得
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				report = sb.toString();
				System.out.println("body部: "+report);
				OutputLog.outputLogMessage(OutputLog.DEBUG, "Altea HTTP body:" + report);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Altea IO ERROR. " + e);
			OutputLog.outputLogMessage(OutputLog.ERROR, "Altea IO ERROR. " + e);
		}

		String soapenvEnvelope = extractValue(report,TAG_SOAP_ENVELOPE);
		String soapenvBod = extractValue(soapenvEnvelope, TAG_SOAP_BODY);
		String reserveDetails = extractValue(soapenvBod, TAG_RESERVE_DETAIL);
		String reseveInfo = extractValue(reserveDetails, TAG_RESERVE_INFO);
		return reseveInfo;
	}


	public String extractValue(String xmlData, String tag) {
		if(xmlData == null ) {
			return null;
		}
		else if(tag == null) {
			return xmlData;
		}
		int startIndex = xmlData.indexOf("<"+tag) ;
		int endIndex = xmlData.indexOf("</"+tag+">") ;

		String tagValue = null;
		try {
			startIndex = xmlData.indexOf(">",startIndex);
			tagValue =xmlData.substring(startIndex+1, endIndex) ;
			System.out.println("tagValue: "+tagValue);
		}
		catch(Exception ex) {
			return xmlData;
		}
		return tagValue;
	}

	/**
	 * SOAPメッセージのUserToken生成処理.
	 * <ol>
	 * <li>現在時刻を取得(ミリ秒)</li>
	 * <li>Createdを作成</li>
	 * <li>Nonceを作成</li>
	 * <li>passwordを作成</li>
	 * <li>作成したUserTokenをSOAPメッセージ作成処理に返却</li>
	 * </ol>
	 *
	 * @return　wsSecurity　UserToken
	 */
	public HashMap<String, String> wsSecurity() {
		HashMap<String, String> wsSecurity = new HashMap<String, String>();
		//Created
		Calendar cal = Calendar.getInstance();
		long timeZone = cal.getTimeInMillis();
		String created = createCreated(timeZone);
		String createdHex = createCreatedHex(created);
		wsSecurity.put("Created", created);
		//Nonce
		String noceHex = createNonceHex(timeZone);
		String nonce = createNonce(noceHex);
		wsSecurity.put("Nonce", nonce);
		//password
		String passwdSha = createPasswdSha();
		String passwd = createPasswd(noceHex, createdHex, passwdSha);
		wsSecurity.put("Password", passwd);

		return wsSecurity;
	}

	/**
	 * Created作成メソッド.
	 * <ol>
	 * <li>ミリ秒からCreatedフォーマットに変換</li>
	 * <li>UserToken生成処理に変換した値を返却</li>
	 * </ol>
	 *
	 * @param currentTime 現在時刻(ミリ秒)
	 * @return created 現在時刻(yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
	 */
	public String createCreated(long currentTime) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		timeFormat.setTimeZone(TimeZone.getTimeZone("Zulu"));
		Date curDate = new Date(currentTime);
		String created = timeFormat.format(curDate.getTime());

		return created;
	}

	/**
	 * CreatedのHex変換処理.
	 * <ol>
	 * <li>CreatedをHex変換</li>
	 * <li>UserToken生成処理に変換した値を返却</li>
	 * </ol>
	 *
	 * @param created Created
	 * @return createdHex CreatedのHex変換した値
	 */
	public String createCreatedHex(String created) {
		//パスワード生成用にCreatedをHEX変換
		byte[] createByte = created.getBytes();
		String createdHex = String.format("%040x", new BigInteger(1, createByte));
		return createdHex;
	}

	/**
	 * NonceのHex値生成処理.
	 * <ol>
	 * <li>ミリ秒をSeed値に設定し乱数を生成</li>
	 * <li>乱数をHex変換</li>
	 * <li>UserToken生成処理に変換した値を返却</li>
	 * </ol>
	 *
	 * @param currentTime 現在時刻(ミリ秒)
	 * @return nonceHex NonceをHex変換した値
	 */
	public String createNonceHex(long currentTime) {
		//Nonce
		String nonceHex = "";
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			//Seed値設定
		    double time = (double)currentTime / 1000; 
			random.setSeed((byte)time);
			byte[] resultValue = new byte[16];
			//パスワード生成用にNonceをHEX変換
			random.nextBytes(resultValue);
			nonceHex = String.format("%040x", new BigInteger(1, resultValue));
			nonceHex = nonceHex.toLowerCase();
			nonceHex = nonceHex.replaceFirst("^0+", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nonceHex;
	}

	/**
	 * Nonce生成処理.
	 * <ol>
	 * <li>NonceのHex値をバイト変換</li>
	 * <li>変換した値をBase64変換</li>
	 * <li>UserToken生成処理に変換した値を返却</li>
	 * </ol>
	 *
	 * @param nonceHex NonceのHex値
	 * @return nonce Nonce
	 */
	public String createNonce(String nonceHex) {
		byte[] nonceByte = null;
		char[] nonceHexstr = nonceHex.toCharArray();
		byte[] decodedHex;
		try {
			decodedHex = Hex.decodeHex(nonceHexstr);
			//NonceをBase64にエンコード
			nonceByte = Base64.encodeBase64(decodedHex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String nonce = "";
		if(nonceByte!=null) {
			for(byte b : nonceByte) {
				nonce = nonce + (char)b;
			}
		}
		return nonce;
	}

	/**
	 * Alteaパスワードのハッシュ値生成処理.
	 * <ol>
	 * <li>Alteaパスワードをハッシュ値を生成</li>
	 * <li>ハッシュ値を文字列に変換</li>
	 * <li>UserToken生成処理に変換した値を返却</li>
	 * </ol>
	 *
	 * @return passwdSha Alteaパスワードのハッシュ値
	 */
	public String createPasswdSha() {
		//password
		MessageDigest sha = null;
		String passwdSha = null;
		try {
			//パスワードのsha-1変換
			sha = MessageDigest.getInstance("SHA-1");
			sha.reset();
			sha.update(alteaPasswd.getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		}
		//Sha-1(パスワード)を文字列化
		if (sha!=null) {
			passwdSha = String.format("%040x", new BigInteger(1, sha.digest()));	
		}
		return passwdSha;
	}

	/**
	 * Password生成.
	 * <ol>
	 * <li>nonceHex、createdHex、createdHexを結合</li>
	 * <li>結合した値からハッシュ値生成</li>
	 * <li>ハッシュ値をHex変換</li>
	 * <li>変換した値をBase64変換</li>
	 * <li>UserToken生成処理に変換した値を返却</li>
	 * </ol>
	 *
	 * @param nonceHex NonceのHex値
	 * @param createdHex CreatedのHex値
	 * @param passwdSha Alteaパスワードのハッシュ値
	 * @return passwd Password
	 */
	public String createPasswd(String nonceHex, String createdHex, String passwdSha) {
		byte[] passwdDigest = null;
		MessageDigest sha = null;
		String passwdHex = null;
		//Nonce + Created + Password
		String hex = nonceHex + createdHex + passwdSha;
		try {
			//Sha1(Nonce + Created + Password)
			char[] passShaChar = hex.toCharArray();
			byte[] decodedShaHex = Hex.decodeHex(passShaChar);
			sha = MessageDigest.getInstance("SHA-1");
			sha.reset();
			sha.update(decodedShaHex);
			//Hex(Sha1(Nonce + Created + Password))
			passwdHex = String.format("%040x", new BigInteger(1, sha.digest()));
			//Hex(Sha1(Nonce + Created + Password))をBase64変換
			char[] passHexCha = passwdHex.toCharArray();
			byte[] decodedHex = Hex.decodeHex(passHexCha);
			passwdDigest = Base64.encodeBase64(decodedHex);
		} catch (DecoderException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String passwd = "";
		if(passwdDigest!=null) {
			for(byte b : passwdDigest) {
				passwd = passwd + (char)b;
			}
//		} else {
//			passwd = createPasswd(nonceHex, createdHex, passwdSha);
		}

		return passwd;
	}
}