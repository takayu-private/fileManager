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
 * �\����擾�N���X.
 * @author 113105A00ACF0
 *
 */
public class ReserveInformation {

	public static final String TAG_SOAP_ENVELOPE = "soapenv:Envelope";
	public static final String TAG_SOAP_BODY = "soapenv:Body";
	public static final String TAG_RESERVE_DETAIL = "reserveDetails";
	public static final String TAG_RESERVE_INFO = "reserveInformation";
	public static final String CRLF = "\r\n";

	//Altea���[�U
	private final String alteaUser;
	//Altea�p�X���[�h
	private final String alteaPasswd;
	//Altea�ڑ����
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
		//Altea�p�X���[�h
		alteaPasswd = System.getenv("ALTEA_PASSWD");
		//Altea�ڑ����
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
		// �N���[�Y�Y�ꂽ�܂܁A�N���X��������Ă��܂�Ȃ��悤�ɑ΍􂵂Ă���
		closeConnection();
		super.finalize();
	}

	public void openConnection() throws MalformedURLException, IOException {
		System.out.println("HttpConnect START!!");

		//�ڑ�����URL���w�肷��
		URL url = new URL( protocol, alteaHost, alteaPort, filePath );
		//�R�l�N�V�������擾����
		urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setConnectTimeout(10000);

		// HTTP���\�b�h��ݒ肷��
		//    urlConn.setRequestMethod("GET");
		urlConn.setRequestMethod(httpMethod);
		// ���X�G�X�g�ƃ{�f�B���M��������
		urlConn.setDoOutput(true);
		// ���X�|���X�̃{�f�B��M��������
		urlConn.setDoInput(true);
		// �R�l�N�V�����ڑ�
		urlConn.connect();

		// �ǂݍ��݁E�������݃X�g���[���擾
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
	 * �\����擾���C������.
	 * @param reserveNum ANA�\��ԍ�
	 * @return reserveInfo �\����CSV�t�@�C���̃p�X
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
	 * SOAP���b�Z�[�W�쐬����
	 * @param reserveNum
	 * @return
	 */
	public String createReserveInformationRequest(String reserveNum) {
		//SOAP�̌x���΍�
		System.setProperty("javax.xml.soap.SAAJMetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
		//		HashMap<String, String> userToken = wsSecurity();
		MessageFactory msgFactory = null;
		SOAPMessage requestMsg = null;
		String strMsg = null;
		try {

			StringBuilder sb = new StringBuilder();
			String body;
			// ���X�|���X��HTTP body�擾
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
				// ���X�|���X��HTTP body�擾
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				report = sb.toString();
				System.out.println("body��: "+report);
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
	 * SOAP���b�Z�[�W��UserToken��������.
	 * <ol>
	 * <li>���ݎ������擾(�~���b)</li>
	 * <li>Created���쐬</li>
	 * <li>Nonce���쐬</li>
	 * <li>password���쐬</li>
	 * <li>�쐬����UserToken��SOAP���b�Z�[�W�쐬�����ɕԋp</li>
	 * </ol>
	 *
	 * @return�@wsSecurity�@UserToken
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
	 * Created�쐬���\�b�h.
	 * <ol>
	 * <li>�~���b����Created�t�H�[�}�b�g�ɕϊ�</li>
	 * <li>UserToken���������ɕϊ������l��ԋp</li>
	 * </ol>
	 *
	 * @param currentTime ���ݎ���(�~���b)
	 * @return created ���ݎ���(yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
	 */
	public String createCreated(long currentTime) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		timeFormat.setTimeZone(TimeZone.getTimeZone("Zulu"));
		Date curDate = new Date(currentTime);
		String created = timeFormat.format(curDate.getTime());

		return created;
	}

	/**
	 * Created��Hex�ϊ�����.
	 * <ol>
	 * <li>Created��Hex�ϊ�</li>
	 * <li>UserToken���������ɕϊ������l��ԋp</li>
	 * </ol>
	 *
	 * @param created Created
	 * @return createdHex Created��Hex�ϊ������l
	 */
	public String createCreatedHex(String created) {
		//�p�X���[�h�����p��Created��HEX�ϊ�
		byte[] createByte = created.getBytes();
		String createdHex = String.format("%040x", new BigInteger(1, createByte));
		return createdHex;
	}

	/**
	 * Nonce��Hex�l��������.
	 * <ol>
	 * <li>�~���b��Seed�l�ɐݒ肵�����𐶐�</li>
	 * <li>������Hex�ϊ�</li>
	 * <li>UserToken���������ɕϊ������l��ԋp</li>
	 * </ol>
	 *
	 * @param currentTime ���ݎ���(�~���b)
	 * @return nonceHex Nonce��Hex�ϊ������l
	 */
	public String createNonceHex(long currentTime) {
		//Nonce
		String nonceHex = "";
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			//Seed�l�ݒ�
		    double time = (double)currentTime / 1000; 
			random.setSeed((byte)time);
			byte[] resultValue = new byte[16];
			//�p�X���[�h�����p��Nonce��HEX�ϊ�
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
	 * Nonce��������.
	 * <ol>
	 * <li>Nonce��Hex�l���o�C�g�ϊ�</li>
	 * <li>�ϊ������l��Base64�ϊ�</li>
	 * <li>UserToken���������ɕϊ������l��ԋp</li>
	 * </ol>
	 *
	 * @param nonceHex Nonce��Hex�l
	 * @return nonce Nonce
	 */
	public String createNonce(String nonceHex) {
		byte[] nonceByte = null;
		char[] nonceHexstr = nonceHex.toCharArray();
		byte[] decodedHex;
		try {
			decodedHex = Hex.decodeHex(nonceHexstr);
			//Nonce��Base64�ɃG���R�[�h
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
	 * Altea�p�X���[�h�̃n�b�V���l��������.
	 * <ol>
	 * <li>Altea�p�X���[�h���n�b�V���l�𐶐�</li>
	 * <li>�n�b�V���l�𕶎���ɕϊ�</li>
	 * <li>UserToken���������ɕϊ������l��ԋp</li>
	 * </ol>
	 *
	 * @return passwdSha Altea�p�X���[�h�̃n�b�V���l
	 */
	public String createPasswdSha() {
		//password
		MessageDigest sha = null;
		String passwdSha = null;
		try {
			//�p�X���[�h��sha-1�ϊ�
			sha = MessageDigest.getInstance("SHA-1");
			sha.reset();
			sha.update(alteaPasswd.getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		}
		//Sha-1(�p�X���[�h)�𕶎���
		if (sha!=null) {
			passwdSha = String.format("%040x", new BigInteger(1, sha.digest()));	
		}
		return passwdSha;
	}

	/**
	 * Password����.
	 * <ol>
	 * <li>nonceHex�AcreatedHex�AcreatedHex������</li>
	 * <li>���������l����n�b�V���l����</li>
	 * <li>�n�b�V���l��Hex�ϊ�</li>
	 * <li>�ϊ������l��Base64�ϊ�</li>
	 * <li>UserToken���������ɕϊ������l��ԋp</li>
	 * </ol>
	 *
	 * @param nonceHex Nonce��Hex�l
	 * @param createdHex Created��Hex�l
	 * @param passwdSha Altea�p�X���[�h�̃n�b�V���l
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
			//Hex(Sha1(Nonce + Created + Password))��Base64�ϊ�
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