package jp.co.ana.cas.proto.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.io.IOUtils;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;


public class AlteaConnection {

	private String alteaUrl = "https://nodeA1.test.webservices.amadeus.com/1ASIWGENNH";
//	private String alteaUrl = "http://localhost:8081";
	private String conType = "text/xml;charset=UTF-8";
	private String alteaValue = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sec=\"http://xml.amadeus.com/2010/06/Security_v1\" xmlns:typ=\"http://xml.amadeus.com/2010/06/Types_v1\" xmlns:iat=\"http://www.iata.org/IATA/2007/00/IATA2010.1\" xmlns:app=\"http://xml.amadeus.com/2010/06/AppMdw_CommonTypes_v3\" xmlns:link=\"http://wsdl.amadeus.com/2010/06/ws/Link_v1\" xmlns:ses=\"http://xml.amadeus.com/2010/06/Session_v3\" xmlns:flir=\"http://xml.amadeus.com/FLIREQ_97_3_1A\"><soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><sec:AMA_SecurityHostedUser><sec:UserID POS_Type=\"1\" RequestorType=\"U\" PseudoCityCode=\"TYONH0100\" AgentDutyCode=\"RC\"><typ:RequestorID><iat:CompanyName>NH</iat:CompanyName></typ:RequestorID></sec:UserID></sec:AMA_SecurityHostedUser><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsse:UsernameToken><wsse:Username>DMSPSS</wsse:Username><wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">/40v3MwmmvDZ4zFS1WbPCuxTYyo=</wsse:Password><wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">UHVRUm2SFU7Zvz9ljbfeSQ==</wsse:Nonce><wsu:Created>2020-05-01T07:08:36.332Z</wsu:Created></wsse:UsernameToken></wsse:Security><wsa:Action>http://webservices.amadeus.com/FLIREQ_97_3_1A</wsa:Action><wsa:MessageID>uuid:2078c2e0-4873-4d91-b372-c5dd0ba72f75</wsa:MessageID><wsa:To>https://nodeA1.test.webservices.amadeus.com/1ASIWGENNH</wsa:To></soapenv:Header><soapenv:Body><Air_FlightInfo><generalFlightInfo><companyDetails><marketingCompany>NH</marketingCompany></companyDetails><flightIdentification><flightNumber>377</flightNumber></flightIdentification></generalFlightInfo></Air_FlightInfo></soapenv:Body></soapenv:Envelope>";
	private String httpMethod = "POST";
	private PrintStream ps = null;
	private BufferedReader reader = null;
	HttpURLConnection alteaConn = null;
	private final static String PROXY_ADDRESS = "proxygate2.nic.nec.co.jp";
	private final static int PROXY_PORT = 8080;
	
