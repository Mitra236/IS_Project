package ib.project.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ib.project.certificate.CertificateGenerator;
import ib.project.certificate.KeyStoreReader;
import ib.project.certificate.KeyStoreWriter;
import ib.project.certificatemodel.IssuerData;
import ib.project.certificatemodel.SubjectData;

@RestController
@RequestMapping(value = "/api/demo")
@CrossOrigin("*")
public class DemoController {

	private static String DATA_DIR_PATH;

	@Autowired
	ServletContext context;

	static {
		ResourceBundle rb = ResourceBundle.getBundle("application");
		DATA_DIR_PATH = rb.getString("dataDir");
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> createAFileInResources() throws IOException {

		byte[] content = "Content".getBytes();

		String directoryPath = getResourceFilePath(DATA_DIR_PATH).getAbsolutePath();

		Path path = Paths.get(directoryPath + File.separator + "demo.txt");

		Files.write(path, content);
		return new ResponseEntity<String>(path.toString(), HttpStatus.OK);
	}

	@RequestMapping(value = "/download/{filename}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> download(@PathVariable("filename") String filename) {

		System.out.println("fileName" + filename);
		//email(username)
		
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		String myUrl = DATA_DIR_PATH + "/" + filename+".jks";
		System.out.println(myUrl);
		URL urlPath = classloader.getResource(myUrl);

		System.out.println("urlPath " + urlPath);
		File file = null;
		try {
			file = new File(urlPath.getFile());

		}
		catch (Exception e) {
			System.out.println("NOT_FOUND");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("filename", filename + ".jks");

		byte[] bFile = readBytesFromFile(file.toString());
		return ResponseEntity.ok().headers(headers).body(bFile);
	}

	private static byte[] readBytesFromFile(String filePath) {

		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;
		try {

			File file = new File(filePath);
			bytesArray = new byte[(int) file.length()];

			// read file into bytes[]
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return bytesArray;

	}
	
	
	public static X509CRLHolder createCRL(X509Certificate pub, PrivateKey priv) throws CertificateParsingException, InvalidKeyException, NoSuchProviderException, SecurityException, SignatureException, CertificateEncodingException, CertIOException, NoSuchAlgorithmException, OperatorCreationException, FileNotFoundException {
		/*
		 * Kreira se prazna CRL lista potpisana privatnim kljucem
		 */
		Date now = new Date();
		X509v2CRLBuilder crlGen = new X509v2CRLBuilder(new X500Name(pub.getSubjectDN().getName()), now);

		Date nextUpdate = new Date(now.getTime()+30*24*60*60*1000); // Trebalo bi da se azurira svakih 30 dana
		PrivateKey caCrlPrivateKey = priv;

		crlGen.setNextUpdate(nextUpdate);

		//Kreiramo novu CRL listu pa joj dajemo broj 1
		crlGen.addExtension(X509Extension.cRLNumber, false, new CRLNumber(BigInteger.valueOf(1)));

		//Potpisivanje privatnim kljcem CA
		ContentSigner contentSigner = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(caCrlPrivateKey);
		X509CRLHolder crlholder = crlGen.build(contentSigner);

		return crlholder;
	}
	
	
	public static boolean isCRLValid(X509CRLHolder crl, X509Certificate caCert) {
		/*
		 * Provera da li je CRL lista ispravna, tj. da li je potpisana prosledjenim CA sertifikatom
		 */
		try {
			return crl.isSignatureValid(new JcaContentVerifierProviderBuilder().setProvider("BC").build(caCert));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public static X509CRLHolder updateCRL(X509CRLHolder crl, X509Certificate pub, PrivateKey priv, BigInteger serial, int reason) {
		/*
		 * Azuriranje CRL liste. Prosledjuje se serijski broj sertifikata koji se povlaci
		 */
		Security.addProvider(new BouncyCastleProvider());
		try {
			Date now = new Date();
			X509v2CRLBuilder crlGen = new X509v2CRLBuilder(crl.getIssuer(), now);
			Date nextUpdate = new Date(now.getTime()+30*24*60*60*1000);

			//Dodavanje postojece CRL liste u novu listu
			crlGen.addCRL(crl);

			//Dodavanje serijskog broja sertifikata koji se povlaci uz navodjenje razloga povlacenja kao i trenutka povlacenja
			crlGen.addCRLEntry(serial, now, reason);

			crlGen.setNextUpdate(nextUpdate);

			Extension ex = crl.getExtension(X509Extension.cRLNumber);
			//Azuriranje broja CRL liste - inkrementiranje broja za 1
			BigInteger newnumber = new BigInteger(ex.getParsedValue().toString()).add(BigInteger.ONE);

			crlGen.addExtension(X509Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(pub));
			crlGen.addExtension(X509Extension.cRLNumber, false, new CRLNumber(newnumber));

			ContentSigner contentSigner = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(priv);
			X509CRLHolder crlholder = crlGen.build(contentSigner);

			return crlholder;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	
	
	

	public File getResourceFilePath(String path) {

		URL url = this.getClass().getClassLoader().getResource(path);
		File file = null;

		try {

			file = new File(url.toURI());
		} catch (Exception e) {
			file = new File(url.getPath());
		}

		return file;
	}
	
	public static X509CRL CRLFromCrlHolder(X509CRLHolder crlh) {
		/*
		 * Kreiranje X509CRL liste iz X509CRLHolder-a
		 */
		Security.addProvider(new BouncyCastleProvider());
		JcaX509CRLConverter crlConverter = new JcaX509CRLConverter().setProvider("BC");
		try {
			return crlConverter.getCRL(crlh);
		} catch (CRLException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	@PostMapping(value="/create/{email}/{password}")
	public void SignedCertificateGenerator(@PathVariable("email") String email,@PathVariable("password") String password,KeyStoreReader keyStoreReader) {

		
		System.out.println("Sta je email :" + email);
		System.out.println("Sta je password : " + password);
		CertificateGenerator cg = new CertificateGenerator();
		
		
		try {
			//Kreiranje CA sertifikata, za kojeg je vezana CRL
			SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = iso8601Formater.parse("2015-03-31");
			Date endDate = iso8601Formater.parse("2020-03-31");
			KeyPair keyPairCA = cg.generateKeyPair();
			
			//osnovni podaci za issuer
			IssuerData issuerData = new IssuerData("FTN", "Fakultet tehnickih nauka", "Katedra za informatiku", "RS",  "ftnmail@uns.ac.rs", "123445", keyPairCA.getPrivate());
			SubjectData subjectData1 = new SubjectData(keyPairCA.getPublic(), issuerData.getX500name(), "1", startDate, endDate); 
			
			
			X509Certificate certCA = cg.generateCertificate(issuerData, subjectData1);
			X509CRLHolder crlHolder = DemoController.createCRL(certCA, keyPairCA.getPrivate());
			
			//Kreiranje sertifikata potpisanog od strane CA
			startDate = iso8601Formater.parse("2017-03-31");
			endDate = iso8601Formater.parse("2020-03-31");
			
			KeyPair keyPair2 = cg.generateKeyPair();
			
			SubjectData subjectData2 = new SubjectData(keyPair2.getPublic(), email, email, email, email, email, email, "1", startDate, endDate); 
			X509Certificate cert = cg.generateCertificate(issuerData, subjectData2);
			
			
			KeyStoreWriter keyStoreWriter = new KeyStoreWriter();
			keyStoreWriter.loadKeyStore(null, (email + "1").toCharArray());
			keyStoreWriter.write(email, keyPair2.getPrivate(), (email + "1").toCharArray(), cert);
			keyStoreWriter.saveKeyStore("./data/" + email + ".jks", (email + "10").toCharArray());
			
			
			System.out.println("ISSUER: " + cert.getIssuerX500Principal().getName());
			System.out.println("SUBJECT: " + cert.getSubjectX500Principal().getName());
			System.out.println("Sertifikat:");
			System.out.println("-------------------------------------------------------");
			System.out.println(cert);
			System.out.println("-------------------------------------------------------");
			
			
			
			crlHolder = DemoController.updateCRL(crlHolder, certCA, keyPairCA.getPrivate(), cert.getSerialNumber(), CRLReason.privilegeWithdrawn);
			
			X509CRL crl = DemoController.CRLFromCrlHolder(crlHolder);
			System.out.println(crl);
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
