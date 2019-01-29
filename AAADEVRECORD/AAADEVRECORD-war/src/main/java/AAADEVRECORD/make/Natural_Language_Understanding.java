package AAADEVRECORD.make;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import service.AAADEVRECORD.LanguageAttribute;
import AAADEVRECORD.util.Constants;

import com.avaya.collaboration.businessdata.api.NoAttributeFoundException;
import com.avaya.collaboration.businessdata.api.ServiceNotFoundException;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;


/**
 *
 * @author umansilla
 */

/*
 * Al método ingresa el téxto que se desea analizar (solo en ingles), y regresa un arreglo de emociones cada una con un 
 * rango en porcentaje. 
 * Se usa la variable statica myBeanObj_NLU para inicializar las credenciales necesarias. para inicializar estas credenciales
 * se deben de enviar un POST con las información al servlet NLU en formato json
 */
@SuppressWarnings("deprecation")
public class Natural_Language_Understanding {
	
	public String[] main(String args, Call call) throws NoAttributeFoundException, ServiceNotFoundException {
		String text = args;
		LanguageAttribute languageAttribute = new LanguageAttribute(call);
		if(languageAttribute.getLanguageAttribute().equals("es")){
			//Model Languaje Translator es-en
			text = Languaje_Translator.main(text, "es-en");
		}
		if(languageAttribute.getLanguageAttribute().equals("pt")){
			//Model Languaje Translator pt-en
			text = Languaje_Translator.main(text, "pt-en");
		}
		/*
		 * HTTPS
		 */
		
		try {
			String user = AttributeStore.INSTANCE.getAttributeValue(Constants.NLU_USER_NAME);
			String password = AttributeStore.INSTANCE.getAttributeValue(Constants.NLU_PASSWORD);
		      final SSLProtocolType protocolType = SSLProtocolType.TLSv1_2;
		      final SSLContext sslContext = SSLUtilityFactory.createSSLContext(protocolType);
			final CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(user, password));
			String encodedMessage = URLEncoder.encode(text, "UTF-8");
			final String URI = "https://gateway.watsonplatform.net/natural-language-understanding/api/v1/analyze?version=2018-05-01&text="
					+encodedMessage+"&features=emotion&return_analyzed_text=false&clean=true&fallback_to_raw=true&concepts.limit=8&emotion.document=true&entities.limit=50&keywords.limit=50&sentiment.document=true";

		      HttpClient client = HttpClients.custom().setSslcontext(sslContext).setHostnameVerifier(new AllowAllHostnameVerifier()).build();
		      HttpGet getMethod = new HttpGet(URI);
		      getMethod.addHeader("Accept", "application/json");
		      getMethod.addHeader("Content-Type", "application/json");

			final String authString = user + ":" + password;
			final String authEncBytes = DatatypeConverter
					.printBase64Binary(authString.getBytes());
			getMethod.addHeader("Authorization", "Basic " + authEncBytes);


			final HttpResponse response = client.execute(getMethod);

			final BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			final StringBuilder result = new StringBuilder();
			while ((line = inputStream.readLine()) != null) {
				result.append(line);
			}
			
			JSONObject json = new JSONObject(result.toString());
			
			String sadness = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("sadness");
			String joy = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("joy");
			String fear = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("fear");
			String disgust = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("disgust");
			String anger = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("anger");
			
			
			String [] arregloEmociones = {sadness, joy, fear, disgust, anger};
			inputStream.close();
			return arregloEmociones;
			
		} catch (Exception ex) {
			
			return null;
		}

	}

}