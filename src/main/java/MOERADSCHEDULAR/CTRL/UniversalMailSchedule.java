package MOERADSCHEDULAR.CTRL;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import MOERADSCHEDULAR.UTILITY.MailBean;
import MOERADSCHEDULAR.UTILITY.NativeRepository;
import MOERADSCHEDULAR.UTILITY.QueryResult;

@Service
@EnableScheduling
public class UniversalMailSchedule {
	@Autowired
	NativeRepository nativeRepository;
	
	@Value("${apilocalurl.path}")
	private String localApiUrl;
	
	@Value("${apiprodurl.path}")
	private String prodApiUrl;
	
	  private RestTemplate restTemplate = new RestTemplate();
	  public void RestMailService(RestTemplateBuilder restTemplateBuilder) {
	        this.restTemplate = restTemplateBuilder.build();
	    }
	
		@Scheduled(fixedDelay = 1000)
		public void forgetMail() throws InterruptedException {
			HttpHeaders headers = new HttpHeaders();
			QueryResult usrFrogetObj = getMailUserDetails();
			for (Map<String, Object> dataObj : usrFrogetObj.getRowValue()) {
				MailBean obj = new MailBean();
				obj.setApplicationName("Kvs Teacher");
				obj.setApplicationId("1");
				obj.setEmailTemplateId("MSG-5838");
				obj.setEmailTo(String.valueOf(dataObj.get("mail")));
				obj.setSubject("Teacher Module Credential");
				obj.setSignature("Dear " + dataObj.get("username"));
//				<a href=https://demopgi.udiseplus.gov.in/school_1/#/restPassword?sessionId="+dataObj.get("sessionid")+">Reset</a>
				
				
				obj.setContent("Your login account has been created successfully for KV Annual Transfer Process. You are requested to generate password by clicking on the portal http://localhost:8014/api/usermanagement/generatePassword?sessionId="+dataObj.get("sessionid")+" Team NIC -Ministry of Education, Government of India.");
				
				
				obj.setClosing("KVS Headquarter");
				obj.setMobile("");
				obj.setUserid(String.valueOf(dataObj.get("username")));
				obj.setName(String.valueOf(dataObj.get("username")));
				obj.setAttachmentYn(null);
				obj.setAttachmentPath(null);

				try {
					String requestJson = "{ \"applicationName\":\"Kvs Teacher\",\"attachmentYn\":\"0\" ,\"attachmentPath\":\"\",\"applicationId\":\"1\", \"emailTemplateId\": \"MSG-5836\", \"emailTo\": '"
							+ obj.getEmailTo()
							+ "', \"emailCc\": \"shamim.ahmad586@gmail.com\", \"subject\": \"Forget Password \", \"signature\": '"
							+ obj.getSignature() + "', \"content\": '" + obj.getContent() + "', \"closing\":'"
							+ obj.getClosing() + "' }".replaceAll("'", "\"");
					requestJson=requestJson.replaceAll("'", "\"");
					
					System.out.println("Only dynamic content--->"+obj.getContent());
					System.out.println("Request JSON--->"+requestJson);

					try {
						HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
//						String url = prodApiUrl;
						String url = "http://10.247.141.227:8080/ME-RAD-MESSAGE/api/sendMessage";
						this.restTemplate.exchange(url, HttpMethod.POST, request, Map.class, 1);
						updateMailUserDetails(obj.getEmailTo(),"M",String.valueOf(dataObj.get("mailtype")));
					} catch (Exception ex) {
						updateMailUserDetails(obj.getEmailTo(),"E",String.valueOf(dataObj.get("mailtype")));
						ex.printStackTrace();
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
		
		
		public QueryResult getMailUserDetails() {
			return nativeRepository.executeQueries("select * from public.auth_universal_mail where status is null or status='' limit 5");
		}
		
		public void updateMailUserDetails(String email,String status,String mailType) {
			nativeRepository.updateQueries("update public.auth_universal_mail set status='"+status+"' where  mailtype='"+mailType+"' and mail='"+email+"'");
		}
		
		@Scheduled(fixedDelay = 1000)
		public void deleteMailUserDetails() {
			System.out.println("delete start");
			nativeRepository.insertQueries("insert into public.auth_universal_mail_history (select * from public.auth_universal_mail where status ='S' or status ='E'  or (EXTRACT(EPOCH FROM (now() - createddate)) >expirytimeinsecond))");
			nativeRepository.updateQueries("delete from public.auth_universal_mail where status ='S' or status ='E' or EXTRACT(EPOCH FROM (now() - createddate)) >expirytimeinsecond-1");
		}
		
		
}