	public static void main(String[] args) throws IOException {
		AlteaConnection aConn = new AlteaConnection();
		try {
			String reqMsg = aConn.createMsg("123456");
			URL proxyConn = aConn.proxySetUp();
			aConn.openConnection(proxyConn);
			aConn.alteaRequest(reqMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public URL proxySetUp() {
		URL endPoint = null;
		Socket socket = null;
		try {
			socket = new Socket();
			SocketAddress sockaddr = new InetSocketAddress(PROXY_ADDRESS, PROXY_PORT);
			socket.connect(sockaddr, 10000);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(socket.getInetAddress(), PROXY_PORT));
			endPoint = new URL(null, alteaUrl, new URLStreamHandler() {
				protected URLConnection openConnection(URL url) throws IOException {
					// The url is the parent of this stream handler, so must
					// create clone
					URL clone = new URL(url.toString());
					URLConnection connection = null;
					if (proxy.address().toString().equals("0.0.0.0/0.0.0.0:80")) {
						connection = clone.openConnection();
					} else {
						connection = clone.openConnection(proxy);
					}
					connection.setConnectTimeout(5 * 1000); // 5 sec
					connection.setReadTimeout(5 * 1000); // 5 sec
					// Custom header
					connection.addRequestProperty("Developer-Mood", "Happy");
					return connection;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return endPoint;
	}
	
	/**
	 * 予約情報取得処理
	 * @param msg
	 * @return
	 */
	public void openConnection(URL url) throws MalformedURLException, IOException {
		System.out.println("HttpConnect START!!");

		//コネクションを取得する
		alteaConn = (HttpURLConnection) url.openConnection();
		alteaConn.setConnectTimeout(10000);
//		alteaConn.setRequestProperty("Accept-Encoding", "gzip,deflate");
		alteaConn.setRequestProperty("Accept-Encoding", "identity");
		alteaConn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		alteaConn.setRequestProperty("SOAPAction", "http://webservices.amadeus.com/FLIREQ_97_3_1A");
		// HTTPメソッドを設定する
		alteaConn.setRequestMethod(httpMethod);
		// リスエストとボディ送信を許可する
		alteaConn.setDoOutput(true);
		// レスポンスのボディ受信を許可する
		alteaConn.setDoInput(true);
		// コネクション接続
		alteaConn.connect();

		// 読み込み・書き込みストリーム取得
		ps = new PrintStream(alteaConn.getOutputStream(), true, "UTF-8");
	}
	
	public String createMsg(String reserveNum) {
		//SOAPの警告対策
		System.setProperty("javax.xml.soap.SAAJMetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
		MessageFactory msgFactory = null;
		SOAPMessage requestMsg = null;
		String strMsg = null;
		HashMap<String, String> userToken = wsSecurity();
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
			sb.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">"+ userToken.get("Password") +"</wsse:Password>");
			sb.append("<wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">"+ userToken.get("Nonce") +"</wsse:Nonce>");
			sb.append("<wsu:Created>"+ userToken.get("Created") +"</wsu:Created>");
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
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strMsg;
	}

	public HashMap<String, String> wsSecurity() {
		HashMap<String, String> userToken = new HashMap<String, String>();
		String created = null;
		String nonce = null;
		String passwd = null;
		try {
			GroovyScriptEngine  createdGroovy = new GroovyScriptEngine("src/main/groovy/");
			Binding bind = new Binding();
			userToken = (HashMap<String, String>) createdGroovy.run("CreateUserToken.groovy", bind);
			System.out.println("created: "+userToken.get("Created"));
			System.out.println("nonce: "+userToken.get("Nonce"));
			System.out.println("passwd: "+userToken.get("Password"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return userToken;
	}

	public String alteaRequest(String reqMsg) {
		String report = null;

		try {
			System.out.println("[Altea] RequestMessage: "+reqMsg);
			ps.print(reqMsg);
			System.out.println("connect OK!!");
			int status = alteaConn.getResponseCode();
			System.out.println("HTTP status code:" + status);
			OutputLog.outputLogMessage(OutputLog.DEBUG, "Altea HTTP status code:" + status);

			if(status == HttpURLConnection.HTTP_OK) {
				StringBuilder sb = new StringBuilder();
				String line;
				// 読み込みストリーム取得
				System.out.println(alteaConn.getContentEncoding());
				reader = new BufferedReader(new InputStreamReader(alteaConn.getInputStream(), Charset.forName("UTF-8")));
				// レスポンスのHTTP body取得
//				byte[] bodyByte = IOUtils.toByteArray(alteaConn.getInputStream());
//				ungzip(bodyByte);
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

//		String soapenvEnvelope = extractValue(report,TAG_SOAP_ENVELOPE);
//		String soapenvBod = extractValue(soapenvEnvelope, TAG_SOAP_BODY);
//		String reserveDetails = extractValue(soapenvBod, TAG_RESERVE_DETAIL);
//		String reseveInfo = extractValue(reserveDetails, TAG_RESERVE_INFO);
//		return reseveInfo;
		return "";
	}
	
	public void ungzip(byte[] bodyByte) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bodyByte); 
		try {
			GZIPInputStream gzis = new GZIPInputStream(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
