package xml.main;


import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.keyresolver.implementations.RSAKeyValueResolver;
import org.apache.xml.security.keys.keyresolver.implementations.X509CertificateResolver;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class SignAndZip {
	
	private static final String IN_DOC = "C:\\Users\\mitra\\Desktop\\images.xml";
	private static final String OUT_DOC = "C:\\Users\\mitra\\Desktop\\images_signed.xml";
	private static final String KEY_STORE_FILE = "./data/miki.jks";
	public static ArrayList<File> directory = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		SignAndZip sign = new SignAndZip();
		sign.testIt();
		
	}

	static {
	      Security.addProvider(new BouncyCastleProvider());
	      org.apache.xml.security.Init.init();
	  }
		
		public void testIt() {
			
			GetImages();
			createXMLDocument(directory);

			
			Document document = loadDocument(IN_DOC);
			
			PrivateKey pk = loadPrivateKey();	
			Certificate cert = loadCertificate();
			
			System.out.println("Signing document....");
			document = signDocument(document, pk, cert);
			
			boolean verify=verifySignature(document);
			System.out.println("Verify signature... " + verify);
			
			
			saveDocument(document, OUT_DOC);
			System.out.println("Signing of document done");

			try {
				zipIt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		

		public static void createXMLDocument(ArrayList<File> file) {
			try {
				DocumentBuilderFactory dbuildF = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbuildF.newDocumentBuilder();
				Document document = db.newDocument();
				
				Element rootElement = document.createElement("root");
				document.appendChild(rootElement);
				
				Element user = document.createElement("user");
				user.appendChild(document.createTextNode("Batman"));
				rootElement.appendChild(user);
				
				Element photos = document.createElement("images");
				rootElement.appendChild(photos);
				
				for (File f : file) {
					Element photo = document.createElement("image");
					photos.appendChild(photo);
					
					Element photoName = document.createElement("imageTitle");
					photoName.appendChild(document.createTextNode(f.getName()));
					photo.appendChild(photoName);
					
					Element photoSize = document.createElement("imageSize");
					photoSize.appendChild(document.createTextNode(String.valueOf(f.length() / 1024)));
					photo.appendChild(photoSize);
					
					Element photoHash = document.createElement("imageHash");
					photoHash.appendChild(document.createTextNode(String.valueOf(f.hashCode())));
					photo.appendChild(photoHash);
					
				}
				
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				Date date = new Date();
			
				
				Element dateElement = document.createElement("date");
				dateElement.appendChild(document.createTextNode(dateFormat.format(date)));
				rootElement.appendChild(dateElement);
				
				
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(new File(IN_DOC));
				transformer.transform(source, result);
				file.add(new File(IN_DOC));
				
				StreamResult resultSigned = new StreamResult(new File(OUT_DOC));
				transformer.transform(source, resultSigned);
				file.add(new File(OUT_DOC));
			
				
				for (File f : file) {
					System.out.println(f.getName());
				}

				
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
				
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		
		private Document loadDocument(String file) {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(new File(file));

				return document;
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
				return null;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return null;
			} catch (SAXException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		
		private void saveDocument(Document doc, String fileName) {
			try {
				File outFile = new File(fileName);
				FileOutputStream f = new FileOutputStream(outFile);

				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer();
				
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(f);
				
				transformer.transform(source, result);

				f.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		private Certificate loadCertificate() {
			try {
				KeyStore ks = KeyStore.getInstance("JKS", "SUN");

				BufferedInputStream in = new BufferedInputStream(new FileInputStream(KEY_STORE_FILE));
				ks.load(in, "miki".toCharArray());
				
				if(ks.isKeyEntry("miki")) {
					Certificate cert = ks.getCertificate("miki");
					return cert;
					
				}
				else
					return null;
				
			} catch (KeyStoreException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
				return null;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			} catch (CertificateException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} 
		}
		

		private PrivateKey loadPrivateKey() {
			try {
				
				KeyStore ks = KeyStore.getInstance("JKS", "SUN");

				BufferedInputStream in = new BufferedInputStream(new FileInputStream(KEY_STORE_FILE));
				ks.load(in, "miki".toCharArray());
				
				if(ks.isKeyEntry("miki")) {
					PrivateKey pk = (PrivateKey) ks.getKey("miki", "miki".toCharArray());
					return pk;
				}
				else
					return null;
				
			} catch (KeyStoreException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
				return null;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			} catch (CertificateException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
				return null;
			} 
		}
		
		private Document signDocument(Document doc, PrivateKey privateKey, Certificate cert) {
	      
	      try {
				Element rootEl = doc.getDocumentElement();

				XMLSignature sig = new XMLSignature(doc, null, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);

				Transforms transforms = new Transforms(doc);
				    
				transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);

				transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);

				sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);

				sig.addKeyInfo(cert.getPublicKey());
				sig.addKeyInfo((X509Certificate) cert);
				    
				rootEl.appendChild(sig.getElement());

				sig.sign(privateKey);
				
				return doc;
				
			} catch (TransformationException e) {
				e.printStackTrace();
				return null;
			} catch (XMLSignatureException e) {
				e.printStackTrace();
				return null;
			} catch (DOMException e) {
				e.printStackTrace();
				return null;
			} catch (XMLSecurityException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		private static void GetImages()  {
			
			System.out.println("Enter directory path: ");
			BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
			
			String images = null;
			try {
				images = scanner.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			File[] listOfFiles = new File(images).listFiles();

			for (File file : listOfFiles) {
			    if (file.isFile()) {
			        directory.add(file);
			    }
			}

		}
		
		private boolean verifySignature(Document doc) {
			try {
				NodeList signatures = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
				Element signatureEl = (Element) signatures.item(0);
				
				XMLSignature signature = new XMLSignature(signatureEl, null);
				
			
				KeyInfo keyInfo = signature.getKeyInfo();
				if(keyInfo != null) {
					
					keyInfo.registerInternalKeyResolver(new RSAKeyValueResolver());
				    keyInfo.registerInternalKeyResolver(new X509CertificateResolver());
				    
				   
				    if(keyInfo.containsX509Data() && keyInfo.itemX509Data(0).containsCertificate()) { 
				        Certificate cert = keyInfo.itemX509Data(0).itemCertificate(0).getX509Certificate();
				        
				        
				        if(cert != null) 
				        	return signature.checkSignatureValue((X509Certificate) cert);
				        else
				        	return false;
				    }
				    else
				    	return false;
				}
				else
					return false;
		
			}catch (XMLSignatureException e) {
				e.printStackTrace();
				return false;
			} catch (XMLSecurityException e) {
				e.printStackTrace();
				return false;
			}
			
			
		}
		
		
		
		
		
		
		
		
		public static void zipIt() throws IOException {
			FileOutputStream fos = new FileOutputStream("C:\\Users\\mitra\\Desktop\\images.zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			
			for (File file : directory) {
				FileInputStream fis = new FileInputStream(file);
				ZipEntry zipEntry = new ZipEntry(file.getName());
				zipOut.putNextEntry(zipEntry);
				
				byte[] bytes = new byte[1024];
				int length;
				
				while((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				
				fis.close();
			}
			
			zipOut.close();
			fos.close();		

			
		}
}
